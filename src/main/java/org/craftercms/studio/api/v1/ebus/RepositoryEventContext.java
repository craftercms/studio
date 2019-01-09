/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
 */

package org.craftercms.studio.api.v1.ebus;

import java.io.Serializable;

public class RepositoryEventContext implements Serializable {

    private static ThreadLocal<RepositoryEventContext> threadLocal = new ThreadLocal<>();

    public static RepositoryEventContext getCurrent() {
        return threadLocal.get();
    }

    public static void setCurrent(RepositoryEventContext repositoryEventContext) {
        threadLocal.set(repositoryEventContext);
    }

    public static void clear() {
        threadLocal.remove();
    }

    public RepositoryEventContext(String authenticationToken, String currentUser) {
        this.authenticationToken = authenticationToken;
        this.currentUser = currentUser;
    }

    public String getAuthenticationToken() { return authenticationToken; }
    public void setAuthenticationToken(String authenticationToken) { this.authenticationToken = authenticationToken; }

    public String getCurrentUser() { return currentUser; }
    public void setCurrentUser(String currentUser) { this.currentUser = currentUser; }

    private String authenticationToken;
    private String currentUser;
}
