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

package org.craftercms.studio.api.v2.service.system;

import java.util.List;
import java.util.Map;

/**
 * Allows to manage system properties.
 */
public interface SystemPropertiesService {

	/**
	 * Allowed pattern for property names.
	 */
	String PROPERTY_NAME_ALLOWED_PATTERN = "[a-zA-Z0-9_.]+";

	/**
	 * Get system properties for the given keys.
	 *
	 * @param propertyNames the property names
	 * @return a map with the requested properties
	 */
	Map<String, String> getSystemProperties(List<String> propertyNames);

	/**
	 * Set system properties.
	 * The non-null property value will be set/updated, while properties with null values will be deleted.
	 *
	 * @param properties a map with the property names and their corresponding values
	 */
	void setSystemProperties(Map<String, String> properties);
}
