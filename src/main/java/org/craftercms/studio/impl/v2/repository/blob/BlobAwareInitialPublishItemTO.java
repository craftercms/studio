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

package org.craftercms.studio.impl.v2.repository.blob;

import org.craftercms.studio.api.v2.dal.publish.PublishItem;
import org.craftercms.studio.api.v2.repository.PublishItemTO;

/**
 * Represents an initial publish blob item to be processed by the blob store.
 * This item is not tied to an actual {@link PublishItem}. A {@link PublishItem}
 * will be inserted for an initial publish package only for failures.
 */
public class BlobAwareInitialPublishItemTO implements PublishItemTO {

	private final String path;
	private final String repoPath;
	private int error;

	/**
	 * Constructor
	 *
	 * @param path     the path of the item in the blob store
	 * @param repoPath the path of the item pointer in the repository
	 */
	public BlobAwareInitialPublishItemTO(String path, String repoPath) {
		this.path = path;
		this.repoPath = repoPath;
	}

	@Override
	public String getPath() {
		return path;
	}

	public String getRepoPath() {
		return repoPath;
	}

	@Override
	public PublishItem.Action getAction() {
		return PublishItem.Action.ADD;
	}

	@Override
	public int getError() {
		return error;
	}

	@Override
	public void setFailed(int error) {
		this.error = error;
	}

	@Override
	public void setCompleted() {
	}
}
