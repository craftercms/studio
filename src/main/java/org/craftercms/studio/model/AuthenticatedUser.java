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
package org.craftercms.studio.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.craftercms.studio.api.v2.dal.User;

/**
 Represents a {@link User} that has been authenticated.
 *
 * @author avasquez
 */
public class AuthenticatedUser extends User {

    private static final long serialVersionUID = -4678834461080865934L;

    @JsonProperty("authenticationType")
    private AuthenticationType authenticationType;

    public AuthenticatedUser(User user) {
        setId(user.getId());
        setUsername(user.getUsername());
        setPassword(user.getPassword());
        setEmail(user.getEmail());
        setFirstName(user.getFirstName());
        setLastName(user.getLastName());
        setEnabled(user.isEnabled());
        setDeleted(user.isDeleted());
        setExternallyManaged(user.isExternallyManaged());
        setTimezone(user.getTimezone());
        setLocale(user.getLocale());
        setRecordLastUpdated(getRecordLastUpdated());
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

}
