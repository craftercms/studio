/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v1.deployment;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.ebus.DeploymentEventContext;
import org.craftercms.studio.api.v1.ebus.DeploymentItem;
import org.craftercms.studio.api.v1.ebus.EBusConstants;
import org.craftercms.studio.api.v1.ebus.EventListener;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.api.v1.ebus.EBusConstants.EVENT_PUBLISH_TO_ENVIRONMENT;

public class EnvironmentDeployer {

    private final static Logger logger = LoggerFactory.getLogger(EnvironmentDeployer.class);

    private final static String METHOD_PUBLISH_TO_ENVIRONMENT_LISTENER = "onEnvironmentDeploymentEvent";

    @EventListener(EVENT_PUBLISH_TO_ENVIRONMENT)
    public void onEnvironmentDeploymentEvent(DeploymentEventContext context) {
        List<DeploymentItem> items = context.getItems();
        List<DeploymentItemTO> deploymentItems = new ArrayList<DeploymentItemTO>();
        for (DeploymentItem item : items) {
            DeploymentItemTO deploymentItem = new DeploymentItemTO();
            deploymentItem.setSite(item.getSite());
            deploymentItem.setPath(item.getPath());
            deploymentItem.setCommitId(item.getCommitId());
            deploymentItem.setPackageId(item.getPackageId());
        }
        try {
            contentRepository.publish(context.getSite(), StringUtils.EMPTY, deploymentItems, context.getEnvironment(),
                    context.getAuthor(), context.getComment());
        } catch (DeploymentException e) {
            logger.error("Error when publishing site " + context.getSite() + " to environment " +
                    context.getEnvironment(), e);
        }
    }

    public void subscribeToPublishToEnvironmentEvents() {
        try {
            Method subscribeMethod = EnvironmentDeployer.class.getMethod(METHOD_PUBLISH_TO_ENVIRONMENT_LISTENER,
                    DeploymentEventContext.class);
            this.eventService.subscribe(EBusConstants.EVENT_PUBLISH_TO_ENVIRONMENT, beanName, subscribeMethod);
        } catch (NoSuchMethodException e) {
            logger.error("Could not subscribe to publish to environment events", e);
        }
    }

    public ContentRepository getContentRepository() { return contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this.contentRepository = contentRepository; }

    public EventService getEventService() { return eventService; }
    public void setEventService(EventService eventService) { this.eventService = eventService; }

    public String getBeanName() { return beanName; }
    public void setBeanName(String beanName) { this.beanName = beanName; }

    protected ContentRepository contentRepository;
    protected EventService eventService;
    protected String beanName;
}