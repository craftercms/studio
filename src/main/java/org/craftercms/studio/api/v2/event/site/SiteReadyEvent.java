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
 * Event triggered when there is a change in a site
 *
 * <p><b>Note:</b>For now this is only triggered when a site creation is complete</p>
 *
 * @author joseross
 * @since 4.0.0
 */
public class SiteReadyEvent extends SiteLifecycleEvent {

    public SiteReadyEvent(final Authentication authentication, final String siteId) {
        super(authentication, siteId);
    }

    public SiteReadyEvent(final String siteId) {
        super(siteId);
    }

    @Override
    public String getEventType() {
        return "SITE_READY_EVENT";
    }
}
