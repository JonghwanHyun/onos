package org.onlab.onos.store.service.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import net.kuujo.copycat.Command;
import net.kuujo.copycat.Query;
import net.kuujo.copycat.StateMachine;

import org.onlab.onos.store.cluster.messaging.MessageSubject;
import org.onlab.onos.store.serializers.KryoSerializer;
import org.onlab.onos.store.service.BatchReadRequest;
import org.onlab.onos.store.service.BatchWriteRequest;
import org.onlab.onos.store.service.ReadRequest;
import org.onlab.onos.store.service.ReadResult;
import org.onlab.onos.store.service.ReadStatus;
import org.onlab.onos.store.service.VersionedValue;
import org.onlab.onos.store.service.WriteRequest;
import org.onlab.onos.store.service.WriteResult;
import org.onlab.onos.store.service.WriteStatus;
import org.onlab.util.KryoNamespace;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

/**
 * StateMachine whose transitions are coordinated/replicated
 * by Raft consensus.
 * Each Raft cluster member has a instance of this state machine that is
 * independently updated in lock step once there is consensus
 * on the next transition.
 */
public class DatabaseStateMachine implements StateMachine {

    private final Logger log = getLogger(getClass());

    // message subject for database update notifications.
    public static final MessageSubject DATABASE_UPDATE_EVENTS =
            new MessageSubject("database-update-events");

    // serializer used for snapshot
    public static final KryoSerializer SERIALIZER = new KryoSerializer() {
        @Override
        protected void setupKryoPool() {
            serializerPool = KryoNamespace.newBuilder()
                    .register(VersionedValue.class)
                    .register(State.class)
                    .register(TableMetadata.class)
                    .register(BatchReadRequest.class)
                    .register(BatchWriteRequest.class)
                    .register(ReadStatus.class)
                    .register(WriteStatus.class)
                    // TODO: Move this out ?
                    .register(TableModificationEvent.class)
                    .register(ClusterMessagingProtocol.COMMON)
                    .build()
                    .populate(1);
        }
    };

    private final Set<DatabaseUpdateEventListener> listeners = Sets.newIdentityHashSet();

    // durable internal state of the database.
    private State state = new State();

    private boolean compressSnapshot = false;

    @Command
    public boolean createTable(String tableName) {
        TableMetadata metadata = new TableMetadata(tableName);
        return createTable(metadata);
    }

    @Command
    public boolean createTable(String tableName, int ttlMillis) {
        TableMetadata metadata = new TableMetadata(tableName, ttlMillis);
        return createTable(metadata);
    }

    private boolean createTable(TableMetadata metadata) {
        Map<String, VersionedValue> existingTable = state.getTable(metadata.tableName());
        if (existingTable != null) {
            return false;
        }
        state.createTable(metadata);
        for (DatabaseUpdateEventListener listener : listeners) {
            listener.tableCreated(metadata);
        }
        return true;
    }

    @Command
    public boolean dropTable(String tableName) {
        if (state.removeTable(tableName)) {
            for (DatabaseUpdateEventListener listener : listeners) {
                listener.tableDeleted(tableName);
            }
            return true;
        }
        return false;
    }

    @Command
    public boolean dropAllTables() {
        Set<String> tableNames = state.getTableNames();
        state.removeAllTables();
        for (DatabaseUpdateEventListener listener : listeners) {
            for (String tableName : tableNames) {
                listener.tableDeleted(tableName);
            }
        }
        return true;
    }

    @Query
    public Set<String> listTables() {
        return ImmutableSet.copyOf(state.getTableNames());
    }

    @Query
    public List<ReadResult> read(BatchReadRequest batchRequest) {
        List<ReadResult> results = new ArrayList<>(batchRequest.batchSize());
        for (ReadRequest request : batchRequest.getAsList()) {
            Map<String, VersionedValue> table = state.getTable(request.tableName());
            if (table == null) {
                results.add(new ReadResult(ReadStatus.NO_SUCH_TABLE, request.tableName(), request.key(), null));
                continue;
            }
            VersionedValue value = VersionedValue.copy(table.get(request.key()));
            results.add(new ReadResult(ReadStatus.OK, request.tableName(), request.key(), value));
        }
        return results;
    }

    WriteStatus checkIfApplicable(WriteRequest request,
                                        VersionedValue value) {

        switch (request.type()) {
        case PUT:
            return WriteStatus.OK;

        case PUT_IF_ABSENT:
            if (value == null) {
                return WriteStatus.OK;
            }
            return WriteStatus.PRECONDITION_VIOLATION;
        case PUT_IF_VALUE:
        case REMOVE_IF_VALUE:
            if (value != null && Arrays.equals(value.value(), request.oldValue())) {
                return WriteStatus.OK;
            }
            return WriteStatus.PRECONDITION_VIOLATION;
        case PUT_IF_VERSION:
        case REMOVE_IF_VERSION:
            if (value != null && request.previousVersion() == value.version()) {
                return WriteStatus.OK;
            }
            return WriteStatus.PRECONDITION_VIOLATION;
        case REMOVE:
            return WriteStatus.OK;
        default:
            break;
        }
        log.error("Should never reach here {}", request);
        return WriteStatus.ABORTED;
    }

    @Command
    public List<WriteResult> write(BatchWriteRequest batchRequest) {

        // applicability check
        boolean abort = false;
        List<WriteStatus> validationResults = new ArrayList<>(batchRequest.batchSize());
        for (WriteRequest request : batchRequest.getAsList()) {
            Map<String, VersionedValue> table = state.getTable(request.tableName());
            if (table == null) {
                validationResults.add(WriteStatus.NO_SUCH_TABLE);
                abort = true;
                continue;
            }
            final VersionedValue value = table.get(request.key());
            WriteStatus result = checkIfApplicable(request, value);
            validationResults.add(result);
            if (result != WriteStatus.OK) {
                abort = true;
            }
        }

        List<WriteResult> results = new ArrayList<>(batchRequest.batchSize());

        if (abort) {
            for (WriteStatus validationResult : validationResults) {
                if (validationResult == WriteStatus.OK) {
                    // aborted due to applicability check failure on other request
                    results.add(new WriteResult(WriteStatus.ABORTED, null));
                } else {
                    results.add(new WriteResult(validationResult, null));
                }
            }
            return results;
        }

        List<TableModificationEvent> tableModificationEvents = Lists.newLinkedList();

        // apply changes
        for (WriteRequest request : batchRequest.getAsList()) {
            Map<String, VersionedValue> table = state.getTable(request.tableName());

            TableModificationEvent tableModificationEvent = null;
            // FIXME: If this method could be called by multiple thread,
            // synchronization scope is wrong.
            // Whole function including applicability check needs to be protected.
            // Confirm copycat's thread safety requirement for StateMachine
            // TODO: If we need isolation, we need to block reads also
            synchronized (table) {
                switch (request.type()) {
                case PUT:
                case PUT_IF_ABSENT:
                case PUT_IF_VALUE:
                case PUT_IF_VERSION:
                    VersionedValue newValue = new VersionedValue(request.newValue(), state.nextVersion());
                    VersionedValue previousValue = table.put(request.key(), newValue);
                    WriteResult putResult = new WriteResult(WriteStatus.OK, previousValue);
                    results.add(putResult);
                    tableModificationEvent = (previousValue == null) ?
                            TableModificationEvent.rowAdded(request.tableName(), request.key(), newValue) :
                            TableModificationEvent.rowUpdated(request.tableName(), request.key(), newValue);
                    break;

                case REMOVE:
                case REMOVE_IF_VALUE:
                case REMOVE_IF_VERSION:
                    VersionedValue removedValue = table.remove(request.key());
                    WriteResult removeResult = new WriteResult(WriteStatus.OK, removedValue);
                    results.add(removeResult);
                    if (removedValue != null) {
                        tableModificationEvent =
                                TableModificationEvent.rowDeleted(request.tableName(), request.key(), removedValue);
                    }
                    break;

                default:
                    log.error("Invalid WriteRequest type {}", request.type());
                    break;
                }
            }

            if (tableModificationEvent != null) {
                tableModificationEvents.add(tableModificationEvent);
            }
        }

        // notify listeners of table mod events.
        for (DatabaseUpdateEventListener listener : listeners) {
            for (TableModificationEvent tableModificationEvent : tableModificationEvents) {
                listener.tableModified(tableModificationEvent);
            }
        }

        return results;
    }

    public static class State {

        private final Map<String, TableMetadata> tableMetadata = Maps.newHashMap();
        private final Map<String, Map<String, VersionedValue>> tableData = Maps.newHashMap();
        private long versionCounter = 1;

        public Map<String, VersionedValue> getTable(String tableName) {
            return tableData.get(tableName);
        }

        void createTable(TableMetadata metadata) {
            tableMetadata.put(metadata.tableName, metadata);
            tableData.put(metadata.tableName, Maps.newHashMap());
        }

        TableMetadata getTableMetadata(String tableName) {
            return tableMetadata.get(tableName);
        }

        long nextVersion() {
            return versionCounter++;
        }

        Set<String> getTableNames() {
            return ImmutableSet.copyOf(tableMetadata.keySet());
        }


        boolean removeTable(String tableName) {
            if (!tableMetadata.containsKey(tableName)) {
                return false;
            }
            tableMetadata.remove(tableName);
            tableData.remove(tableName);
            return true;
        }

        void removeAllTables() {
            tableMetadata.clear();
            tableData.clear();
        }
    }

    public static class TableMetadata {
        private final String tableName;
        private final boolean expireOldEntries;
        private final int ttlMillis;

        public TableMetadata(String tableName) {
            this.tableName = tableName;
            this.expireOldEntries = false;
            this.ttlMillis = Integer.MAX_VALUE;

        }

        public TableMetadata(String tableName, int ttlMillis) {
            this.tableName = tableName;
            this.expireOldEntries = true;
            this.ttlMillis = ttlMillis;
        }

        public String tableName() {
            return tableName;
        }

        public boolean expireOldEntries() {
            return expireOldEntries;
        }

        public int ttlMillis() {
            return ttlMillis;
        }
    }

    @Override
    public byte[] takeSnapshot() {
        try {
            if (compressSnapshot) {
                byte[] input = SERIALIZER.encode(state);
                ByteArrayOutputStream comp = new ByteArrayOutputStream(input.length);
                DeflaterOutputStream compressor = new DeflaterOutputStream(comp);
                compressor.write(input, 0, input.length);
                compressor.close();
                return comp.toByteArray();
            } else {
                return SERIALIZER.encode(state);
            }
        } catch (Exception e) {
            log.error("Failed to take snapshot", e);
            throw new SnapshotException(e);
        }
    }

    @Override
    public void installSnapshot(byte[] data) {
        try {
            if (compressSnapshot) {
                ByteArrayInputStream in = new ByteArrayInputStream(data);
                InflaterInputStream decompressor = new InflaterInputStream(in);
                ByteStreams.toByteArray(decompressor);
                this.state = SERIALIZER.decode(ByteStreams.toByteArray(decompressor));
            } else {
                this.state = SERIALIZER.decode(data);
            }

            // FIXME: synchronize.
            for (DatabaseUpdateEventListener listener : listeners) {
                listener.snapshotInstalled(state);
            }
        } catch (Exception e) {
            log.error("Failed to install from snapshot", e);
            throw new SnapshotException(e);
        }
    }

    /**
     * Adds specified DatabaseUpdateEventListener.
     * @param listener listener to add
     */
    public void addEventListener(DatabaseUpdateEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes specified DatabaseUpdateEventListener.
     * @param listener listener to remove
     */
    public void removeEventListener(DatabaseUpdateEventListener listener) {
        listeners.remove(listener);
    }
}
