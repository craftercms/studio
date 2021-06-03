/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.api.v2.service.workflow;

import org.craftercms.studio.model.rest.content.SandboxItem;

import java.util.List;

public interface WorkflowService {

    /**
     * Get total number of item states records for given filters by path regex and states mask
     * @param siteId site identifier
     * @param path path regex to filter items
     * @param states states mask to filter items by state
     * @return number of records
     */
    int getItemStatesTotal(String siteId, String path, Long states);

    /**
     * Get item states for given filters by path regex and states mask
     * @param siteId site identifier
     * @param path path regex to filter items
     * @param states states mask to filter items by state
     * @param offset offset for the first record in result set
     * @param limit number of item states records to return
     * @return list of sandbox items
     */
    List<SandboxItem> getItemStates(String siteId, String path, Long states, int offset, int limit);

    /**
     * Update item state flags for given items
     * @param siteId site identifier
     * @param paths item paths
     * @param clearSystemProcessing if true clear system processing flag, otherwise ignore
     * @param clearUserLocked if true clear user locked flag, otherwise ignore
     * @param live if true set live flag, otherwise reset it
     * @param staged if true set staged flag, otherwise reset it
     */
    void updateItemStates(String siteId, List<String> paths, boolean clearSystemProcessing, boolean clearUserLocked,
                          boolean live, boolean staged);

    /**
     * Update item state flags for given path query
     * @param siteId site identifier
     * @param path path regex to identify items
     * @param clearSystemProcessing if true clear system processing flag, otherwise ignore
     * @param clearUserLocked if true clear user locked flag, otherwise ignore
     * @param live if true set live flag, otherwise reset it
     * @param staged if true set staged flag, otherwise reset it
     */
    void updateItemStatesByQuery(String siteId, String path, Long states, boolean clearSystemProcessing,
                                 boolean clearUserLocked, Boolean live, Boolean staged);
}
