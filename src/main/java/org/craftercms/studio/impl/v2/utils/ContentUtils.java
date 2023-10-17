/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;

public class ContentUtils {

    /**
     * Returns the content parent url
     * @param url the url to process
     * @return parent url
     */
    public static String getParentUrl(String url) {
        int lastIndex = url.lastIndexOf(FILE_SEPARATOR);
        return url.substring(0, lastIndex);
    }

    /**
     * Returns the page name part (e.g.index.xml) of a given URL
     * @param url the url to process
     * @return page name
     */
    public static String getPageName(String url) {
        int lastIndex = url.lastIndexOf(FILE_SEPARATOR);
        return url.substring(lastIndex + 1);
    }
}
