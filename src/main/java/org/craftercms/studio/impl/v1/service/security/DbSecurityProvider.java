/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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

package org.craftercms.studio.impl.v1.service.security;

import org.craftercms.studio.api.v1.dal.SecurityMapper;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Set;

public class DbSecurityProvider implements SecurityProvider {

    @Override
    public Set<String> getUserGroups(String user) {
        return null;
    }

    @Override
    public String getCurrentUser() {
        return null;
    }

    @Override
    public Map<String, String> getUserProfile(String user) {
        return null;
    }

    @Override
    public String authenticate(String username, String password) {
        return null;
    }

    @Override
    public boolean validateTicket(String ticket) {
        return false;
    }

    @Override
    public void addUserGroup(String groupName) {

    }

    @Override
    public void addUserGroup(String parentGroup, String groupName) {

    }

    @Override
    public String getCurrentToken() {
        return null;
    }

    @Override
    public void addUserToGroup(String groupName, String user) {

    }

    @Override
    public boolean logout() {
        return false;
    }

    @Override
    public void addContentWritePermission(String path, String group) {

    }

    @Override
    public void addConfigWritePermission(String path, String group) {

    }

    @Autowired
    protected SecurityMapper securityMapper;
}
