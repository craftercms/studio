/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v1.service.content;

import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DmOrderTO;

import java.util.List;

/**
 * Content Services that other services may use
 *
 * @author russdanner
 */
public interface ContentService {

	/**
	 * get the tree of content items (metadata) beginning at a root
	 *
	 * @param site - the project ID
	 * @param path - the path to root at
	 * @return content item with children tree
	 */
	ContentItemTO getContentItemTree(String site, String path, int depth);

	/**
	 * get the content item (metadata) at a specific path
	 *
	 * @param site - the project ID
	 * @param path - the path of the content item
	 * @return content item representation
	 */
	ContentItemTO getContentItem(String site, String path);

	List<DmOrderTO> getItemOrders(String site, String path) throws ContentNotFoundException;

}
