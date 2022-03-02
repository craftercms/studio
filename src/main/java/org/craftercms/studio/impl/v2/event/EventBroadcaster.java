/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
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

    @EventListener
    public void publishEvent(BroadcastEvent event) {
        logger.debug("Broadcasting event {}", event);
        long startTime = System.currentTimeMillis();
        String destination = DESTINATION_ROOT;
        if (event instanceof SiteAwareEvent) {
            destination += "/" + ((SiteAwareEvent) event).getSiteId();
        }
        messagingTemplate.convertAndSend(destination, event);
        if (logger.isTraceEnabled()) {
            long total = System.currentTimeMillis() - startTime;
            logger.trace("Broadcast of event {} took {} ms", event, total);
        }
    }

}
