/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.dal;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LEVEL_DESCRIPTOR_PATH;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LOCALE_CODE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ORDER;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PARENT_PATH;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PATH;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SORT;

public interface ItemMetadataDAO {

    /**
     * Get children for given path from database
     *
     * @param siteId site identifier
     * @param parentPath parent path
     * @param ldPath level descriptor path
     * @param path path to get children for
     * @param localeCode locale code
     * @param sort sort strategy
     * @param order order of children
     * @param offset offset of the first record to return
     * @param limit number of children to return
     *
     * @return list of items (parent, level descriptor, children)
     */
    List<SandboxItem> getChildrenByPath(@Param(SITE) String siteId, @Param(PARENT_PATH) String parentPath,
                                        @Param(LEVEL_DESCRIPTOR_PATH) String ldPath, @Param(PATH) String path,
                                        @Param(LOCALE_CODE) String localeCode, @Param(SORT) String sort,
                                        @Param(ORDER) String order, @Param(OFFSET) int offset, @Param(LIMIT) int limit);

    /**
     * Get total number of children for given path
     *
     * @param siteId site identifier
     * @param path path to get children for
     * @param localeCode local code
     *
     * @return total number of children
     */
    int getChildrenByPathTotal(@Param(SITE) String siteId, @Param(PATH) String path,
                                        @Param(LOCALE_CODE) String localeCode);
}
