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
package org.craftercms.studio.api.v1.service.deployment;

import org.craftercms.studio.api.domain.DeploymentSyncHistory;

import java.util.Date;
import java.util.List;

/**
 * 	// document
 */
public interface DeploymentService {

    // document
    void deploy(String site, String environment, List<String> paths, Date scheduledDate, String approver, String submissionComment) throws DeploymentException;

    // document
    void delete(String site, List<String> paths, String approver, Date scheduledDate) throws DeploymentException;

    List<DeploymentSyncHistory> getDeploymentHistory(String site, Date fromDate, Date toDate, String filterType, int numberOfItems);

    List<CopyToEnvironmentItem> getScheduledItems(String site);

    void cancelWorkflow(String site, String path) throws DeploymentException;

    void deleteDeploymentDataForSite(String site);
}
