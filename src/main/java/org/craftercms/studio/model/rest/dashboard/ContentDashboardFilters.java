/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.model.rest.dashboard;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.ZonedDateTime;

public class ContentDashboardFilters {

    private String path;
    private String modifier;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime modifiedDateFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime modifiedDateTo;
    private String contentType;
    private long state;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public ZonedDateTime getModifiedDateFrom() {
        return modifiedDateFrom;
    }

    public void setModifiedDateFrom(ZonedDateTime modifiedDateFrom) {
        this.modifiedDateFrom = modifiedDateFrom;
    }

    public ZonedDateTime getModifiedDateTo() {
        return modifiedDateTo;
    }

    public void setModifiedDateTo(ZonedDateTime modifiedDateTo) {
        this.modifiedDateTo = modifiedDateTo;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getState() {
        return state;
    }

    public void setState(long state) {
        this.state = state;
    }
}
