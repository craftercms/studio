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

import org.craftercms.studio.api.v2.event.SiteAwareEvent;

/**
 * Event fired to request a repository sync.
 *
 * @since 4.1.2
 */
public class RepoSyncEvent extends SiteAwareEvent {
    public RepoSyncEvent(final String siteId) {
        super(siteId);
    }
}
