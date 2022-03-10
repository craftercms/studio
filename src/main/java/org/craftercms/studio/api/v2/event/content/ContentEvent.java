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
package org.craftercms.studio.api.v2.event.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.craftercms.studio.api.v2.event.BroadcastEvent;
import org.craftercms.studio.api.v2.event.SiteAwareEvent;
import org.springframework.security.core.Authentication;

/**
 * Event triggered when there is a change in one or more content items
 *
 * @author joseross
 * @since 4.0.0
 */
public class ContentEvent extends SiteAwareEvent implements BroadcastEvent {

    protected final String targetPath;

    @JsonIgnore
    protected final boolean waitForCompletion;

    public ContentEvent(Authentication authentication, String siteId, String targetPath, boolean waitForCompletion) {
        super(authentication, siteId);
        this.targetPath = targetPath;
        this.waitForCompletion = waitForCompletion;
    }

    public ContentEvent(Authentication authentication, String siteId, String targetPath) {
        this(authentication, siteId, targetPath, false);
    }

    public String getTargetPath() {
        return targetPath;
    }

    public boolean isWaitForCompletion() {
        return waitForCompletion;
    }

    @Override
    public String getEventType() {
        return "CONTENT_EVENT";
    }

    @Override
    public String toString() {
        return "ContentEvent{" +
                "siteId='" + siteId + '\'' +
                ", timestamp=" + timestamp +
                ", user=" + user +
                ", targetPath='" + targetPath + '\'' +
                ", waitForCompletion=" + waitForCompletion +
                '}';
    }

}
