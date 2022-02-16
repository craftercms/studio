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

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.util.DmContentItemComparator;

import java.util.List;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;


public class GoLiveQueueOrganizer {

    protected static final Logger logger = LoggerFactory.getLogger(GoLiveQueueOrganizer.class);
    
    protected ContentService contentService;
    protected ContentItemTO.ChildFilter childFilter;

    public GoLiveQueueOrganizer(ContentService contentService, ContentItemTO.ChildFilter childFilter) {
        this.contentService = contentService;
        this.childFilter = childFilter;
    }

    public void addToGoLiveItems(String site, ContentItemTO node,
                                 List<ContentItemTO> categoryItems, DmContentItemComparator comparator,
                                 boolean includeInProgress, List<String> displayPatterns) throws ServiceLayerException {


        // if deleted, just add the top level items
        /*WcmAvmPathTO path = new WcmAvmPathTO(node.getPath());*/
        // display only if the path matches one of display patterns
        if (ContentUtils.matchesPatterns(node.getUri(), displayPatterns)) {

            _addToCategoryList(categoryItems, site, node, includeInProgress, comparator);

        }

    }

    protected void _addToCategoryList(final List<ContentItemTO> categoryItems, final String site,
                                      final ContentItemTO node,
                                      boolean includeInProgress, final DmContentItemComparator comparator) {

        addThis(categoryItems, comparator, node, includeInProgress);
    }

    protected void addThis(List<ContentItemTO> categoryItems, DmContentItemComparator comparator, ContentItemTO itemToAdd, boolean includeInProgress) {
        boolean include = itemToAdd.isSubmitted() || itemToAdd.isSubmittedForDeletion();
        if (includeInProgress) {
            include = include || itemToAdd.isInProgress();
        }
        if (!include) {
            return;
        }
        ContentItemTO found = null;
        String uri = itemToAdd.getUri();
        for (ContentItemTO categoryItem : categoryItems) {
            String categoryPath = categoryItem.getPath() + FILE_SEPARATOR;
            if (uri.startsWith(categoryPath)) {
                found = categoryItem;
                break;
            }
        }
        if (found != null && !found.getUri().equals(itemToAdd.getUri())) {
            found.addChild(itemToAdd, comparator, true, childFilter);
        }
    }
}
