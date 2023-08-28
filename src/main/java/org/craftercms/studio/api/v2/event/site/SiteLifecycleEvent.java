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

import org.craftercms.studio.api.v2.event.BroadcastEvent;
import org.craftercms.studio.api.v2.event.SiteAwareEvent;
import org.craftercms.studio.model.rest.Person;
import org.springframework.security.core.Authentication;

/**
 * Base class for site lifecycle events
 *
 * @author jmendeza
 * @since 4.1.2
 */
public abstract class SiteLifecycleEvent extends SiteAwareEvent implements BroadcastEvent {

    public SiteLifecycleEvent(Authentication authentication, String siteId) {
        super(Person.from(authentication), siteId);
    }

    public SiteLifecycleEvent(final String siteId) {
        super(siteId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "siteId='" + siteId + '\'' +
                ", user='" + user + '\'' +
                ", timestamp=" + timestamp +
                ", eventType='" + getEventType() + '\'' +
                '}';
    }
}
