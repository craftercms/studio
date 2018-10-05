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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class User implements UserDetails {

    private static final long serialVersionUID = 968000561389890945L;

    @JsonProperty("id")
    private long id;
    @JsonIgnore
    private ZonedDateTime recordLastUpdated;
    @JsonProperty("username")
    private String username;
    @JsonProperty("password")
    private String password;
    @JsonProperty("firstName")
    private String firstName;
    @JsonProperty("lastName")
    private String lastName;
    @JsonIgnore
    private int externallyManaged;
    @JsonIgnore
    private String timezone;
    @JsonIgnore
    private String locale;
    @JsonProperty("email")
    private String email;
    @JsonIgnore
    private int active;

    @JsonProperty("externallyManaged")
    private boolean extManaged;
    @JsonProperty("enabled")
    private boolean enabled;

    private List<UserGroup> groups = new ArrayList<UserGroup>();

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active != 0;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.active = enabled ? 1 : 0;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<Group> toRet = new ArrayList<Group>();
        groups.forEach((g) -> toRet.add(g.getGroup()));
        return toRet;
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

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getExternallyManaged() {
        return externallyManaged;
    }

    public void setExternallyManaged(int externallyManaged) {
        this.externallyManaged = externallyManaged;
        this.extManaged = externallyManaged != 0;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
        this.enabled = active != 0;
    }

    public List<UserGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<UserGroup> groups) {
        this.groups = groups;
    }
}
