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

package org.craftercms.studio.api.v2.security;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.item.ContentItem;

/**
 * Interface to resolve available actions from user permissions
 * The {@link SemanticsAvailableActionsResolver} consider not only permissions but
 * also the state and metadata of a content item to calculate available actions
 */
public interface SemanticsAvailableActionsResolver {

	/**
	 * Calculate available actions for given content item
	 *
	 * @param username     user name to apply permissions
	 * @param siteId       site identifier
	 * @param detailedItem detailed content item to calculate available actions for
	 * @return bitmap representing available actions
	 */
	long calculateContentItemAvailableActions(String username, String siteId, ContentItem detailedItem)
			throws ServiceLayerException, UserNotFoundException;
}
