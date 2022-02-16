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
package org.craftercms.studio.impl.v2.utils;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class with methods for pagination.
 *
 * @author avasquez
 */
public class PaginationUtils {

    private PaginationUtils() {
    }

    /**
     * Performs pagination on the {@code list}, returning from the specified {@code offset} to the specified
     * {@code limit}. The list can also be sorted by the {@code sortBy} (optional).
     *
     * @param list      the list to paginate
     * @param offset    the offset from where to start
     * @param limit     the max number of elements that the paginated list should include
     * @param sortBy    the property used for sorting
     *
     * @return the paginated list
     */
    public static <T> List<T> paginate(List<T> list, int offset, int limit, String sortBy) {
        Stream<T> stream = list.stream();

        if (StringUtils.isNotEmpty(sortBy)) {
            stream = stream.sorted(new BeanComparator<>(sortBy));
        }

        return stream.skip(offset).limit(limit).collect(Collectors.toList());
    }

}
