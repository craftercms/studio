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
package org.craftercms.studio.permissions;

import org.apache.commons.collections4.MapUtils;
import org.craftercms.commons.security.exception.PermissionException;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.Permission;
import org.craftercms.commons.security.permissions.PermissionResolver;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link PermissionResolver} that resolves user permissions based on Studio's
 * {@link SecurityService}.
 *
 * @author avasquez
 */
public class PermissionResolverImpl implements PermissionResolver<String, Map<String, Object>> {

    public static final String SITE_ID_RESOURCE_ID = "siteId";
    public static final String PATH_RESOURCE_ID = "path";

    private SecurityService securityService;

    @Required
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    public Permission getGlobalPermission(String username) throws PermissionException {
       return getPermission(username, Collections.emptyMap());
    }

    @Override
    public Permission getPermission(String username, Map<String, Object> resourceIds) throws PermissionException {
        String siteName = "";
        String path = "/";

        if (MapUtils.isNotEmpty(resourceIds)) {
            if (resourceIds.containsKey(SITE_ID_RESOURCE_ID)) {
                siteName = (String) resourceIds.get(SITE_ID_RESOURCE_ID);
            }
            if (resourceIds.containsKey(PATH_RESOURCE_ID)) {
                path = (String) resourceIds.get(PATH_RESOURCE_ID);
            }
        }

        Set<String> allowedActions = securityService.getUserPermissions(siteName, path, username, null);

        DefaultPermission permission = new DefaultPermission();
        permission.setAllowedActions(allowedActions);

        return permission;
    }

}
