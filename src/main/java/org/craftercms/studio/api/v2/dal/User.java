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
import org.springframework.security.core.userdetails.UserDetails;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class User implements UserDetails {

    private static final long serialVersionUID = 968000561389890945L;

    private long id = -1;
    private ZonedDateTime recordLastUpdated;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private boolean externallyManaged;
    private String timezone;
    private String locale;
    private String email;
    private boolean enabled;
    private boolean deleted;
    private List<UserGroup> groups = new ArrayList<>();

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonProperty("enabled")
    public boolean isEnabled() {
        return enabled;
    }

    @JsonProperty("enabled")
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonIgnore
    public boolean isDeleted() {
        return deleted;
    }

    @JsonIgnore
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @JsonIgnore
    public int getEnabledAsInt() {
        return enabled ? 1 : 0;
    }

    @JsonIgnore
    public void setEnabledAsInt(int enabled) {
        this.enabled = enabled > 0;
    }

    @JsonIgnore
    public int getDeletedAsInt() {
        return deleted ? 1 : 0;
    }

    @JsonIgnore
    public void setDeletedAsInt(int deleted) {
        this.deleted = deleted > 0;
    }


    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return groups.stream().map(UserGroup::getGroup).collect(Collectors.toList());
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

    @Override
    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    @JsonProperty("username")
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    @JsonProperty("firstName")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    @JsonProperty("lastName")
    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    @JsonIgnore
    public String getTimezone() {
        return timezone;
    }

    @JsonIgnore
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @JsonIgnore
    public String getLocale() {
        return locale;
    }

    @JsonIgnore
    public void setLocale(String locale) {
        this.locale = locale;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonIgnore
    public List<UserGroup> getGroups() {
        return groups;
    }

    @JsonIgnore
    public void setGroups(List<UserGroup> groups) {
        this.groups = groups;
    }

}
