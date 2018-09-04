/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.craftercms.studio.api.v2.dal;

import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class GroupTO implements Serializable, GrantedAuthority {

    private static final long serialVersionUID = 4723035066512137838L;

    private long id;
    private ZonedDateTime recordLastUpdated;
    private OrganizationTO organization;
    private String groupName;
    private String groupDescription;

    @Override
    public String getAuthority() {
        return groupName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ZonedDateTime getRecordLastUpdated() {
        return recordLastUpdated;
    }

    public void setRecordLastUpdated(ZonedDateTime recordLastUpdated) {
        this.recordLastUpdated = recordLastUpdated;
    }

    public OrganizationTO getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationTO organizationTO) {
        this.organization = organization;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }
}
