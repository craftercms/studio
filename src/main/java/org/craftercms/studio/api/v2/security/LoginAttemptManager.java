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

package org.craftercms.studio.api.v2.security;

/**
 * Keeps track of login success and failures per user. <br/>
 * The logic to lock/unlock users is up to the implementation.
 *
 * @since 4.1.2
 */
public interface LoginAttemptManager {

    /**
     * Indicates if a user is currently locked. <br/>
     * A locked user is not allowed to log in
     *
     * @param username the username
     * @return true if the user is locked, false otherwise
     */
    boolean isUserLocked(String username);

    /**
     * Notify this manager that a login attempt has succeeded for the given user.
     *
     * @param username the username
     */
    void loginSucceeded(String username);

    /**
     * Notify this manager that a login attempt has failed for the given user.
     *
     * @param username the username
     */
    void loginFailed(String username);

}
