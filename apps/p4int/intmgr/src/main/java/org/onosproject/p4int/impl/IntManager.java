/*
 * Copyright 2018-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.p4int.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.util.KryoNamespace;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.p4int.api.IntConfig;
import org.onosproject.p4int.api.IntIntent;
import org.onosproject.pipelines.basic.IntObjective;
import org.onosproject.pipelines.basic.IntProgrammable;
import org.onosproject.p4int.api.IntService;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.AtomicIdGenerator;
import org.onosproject.store.service.ConsistentMap;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

@Component(immediate = true)
@Service
public class IntManager implements IntService {
    private final String appName = "org.onosproject.p4int";
    private ApplicationId appId;
    private final Logger log = getLogger(getClass());
    private ConsistentMap<Integer, IntIntent> intentConsistentMap;
    private ConsistentMap<DeviceId, IntDeviceRole> deviceRoleConsistentMap;
    private IntConfig cfg;
    private AtomicIdGenerator intentIds;

    private InternalHostListener hostListener = new InternalHostListener();

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Activate
    public void activate() {
        appId = coreService.registerApplication(appName);

        KryoNamespace.Builder serializer = KryoNamespace.newBuilder()
                .register(KryoNamespaces.API)
                .register(IntIntent.class)
                .register(IntDeviceRole.class);

        intentConsistentMap = storageService.<Integer, IntIntent>consistentMapBuilder()
                .withSerializer(Serializer.using(serializer.build()))
                .withName("int-intents")
                .withApplicationId(appId)
                .withPurgeOnUninstall()
                .build();

        deviceRoleConsistentMap = storageService.<DeviceId, IntDeviceRole>consistentMapBuilder()
                .withSerializer(Serializer.using(serializer.build()))
                .withName("int-device-roles")
                .withApplicationId(appId)
                .withPurgeOnUninstall()
                .build();

        // Assign IntDeviceRole to each device
        deviceService.getAvailableDevices().forEach(device ->
                deviceRoleConsistentMap.put(device.id(),
                                            hostService.getConnectedHosts(device.id()).isEmpty() ?
                                                    IntDeviceRole.TRANSIT :
                                                    IntDeviceRole.SOURCE_SINK)
        );
        hostService.addListener(hostListener);
        intentIds = storageService.getAtomicIdGenerator("int-intent-id-generator");
        startInt();
        log.info("Started", appId.id());
    }

    @Deactivate
    public void deactivate() {
        hostService.removeListener(hostListener);
        log.info("Deactivated");
    }

    @Override
    public void startInt() {
        deviceService.getAvailableDevices().forEach(device -> {
            if (device.is(IntProgrammable.class)) {
                IntProgrammable intDevice = device.as(IntProgrammable.class);
                intDevice.init(appId);
            }
        });
    }

    @Override
    public void startInt(Set<DeviceId> deviceIds) {
        deviceIds.forEach(deviceId -> {
            Device device = deviceService.getDevice(deviceId);
            if (device.is(IntProgrammable.class) &&
                    getIntRole(deviceId) == IntDeviceRole.TRANSIT) {
                IntProgrammable intDevice = device.as(IntProgrammable.class);
                intDevice.init(appId);
            }
        });
    }

    @Override
    public void stopInt() {
        flowRuleService.removeFlowRulesById(appId);
    }

    @Override
    public void stopInt(Set<DeviceId> deviceIds) {

    }

    @Override
    public void setConfig(IntConfig cfg) {
        this.cfg = cfg;
        deviceService.getAvailableDevices().forEach(device -> {
            if (device.is(IntProgrammable.class)) {
                IntProgrammable intDevice = device.as(IntProgrammable.class);
//                intDevice.setupReportEntry(cfg);
            }
        });
    }

    @Override
    public IntConfig getConfig() {
        return cfg;
    }

    @Override
    public int installIntIntent(IntIntent intent) {
        Integer intentId = (int) intentIds.nextId();

        intentConsistentMap.put(intentId, intent);

        // Convert IntIntent into an IntObjective
        IntObjective obj = new IntObjective.Builder()
                .withSelector(intent.selector())
                .withInstructionBitmap(buildInstructionBitmap(intent.metadataTypes()))
                .withHeaderType(intent.headerType().getValue())
                .build();

        // Install IntObjective on each INT source device
        deviceService.getAvailableDevices().forEach(device -> {
            if (device.is(IntProgrammable.class)
                    && deviceRoleConsistentMap.get(device.id()).value() == IntDeviceRole.SOURCE_SINK) {
                IntProgrammable intDevice = device.as(IntProgrammable.class);
                intDevice.addWatchlistEntry(obj);
            }
        });
        return intentId;
    }

    @Override
    public void removeIntIntent(int intentId) {
        IntIntent intent = intentConsistentMap.remove(intentId).value();

        // Convert IntIntent into an IntObjective
        IntObjective obj = new IntObjective.Builder()
                .withSelector(intent.selector())
                .withInstructionBitmap(buildInstructionBitmap(intent.metadataTypes()))
                .withHeaderType(intent.headerType().getValue())
                .build();

        // Remove IntObjective on each INT source device
        deviceService.getAvailableDevices().forEach(device -> {
            if (device.is(IntProgrammable.class)
                    && deviceRoleConsistentMap.get(device.id()).value() == IntDeviceRole.SOURCE_SINK) {
                IntProgrammable intDevice = device.as(IntProgrammable.class);
                intDevice.removeWatchlistEntry(obj);
            }
        });
    }

    @Override
    public IntIntent getIntIntent(int intentId) {
        return Optional.ofNullable(intentConsistentMap.get(intentId).value()).orElse(null);
    }

    private IntDeviceRole getIntRole(DeviceId deviceId) {
        return deviceRoleConsistentMap.get(deviceId).value();
    }

    private void setIntRole(DeviceId deviceId, IntDeviceRole role) {
        deviceRoleConsistentMap.put(deviceId, role);
    }

    private class InternalHostListener implements HostListener {
        @Override
        public void event(HostEvent event) {
            DeviceId deviceId = event.subject().location().deviceId();
            if (!deviceService.getDevice(deviceId).is(IntProgrammable.class)) {
                return;
            }
            switch (event.type()) {
                case HOST_ADDED:
                    if (deviceRoleConsistentMap.getOrDefault(deviceId, IntDeviceRole.TRANSIT).value()
                            != IntDeviceRole.SOURCE_SINK) {
                        setIntRole(deviceId, IntDeviceRole.SOURCE_SINK);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private int buildInstructionBitmap(Set<IntIntent.IntMetadataType> metadataTypes) {
        int instBitmap = 0;
        for (IntIntent.IntMetadataType metadataType : metadataTypes) {
            switch(metadataType) {
                case SWITCH_ID:
                    instBitmap |= (1 << 15);
                    break;
                case L1_PORT_ID:
                    instBitmap |= (1 << 14);
                    break;
                case HOP_LATENCY:
                    instBitmap |= (1 << 13);
                    break;
                case QUEUE_OCCUPANCY:
                    instBitmap |= (1 << 12);
                    break;
                case INGRESS_TIMESTAMP:
                    instBitmap |= (1 << 11);
                    break;
                case EGRESS_TIMESTAMP:
                    instBitmap |= (1 << 10);
                    break;
                case L2_PORT_ID:
                    instBitmap |= (1 << 9);
                    break;
                case EGRESS_TX_UTIL:
                    instBitmap |= (1 << 8);
                    break;
                default:
                    log.info("Unsupported metadata type {}. Ignoring...", metadataType);
                    break;
            }
        }
        return instBitmap;
    }


}
