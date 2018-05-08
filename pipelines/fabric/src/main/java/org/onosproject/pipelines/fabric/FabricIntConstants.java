package org.onosproject.pipelines.fabric;

import org.onosproject.net.pi.model.PiActionId;
import org.onosproject.net.pi.model.PiActionParamId;
import org.onosproject.net.pi.model.PiMatchFieldId;
import org.onosproject.net.pi.model.PiTableId;

import static org.onosproject.pipelines.fabric.FabricConstants.*;

public final class FabricIntConstants {
    // Hide default constructor
    private FabricIntConstants() {
    }

    // Strings
    private static final String FABRIC_INGRESS = "FabricIngress";
    private static final String FABRIC_EGRESS = "FabricEgress";
    private static final String CTRL_SET_SOURCE_SINK = FABRIC_EGRESS + DOT + "process_set_source_sink";
    private static final String CTRL_INT_SOURCE = FABRIC_EGRESS + DOT + "process_int_source";
    private static final String CTRL_INT_TRANSIT = FABRIC_EGRESS + DOT + "process_int_transit";
    private static final String CTRL_INT_SINK = FABRIC_EGRESS + DOT + "process_int_sink";
    private static final String CTRL_INT_OUTER_ENCAP = FABRIC_EGRESS + DOT + "process_int_outer_encap";
    private static final String INT_METADATA = "int_meta";
    private static final String INT_HDR = "int_header";

    // Header field IDs
    public static final PiMatchFieldId INT_META_SINK_ID =
            PiMatchFieldId.of(FABRIC_METADATA + DOT + INT_METADATA + DOT + "sink");
    public static final PiMatchFieldId INT_HDR_INST_MASK_0003_ID =
            PiMatchFieldId.of(HDR + DOT + INT_HDR + DOT + "instruction_mask_0003");
    public static final PiMatchFieldId INT_HDR_INST_MASK_0407_ID =
            PiMatchFieldId.of(HDR + DOT + INT_HDR + DOT + "instruction_mask_0407");
    public static final PiMatchFieldId INT_HDR_INST_MASK_1215_ID =
            PiMatchFieldId.of(HDR + DOT + INT_HDR + DOT + "instruction_mask_1215");
    public static final PiMatchFieldId HF_STANDARD_METADATA_EGRESS_PORT_ID =
            PiMatchFieldId.of(STANDARD_METADATA + DOT + "egress_port");
    // Table IDs
    public static final PiTableId TBL_SET_SOURCE_ID =
            PiTableId.of(FABRIC_INGRESS + DOT + CTRL_SET_SOURCE_SINK + DOT + "tb_set_source");
    public static final PiTableId TBL_SET_SINK_ID =
            PiTableId.of(FABRIC_INGRESS + DOT + CTRL_SET_SOURCE_SINK + DOT + "tb_set_sink");
    public static final PiTableId TBL_INT_SOURCE_ID =
            PiTableId.of(FABRIC_EGRESS + DOT + CTRL_INT_SOURCE + DOT + "tb_int_source");
    public static final PiTableId TBL_INT_INSERT_ID =
            PiTableId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "tb_int_insert");
    public static final PiTableId TBL_INT_INST_0003_ID =
            PiTableId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "tb_int_inst_0003");
    public static final PiTableId TBL_INT_INST_0407_ID =
            PiTableId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "tb_int_inst_0407");
    public static final PiTableId TBL_INT_INST_1215_ID =
            PiTableId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "tb_int_inst_1215");
    // Action IDs
    public static final PiActionId ACT_INT_SET_SOURCE_ID =
            PiActionId.of(FABRIC_INGRESS + DOT + CTRL_SET_SOURCE_SINK + DOT + "int_set_source");
    public static final PiActionId ACT_INT_SET_SINK_ID =
            PiActionId.of(FABRIC_INGRESS + DOT + CTRL_SET_SOURCE_SINK + DOT + "int_set_sink");
    public static final PiActionId ACT_INT_SOURCE_DSCP_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_SOURCE + DOT + "int_source_dscp");
    public static final PiActionId ACT_INT_TRANSIT_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_transit");
    public static final PiActionId ACT_INT_SET_HEADER_0003_I0_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0003_i0");
    public static final PiActionId ACT_INT_SET_HEADER_0003_I1_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0003_i1");
    public static final PiActionId ACT_INT_SET_HEADER_0003_I2_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0003_i2");
    public static final PiActionId ACT_INT_SET_HEADER_0003_I3_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0003_i3");
    public static final PiActionId ACT_INT_SET_HEADER_0003_I4_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0003_i4");
    public static final PiActionId ACT_INT_SET_HEADER_0003_I5_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0003_i5");
    public static final PiActionId ACT_INT_SET_HEADER_0003_I6_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0003_i6");
    public static final PiActionId ACT_INT_SET_HEADER_0003_I7_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0003_i7");
    public static final PiActionId ACT_INT_SET_HEADER_0003_I8_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0003_i8");
    public static final PiActionId ACT_INT_SET_HEADER_0003_I9_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0003_i9");
    public static final PiActionId ACT_INT_SET_HEADER_0003_I10_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0003_i10");
    public static final PiActionId ACT_INT_SET_HEADER_0003_I11_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0003_i11");
    public static final PiActionId ACT_INT_SET_HEADER_0003_I12_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0003_i12");
    public static final PiActionId ACT_INT_SET_HEADER_0003_I13_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0003_i13");
    public static final PiActionId ACT_INT_SET_HEADER_0003_I14_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0003_i14");
    public static final PiActionId ACT_INT_SET_HEADER_0003_I15_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0003_i15");
    public static final PiActionId ACT_INT_SET_HEADER_0407_I0_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0407_i0");
    public static final PiActionId ACT_INT_SET_HEADER_0407_I1_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0407_i1");
    public static final PiActionId ACT_INT_SET_HEADER_0407_I2_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0407_i2");
    public static final PiActionId ACT_INT_SET_HEADER_0407_I3_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0407_i3");
    public static final PiActionId ACT_INT_SET_HEADER_0407_I4_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0407_i4");
    public static final PiActionId ACT_INT_SET_HEADER_0407_I5_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0407_i5");
    public static final PiActionId ACT_INT_SET_HEADER_0407_I6_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0407_i6");
    public static final PiActionId ACT_INT_SET_HEADER_0407_I7_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0407_i7");
    public static final PiActionId ACT_INT_SET_HEADER_0407_I8_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0407_i8");
    public static final PiActionId ACT_INT_SET_HEADER_0407_I9_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0407_i9");
    public static final PiActionId ACT_INT_SET_HEADER_0407_I10_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0407_i10");
    public static final PiActionId ACT_INT_SET_HEADER_0407_I11_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0407_i11");
    public static final PiActionId ACT_INT_SET_HEADER_0407_I12_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0407_i12");
    public static final PiActionId ACT_INT_SET_HEADER_0407_I13_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0407_i13");
    public static final PiActionId ACT_INT_SET_HEADER_0407_I14_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0407_i14");
    public static final PiActionId ACT_INT_SET_HEADER_0407_I15_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_0407_i15");
    public static final PiActionId ACT_INT_SET_HEADER_1215_I1_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "int_set_header_1215_i1");
    public static final PiActionId ACT_SET_E_BIT_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_TRANSIT + DOT + "set_e_bit");
    public static final PiActionId ACT_INT_UPDATE_IPV4_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_OUTER_ENCAP + DOT + "int_update_ipv4");
    public static final PiActionId ACT_INT_UPDATE_UDP_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_OUTER_ENCAP + DOT + "int_update_udp");
    public static final PiActionId ACT_INT_UPDATE_TCP_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_OUTER_ENCAP + DOT + "int_update_tcp");
    public static final PiActionId ACT_INT_UPDATE_SHIM_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_OUTER_ENCAP + DOT + "int_update_shim");
    public static final PiActionId ACT_INT_RESTORE_HEADER_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_SINK + DOT + "restore_header");
    public static final PiActionId ACT_INT_SINK_ID =
            PiActionId.of(FABRIC_EGRESS + DOT + CTRL_INT_SINK + DOT + "int_sink");

    // Action param IDs
    public static final PiActionParamId ACT_PRM_REMAINING_HOP_CNT_ID = PiActionParamId.of("remaining_hop_cnt");
    public static final PiActionParamId ACT_PRM_HOP_META_LEN_ID = PiActionParamId.of("hop_metadata_len");
    public static final PiActionParamId ACT_PRM_INS_MASK0003_ID = PiActionParamId.of("ins_mask0003");
    public static final PiActionParamId ACT_PRM_INS_MASK0407_ID = PiActionParamId.of("ins_mask0407");
    public static final PiActionParamId ACT_PRM_INS_MASK1215_ID = PiActionParamId.of("ins_mask1215");
    public static final PiActionParamId ACT_PRM_SWITCH_ID = PiActionParamId.of("switch_id");
}