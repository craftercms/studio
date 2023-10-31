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

import org.springframework.security.core.Authentication;

/**
 * Triggered where a site is deleted
 *
 * @author jmendeza
 * @since 4.0.3
 */
public class SiteDeletedEvent extends SiteLifecycleEvent {

    private final String siteUuid;

    public SiteDeletedEvent(final Authentication authentication, final String siteId, final String siteUuid) {
        super(authentication, siteId);
        this.siteUuid = siteUuid;
    }

    public SiteDeletedEvent(final String siteId, final String siteUuid) {
        super(siteId);
        this.siteUuid = siteUuid;
    }

    @Override
    public String getEventType() {
        return "SITE_DELETED_EVENT";
    }

    @Override
    public String toString() {
        return "SiteDeleteEvent{" +
                "siteId='" + siteId + '\'' +
                ", siteUuid='" + siteUuid + '\'' +
                ", timestamp=" + timestamp +
                ", user=" + user +
                '}';
    }
}
