package org.onosproject.pipelines.fabric;

import com.google.common.collect.ImmutableBiMap;
import org.onosproject.net.pi.model.PiTableId;

import java.util.Optional;

public class FabricIntInterpreter extends FabricInterpreter{
    private static final ImmutableBiMap<Integer, PiTableId> TABLE_ID_MAP =
            ImmutableBiMap.<Integer, PiTableId>builder()
            // Id starting from 100, to avoid possible overlapping with fabric table id map in the future
                    .put(100, FabricIntConstants.TBL_SET_SOURCE_ID)
                    .put(101, FabricIntConstants.TBL_SET_SINK_ID)
                    .put(102, FabricIntConstants.TBL_INT_INSERT_ID)
                    .put(103, FabricIntConstants.TBL_INT_INST_0003_ID)
                    .put(104, FabricIntConstants.TBL_INT_INST_0407_ID)
                    .put(105, FabricIntConstants.TBL_INT_INST_1215_ID)
                    .put(106, FabricIntConstants.TBL_INT_SOURCE_ID)
                    .build();

    @Override
    public Optional<PiTableId> mapFlowRuleTableId(int flowRuleTableId) {
        return TABLE_ID_MAP.containsKey(flowRuleTableId) ?
                Optional.ofNullable(TABLE_ID_MAP.get(flowRuleTableId)) :
                super.mapFlowRuleTableId(flowRuleTableId);
    }

    @Override
    public Optional<Integer> mapPiTableId(PiTableId piTableId) {
        return TABLE_ID_MAP.inverse().containsKey(piTableId) ?
                Optional.ofNullable(TABLE_ID_MAP.inverse().get(piTableId)) :
                super.mapPiTableId(piTableId);
    }
}
