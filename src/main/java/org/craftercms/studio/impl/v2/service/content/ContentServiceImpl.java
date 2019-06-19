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

package org.craftercms.studio.impl.v2.service.content;

import org.craftercms.studio.api.v2.dal.QuickCreateItem;
import org.craftercms.studio.api.v2.service.content.ContentService;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.api.v2.service.content.internal.ContentTypeServiceInternal;

import java.util.List;

public class ContentServiceImpl implements ContentService {

    private ContentServiceInternal contentServiceInternal;
    private ContentTypeServiceInternal contentTypeServiceInternal;

    @Override
    public List<QuickCreateItem> getQuickCreatableContentTypes(String siteId) {
        return contentTypeServiceInternal.getQuickCreatableContentTypes(siteId);
    }

    public ContentServiceInternal getContentServiceInternal() {
        return contentServiceInternal;
    }

    public void setContentServiceInternal(ContentServiceInternal contentServiceInternal) {
        this.contentServiceInternal = contentServiceInternal;
    }

    public ContentTypeServiceInternal getContentTypeServiceInternal() {
        return contentTypeServiceInternal;
    }

    public void setContentTypeServiceInternal(ContentTypeServiceInternal contentTypeServiceInternal) {
        this.contentTypeServiceInternal = contentTypeServiceInternal;
    }
}
