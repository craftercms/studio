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

package org.craftercms.studio.impl.v2.service.system.internal;

import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.system.SystemPropertiesDAO;
import org.craftercms.studio.api.v2.dal.system.SystemProperty;
import org.craftercms.studio.api.v2.service.system.SystemPropertiesService;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Internal implementation of {@link SystemPropertiesService}.
 */
public class SystemPropertiesServiceInternalImpl implements SystemPropertiesService {

	protected final SystemPropertiesDAO systemPropertiesDAO;
	protected final RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

	@ConstructorProperties({"retryingDatabaseOperationFacade", "systemPropertiesDAO"})
	public SystemPropertiesServiceInternalImpl(final RetryingDatabaseOperationFacade retryingDatabaseOperationFacade,
											   final SystemPropertiesDAO systemPropertiesDAO) {
		this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
		this.systemPropertiesDAO = systemPropertiesDAO;
	}

	@Override
	public Map<String, String> getSystemProperties(final List<String> propertyNames) {
		return systemPropertiesDAO.getProperties(propertyNames).stream()
				.collect(Collectors.toMap(SystemProperty::name, SystemProperty::value));
	}

	@Override
	public void setSystemProperties(final Map<String, String> properties) {
		List<SystemProperty> propertyList = properties.entrySet().stream()
				.map(e -> new SystemProperty(e.getKey(), e.getValue()))
				.toList();
		retryingDatabaseOperationFacade.retry(() -> systemPropertiesDAO.updateProperties(propertyList));
	}
}
