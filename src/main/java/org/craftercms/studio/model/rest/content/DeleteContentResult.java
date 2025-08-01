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

package org.craftercms.studio.model.rest.content;

import java.util.List;

/**
 * Extension of {@link WriteContentResult} that represents the result of a delete operation.
 * It contains the commit id and a list of affected items (including deletes and potential
 * writes from the lifecycle controller)
 * It also contains the publish package id if delete operation is performed on a published site
 */
public class DeleteContentResult extends WriteContentResult {
	private final long publishPackageId;

	public DeleteContentResult(String commitId, List<WriteContentResultItem> items, long publishPackageId) {
		super(commitId, items);
		this.publishPackageId = publishPackageId;
	}

	public long getPublishPackageId() {
		return publishPackageId;
	}
}
