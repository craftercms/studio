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

package org.craftercms.studio.api.v2.service.security;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface SecurityService {

    /**
     * Get user permissions for given site
     * @param siteId crafter site Id
     * @param username user
     * @param roles roles the user is assigned to
     * @return list of user permissions
     */
    List<String> getUserPermission(String siteId, String username, List<String> roles) throws ExecutionException;

    /**
     * Returns the username of the current user
     * @return username of the current user, or null if no user is authenticated
     */
    String getCurrentUser();

    /**
     * Returns the {@link Authentication} for the current user or null if not user is authenticated.
     *
     * @return authentication
     */
    Authentication getAuthentication();

    /**
     * Get list groups for a site
     * @param siteId site identifier
     * @return list of groups
     * @throws ServiceLayerException
     */
    List<String> getSiteGroups(String siteId) throws ServiceLayerException;

    /**
     * Check if a user is a member of a site
     * User is a member of a site if they are member of any site group. A site group is any group mapped in the site's
     * role mapping configuration file.
     *
     * @param username the username
     * @param siteName the site name
     * @return true if user is a member of the site, false otherwise
     */
    boolean isSiteMember(String username, String siteName);

    /**
     * Check if given user has system_admin role
     *
     * @param username user
     * @return true if user is system_admin, false otherwise
     */
    boolean isSystemAdmin(String username);
}
