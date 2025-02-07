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

package org.craftercms.studio.api.v2.security.publish;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;

/**
 * Resolve the available actions for publish packages
 */
public interface PublishPackageAvailableActionResolver {

	/**
	 * Get the actions the current user is allowed to perform for the given publish package
	 *
	 * @param publishPackage the publish package
	 * @return the available actions bitmap
	 * @throws ServiceLayerException if an error occurs while calculating the available actions
	 * @throws UserNotFoundException if the current user cannot be found
	 */
	long getPublishPackageAvailableActions(PublishPackage publishPackage) throws ServiceLayerException, UserNotFoundException;
}
