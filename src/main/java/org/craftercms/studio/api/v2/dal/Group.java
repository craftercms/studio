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

package org.craftercms.studio.api.v2.dal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.ZonedDateTime;

import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.GROUP_NAME;

public class Group implements Serializable, GrantedAuthority {

    private static final long serialVersionUID = 4723035066512137838L;

    private long id = -1;
    private ZonedDateTime recordLastUpdated;
    private Organization organization;
    private boolean externallyManaged;
    @NotNull
    @Size(max=512)
    @EsapiValidatedParam(type = GROUP_NAME)
    private String groupName;
    @Size(max=1024)
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

    @JsonProperty("externallyManaged")
    public boolean isExternallyManaged() {
        return externallyManaged;
    }

    @JsonProperty("externallyManaged")
    public void setExternallyManaged(boolean externallyManaged) {
        this.externallyManaged = externallyManaged;
    }

    @JsonIgnore
    public int getExternallyManagedAsInt() {
        return externallyManaged ? 1 : 0;
    }

    @JsonIgnore
    public void setExternallyManagedAsInt(int externallyManaged) {
        this.externallyManaged = externallyManaged > 0;
    }

}
