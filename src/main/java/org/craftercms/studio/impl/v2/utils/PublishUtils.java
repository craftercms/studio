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

package org.craftercms.studio.impl.v2.utils;

import org.craftercms.studio.model.rest.ApiResponse;

/**
 * Utility class for publish operations.
 */
public class PublishUtils {

    /**
     * Translates an exception during publishing to an error code.
     *
     * @param e the exception
     * @return the error code
     */
    public static int translateException(final Exception e) {
        // TODO: implement
        return ApiResponse.INTERNAL_SYSTEM_FAILURE.getCode();
    }
}
