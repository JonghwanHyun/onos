package org.onosproject.pipelines.basic;

import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class IntObjective {
    /**
     * Represents a type of INT metadata.
     */
    public enum IntMetadataType {
        /**
         * The unique ID of a switch.
         */
        SWITCH_ID,
        /**
         * The ports on which the INT packet was received and sent out.
         */
        L1_PORT_ID,
        /**
         * Time taken for the INT packet to be switched within the device.
         */
        HOP_LATENCY,
        /**
         * The build-up of traffic in the queue that the INT packet observes
         * in the device while being forwarded.
         */
        QUEUE_OCCUPANCY,
        /**
         * The device local time when the INT packet was received on the ingress port.
         */
        INGRESS_TIMESTAMP,
        /**
         * The device local time when the INT packet was processed by the egress port.
         */
        EGRESS_TIMESTAMP,
        /**
         * The logical ports on which the INT packet was received and sent out.
         */
        L2_PORT_ID,
        /**
         * Current utilization of the egress port via witch the INT packet was sent out.
         */
        EGRESS_TX_UTIL
    }

    /**
     * Represents an INT header type.
     */
    public enum IntHeaderType {
        /**
         * Intemediate devices must process this type of INT header.
         */
        HOP_BY_HOP,
        /**
         * Intemediate devices must ignore this type of INT header.
         */
        DESTINATION
    }

    /**
     * Represents a type of telemetry report.
     */
    public enum IntReportType {
        /**
         * Report for flows matching certain definitions.
         */
        TRACKED_FLOW,
        /**
         * Reports for all dropeed packets matching a drop watchlist.
         */
        DROPPED_PACKET,
        /**
         * Reports for traffic entering a specific queue during a period of queue congestion.
         */
        CONGESTED_QUEUE
    }

    /**
     * Represents telemetry mode.
     */
    public enum TelemetryMode {
        /**
         * Each network device generates its own telemetry reports.
         */
        POSTCARD,
        /**
         * Telemetry metadata is embedded in between the original
         * headers of data packets as they traverse the network.
         */
        INBAND_TELEMETRY
    }

    private static final int DEFAULT_PRIORITY = 10;

    // TrafficSelector to describe target flows to monitor
    private final TrafficSelector selector;
    // set of metadata type to collect
    private final Set<IntMetadataType> metadataTypes;
    // hop-by-hop or destination
    private final IntHeaderType headerType;
    // telemetry report types
    private final Set<IntReportType> reportTypes;
    // telemetry mode
    private final TelemetryMode telemetryMode;

    /**
     * Creates an IntObjective.
     *
     * @param selector      the traffic selector that identifies traffic to enable INT
     * @param metadataTypes the types of metadata to collect
     * @param headerType    the type of INT header
     * @param reportTypes   the types of report to be generated
     * @param telemetryMode the telemetry mode
     */
    private IntObjective(TrafficSelector selector, Set<IntMetadataType> metadataTypes,
                         IntHeaderType headerType, Set<IntReportType> reportTypes,
                         TelemetryMode telemetryMode) {
        this.selector = selector;
        this.metadataTypes = new HashSet<>(metadataTypes);
        this.headerType = headerType;
        this.reportTypes = new HashSet<>(reportTypes);
        this.telemetryMode = telemetryMode;
    }

    /**
     * Returns traffic selector of this objective.
     *
     * @return traffic selector
     */
    public TrafficSelector selector() {
        return selector;
    }

    /**
     * Returns a set of metadata type to be collected by this objective.
     *
     * @return set of metadata type
     */
    public Set<IntMetadataType> metadataTypes() {
        return metadataTypes;
    }

    /**
     * Returns a INT header type specified in this intent.
     *
     * @return INT header type
     */
    public IntHeaderType headerType() {
        return headerType;
    }

    /**
     * Returns a set of report type to be generated.
     *
     * @return set of report type
     */
    public Set<IntReportType> reportTypes() {
        return reportTypes;
    }

    /**
     * Returns a telemetry mode specified in this intent.
     *
     * @return telemtry mode
     */
    public TelemetryMode telemetryMode() {
        return telemetryMode;
    }

    /**
     * An IntObjective builder.
     */
    public static final class Builder {
        private TrafficSelector selector = DefaultTrafficSelector.emptySelector();
        private Set<IntMetadataType> metadataTypes = new HashSet<>();
        private IntHeaderType headerType = IntHeaderType.HOP_BY_HOP;
        private Set<IntReportType> reportTypes = new HashSet<>();
        private TelemetryMode telemetryMode = TelemetryMode.INBAND_TELEMETRY;

        /**
         * Assigns a selector to the IntObjective.
         *
         * @param selector a traffic selector
         * @return an IntObjective builder
         */
        public IntObjective.Builder withSelector(TrafficSelector selector) {
            this.selector = selector;
            return this;
        }

        /**
         * Add a metadata type to the IntObjective.
         *
         * @param metadataType a type of metadata to collect
         * @return an IntObjective builder
         */
        public IntObjective.Builder withMetadataType(IntMetadataType metadataType) {
            this.metadataTypes.add(metadataType);
            return this;
        }

        /**
         * Assigns a header type to the IntObjective.
         *
         * @param headerType a header type
         * @return an IntObjective builder
         */
        public IntObjective.Builder withHeaderType(IntHeaderType headerType) {
            this.headerType = headerType;
            return this;
        }

        /**
         * Add a report type to the IntObjective.
         *
         * @param reportType a type of report
         * @return an IntObjective builder
         */
        public IntObjective.Builder withReportType(IntReportType reportType) {
            this.reportTypes.add(reportType);
            return this;
        }

        /**
         * Assigns a telemetry mode to the IntObjective.
         *
         * @param telemetryMode a telemetry mode
         * @return an IntObjective builder
         */
        public IntObjective.Builder withTelemetryMode(TelemetryMode telemetryMode) {
            this.telemetryMode = telemetryMode;
            return this;
        }

        /**
         * Builds the IntObjective.
         *
         * @return an IntObjective
         */
        public IntObjective build() {
            checkArgument(!selector.criteria().isEmpty(), "Empty selector cannot match any flow.");
            checkArgument(!metadataTypes.isEmpty(), "Metadata types cannot be empty.");
            checkNotNull(headerType, "Header type cannot be null.");
            checkNotNull(!reportTypes.isEmpty(), "Report types cannot be empty.");
            checkNotNull(telemetryMode, "Telemetry mode cannot be null.");

            return new IntObjective(selector, metadataTypes, headerType, reportTypes, telemetryMode);
        }
    }
}
