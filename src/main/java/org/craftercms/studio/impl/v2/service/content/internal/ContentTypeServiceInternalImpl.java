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

package org.craftercms.studio.impl.v2.service.content.internal;

import org.craftercms.studio.api.v1.service.content.ContentTypeService;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v2.dal.QuickCreateItem;
import org.craftercms.studio.api.v2.service.content.internal.ContentTypeServiceInternal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ContentTypeServiceInternalImpl implements ContentTypeServiceInternal {

    private ContentTypeService contentTypeService;

    @Override
    public List<QuickCreateItem> getQuickCreatableContentTypes(String siteId) {
        List<QuickCreateItem> toRet = new ArrayList<QuickCreateItem>();
        List<ContentTypeConfigTO> allContentTypes = contentTypeService.getAllContentTypes(siteId, true);
        List<ContentTypeConfigTO> quickCreatable = allContentTypes.stream()
                .filter(ct -> ct.isQuickCreate()).collect(Collectors.toList());
        for (ContentTypeConfigTO ctto : quickCreatable) {
            QuickCreateItem qci = new QuickCreateItem();
            qci.setSiteId(siteId);
            qci.setContentTypeId(ctto.getForm());
            qci.setLabel(ctto.getLabel());
            qci.setPath(ctto.getQuickCreatePath());
            toRet.add(qci);
        }
        return toRet;
    }

    public ContentTypeService getContentTypeService() {
        return contentTypeService;
    }

    public void setContentTypeService(ContentTypeService contentTypeService) {
        this.contentTypeService = contentTypeService;
    }
}
