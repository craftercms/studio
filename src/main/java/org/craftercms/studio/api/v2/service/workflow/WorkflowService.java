/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.model.rest.content.SandboxItem;

import java.time.ZonedDateTime;
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
     * @param isNew value to set the 'new' flag to, or null if the flag should not change
     * @param modified value to set the 'modified' flag to, or null if the flag should not change
     */
    void updateItemStates(String siteId, List<String> paths, boolean clearSystemProcessing, boolean clearUserLocked,
                          Boolean live, Boolean staged, Boolean isNew, Boolean modified);

    /**
     * Update item state flags for given path query
     * @param siteId site identifier
     * @param path path regex to identify items
     * @param clearSystemProcessing if true clear system processing flag, otherwise ignore
     * @param clearUserLocked if true clear user locked flag, otherwise ignore
     * @param live if true set live flag, otherwise reset it
     * @param staged if true set staged flag, otherwise reset it
     * @param isNew value to set the 'new' flag to, or null if the flag should not change
     * @param modified value to set the 'modified' flag to, or null if the flag should not change
     */
    void updateItemStatesByQuery(String siteId, String path, Long states, boolean clearSystemProcessing,
                                 boolean clearUserLocked, Boolean live, Boolean staged, Boolean isNew, Boolean modified);

    /**
     * Get workflow affected paths if content is edited
     * @param siteId site identifier
     * @param path path of the content to be edited
     * @return List of sandbox items that will be taken out of workflow after edit
     */
    List<SandboxItem> getWorkflowAffectedPaths(String siteId, String path)
            throws UserNotFoundException, ServiceLayerException;

    /**
     * Request approval for content to be published
     * @param siteId site identifier
     * @param paths list of paths for content items
     * @param optionalDependencies list of paths soft dependencies
     * @param publishingTarget publishing target
     * @param schedule schedule when to publish content
     * @param comment submission comment
     * @param sendEmailNotifications if true send email notifications
     */
    void requestPublish(String siteId, List<String> paths, List<String> optionalDependencies, String publishingTarget,
                        ZonedDateTime schedule, String comment, boolean sendEmailNotifications)
            throws ServiceLayerException, UserNotFoundException, DeploymentException;

    /**
     * Direct publish content
     * @param siteId site identifier
     * @param paths list of paths for content items to publish
     * @param optionalDependencies list of paths soft dependencies
     * @param publishingTarget publishing target
     * @param schedule schedule when to publish content
     * @param comment publishing comment
     */
    void publish(String siteId, List<String> paths, List<String> optionalDependencies, String publishingTarget,
                 ZonedDateTime schedule, String comment)
            throws ServiceLayerException, UserNotFoundException, DeploymentException;

    /**
     * Approve request for publish
     * @param siteId site identifier
     * @param paths list of paths for content item that author requested publish
     * @param optionalDependencies list of paths soft dependencies
     * @param publishingTarget publishing target
     * @param schedule schedule when to publish content
     * @param comment approval comment
     */
    void approve(String siteId, List<String> paths, List<String> optionalDependencies, String publishingTarget,
                 ZonedDateTime schedule, String comment)
            throws UserNotFoundException, ServiceLayerException, DeploymentException;

    /**
     * Reject request for publish
     * @param siteId site identifier
     * @param paths list of paths for content items that author requested publish
     * @param comment rejection comment
     */
    void reject(String siteId, List<String> paths, String comment)
            throws ServiceLayerException, DeploymentException, UserNotFoundException;

    /**
     * Delete content items
     * @param siteId site identifier
     * @param paths list of paths for content items to be deleted
     * @param optionalDependencies list of paths soft dependencies
     * @param comment deletion comment
     */
    void delete(String siteId, List<String> paths, List<String> optionalDependencies, String comment)
            throws DeploymentException, ServiceLayerException, UserNotFoundException;
}
