/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.alfresco.dm.service.impl;

import java.util.List;

import org.craftercms.cstudio.alfresco.dm.service.api.DmContentService;
import org.craftercms.cstudio.alfresco.dm.to.DmContentItemTO;
import org.craftercms.cstudio.alfresco.dm.util.DmContentItemComparator;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoLiveQueueOrganizer {

    protected static final Logger logger = LoggerFactory.getLogger(GoLiveQueueOrganizer.class);
    
    protected DmContentService _dmContentService;
    protected DmContentItemTO.ChildFilter _childFilter;

    public GoLiveQueueOrganizer(DmContentService dmContentService, DmContentItemTO.ChildFilter childFilter) {
        this._dmContentService = dmContentService;
        this._childFilter = childFilter;
    }

    public void addToGoLiveItems(String site, DmContentItemTO node,
                                 List<DmContentItemTO> categoryItems, DmContentItemComparator comparator,
                                 boolean includeInProgress, List<String> displayPatterns) throws ServiceException {


        // if deleted, just add the top level items
        /*WcmAvmPathTO path = new WcmAvmPathTO(node.getPath());*/
        // display only if the path matches one of display patterns
        if (DmUtils.matchesPattern(node.getUri(), displayPatterns)) {

            _addToCategoryList(categoryItems, site, node, includeInProgress, comparator);

        }

    }

    protected void _addToCategoryList(final List<DmContentItemTO> categoryItems, final String site,
                                      final DmContentItemTO node,
                                      boolean includeInProgress, final DmContentItemComparator comparator) {
        // add only folders or xml files

        /*WcmContentItemTO itemToAdd = null;
        try {
            itemToAdd = _wcmContentService.getContentItem(site, path, true, true, true);

        } catch (ServiceException e) {
            LOGGER.error("Could not add to category[" + path + "]", e);
            return;
        }*/
        addThis(categoryItems, comparator, node, includeInProgress);
        /*List<WcmContentItemTO> contentItemTOList = itemToAdd.getAssets();
        for (WcmContentItemTO itemTO : contentItemTOList) {
            addThis(categoryItems, comparator, itemTO, includeInProgress);
        }
        List<WcmContentItemTO> wcmContentItemTOs = itemToAdd.getDocuments();
        for (WcmContentItemTO wcmContentItemTO : wcmContentItemTOs) {
            addThis(categoryItems, comparator, wcmContentItemTO, includeInProgress);
        }
        List<WcmContentItemTO> tos = itemToAdd.getComponents();
        for (WcmContentItemTO wcmContentItemTO : tos) {
            addThis(categoryItems, comparator, wcmContentItemTO, includeInProgress);
        }
*/
    }

    protected void addThis(List<DmContentItemTO> categoryItems, DmContentItemComparator comparator, DmContentItemTO itemToAdd, boolean includeInProgress) {
        boolean include = itemToAdd.isSubmitted() || itemToAdd.isSubmittedForDeletion();
        if (includeInProgress) {
            include = include || itemToAdd.isInProgress();
        }
        if (!include) {
            return;
        }
        DmContentItemTO found = null;
        String uri = itemToAdd.getUri();
        for (DmContentItemTO categoryItem : categoryItems) {
            String categoryPath = categoryItem.getPath() + "/";
            if (uri.startsWith(categoryPath)) {
                found = categoryItem;
                break;
            }
        }
        if (found != null && !found.getUri().equals(itemToAdd.getUri())) {
            //packages.put(taskId,itemToAdd);
            found.addChild(itemToAdd, comparator, true, _childFilter);
        }
    }
}
