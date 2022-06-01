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

import org.springframework.security.core.Authentication;

/**
 * Event triggered when there is a content move change
 *
 * @author Phil Nguyen
 * @since 4.0.1
 */

public class MoveContentEvent extends ContentEvent {

    protected final String sourcePath;

    public MoveContentEvent(Authentication authentication, String siteId, String sourcePath, String targetPath, boolean waitForCompletion) {
        super(authentication, siteId, targetPath, waitForCompletion);
        this.sourcePath = sourcePath;
    }

    public MoveContentEvent(Authentication authentication, String siteId, String sourcePath, String targetPath) {
        this(authentication, siteId, sourcePath, targetPath, false);
    }

    public String getSourcePath() {
        return sourcePath;
    }

    @Override
    public String getEventType() {
        return "MOVE_CONTENT_EVENT";
    }

    @Override
    public String toString() {
        return "MoveContentEvent{" +
                "siteId='" + siteId + '\'' +
                ", timestamp=" + timestamp +
                ", user=" + user +
                ", sourcePath='" + sourcePath + '\'' +
                ", targetPath='" + targetPath + '\'' +
                ", waitForCompletion=" + waitForCompletion +
                '}';
    }

}