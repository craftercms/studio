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

package org.craftercms.studio.model.history;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Represents a version of a content item.
 */
public class ItemVersion {

	private final RepositoryVersion repositoryVersion;
	// Path can change between versions if the item has been renamed
	private String path;
	// oldPath will be the same as path, unless the item has been renamed
	private String oldPath;
	private boolean revertible;

	public ItemVersion(RepositoryVersion repositoryVersion) {
		this.repositoryVersion = repositoryVersion;
	}

	public boolean isRevertible() {
		return revertible;
	}

	public void setRevertible(boolean revertible) {
		this.revertible = revertible;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getOldPath() {
		return oldPath;
	}

	public void setOldPath(String oldPath) {
		this.oldPath = oldPath;
	}

	@JsonUnwrapped
	public RepositoryVersion getRepositoryVersion() {
		return repositoryVersion;
	}
}
