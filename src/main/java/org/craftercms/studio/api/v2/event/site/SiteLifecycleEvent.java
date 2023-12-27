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

package org.craftercms.studio.api.v2.event.site;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.craftercms.studio.api.v2.event.GlobalBroadcastEvent;
import org.craftercms.studio.api.v2.event.SiteAwareEvent;
import org.craftercms.studio.model.rest.Person;

/**
 * Base class for site lifecycle events
 *
 * @author jmendeza
 * @since 4.1.2
 */
public abstract class SiteLifecycleEvent extends SiteAwareEvent implements GlobalBroadcastEvent {

    private final String siteUuid;

    public SiteLifecycleEvent(String siteId, final String siteUuid) {
        super(siteId);
        this.siteUuid = siteUuid;
    }

    @JsonIgnore
    public String getSiteId() {
        return siteId;
    }

    @Override
    @JsonIgnore
    public Person getUser() {
        return super.getUser();
    }

    public String getSiteUuid() {
        return siteUuid;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "siteId='" + siteId + '\'' +
                ", siteUuid='" + siteUuid + '\'' +
                ", user='" + user + '\'' +
                ", timestamp=" + timestamp +
                ", eventType='" + getEventType() + '\'' +
                '}';
    }
}
