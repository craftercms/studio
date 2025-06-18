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

package org.craftercms.studio.api.v2.repository;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.repository.publish.GitPublishChangeSet;

import java.io.IOException;
import java.util.Collection;

/**
 * Interface for content repositories that support publishing
 */
public interface PublishCapableRepository {

	/**
	 * Publishes the given items to the given target
	 *
	 * @param publishPackage   the publish package
	 * @param publishingTarget the publishing target
	 * @param publishItems     the items to publish
	 * @param <T>              the type of the {@link PublishItemTO} objects
	 * @return the change set listing the affected paths and new commit id
	 * @throws ServiceLayerException if there is any error while publishing or publishItems is null or empty
	 */
	<T extends PublishItemTO> GitPublishChangeSet<T> publish(PublishPackage publishPackage,
															 String publishingTarget,
															 Collection<T> publishItems) throws ServiceLayerException, IOException;
}
