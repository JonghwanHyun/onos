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
package org.onosproject.p4int.api;

import com.google.common.annotations.Beta;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.driver.HandlerBehaviour;
import org.onosproject.net.flow.TrafficSelector;

import java.util.Set;

@Beta
public interface IntProgrammable extends HandlerBehaviour {

    /**
     * Initializes the pipeline, by installing required flow rules
     * not relevant to specific watchlist, report and event.
     *
     * @param appId ID of the application which initiates this pipeline
     */
    void init(ApplicationId appId);

    /**
     * Installs a given watchlist entry to the device.
     *
     * @param intent an IntIntent
     */
    void addWatchlistEntry(IntIntent intent);

//    /**
//     * Removes a given watchlist entry from the device.
//     *
//     * @param flow a watchlist entry to remove
//     */
//    void removeWatchlistEntry(IntFlow flow);
//
//    /**
//     * Installs a given flow entry to the INT source device.
//     *
//     * @param flow a flow entry to install
//     */
//    void addIntSourceEntry(IntFlow flow);
//
//    /**
//     * Removes a given flow entry from the INT source device.
//     *
//     * @param flow a flow entry to remove
//     */
//    void removeIntSourceEntry(IntFlow flow);
//
//    /**
//     * Installs a given flow entry to the INT sink device.
//     *
//     * @param flow a flow entry to install
//     */
//    void addIntSinkEntry(IntFlow flow);
//
//    /**
//     * Removes a given flow entry from the INT sink device.
//     *
//     * @param flow a flow entry to remove
//     */
//    void removeIntSinkEntry(IntFlow flow);
//
    /**
     * Set up report-related configuration.
     *
     * @param config a configuration regarding to the collector
     */
    void setupReportEntry(IntConfig config);
//
//    /**
//     * Installs given event onto the device.
//     *
//     * @param event an event description to install
//     */
//    void addEventEntry(IntEvent event);
//
//    /**
//     * Removes given event from the device.
//     *
//     * @param event an event description to remove
//     */
//    void removeEventEntry(IntEvent event);
}
