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
import java.util.List;

/**
 * Interface for publish operations of a git repository
 */
public interface GitPublishCapableRepository extends GitContentRepository, PublishCapableRepository {

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

}
