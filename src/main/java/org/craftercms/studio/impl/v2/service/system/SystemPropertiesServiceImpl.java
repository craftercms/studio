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

package org.craftercms.studio.impl.v2.service.system;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.studio.api.v2.service.system.SystemPropertiesService;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_MANAGE_SYSTEM_PROPERTIES;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_READ_SYSTEM_PROPERTIES;

/**
 * Implementation of {@link org.craftercms.studio.api.v2.service.system.SystemPropertiesService}.
 */
public class SystemPropertiesServiceImpl implements SystemPropertiesService {
	protected final SystemPropertiesService systemPropertiesServiceInternal;

	@ConstructorProperties({"systemPropertiesServiceInternal"})
	public SystemPropertiesServiceImpl(final SystemPropertiesService systemPropertiesServiceInternal) {
		this.systemPropertiesServiceInternal = systemPropertiesServiceInternal;
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_SYSTEM_PROPERTIES)
	public Map<String, String> getSystemProperties(final List<String> propertyNames) {
		return systemPropertiesServiceInternal.getSystemProperties(propertyNames);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_MANAGE_SYSTEM_PROPERTIES)
	public void setSystemProperties(final Map<String, String> properties) {
		systemPropertiesServiceInternal.setSystemProperties(properties);
	}
}
