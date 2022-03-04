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
package org.craftercms.studio.api.v2.service.clipboard;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.model.clipboard.PasteItem;
import org.craftercms.studio.model.clipboard.Operation;

import java.util.List;

/**
 * Defines all clipboard related operations
 *
 * @author joseross
 * @since 3.2
 */
public interface ClipboardService {

    /**
     * Performs the given clipboard operation
     *
     * @param siteId the id of the site
     * @param operation the clipboard operation
     * @param targetPath the target path
     * @param item the item to paste
     * @return the list of pasted items
     * @throws ServiceLayerException if there is any error during the operation
     * @throws UserNotFoundException if the user is not found
     */
    List<String> pasteItems(String siteId, Operation operation, String targetPath, PasteItem item)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * Duplicates the given item
     *
     * @param siteId the id of the site
     * @param path the path of the item
     * @return the path of the new item
     * @throws ServiceLayerException if there is any error copying the item
     * @throws UserNotFoundException if the user is not found
     */
    String duplicateItem(String siteId, String path) throws ServiceLayerException, UserNotFoundException;

}
