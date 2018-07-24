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
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;

import java.util.List;
import java.util.Set;

/**
 *document
 */
public interface PublishingManager {

    List<PublishRequest> getItemsReadyForDeployment(String site, String environment);

    DeploymentItemTO processItem(PublishRequest item) throws DeploymentException;

    void markItemsCompleted(String site, String environment, List<PublishRequest> processedItems) throws DeploymentException;

    void markItemsProcessing(String site, String environment, List<PublishRequest> itemsToDeploy) throws DeploymentException;

    void markItemsReady(String site, String liveEnvironment, List<PublishRequest> copyToEnvironmentItems) throws DeploymentException;

    void markItemsBlocked(String site, String environment, List<PublishRequest> copyToEnvironmentItems) throws DeploymentException;

    List<DeploymentItemTO> processMandatoryDependencies(PublishRequest item, Set<String> pathsToDeploy,
                                                        Set<String> missingDependenciesPaths)
            throws DeploymentException, ServiceException;

    boolean isPublishingBlocked(String site);

    String getPublishingStatus(String site);

    boolean isPublishingQueueEmpty(String site);
}
