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

package org.craftercms.studio.api.v2.security;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.model.rest.content.DetailedItem;

public interface SemanticsAvailableActionsResolver {

    /**
     * Calculate available actions for given content item
     * @param username user name to apply permissions
     * @param siteId site identifier
     * @param item Item
     * @return bitmap representing available actions
     */
    long calculateContentItemAvailableActions(String username, String siteId, Item item)
            throws ServiceLayerException, UserNotFoundException;

    /**
     * Calculate available actions for given content item
     * @param username user name to apply permissions
     * @param siteId site identifier
     * @param detailedItem Item
     * @return bitmap representing available actions
     */
    long calculateContentItemAvailableActions(String username, String siteId, DetailedItem detailedItem)
            throws ServiceLayerException, UserNotFoundException;
}
