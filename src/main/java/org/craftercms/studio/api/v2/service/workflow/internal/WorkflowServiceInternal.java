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

package org.craftercms.studio.api.v2.service.workflow.internal;

import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.Workflow;
import org.craftercms.studio.api.v2.dal.WorkflowItem;

import java.util.List;

public interface WorkflowServiceInternal {

    /**
     * Get workflow entry
     * @param siteId
     * @param path
     * @return
     */
    WorkflowItem getWorkflowEntry(String siteId, String path);

    /**
     * Get workflow entry
     * @param siteId
     * @param path
     * @param publishingPackageId
     * @return
     */
    Workflow getWorkflowEntry(String siteId, String path, String publishingPackageId);

    /**
     * insert new workflow entry
     * @param workflow workflow entry
     */
    void insertWorkflow(Workflow workflow);

    /**
     * Get submitted items for site
     * @param site site identifier
     * @return
     */
    List<WorkflowItem> getSubmittedItems(String site);

    /**
     * Delete workflow entries for given site and paths
     * @param site site identifier
     * @param paths list of paths to delete workflow
     */
    void deleteWorkflowEntries(String site, List<String> paths);

    /**
     * Delete workflow entry for given site and path
     * @param site site identifier
     * @param path path to delete workflow
     */
    void deleteWorkflowEntry(String site, String path);

    /**
     * Delete workflow entries for given site
     * @param siteId site id
     */
    void deleteWorkflowEntriesForSite(long siteId);
}
