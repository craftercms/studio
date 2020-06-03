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

package org.craftercms.studio.api.v1.ebus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class DeploymentEventItem implements Serializable {

    public static final String STATE_NEW = "NEW";
    public static final String STATE_UPDATED = "UPDATED";
    public static final String STATE_DELETED = "DELETED";
    public static final String STATE_MOVED = "MOVED";

    private static final long serialVersionUID = 9079411417723890371L;

    @JsonCreator
    public DeploymentEventItem(@JsonProperty String site, @JsonProperty String path, @JsonProperty String oldPath,
                               @JsonProperty String user, @JsonProperty ZonedDateTime dateTime, @JsonProperty String state) {
        this.site = site;
        this.path = path;
        this.oldPath = oldPath;
        this.user = user;
        this.dateTime = dateTime;
        this.state = state;
    }

    protected String site;
    protected String path;
    protected String oldPath;
    protected String user;
    protected ZonedDateTime dateTime;
    protected String state;

    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getOldPath() { return oldPath; }
    public void setOldPath(String oldPath) { this.oldPath = oldPath; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public ZonedDateTime getDateTime() { return dateTime; }
    public void setDateTime(ZonedDateTime dateTime) { this.dateTime = dateTime; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}
