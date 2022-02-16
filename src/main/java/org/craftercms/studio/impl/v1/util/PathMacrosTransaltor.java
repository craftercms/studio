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
package org.craftercms.studio.impl.v1.util;


import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;

import java.util.Map;

public class PathMacrosTransaltor {

    public static final String PAGEID = "{pageId}";

    public static final String PAGE_GROUPID = "{pageGroupId}";


    /**
     * Match the URL with the know patterns and translate them to actual value
     *
     * @param path
     */
    public static String resolvePath(String path,Map<String,String> properties) throws ServiceLayerException {

        String pageId = properties.get(DmConstants.KEY_PAGE_ID);

        String groupId = properties.get(DmConstants.KEY_PAGE_GROUP_ID);

        if(StringUtils.isNotEmpty(pageId) && (path.contains(PAGEID))){
            path = path.replace(PAGEID,  pageId);
        }

        if(StringUtils.isNotEmpty(groupId) && (path.contains(PAGE_GROUPID))){
            path = path.replace(PAGE_GROUPID, groupId);
        }

        return path;
    }
}
