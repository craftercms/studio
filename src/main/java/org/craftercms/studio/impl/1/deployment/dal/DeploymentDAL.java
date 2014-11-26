/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.cstudio.impl.service.deployment.dal;


import org.craftercms.studio.api.domain.DeploymentSyncHistory;
import org.craftercms.studio.api.v1.service.deployment.CopyToEnvironmentItem;
import org.craftercms.studio.api.v1.service.deployment.PublishingSyncItem;
import org.craftercms.studio.api.v1.service.deployment.PublishingTargetItem;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 */
public interface DeploymentDAL {

    List<CopyToEnvironmentItem> getItemsReadyForDeployment(String site, String environment);

    void setupItemsToDeploy(String site, String environment, Map<CopyToEnvironmentItem.Action, List<String>> paths, Date scheduledDate, String approver, String submissionComment) throws DeploymentDALException;

    List<PublishingSyncItem> getItemsReadyForTargetSync(String site, long version, List<String> environments);

    void setupItemsToDelete(String site, String environment, List<String> paths, String approver, Date scheduledDate) throws DeploymentDALException;

    void setupItemsForPublishingSync(String site, String environment, List<CopyToEnvironmentItem> itemsToDeploy) throws DeploymentDALException;

    void insertDeploymentHistory(PublishingTargetItem target, List<PublishingSyncItem> filteredItems, Date publishingDate) throws DeploymentDALException;

    List<DeploymentSyncHistory> getDeploymentHistory(String site, Date fromDate, Date toDate, String filterType, int numberOfItems);

    List<CopyToEnvironmentItem> getScheduledItems(String site);

    void cancelWorkflow(String site, String path) throws DeploymentDALException;

    void markItemsCompleted(String site, String environment, List<CopyToEnvironmentItem> processedItems) throws DeploymentDALException;

    void markItemsProcessing(String site, String environment, List<CopyToEnvironmentItem> itemsToDeploy) throws DeploymentDALException;

    void markItemsReady(String site, String environment, List<CopyToEnvironmentItem> copyToEnvironmentItems) throws DeploymentDALException;

    void deleteDeploymentDataForSite(String site) throws DeploymentDALException;
}
