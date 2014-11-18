/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.cstudio.alfresco.deployment;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.craftercms.cstudio.alfresco.event.EventListener;
import org.craftercms.cstudio.alfresco.event.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class DeploymentEventsLoggerListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentEventsLoggerListener.class);

    protected EventService eventService;
    protected String beanName;

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void subscribeToDeploymentEvents() {
        try {
            Method subscribeMethod = DeploymentEventsLoggerListener.class.getMethod("logDeployEvent", Object.class);
            this.eventService.subscribe(DeploymentEngineConstants.EVENT_DEPLOYMENT_ENGINE_DEPLOY, beanName, subscribeMethod);
        } catch (NoSuchMethodException e) {
            LOGGER.error("Could not subscribe to deployment event", e);
        }
    }

    @EventListener(DeploymentEngineConstants.EVENT_DEPLOYMENT_ENGINE_DEPLOY)
    public void logDeployEvent(Object json) {
        JSONObject jsonObject = JSONObject.fromObject(json);
        String endpoint = jsonObject.getString("endpoint");
        JSONArray jsonItems = jsonObject.getJSONArray("items");
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(String.format("Event: %s", DeploymentEngineConstants.EVENT_DEPLOYMENT_ENGINE_DEPLOY));
            LOGGER.info(String.format("Endpoint: %s", endpoint));
            LOGGER.info(String.format("Items: %s", jsonItems.toString()));
        }
    }
}
