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
package org.craftercms.studio.api.v2.event.lock;

import org.craftercms.studio.api.v2.event.BroadcastEvent;
import org.craftercms.studio.api.v2.event.SiteAwareEvent;
import org.springframework.security.core.Authentication;

/**
 * Event triggered when content is locked or unlocked
 *
 * @implNote This event is not part of the content hierarchy to prevent triggering a reindex
 *
 * @author joseross
 * @since 4.0.0
 */
public class LockContentEvent extends SiteAwareEvent implements BroadcastEvent {

    protected final String targetPath;

    protected final boolean locked;

    public LockContentEvent(Authentication authentication, String siteId, String targetPath, boolean locked) {
        super(authentication, siteId);
        this.targetPath = targetPath;
        this.locked = locked;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    public String getEventType() {
        return "LOCK_CONTENT_EVENT";
    }

    @Override
    public String toString() {
        return "LockContentEvent{" +
                "siteId='" + siteId + '\'' +
                ", timestamp=" + timestamp +
                ", user=" + user +
                ", targetPath='" + targetPath + '\'' +
                ", locked=" + locked +
                '}';
    }

}
