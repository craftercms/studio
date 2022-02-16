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
package org.craftercms.studio.controller.rest.v2;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.exceptions.InvalidManagementTokenException;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import java.util.Objects;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_MANAGEMENT_AUTHORIZATION_TOKEN;

/**
 * Base class for all controllers that use the management token to protect public urls
 *
 * @author joseross
 * @since 4.0
 */
public abstract class ManagementTokenAware {

    protected final StudioConfiguration studioConfiguration;
    protected final SecurityService securityService;

    public ManagementTokenAware(StudioConfiguration studioConfiguration, SecurityService securityService) {
        this.studioConfiguration = studioConfiguration;
        this.securityService = securityService;
    }

    protected void validateToken(String token) throws InvalidManagementTokenException, InvalidParametersException {
        if (StringUtils.isEmpty(securityService.getCurrentUser())) {
            if (Objects.isNull(token)) {
                throw new InvalidParametersException("Missing parameter: 'token'");
            } else if(!StringUtils.equals(token, getConfiguredToken())) {
                throw new InvalidManagementTokenException("Management authorization failed, invalid token.");
            }
        }
    }

    protected String getConfiguredToken() {
        return studioConfiguration.getProperty(CONFIGURATION_MANAGEMENT_AUTHORIZATION_TOKEN);
    }

}
