/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.service.content.internal;

import org.craftercms.studio.api.v2.dal.QuickCreateItem;

import java.util.List;

public interface ContentTypeServiceInternal {

    /**
     * Get list of content types marked as quick creatable for given site
     *
     * @param siteId
     * @return
     */
    List<QuickCreateItem> getQuickCreatableContentTypes(String siteId);
}
