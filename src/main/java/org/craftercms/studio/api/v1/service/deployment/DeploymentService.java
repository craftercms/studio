/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2016 Crafter Software Corporation.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.studio.api.v1.service.deployment;

import org.craftercms.studio.api.v1.dal.CopyToEnvironment;
import org.craftercms.studio.api.v1.dal.PublishToTarget;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.to.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 	// document
 */
public interface DeploymentService {

    // document
    void deploy(String site, String environment, List<String> paths, Date scheduledDate, String approver, String submissionComment, final boolean scheduleDateNow) throws DeploymentException;

    // document
    void delete(String site, List<String> paths, String approver, Date scheduledDate) throws DeploymentException;

    List<CopyToEnvironment> getScheduledItems(String site);

    void cancelWorkflow(String site, String path) throws DeploymentException;

    void cancelWorkflowBulk(String site, Set<String> paths) throws DeploymentException;

    void deleteDeploymentDataForSite(String site);

    /**
     * get deployment history given a specified date range
     *
     * @param daysFromToday
     * @param numberOfItems
     * @param site
     * @param sort
     *            the sort key to sort items within each deployed date
     * @param ascending
     *
     * @return list of deployment items
     */

    public List<DmDeploymentTaskTO> getDeploymentHistory(
            String site, int days, int number, String sort, boolean ascending,
            String filterType);

    List<ContentItemTO> getScheduledItems(String site, String sort, boolean ascending, String subSort, boolean subAscending, String filterType) throws ServiceException;

    void setupItemsForPublishingSync(String site, String environment, List<CopyToEnvironment> itemsToDeploy) throws DeploymentException;

    List<PublishToTarget> getItemsToSync(String site, long targetVersion, List<String> environments);

    void insertDeploymentHistory(DeploymentEndpointConfigTO target, List<PublishToTarget> publishedItems, Date publishingDate);

    void syncAllContentToPreview(String site) throws ServiceException;

    List<CopyToEnvironment> getDeploymentQueue(String site) throws ServiceException;

    List<PublishToTarget> getSyncTargetQueue(String site, String endpoint, long targetVersion);

    List<DeploymentEndpointConfigTO> getDeploymentEndpoints(String site);

    boolean cancelDeployment(String site, String path, long deploymentId) throws ServiceException;

    void bulkGoLive(String site, String environment, String path) throws ServiceException;

    void bulkDelete(String site, String path);

    List<DeploymentJobTO> getDeploymentJobs();

    Map<String, List<PublishingChannelTO>> getAvailablePublishingChannelGroups(String site, String path);

    /**
     * Publish items in given environment for given site
     * @param site site id to use for publishing
     * @param environment environment to use for publishing
     * @param paths item paths to publish
     */
    void publishItems(String site, String environment, Date schedule, List<String> paths,
                      String submissionComment) throws ServiceException, DeploymentException;
}
