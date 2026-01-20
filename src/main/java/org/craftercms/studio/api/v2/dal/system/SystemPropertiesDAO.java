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

package org.craftercms.studio.api.v2.dal.system;

import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PROPERTIES;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PROPERTY_NAMES;

/**
 * DAO for system properties.
 */
public interface SystemPropertiesDAO {
	/**
	 * Update system properties.
	 * This method will update or insert the given properties, or delete them if their value is null.
	 *
	 * @param properties The properties to upsert
	 */
	default void updateProperties(@Param(PROPERTIES) Collection<SystemProperty> properties) {
		List<SystemProperty> updateProperties = new ArrayList<>();
		List<SystemProperty> deleteProperties = new ArrayList<>();
		for (SystemProperty property : properties) {
			if (property.value() != null) {
				updateProperties.add(property);
			} else {
				deleteProperties.add(property);
			}
		}
		if (isNotEmpty(deleteProperties)) {
			deleteProperties(deleteProperties);
		}
		if (isNotEmpty(updateProperties)) {
			upsertProperties(updateProperties);
		}
	}

	/**
	 * Upsert system properties.
	 *
	 * @param properties The properties to update or insert
	 */
	void upsertProperties(@Param(PROPERTIES) Collection<SystemProperty> properties);

	/**
	 * Delete system properties.
	 *
	 * @param properties The properties to delete
	 */
	void deleteProperties(@Param(PROPERTIES) Collection<SystemProperty> properties);

	/**
	 * Get system properties.
	 *
	 * @param propertyNames The names of the properties to get
	 * @return A collection with the requested properties (if they exist)
	 */
	Collection<SystemProperty> getProperties(@Param(PROPERTY_NAMES) Collection<String> propertyNames);
}
