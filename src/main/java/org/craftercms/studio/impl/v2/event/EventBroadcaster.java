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
package org.craftercms.studio.impl.v2.event;

import org.craftercms.studio.api.v2.event.BroadcastEvent;
import org.craftercms.studio.api.v2.event.GlobalBroadcastEvent;
import org.craftercms.studio.api.v2.event.SiteBroadcastEvent;
import org.craftercms.studio.impl.v2.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Arrays;

import static java.lang.String.format;

/**
 * Implementation of {@link EventListener} that broadcasts events to the message broker
 *
 * @author joseross
 * @since 4.0.0
 */
public class EventBroadcaster {

    public static final String DESTINATION_ROOT = "/topic/studio";

    private static final Logger logger = LoggerFactory.getLogger(EventBroadcaster.class);

    @Autowired
    protected SimpMessagingTemplate messagingTemplate;

    @Order
    @EventListener
    public void publishSiteEvent(final SiteBroadcastEvent event) {
        publishEvent(event, DESTINATION_ROOT + "/" + event.getSiteId());
    }

    @Order
    @EventListener
    public void publishGlobalEvent(final GlobalBroadcastEvent event) {
        publishEvent(event, DESTINATION_ROOT);
    }

    private void publishEvent(final BroadcastEvent event, final String destination) {
        TimeUtils.logExecutionTime(() -> {
            logger.debug("Broadcast event '{}'", event);
            messagingTemplate.convertAndSend(destination, event);
            return null;
        }, logger, format("Method 'EventBroadcaster.publishEvent(..)' with parameters %s", Arrays.asList(event, destination)));
    }

}
