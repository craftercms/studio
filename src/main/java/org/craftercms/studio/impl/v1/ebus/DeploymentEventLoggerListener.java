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

package org.craftercms.studio.impl.v1.ebus;

import org.craftercms.studio.api.v1.ebus.*;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.event.EventService;

import java.lang.reflect.Method;
import java.util.List;

import static org.craftercms.studio.api.v1.ebus.EBusConstants.EVENT_DEPLOYMENT_ENGINE_DEPLOY;

public class DeploymentEventLoggerListener {

    private final static Logger logger = LoggerFactory.getLogger(DeploymentEventLoggerListener.class);

    private final static String METHOD_DEPLOYMENT_ENGINE_DEPLOY = "onDeploymentEvent";

    @EventListener(EVENT_DEPLOYMENT_ENGINE_DEPLOY)
    public void onDeploymentEvent(DeploymentEventMessage message) {
        String endpoint = message.getEndpoint();
        String site = message.getSite();
        List<DeploymentEventItem> items = message.getItems();
        StringBuilder sbItems = new StringBuilder();
        sbItems.append("[ ");
        for (DeploymentEventItem item : items) {
            sbItems.append("{ ")
                    .append("\"site\": ").append(item.getSite()).append(", ")
                    .append("\"path\": ").append(item.getPath()).append(", ")
                    .append("\"oldpath\": ").append(item.getOldPath()).append(", ")
                    .append("\"datetime\": ").append(item.getDateTime()).append(", ")
                    .append("\"state\": ").append(item.getState()).append(", ")
                    .append("\"user\": ").append(item.getUser())
                    .append(" } ");
        }
        sbItems.append("}");
        logger.info(String.format("Event: %s", EVENT_DEPLOYMENT_ENGINE_DEPLOY));
        logger.info(String.format("Site: %s", site));
        logger.info(String.format("Endpoint: %s", endpoint));
        logger.info(String.format("Items: %s", sbItems.toString()));
    }

    public void subscribeToDeploymentEngineDeployEvents() {
        try {
            Method subscribeMethod = DeploymentEventLoggerListener.class.getMethod(METHOD_DEPLOYMENT_ENGINE_DEPLOY, DeploymentEventMessage.class);
            this.eventService.subscribe(EBusConstants.EVENT_DEPLOYMENT_ENGINE_DEPLOY, beanName, subscribeMethod);
        } catch (NoSuchMethodException e) {
            logger.error("Could not subscribe to deloyment engine deploy events", e);
        }
    }

    protected EventService eventService;
    protected String beanName;

    public EventService getEventService() { return eventService; }
    public void setEventService(EventService eventService) { this.eventService = eventService; }

    public String getBeanName() { return beanName; }
    public void setBeanName(String beanName) { this.beanName = beanName; }
}
