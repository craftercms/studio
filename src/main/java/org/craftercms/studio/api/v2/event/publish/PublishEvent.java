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
package org.craftercms.studio.api.v2.event.publish;

import org.craftercms.studio.api.v2.event.BroadcastEvent;
import org.craftercms.studio.api.v2.event.SiteAwareEvent;

/**
 * Event triggered when items are published
 *
 * @implNote For now this only triggered when items are processed in the publishing queue
 *
 * @author joseross
 * @since 4.0.0
 */
public class PublishEvent extends SiteAwareEvent implements BroadcastEvent {

    public PublishEvent(String siteId) {
        super(siteId);
    }

    @Override
    public String getEventType() {
        return "PUBLISH_EVENT";
    }

    @Override
    public String toString() {
        return "PublishEvent{" +
                "siteId='" + siteId + '\'' +
                ", timestamp=" + timestamp +
                ", user=" + user +
                '}';
    }

}
