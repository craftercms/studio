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
package org.craftercms.cstudio.impl.service.deployment;

import org.craftercms.cstudio.api.service.deployment.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 *document
 */
public interface PublishingManager {

    // document methods
    Set<String> getAllAvailableSites();

    Set<PublishingTargetItem> getAllTargetsForSite(String site);

    boolean checkConnection(PublishingTargetItem target);

    long getTargetVersion(PublishingTargetItem target, String site);

    List<PublishingSyncItem> getItemsToSync(String site, long targetVersion, List<String> environments);

    void deployItemsToTarget(String site, List<PublishingSyncItem> filteredItems, PublishingTargetItem target) throws ContentNotFoundForPublishingException, UploadFailedException;

    long setTargetVersion(PublishingTargetItem target, long newVersion, String site);

    List<CopyToEnvironmentItem> getItemsReadyForDeployment(String site, String environment);

    void processItem(CopyToEnvironmentItem item) throws DeploymentException;

    void setupItemsForPublishingSync(String site, String environment, List<CopyToEnvironmentItem> itemsToDeploy) throws DeploymentException;

    void insertDeploymentHistory(PublishingTargetItem target, List<PublishingSyncItem> filteredItems, Date date) throws DeploymentException;

    void markItemsCompleted(String site, String environment, List<CopyToEnvironmentItem> processedItems) throws DeploymentException;

    void markItemsProcessing(String site, String environment, List<CopyToEnvironmentItem> itemsToDeploy) throws DeploymentException;

    void markItemsReady(String site, String liveEnvironment, List<CopyToEnvironmentItem> copyToEnvironmentItems) throws DeploymentException;

    void setLockBehaviourEnabled(boolean enabled);

    List<CopyToEnvironmentItem> processMandatoryDependencies(CopyToEnvironmentItem item, List<String> pathsToDeploy, Set<String> missingDependenciesPaths) throws DeploymentException;
}