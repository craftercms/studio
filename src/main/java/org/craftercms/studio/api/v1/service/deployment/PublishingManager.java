/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.api.v1.service.deployment;

import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.dal.PublishRequest;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 *document
 */
public interface PublishingManager {

    List<PublishRequest> getItemsReadyForDeployment(String site, String environment);

    DeploymentItemTO processItem(PublishRequest item) throws DeploymentException, ServiceLayerException, UserNotFoundException;

    void markItemsCompleted(String site, String environment, List<PublishRequest> processedItems)
        throws DeploymentException;

    void markItemsProcessing(String site, String environment, List<PublishRequest> itemsToDeploy)
        throws DeploymentException;

    void markItemsReady(String site, String liveEnvironment, List<PublishRequest> copyToEnvironmentItems)
        throws DeploymentException;

    boolean isPublishingBlocked(String site);

    String getPublishingStatus(String site);

    boolean isPublishingQueueEmpty(String site);

    /**
     * Reset items being in processing state (skip publishing cycle or recover from error)
     * @param site site to use
     * @param environment environment to use
     *
     */
    void resetProcessingQueue(String site, String environment);

    /**
     * Updates item states to publish state according to the publishing
     * environment (stage vs live)
     * @param siteId the site id
     * @param environment the environment where the items were published
     * @param items the published items
     */
    void setPublishedState(String siteId, String environment, List<PublishRequest> items);
}
