/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.craftercms.studio.api.v1.service.deployment;

import org.craftercms.studio.api.v1.dal.PublishRequest;
import org.craftercms.studio.api.v1.exception.CommitNotFoundException;
import org.craftercms.studio.api.v1.exception.EnvironmentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.to.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 	// document
 */
public interface DeploymentService {

    // document
    void deploy(String site, String environment, List<String> paths, ZonedDateTime scheduledDate, String approver, String submissionComment, final boolean scheduleDateNow) throws DeploymentException;

    // document
    void delete(String site, List<String> paths, String approver, ZonedDateTime scheduledDate) throws DeploymentException;

    List<PublishRequest> getScheduledItems(String site);

    void cancelWorkflow(String site, String path) throws DeploymentException;

    void cancelWorkflowBulk(String site, Set<String> paths) throws DeploymentException;

    void deleteDeploymentDataForSite(String site);

    /**
     * get deployment history given a specified date range
     *
     * @param site
     * @param days
     * @param number
     * @param sort
     *            the sort key to sort items within each deployed date
     * @param ascending
     * @param filterType
     *
     * @return list of deployment items
     */

    public List<DmDeploymentTaskTO> getDeploymentHistory(
            String site, int days, int number, String sort, boolean ascending,
            String filterType);

    List<ContentItemTO> getScheduledItems(String site, String sort, boolean ascending, String subSort, boolean subAscending, String filterType) throws ServiceException;

    Map<String, List<PublishingChannelTO>> getAvailablePublishingChannelGroups(String site, String path);

    void syncAllContentToPreview(String site, boolean waitTillDone) throws ServiceException;

    void bulkGoLive(String site, String environment, String path) throws ServiceException;

    /**
     * Get last deployment date time for given site and path
     *
     * @param site site id
     * @param path path
     * @return last deployment date or null if never deployed
     */
    ZonedDateTime getLastDeploymentDate(String site, String path);

    /**
     * Get publish status for given site
     * @param site site id
     * @return publish status
     */
    PublishStatus getPublishStatus(String site) throws SiteNotFoundException;

    /**
     * Enable/Disable publishing for given site
     * @param site site id
     * @param enabled true to enable publishing, false to disable publishing
     * @throws SiteNotFoundException
     */
    boolean enablePublishing(String site, boolean enabled) throws SiteNotFoundException, AuthenticationException;

    /**
     * Publish given commit IDs on given environment for given site
     * @param site site id to use for publishing
     * @param environment environment to use for publishing
     * @param commitIds commit IDs to publish
     */
    void publishCommits(String site, String environment, List<String> commitIds, String comment)
            throws SiteNotFoundException, EnvironmentNotFoundException, CommitNotFoundException;

    /**
     * Publish items in given environment for given site
     * @param site site id to use for publishing
     * @param environment environment to use for publishing
     * @param paths item paths to publish
     * @throws SiteNotFoundException
     * @throws EnvironmentNotFoundException
     */
    void publishItems(String site, String environment, ZonedDateTime schedule, List<String> paths,
                      String submissionComment) throws ServiceException, DeploymentException;

    /**
     * Reset staging environment to live for given site
     *
     * @param siteId site id to use for resetting
     */
    void resetStagingEnvironment(String siteId) throws ServiceException;
}
