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

public class PublishingDashboardFilters {

    private String path;
    private String publisher;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime publishedDateFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime publishedDateTo;
    private String environment;
    private String contentType;
    private long state;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public ZonedDateTime getPublishedDateFrom() {
        return publishedDateFrom;
    }

    public void setPublishedDateFrom(ZonedDateTime publishedDateFrom) {
        this.publishedDateFrom = publishedDateFrom;
    }

    public ZonedDateTime getPublishedDateTo() {
        return publishedDateTo;
    }

    public void setPublishedDateTo(ZonedDateTime publishedDateTo) {
        this.publishedDateTo = publishedDateTo;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
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
