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

package org.craftercms.studio.api.v2.service.security;

import org.craftercms.studio.api.v1.exception.security.AuthenticationSystemException;
import org.craftercms.studio.api.v1.exception.security.BadCredentialsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Authentication Provider
 */
public interface AuthenticationProvider {

    /**
     * Execute authentication for given username and password
     *
     * @param request HTTP Request
     * @param response HTTP Response
     * @param authenticationChain Authentication chain
     * @param username username to authenticate
     * @param password password
     * @return true if success, otherwise false
     * @throws AuthenticationSystemException general authentication system error
     * @throws BadCredentialsException given credentials are bad
     * @throws UserNotFoundException username not found in the list of active users
     */
    boolean doAuthenticate(HttpServletRequest request,
                           HttpServletResponse response,
                           AuthenticationChain authenticationChain,
                           String username,
                           String password) throws AuthenticationSystemException, BadCredentialsException, UserNotFoundException;

    /**
     * Check if authentication provider is enabled
     *
     * @return true if authentication provider is enabled, otherwise false
     */
    boolean isEnabled();

    /**
     * Enable or disable authentication provider
     *
     * @param enabled true to enable, false to disable
     */
    void setEnabled(boolean enabled);
}
