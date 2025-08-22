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
 * Extension of {@link WriteContentResult} that represents the result of a paste operation.
 * It includes the final target path where the content was pasted.
 * Notice the target path is not necessarily the same as the original target path of the request,
 * as it may have been modified to avoid conflicts (e.g., if the target path already exists).
 */
public class PasteContentResult extends WriteContentResult {

	private final String targetPath;

	public PasteContentResult(final String commitId, final List<WriteContentResultItem> items,
							  final String targetPath) {
		super(commitId, items);
		this.targetPath = targetPath;
	}

	public String getTargetPath() {
		return targetPath;
	}
}
