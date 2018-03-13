/*
 * Copyright 2015-present Open Networking Foundation
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
package org.onosproject.p4int.api;

import com.google.common.annotations.Beta;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onosproject.net.Port;

/**
 * Network-level INT configuration.
 */
@Beta
public final class IntConfig {
    /**
     * Represents a type of telemetry spec to collect in the dataplane.
     */
    enum TelemetrySpec {
        /**
         * Embeds telemetry metadata according to the INT specification.
         *
         * @see <a href="https://github.com/p4lang/p4-applications/blob/master/docs/INT.pdf">
         *     INT sepcification</a>
         */
        P4INT,
        /**
         * Embeds telemetry metadata according to the OAM specification.
         *
         * @see <a href="https://tools.ietf.org/html/draft-ietf-ippm-ioam-data">
         *     Data fields for In-situ OAM</a>
         */
        IOAM
    }

    private final IpAddress collectorIp;
    private final Port collectorPort;
    private final MacAddress collectorMac;
    private final IpAddress sinkIp;
    private final MacAddress sinkMac;
    private final TelemetrySpec spec;
    private boolean enabled;

    private IntConfig(IpAddress collectorIp, Port collectorPort, MacAddress collectorMac,
                      IpAddress sinkIp, MacAddress sinkMac, TelemetrySpec spec, boolean enabled) {
        this.collectorIp = collectorIp;
        this.collectorPort = collectorPort;
        this.collectorMac = collectorMac;
        this.sinkIp = sinkIp;
        this.sinkMac = sinkMac;
        this.spec = spec;
        this.enabled = enabled;
    }

    /**
     * Returns IP address of the collector.
     *
     * @return collector IP address
     */
    public IpAddress collectorIp() {
        return collectorIp;
    }

    /**
     * Returns UDP port number of the collector.
     *
     * @return collector UDP port number
     */
    public Port collectorPort() {
        return collectorPort;
    }

    /**
     * Returns MAC address of the collector.
     *
     * @return collector MAC address
     */
    public MacAddress collectorMac() {
        return collectorMac;
    }

    /**
     * Returns IP address of the sink device.
     *
     * @return sink device's IP address
     */
    public IpAddress sinkIp() {
        return sinkIp;
    }

    /**
     * Returns MAC address of the sink device.
     *
     * @return sink device's MAC address
     */
    public MacAddress sinkMac() {
        return sinkMac;
    }

    /**
     * Returns the type of telemetry spec (P4INT or IOAM).
     *
     * @return telemetry spec
     */
    public TelemetrySpec spec() {
        return spec;
    }

    /**
     * Returns the status of INT functionality.
     *
     * @return true if INT is enabled; false otherwise.
     */
    public boolean enabled() {
        return enabled;
    }

    /**
     * An IntConfig object builder.
     */
    public static final class Builder {

        private IpAddress collectorIp;
        private Port collectorPort;
        private MacAddress collectorMac;
        private IpAddress sinkIp;
        private MacAddress sinkMac;
        private TelemetrySpec spec = TelemetrySpec.P4INT;
        private boolean enabled = false;

        /**
         * Assigns a collector IP address to the IntConfig object.
         *
         * @param collectorIp IP address of the collector
         * @return an IntConfig builder
         */
        public IntConfig.Builder withCollectorIp(IpAddress collectorIp) {
            this.collectorIp = collectorIp;
            return this;
        }

        /**
         * Assigns a collector UDP port to the IntConfig object.
         *
         * @param collectorPort UDP port number of the collector
         * @return an IntConfig builder
         */
        public IntConfig.Builder withCollectorPort(Port collectorPort) {
            this.collectorPort = collectorPort;
            return this;
        }

        /**
         * Assigns a collector MAC address to the IntConfig object.
         *
         * @param collectorMac MAC address of the collector
         * @return an IntConfig builder
         */
        public IntConfig.Builder withCollectorMac(MacAddress collectorMac) {
            this.collectorMac = collectorMac;
            return this;
        }

        /**
         * Assigns an IP address of the sink device to the IntConfig object.
         *
         * @param sinkIp sink device's IP address
         * @return an IntConfig builder
         */
        public IntConfig.Builder withSinkIp(IpAddress sinkIp) {
            this.sinkIp = sinkIp;
            return this;
        }

        /**
         * Assigns a MAC address of the sink device to the IntConfig object.
         *
         * @param sinkMac sink device's MAC address
         * @return an IntConfig builder
         */
        public IntConfig.Builder withSinkMac(MacAddress sinkMac) {
            this.sinkMac = sinkMac;
            return this;
        }

        /**
         * Assigns the type of telemetry spec.
         *
         * @param spec telemetry spec
         * @return an IntConfig builder
         */
        public IntConfig.Builder withTelemetrySpec(TelemetrySpec spec) {
            this.spec = spec;
            return this;
        }

        /**
         * Assigns the status of INT
         * True to enable INT functionality, false otherwise.
         *
         * @param enabled the status of INT
         * @return an IntConfig builder
         */
        public IntConfig.Builder withEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Bulids the IntConfig object.
         *
         * @return an IntConfig object
         */
        public IntConfig build() {
            return new IntConfig(collectorIp, collectorPort, collectorMac,
                                 sinkIp, sinkMac, spec, enabled);
        }
    }
}
