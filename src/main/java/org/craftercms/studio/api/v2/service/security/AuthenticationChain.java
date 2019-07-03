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

package org.craftercms.studio.api.v2.service.security;

import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.GroupDAO;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Authentication Chain
 */
public interface AuthenticationChain {

    /**
     * Execute authentication for given user and password
     *
     * @param request HTTP Request
     * @param response HTTP Response
     * @param username Username to authenticate
     * @param password password
     * @return true if success, otherwise false
     * @throws Exception Exception in case of error during authentication
     */
    boolean doAuthenticate(HttpServletRequest request, HttpServletResponse response, String username, String password)
            throws Exception;

    /**
     * Expose User Service to authentication providers
     *
     * @return User Service Internal
     */
    UserServiceInternal getUserServiceInternal();

    /**
     * Expose Studio Configuration to authentication providers
     *
     * @return Studio Configuration
     */
    StudioConfiguration getStudioConfiguration();

    /**
     * Expose User DAO to authentication providers
     *
     * @return User DAO
     */
    UserDAO getUserDao();

    /**
     * Expose Group DAO to authentication providers
     *
     * @return Group DAO
     */
    GroupDAO getGroupDao();

    /**
     * Expose Audit Service Internal to authentication providers
     *
     * @return Audit Service Internal
     */
    AuditServiceInternal getAuditServiceInternal();

    /**
     * Expose Site Service to authentication providers
     *
     * @return Site Service
     */
    SiteService getSiteService();
}
