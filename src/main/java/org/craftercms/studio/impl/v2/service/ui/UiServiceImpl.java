/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.api.v2.service.ui.UiService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.service.ui.internal.UiServiceInternal;
import org.craftercms.studio.impl.v2.utils.security.SecurityUtils;
import org.craftercms.studio.model.ui.MenuItem;

import java.util.List;
import java.util.Set;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.DEFAULT_PATH_RESOURCE_VALUE;

/**
 * Default implementation of {@link UiService}. Delegates to the {@link UiServiceInternal} for the actual work.
 *
 * @author avasquez
 */
public class UiServiceImpl implements UiService {

	private final UserService userService;
	private final UiServiceInternal uiServiceInternal;
	private StudioConfiguration studioConfiguration;

	public UiServiceImpl(UserService userService, UiServiceInternal uiServiceInternal) {
		this.userService = userService;
		this.uiServiceInternal = uiServiceInternal;
	}

	public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
		this.studioConfiguration = studioConfiguration;
	}

	@Override
	public List<MenuItem> getGlobalMenu() throws AuthenticationException, ServiceLayerException, UserNotFoundException {
		String user = SecurityUtils.getCurrentUsername();
		if (StringUtils.isNotEmpty(user)) {
			Set<String> permissions = userService.getUserPermissions(StringUtils.EMPTY, DEFAULT_PATH_RESOURCE_VALUE, user);

			return uiServiceInternal.getGlobalMenu(permissions);
		} else {
			throw new AuthenticationException("User is not authenticated");
		}
	}

	@Override
	public String getActiveEnvironment() throws AuthenticationException {
		String user = SecurityUtils.getCurrentUsername();
		if (StringUtils.isNotEmpty(user)) {
			return studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE);
		} else {
			throw new AuthenticationException("User is not authenticated");
		}
	}
}
