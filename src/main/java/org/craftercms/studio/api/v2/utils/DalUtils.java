/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.utils;

import org.craftercms.commons.rest.parameters.SortField;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for DAL related operations.
 */
public class DalUtils {

	/**
	 * Batch size to split params list for MyBatis queries.
	 */
	public final static int MY_BATIS_QUERY_BATCH_SIZE = 1000;

	/**
	 * Map the field names from a SortFields list base on provided name mapping.
	 * This is meant to be used to map from API field names to database column names.
	 *
	 * @param sortFields list of SortField objects
	 * @param fieldsMap  API to database field name mapping
	 * @return list of SortField objects with mapped field names
	 */
	public static List<SortField> mapSortFields(Collection<SortField> sortFields, Map<String, String> fieldsMap) {
		if (CollectionUtils.isEmpty(sortFields) || CollectionUtils.isEmpty(fieldsMap)) {
			return Collections.emptyList();
		}
		return sortFields.stream()
			.map(sf -> new SortField(fieldsMap.get(sf.getField()), sf.getOrder()))
			.collect(Collectors.toList());
	}
}
