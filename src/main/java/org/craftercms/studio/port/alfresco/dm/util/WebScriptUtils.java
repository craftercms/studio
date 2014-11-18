/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
 */
package org.craftercms.cstudio.alfresco.dm.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.webscripts.WebScriptException;

public class WebScriptUtils {

    /**
     * check a mandatory parameter
     *
     * @param name
     * @param value
     */
    public static void checkMandatoryParameter(final String name, final String value) {
        if (StringUtils.isEmpty(value)) {
            throw new WebScriptException(name + " is required.");
        }
    }
}
