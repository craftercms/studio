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
import org.craftercms.studio.api.v2.event.SiteAwareEvent;
import org.craftercms.studio.api.v2.event.site.SiteLifecycleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.SimpMessagingTemplate;

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
    @EventListener(classes = BroadcastEvent.class, condition = "!(#event instanceof T(org.craftercms.studio.api.v2.event.site.SiteLifecycleEvent))")
    public void publishSiteEvent(final SiteAwareEvent event) {
        publishEvent(event, DESTINATION_ROOT + "/" + event.getSiteId());
    }

    @Order
    @EventListener
    public void publishGlobalEvent(final SiteLifecycleEvent event) {
        publishEvent(event, DESTINATION_ROOT);
    }

    private void publishEvent(final Object event, final String destination) {
        logger.debug("Broadcast event '{}'", event);
        long startTime = System.currentTimeMillis();
        messagingTemplate.convertAndSend(destination, event);
        if (logger.isTraceEnabled()) {
            long total = System.currentTimeMillis() - startTime;
            logger.trace("Broadcast of event '{}' took '{}' milliseconds", event, total);
        }
    }

}
