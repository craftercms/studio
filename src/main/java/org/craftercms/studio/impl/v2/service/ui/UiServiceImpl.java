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

package org.craftercms.studio.impl.v2.service.ui;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.exception.ConfigurationException;
import org.craftercms.studio.api.v2.ui.UiService;
import org.craftercms.studio.impl.v1.util.ConfigUtils;
import org.craftercms.studio.impl.v2.service.ui.internal.UiServiceInternal;
import org.craftercms.studio.model.ui.MenuItem;
import org.springframework.beans.factory.annotation.Required;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_GLOBAL_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_GLOBAL_MENU_FILE_NAME;

/**
 * Default implementation of {@link UiService}. Delegates to the {@link UiServiceInternal} for the actual work.
 *
 * @author avasquez
 */
public class UiServiceImpl implements UiService {

    private SecurityService securityService;
    private UiServiceInternal uiServiceInternal;

    @Required
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Required
    public void setUiServiceInternal(UiServiceInternal uiServiceInternal) {
        this.uiServiceInternal = uiServiceInternal;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MenuItem> getGlobalMenu() throws AuthenticationException, ServiceException {
        String user = securityService.getCurrentUser();
        if (StringUtils.isNotEmpty(user)) {
            Set<String> permissions = securityService.getUserPermissions(null, "/", user, null);

            return uiServiceInternal.getGlobalMenu(permissions);
        } else {
            throw new AuthenticationException("User is not authenticated");
        }
    }

}
