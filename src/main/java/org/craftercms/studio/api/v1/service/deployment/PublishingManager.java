/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
 */
package org.craftercms.studio.api.v1.service.deployment;

import org.craftercms.studio.api.v1.dal.CopyToEnvironment;
import org.craftercms.studio.api.v1.ebus.DeploymentItem;
import org.craftercms.studio.api.v1.to.DeploymentEndpointConfigTO;

import java.util.List;
import java.util.Set;

/**
 *document
 */
public interface PublishingManager {

    List<CopyToEnvironment> getItemsReadyForDeployment(String site, String environment);

    DeploymentItem processItem(CopyToEnvironment item) throws DeploymentException;

    void markItemsCompleted(String site, String environment, List<CopyToEnvironment> processedItems) throws DeploymentException;

    void markItemsProcessing(String site, String environment, List<CopyToEnvironment> itemsToDeploy) throws DeploymentException;

    void markItemsReady(String site, String liveEnvironment, List<CopyToEnvironment> copyToEnvironmentItems) throws DeploymentException;

    void markItemsBlocked(String site, String environment, List<CopyToEnvironment> copyToEnvironmentItems) throws DeploymentException;

    List<DeploymentItem> processMandatoryDependencies(CopyToEnvironment item, List<String> pathsToDeploy, Set<String> missingDependenciesPaths) throws DeploymentException;

    boolean isPublishingBlocked(String site);

    String getPublishingStatus(String site);
}
