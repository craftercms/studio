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
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Interface for publish operations of a git repository
 */
public interface GitPublishCapableRepository extends GitContentRepository {
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

	/**
	 * Publishes all changes for the given site and target
	 *
	 * @param publishPackage   the publish package
	 * @param publishingTarget the publishing target
	 * @return the change set listing the affected paths and new commit ids (comparing the published repository target branch before and after the publish)
	 */
	<T extends PublishItemTO> GitPublishChangeSet<T> publishAll(PublishPackage publishPackage,
								    String publishingTarget) throws ServiceLayerException, IOException;

	/**
	 * Execute initial publish for given site
	 *
	 * @param publishPackage the package to publish
	 * @param ignorePaths    the paths to ignore
	 * @param target         the target to publish to
	 * @return commit id of the initial publish.
	 */
	String initialPublish(PublishPackage publishPackage, List<String> ignorePaths,
			      String target) throws ServiceLayerException;

	/**
	 * Store the result of a publish operation
	 *
	 * @param successfulItems the paths that were updated
	 * @param failedItems     the paths that failed to publish, mapped to the error message
	 * @param <T>             the actual type of the {@link PublishItemTO} objects
	 */
	record GitPublishChangeSet<T extends PublishItemTO>(String commitId,
							    Collection<T> successfulItems,
							    Collection<T> failedItems) {

		/**
		 * Check if there are successfully published changes
		 *
		 * @return true if the package had successful changes and a commit was created, false otherwise
		 */
		public boolean completed() {
			return !ObjectUtils.isEmpty(commitId);
		}

		/**
		 * Check if there are failed items
		 *
		 * @return true if failed items list contains items, false otherwise
		 */
		public boolean hasFailedItems() {
			return !ObjectUtils.isEmpty(failedItems);
		}
	}
}
