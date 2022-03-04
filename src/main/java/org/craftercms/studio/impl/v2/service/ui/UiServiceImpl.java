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

package org.craftercms.studio.impl.v2.service.ui;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.service.ui.UiService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.service.ui.internal.UiServiceInternal;
import org.craftercms.studio.model.ui.MenuItem;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.Set;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;

/**
 * Default implementation of {@link UiService}. Delegates to the {@link UiServiceInternal} for the actual work.
 *
 * @author avasquez
 */
public class UiServiceImpl implements UiService {

    private SecurityService securityService;
    private UiServiceInternal uiServiceInternal;
    private StudioConfiguration studioConfiguration;

    @Required
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Required
    public void setUiServiceInternal(UiServiceInternal uiServiceInternal) {
        this.uiServiceInternal = uiServiceInternal;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MenuItem> getGlobalMenu() throws AuthenticationException, ServiceLayerException {
        String user = securityService.getCurrentUser();
        if (StringUtils.isNotEmpty(user)) {
            Set<String> permissions = securityService.getUserPermissions("", "/", user, null);

            return uiServiceInternal.getGlobalMenu(permissions);
        } else {
            throw new AuthenticationException("User is not authenticated");
        }
    }

    @Override
    public String getActiveEnvironment() throws AuthenticationException {
        String user = securityService.getCurrentUser();
        if (StringUtils.isNotEmpty(user)) {
            return studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE);
        } else {
            throw new AuthenticationException("User is not authenticated");
        }
    }
}
