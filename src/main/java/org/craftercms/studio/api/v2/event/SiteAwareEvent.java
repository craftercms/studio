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
package org.craftercms.studio.api.v2.event;

import org.craftercms.studio.model.rest.Person;
import org.springframework.security.core.Authentication;

/**
 * Base class for all events related to a specific site
 *
 * @author joseross
 * @since 4.0.0
 */
public abstract class SiteAwareEvent extends StudioEvent {

    protected final String siteId;

    public SiteAwareEvent(Authentication authentication, String siteId) {
        super(authentication);
        this.siteId = siteId;
    }

    public SiteAwareEvent(Person person, String siteId) {
        super(person);
        this.siteId = siteId;
    }

    public SiteAwareEvent(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteId() {
        return siteId;
    }

}
