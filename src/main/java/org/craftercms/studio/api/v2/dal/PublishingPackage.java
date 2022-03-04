
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

package org.craftercms.studio.api.v2.dal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

public class PublishingPackage {

    private String packageId;
    private String siteId;
    private String environment;
    private String state;
    private ZonedDateTime scheduledDate;
    private String user;
    private String comment;

    @JsonProperty("id")
    public String getPackageId() {
        return packageId;
    }

    @JsonProperty("id")
    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("schedule")
    public ZonedDateTime getScheduledDate() {
        return scheduledDate;
    }

    @JsonProperty("schedule")
    public void setScheduledDate(ZonedDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    @JsonProperty("approver")
    public String getUser() {
        return user;
    }

    @JsonProperty("approver")
    public void setUser(String user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
