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

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface SecurityService {

    /**
     * Get available actions for given user over content from site and specified path
     * @param username user to get allowed actions for
     * @param site site identifier
     * @param path path of the content/object
     * @return bitmap representing available actions
     */
    long getAvailableActions(String username, String site, String path)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * Get user permissions for given site
     * @param username user
     * @param groups groups that user belongs to
     * @return list of user permissions
     */
    List<String> getUserPermission(String siteId, String username, List<String> roles) throws ExecutionException;
}
