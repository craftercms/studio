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

package org.craftercms.studio.impl.v2.service.content.internal;

import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;

import java.util.ArrayList;
import java.util.List;

public class ContentServiceInternalImpl implements ContentServiceInternal {

    private ContentRepository contentRepository;

    @Override
    public List<String> getSubtreeItems(String siteId, String path) {
        return contentRepository.getSubtreeItems(siteId, path);
    }

    @Override
    public List<String> getSubtreeItems(String siteId, List<String> paths) {
        List<String> subtreeItems = new ArrayList<String>();
        for (String path : paths) {
            subtreeItems.addAll(contentRepository.getSubtreeItems(siteId, path));
        }
        return subtreeItems;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }
}
