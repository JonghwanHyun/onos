/*
 * Copyright 2017-present Open Networking Foundation
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

/* -*- P4_16 -*- */
#ifndef __INT_SOURCE__
#define __INT_SOURCE__

// Insert INT header to the packet
control process_int_source (
    inout parsed_headers_t hdr,
    inout fabric_metadata_t fabric_metadata,
    inout standard_metadata_t standard_metadata) {

    direct_counter(CounterType.packets_and_bytes) counter_int_source;

    action int_source(bit<8> remaining_hop_cnt, bit<5> hop_metadata_len,
        bit<4> ins_mask0003, bit<4> ins_mask0407, bit<4> ins_mask1215) {
        // insert INT shim header
        hdr.intl4_shim.setValid();
        // int_type: Hop-by-hop type (1) , destination type (2)
        hdr.intl4_shim.int_type = 1;
        hdr.intl4_shim.len = INT_HEADER_LEN_WORD;
        hdr.intl4_shim.dscp = hdr.ipv4.dscp;

        // insert INT header
        hdr.int_header.setValid();
        // 1 for INT version 1.0
        hdr.int_header.ver = 1;
        hdr.int_header.rep = 0;
        hdr.int_header.c = 0;
        hdr.int_header.e = 0;
        hdr.int_header.m = 0;
        hdr.int_header.rsvd1 = 0;
        hdr.int_header.rsvd2 = 0;
        hdr.int_header.hop_metadata_len = hop_metadata_len;
        hdr.int_header.remaining_hop_cnt = remaining_hop_cnt;
        hdr.int_header.instruction_mask_0003 = ins_mask0003;
        hdr.int_header.instruction_mask_0407 = ins_mask0407;
        hdr.int_header.instruction_mask_0811 = 0; // not supported
        hdr.int_header.instruction_mask_1215 = ins_mask1215; // only checksum complement (bit 15) is supported

        // add the header len to total len
        hdr.ipv4.total_len = hdr.ipv4.total_len + (bit<16>)(INT_HEADER_LEN_WORD * INT_WORD_SIZE);
        // TODO: handle this correctly, with exact bit-length
        hdr.tcp.data_offset = hdr.tcp.data_offset + (bit<4>)INT_HEADER_LEN_WORD;
        hdr.udp.len = hdr.udp.len + (bit<16>)(INT_HEADER_LEN_WORD * INT_WORD_SIZE);
    }
    action int_source_dscp(bit<8> remaining_hop_cnt, bit<5> hop_metadata_len,
        bit<4> ins_mask0003, bit<4> ins_mask0407, bit<4> ins_mask1215) {
        int_source(remaining_hop_cnt, hop_metadata_len, ins_mask0003, ins_mask0407, ins_mask1215);
        hdr.ipv4.dscp = INT_DSCP;
    }

    table tb_int_source {
        key = {
            hdr.ipv4.src_addr: ternary;
            hdr.ipv4.dst_addr: ternary;
            fabric_metadata.l4_src_port: ternary;
            fabric_metadata.l4_dst_port: ternary;
        }
        actions = {
            int_source_dscp;
        }
        counters = counter_int_source;
        size = 1024;
    }

    apply {
        tb_int_source.apply();
    }
}

control process_set_source_sink (
    inout parsed_headers_t hdr,
    inout fabric_metadata_t fabric_metadata,
    inout standard_metadata_t standard_metadata) {

    direct_counter(CounterType.packets_and_bytes) counter_set_source_sink;

    action int_set_source () {
        fabric_metadata.int_meta.source = 1;
    }

    action int_set_sink () {
        fabric_metadata.int_meta.sink = 1;
    }

    table tb_set_source {
        key = {
            standard_metadata.ingress_port: exact;
        }
        actions = {
            int_set_source;
        }
        size = 256;
    }
    table tb_set_sink {
        key = {
            standard_metadata.egress_port: exact;
        }
        actions = {
            int_set_sink;
        }
        size = 256;
    }

    apply {
        tb_set_source.apply();
        tb_set_sink.apply();
    }
}
#endif
