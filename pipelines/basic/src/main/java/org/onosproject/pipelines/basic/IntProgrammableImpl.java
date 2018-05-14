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
package org.onosproject.pipelines.basic;

import com.google.common.collect.ImmutableBiMap;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.util.ImmutableByteSequence;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.IPCriterion;
import org.onosproject.net.flow.criteria.PiCriterion;
import org.onosproject.net.flow.criteria.TcpPortCriterion;
import org.onosproject.net.flow.criteria.UdpPortCriterion;
import org.onosproject.net.host.HostService;
import org.onosproject.net.pi.model.PiActionId;
import org.onosproject.net.pi.model.PiMatchFieldId;
import org.onosproject.net.pi.model.PiTableId;
import org.onosproject.net.pi.runtime.PiAction;
import org.onosproject.net.pi.runtime.PiActionParam;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class IntProgrammableImpl extends AbstractHandlerBehaviour implements IntProgrammable {

    // TODO: change this value to the value of diameter of a network.
    private final int MAX_HOP = 64;
    private final int PORT_MASK = 0xffff;
    private final Logger log = getLogger(getClass());
    private ApplicationId appId;
    private final int IDLE_TIMEOUT = 100;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private HostService hostService;

    private DeviceId deviceId;
    private static final int DEFAULT_PRIORITY = 10000;
    private static final ImmutableBiMap<Integer, PiActionId> INST_0003_ACTION_MAP =
            ImmutableBiMap.<Integer, PiActionId>builder()
                    .put(0, IntConstants.ACT_INT_SET_HEADER_0003_I0_ID)
                    .put(1, IntConstants.ACT_INT_SET_HEADER_0003_I1_ID)
                    .put(2, IntConstants.ACT_INT_SET_HEADER_0003_I2_ID)
                    .put(3, IntConstants.ACT_INT_SET_HEADER_0003_I3_ID)
                    .put(4, IntConstants.ACT_INT_SET_HEADER_0003_I4_ID)
                    .put(5, IntConstants.ACT_INT_SET_HEADER_0003_I5_ID)
                    .put(6, IntConstants.ACT_INT_SET_HEADER_0003_I6_ID)
                    .put(7, IntConstants.ACT_INT_SET_HEADER_0003_I7_ID)
                    .put(8, IntConstants.ACT_INT_SET_HEADER_0003_I8_ID)
                    .put(9, IntConstants.ACT_INT_SET_HEADER_0003_I9_ID)
                    .put(10, IntConstants.ACT_INT_SET_HEADER_0003_I10_ID)
                    .put(11, IntConstants.ACT_INT_SET_HEADER_0003_I11_ID)
                    .put(12, IntConstants.ACT_INT_SET_HEADER_0003_I12_ID)
                    .put(13, IntConstants.ACT_INT_SET_HEADER_0003_I13_ID)
                    .put(14, IntConstants.ACT_INT_SET_HEADER_0003_I14_ID)
                    .put(15, IntConstants.ACT_INT_SET_HEADER_0003_I15_ID)
                    .build();

    private static final ImmutableBiMap<Integer, PiActionId> INST_0407_ACTION_MAP =
            ImmutableBiMap.<Integer, PiActionId>builder()
                    .put(0, IntConstants.ACT_INT_SET_HEADER_0407_I0_ID)
                    .put(1, IntConstants.ACT_INT_SET_HEADER_0407_I1_ID)
                    .put(2, IntConstants.ACT_INT_SET_HEADER_0407_I2_ID)
                    .put(3, IntConstants.ACT_INT_SET_HEADER_0407_I3_ID)
                    .put(4, IntConstants.ACT_INT_SET_HEADER_0407_I4_ID)
                    .put(5, IntConstants.ACT_INT_SET_HEADER_0407_I5_ID)
                    .put(6, IntConstants.ACT_INT_SET_HEADER_0407_I6_ID)
                    .put(7, IntConstants.ACT_INT_SET_HEADER_0407_I7_ID)
                    .put(8, IntConstants.ACT_INT_SET_HEADER_0407_I8_ID)
                    .put(9, IntConstants.ACT_INT_SET_HEADER_0407_I9_ID)
                    .put(10, IntConstants.ACT_INT_SET_HEADER_0407_I10_ID)
                    .put(11, IntConstants.ACT_INT_SET_HEADER_0407_I11_ID)
                    .put(12, IntConstants.ACT_INT_SET_HEADER_0407_I12_ID)
                    .put(13, IntConstants.ACT_INT_SET_HEADER_0407_I13_ID)
                    .put(14, IntConstants.ACT_INT_SET_HEADER_0407_I14_ID)
                    .put(15, IntConstants.ACT_INT_SET_HEADER_0407_I15_ID)
                    .build();

    @Override
    public void init(ApplicationId applicationId) {
        appId = applicationId;
        deviceId = this.data().deviceId();
        flowRuleService = this.handler().get(FlowRuleService.class);
        deviceService = this.handler().get(DeviceService.class);
        hostService = this.handler().get(HostService.class);
        Set<PortNumber> hostPorts = deviceService.getPorts(deviceId).stream().filter(port ->
            hostService.getConnectedHosts(new ConnectPoint(deviceId, port.number())).size() > 0
        ).map(Port::number).collect(Collectors.toSet());
        List<FlowRule> flowRules = new ArrayList<>();

        // process_int_transit.tb_int_insert
        PiActionParam transitIdParam = new PiActionParam(
                IntConstants.ACT_PRM_SWITCH_ID,
                ImmutableByteSequence.copyFrom(
                        Integer.parseInt(deviceId.toString().substring(
                                deviceId.toString().length() - 2))));
        PiAction transitAction = PiAction.builder()
                .withId(IntConstants.ACT_INT_TRANSIT_ID)
                .withParameter(transitIdParam)
                .build();
        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .piTableAction(transitAction)
                .build();

        FlowRule transitFlowRule = DefaultFlowRule.builder()
                .withTreatment(treatment)
                .fromApp(appId)
                .withPriority(DEFAULT_PRIORITY)
                .makePermanent()
                .forDevice(deviceId)
                .forTable(IntConstants.TBL_INT_INSERT_ID)
                .build();
        flowRules.add(transitFlowRule);

        for (PortNumber portNumber: hostPorts) {
            // process_set_source_sink.tb_set_source for each host-facing port
            PiCriterion ingressCriterion = PiCriterion.builder()
                    .matchExact(BasicConstants.HDR_IN_PORT_ID, portNumber.toLong())
                    .build();
            TrafficSelector srcSelector = DefaultTrafficSelector.builder()
                    .matchPi(ingressCriterion)
                    .build();
            PiAction setSourceAct = PiAction.builder()
                    .withId(IntConstants.ACT_INT_SET_SOURCE_ID)
                    .build();
            TrafficTreatment srcTreatment = DefaultTrafficTreatment.builder()
                    .piTableAction(setSourceAct)
                    .build();
            FlowRule srcFlowRule = DefaultFlowRule.builder()
                    .withSelector(srcSelector)
                    .withTreatment(srcTreatment)
                    .fromApp(appId)
                    .withPriority(DEFAULT_PRIORITY)
                    .makePermanent()
                    .forDevice(deviceId)
                    .forTable(IntConstants.TBL_SET_SOURCE_ID)
                    .build();
            flowRules.add(srcFlowRule);

            // process_set_source_sink.tb_set_sink
            PiCriterion egressCriterion = PiCriterion.builder()
                    .matchExact(IntConstants.HDR_OUT_PORT_ID, portNumber.toLong())
                    .build();
            TrafficSelector sinkSelector = DefaultTrafficSelector.builder()
                    .matchPi(egressCriterion)
                    .build();
            PiAction setSinkAct = PiAction.builder()
                    .withId(IntConstants.ACT_INT_SET_SINK_ID)
                    .build();
            TrafficTreatment sinkTreatment = DefaultTrafficTreatment.builder()
                    .piTableAction(setSinkAct)
                    .build();
            FlowRule sinkFlowRule = DefaultFlowRule.builder()
                    .withSelector(sinkSelector)
                    .withTreatment(sinkTreatment)
                    .fromApp(appId)
                    .withPriority(DEFAULT_PRIORITY)
                    .makePermanent()
                    .forDevice(deviceId)
                    .forTable(IntConstants.TBL_SET_SINK_ID)
                    .build();
            flowRules.add(sinkFlowRule);
        }
        flowRules.forEach(flowRule -> flowRuleService.applyFlowRules(flowRule));

        // Populate tb_int_inst_0003 table
        INST_0003_ACTION_MAP.forEach((matchValue, actionId) ->
                                             populateInstTableEntry(IntConstants.TBL_INT_INST_0003_ID,
                                                                    IntConstants.INT_HDR_INST_MASK_0003_ID,
                                                                    matchValue,
                                                                    actionId,
                                                                    appId));
        // Populate tb_int_inst_0407 table
        INST_0407_ACTION_MAP.forEach((matchValue, actionId) ->
                                             populateInstTableEntry(IntConstants.TBL_INT_INST_0407_ID,
                                                                    IntConstants.INT_HDR_INST_MASK_0407_ID,
                                                                    matchValue,
                                                                    actionId,
                                                                    appId));
    }

    @Override
    public void addWatchlistEntry(IntObjective obj) {
        // TODO: support different types of watchlist other than flow watchlist
        FlowRule flowRule = buildWatchlistEntry(obj);
        if(flowRule != null) {
            flowRuleService = this.handler().get(FlowRuleService.class);
            flowRuleService.applyFlowRules(flowRule);
            log.debug("Watchlist entry {} has been installed on {}", flowRule, deviceId);
        } else {
            log.warn("Failed to install watchlist entry on {}", deviceId);
        }
    }

    @Override
    public void removeWatchlistEntry(IntObjective obj) {
        FlowRule flowRule = buildWatchlistEntry(obj);
        if(flowRule != null) {
            flowRuleService = this.handler().get(FlowRuleService.class);
            flowRuleService.removeFlowRules(flowRule);
            log.debug("Watchlist entry {} has been removed from {}", flowRule, deviceId);
        } else {
            log.warn("Failed to remove watchlist entry on {}", deviceId);
        }
    }
//
//    @Override
//    public void setupReportEntry(IntConfig config) {
//        // Synthesize IP address and MAC address for this device,
//        // which makes the collector identify where the report comes from.
//        // Note that if a collector is directly attached to the device,
//        // that interface's IP and MAC address should be used.
//
//        IpAddress sinkIp = IpAddress.valueOf(deviceId.hashCode());
//        MacAddress sinkMac = MacAddress.valueOf(deviceId.hashCode());
//
//        // TODO: install a flow rule for report generation
//        throw new UnsupportedOperationException("Not implemented yet");
//    }

    private void populateInstTableEntry(PiTableId tableId, PiMatchFieldId matchFieldId,
                                        int matchValue, PiActionId actionId, ApplicationId appId) {
        PiCriterion instCriterion = PiCriterion.builder()
                .matchExact(matchFieldId, matchValue)
                .build();
        TrafficSelector instSelector = DefaultTrafficSelector.builder()
                .matchPi(instCriterion)
                .build();
        PiAction instAction = PiAction.builder()
                .withId(actionId)
                .build();
        TrafficTreatment instTreatment = DefaultTrafficTreatment.builder()
                .piTableAction(instAction)
                .build();

        FlowRule instFlowRule = DefaultFlowRule.builder()
                .withSelector(instSelector)
                .withTreatment(instTreatment)
                .withPriority(DEFAULT_PRIORITY)
                .makePermanent()
                .forDevice(deviceId)
                .forTable(tableId)
                .fromApp(appId)
                .build();

        flowRuleService.applyFlowRules(instFlowRule);
    }

//
//    @Override
//    public void addEventEntry(IntEvent event) {
//        //TODO
//        throw new UnsupportedOperationException("Not implemented yet");
//    }
//
//    @Override
//    public void removeEventEntry(IntEvent event) {
//        //TODO
//        throw new UnsupportedOperationException("Not implemented yet");
//    }
//
//    @Override
//    public void addIntSourceEntry(IntFlow flow) {
//
//    }
//
//    @Override
//    public void removeIntSourceEntry(IntFlow flow) {
//
//    }
//
//    @Override
//    public void addIntSinkEntry(IntFlow flow) {
//
//    }
//
//    @Override
//    public void removeIntSinkEntry(IntFlow flow) {
//
//    }

    private FlowRule buildWatchlistEntry(IntObjective obj) {
        PiActionParam maxHopParam = new PiActionParam(
                IntConstants.ACT_PRM_MAX_HOP_ID,
                ImmutableByteSequence.copyFrom(MAX_HOP));
        PiActionParam instCntParam = new PiActionParam(
                IntConstants.ACT_PRM_INS_CNT_ID,
                ImmutableByteSequence.copyFrom(Integer.bitCount(obj.instructionBitmap())));
        PiActionParam inst0003Param = new PiActionParam(
                IntConstants.ACT_PRM_INS_MASK0003_ID,
                ImmutableByteSequence.copyFrom((obj.instructionBitmap() >> 12) & 0xF));
        PiActionParam inst0407Param = new PiActionParam(
                IntConstants.ACT_PRM_INS_MASK0407_ID,
                ImmutableByteSequence.copyFrom((obj.instructionBitmap() >> 8) & 0xF));

        PiAction intSourceAction = PiAction.builder()
                .withId(IntConstants.ACT_INT_SOURCE_DSCP_ID)
                .withParameter(maxHopParam)
                .withParameter(instCntParam)
                .withParameter(inst0003Param)
                .withParameter(inst0407Param)
                .build();

        TrafficTreatment instTreatment = DefaultTrafficTreatment.builder()
                .piTableAction(intSourceAction)
                .build();

        TrafficSelector.Builder sBuilder = DefaultTrafficSelector.builder();
        for (Criterion criterion : obj.selector().criteria()) {
            switch(criterion.type()) {
                case IPV4_SRC:
                    sBuilder.matchIPSrc(((IPCriterion) criterion).ip());
                    break;
                case IPV4_DST:
                    sBuilder.matchIPDst(((IPCriterion)criterion).ip());
                    break;
                case TCP_SRC:
                    sBuilder.matchPi(
                            PiCriterion.builder().matchTernary(
                                    IntConstants.LOCAL_META_SRC_PORT_ID,
                                    ((TcpPortCriterion) criterion).tcpPort().toInt(), PORT_MASK)
                                    .build());
                    break;
                case UDP_SRC:
                    sBuilder.matchPi(
                            PiCriterion.builder().matchTernary(
                                    IntConstants.LOCAL_META_SRC_PORT_ID,
                                    ((UdpPortCriterion) criterion).udpPort().toInt(), PORT_MASK)
                                    .build());
                    break;
                case TCP_DST:
                    sBuilder.matchPi(
                            PiCriterion.builder().matchTernary(
                                    IntConstants.LOCAL_META_DST_PORT_ID,
                                    ((TcpPortCriterion)criterion).tcpPort().toInt(), PORT_MASK)
                                    .build());
                    break;
                case UDP_DST:
                    sBuilder.matchPi(
                            PiCriterion.builder().matchTernary(
                                    IntConstants.LOCAL_META_DST_PORT_ID,
                                    ((UdpPortCriterion)criterion).udpPort().toInt(), PORT_MASK)
                                    .build());
                    break;
                default:
                    log.warn("Unsupported criterion type: {}", criterion.type());
            }
        }

        // FIXME: update Cookie value
        return DefaultFlowRule.builder()
                .forDevice(this.data().deviceId())
                .withSelector(sBuilder.build())
                .withTreatment(instTreatment)
                .withPriority(DEFAULT_PRIORITY)
                .forTable(IntConstants.TBL_INT_SOURCE_ID)
                .withCookie(0)
                .withIdleTimeout(IDLE_TIMEOUT)
                .build();
    }
}
