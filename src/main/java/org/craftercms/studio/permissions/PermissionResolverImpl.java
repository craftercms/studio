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
package org.craftercms.studio.permissions;

import org.craftercms.commons.security.exception.PermissionException;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.Permission;
import org.craftercms.commons.security.permissions.PermissionResolver;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.springframework.beans.factory.annotation.Required;

import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link PermissionResolver} that resolves user permissions based on Studio's
 * {@link SecurityService}.
 *
 * @author avasquez
 */
public class PermissionResolverImpl implements PermissionResolver<String, Map<String, Object>> {

    private SecurityService securityService;

    @Required
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    public Permission getGlobalPermission(String username) throws PermissionException {
       Set<String> allowedActions = securityService.getUserPermissions("", "/", username, null);

       DefaultPermission permission = new DefaultPermission();
       permission.setAllowedActions(allowedActions);

       return permission;
    }

    @Override
    public Permission getPermission(String username, Map<String, Object> securedParams) throws PermissionException {
        throw new UnsupportedOperationException("Not supported yet");
    }

}
