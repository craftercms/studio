/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
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
	 * Cut-and-paste content from sourcePath to targetPath.
	 * <p>
	 * Cut-and-paste is a special case of a move operation that will:
	 * - Take the target parent (instead of the target full path) as a parameter
	 * - Calculate the final target path based on the existing content at the target parent
	 * The cut-and-paste operation will be rejected if any of the following conditions are met:
	 * - The source item is not found
	 * - The target parent is the same as the source parent
	 * - The target parent is not found
	 * - The target parent is not a folder or a page
	 * <p>
	 * e.g.:
	 * Consider the initial tree:
	 * - /site/website/index.xml
	 * - /site/website/articles/page1/index.xml
	 * - /site/website/page2/index.xml
	 * <p>
	 * If we cut and paste /site/website/page2/index.xml to /site/website/articles (or /site/website/articles/index.xml), the expected result tree would be:
	 * - /site/website/index.xml
	 * - /site/website/articles/page1/index.xml
	 * - /site/website/articles/page2/index.xml
	 * <p>
	 * If the initial tree was instead:
	 * - /site/website/index.xml
	 * - /site/website/articles/page1/index.xml
	 * - /site/website/articles/page2/index.xml
	 * - /site/website/page2/index.xml
	 * <p>
	 * And we cut and paste /site/website/page2/index.xml to /site/website/articles/index.xml (or /site/website/articles), the expected result tree would be:
	 * - /site/website/index.xml
	 * - /site/website/articles/page1/index.xml
	 * - /site/website/articles/page2/index.xml
	 * - /site/website/articles/page2-copy-1/index.xml ("-copy-1" added to resolve the name collision, subsequent paste operations would increment the number)
	 * <p>
	 * Copy-and-paste is a similar operation that will not affect the source parent and that will:
	 * - Take the target parent (instead of the target full path) as a parameter
	 * - Calculate the final target path based on the existing content at the target parent
	 * - Accept a target path that is the same as the source parent (unlike cut-and-paste)
	 * The copy-and-paste operation will be rejected if any of the following conditions are met:
	 * - The source item is not found
	 * - The target parent is the same as the source parent
	 * - The target parent is not found
	 * - The target parent is not a folder or a page
	 *
	 * @param siteId     the id of the site
	 * @param operation  the clipboard operation
	 * @param targetPath the target path
	 * @param sourcePath the source path of the item
	 * @param includeChildren whether to include children of the source item in the operation
	 * @return the list of pasted items
	 * @throws ServiceLayerException if there is any error during the operation
	 * @throws UserNotFoundException if the user is not found
	 */
	List<String> pasteItems(String siteId, Operation operation, String targetPath, String sourcePath, boolean includeChildren)
		throws ServiceLayerException, UserNotFoundException, AuthenticationException;

	/**
	 * Duplicates the given item
	 *
	 * @param siteId the id of the site
	 * @param path   the path of the item
	 * @return the path of the new item
	 * @throws ServiceLayerException if there is any error copying the item
	 */
	String duplicateItem(String siteId, String path) throws ServiceLayerException, AuthenticationException, UserNotFoundException;

}
