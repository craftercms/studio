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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class Group implements Serializable, GrantedAuthority {

    private static final long serialVersionUID = 4723035066512137838L;

    private long id = -1;
    private ZonedDateTime recordLastUpdated;
    private Organization organization;
    private String groupName;
    private String groupDescription;

    @Override
    @JsonIgnore
    public String getAuthority() {
        return groupName;
    }

    @JsonProperty("id")
    public long getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(long id) {
        this.id = id;
    }

    @JsonIgnore
    public ZonedDateTime getRecordLastUpdated() {
        return recordLastUpdated;
    }

    @JsonIgnore
    public void setRecordLastUpdated(ZonedDateTime recordLastUpdated) {
        this.recordLastUpdated = recordLastUpdated;
    }

    @JsonIgnore
    public Organization getOrganization() {
        return organization;
    }

    @JsonIgnore
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @JsonProperty("name")
    public String getGroupName() {
        return groupName;
    }

    @JsonProperty("name")
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @JsonProperty("desc")
    public String getGroupDescription() {
        return groupDescription;
    }

    @JsonProperty("desc")
    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

}
