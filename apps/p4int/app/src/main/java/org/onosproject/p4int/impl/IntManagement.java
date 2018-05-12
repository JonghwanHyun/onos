package org.onosproject.p4int.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.TpPort;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.p4int.api.IntIntent;
import org.onosproject.p4int.api.IntService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class IntManagement {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private ApplicationId appId;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntService intService;

    @Activate
    protected void activate() {
        appId = coreService.registerApplication("org.onosproject.p4int.app");
        log.info("Started");
        testAddIntent();
    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopped");
    }

    private void testAddIntent() {
        IntIntent.Builder builder = IntIntent.builder();
        TrafficSelector.Builder sBuilder = DefaultTrafficSelector.builder();

        sBuilder.matchUdpDst(TpPort.tpPort(5001));


        builder.withSelector(sBuilder.build())
                .withHeaderType(IntIntent.IntHeaderType.HOP_BY_HOP)
                .withMetadataType(IntIntent.IntMetadataType.HOP_LATENCY)
                .withReportType(IntIntent.IntReportType.TRACKED_FLOW)
                .withTelemetryMode(IntIntent.TelemetryMode.INBAND_TELEMETRY);
        intService.installIntIntent(builder.build());
    }
}
