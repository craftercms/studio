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
package org.craftercms.studio.model.rest.clipboard;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.craftercms.commons.validation.annotations.param.ValidNewContentPath;
import org.craftercms.studio.model.clipboard.Operation;

/**
 * Holds all data needed for a clipboard operation
 *
 * @author joseross
 * @since 3.2
 */
public class PasteRequest {

	/**
	 * The operation to perform
	 */
	@NotNull
	protected Operation operation;

	/**
	 * The target path
	 */
	@NotEmpty
	@ValidNewContentPath
	protected String targetPath;

	/**
	 * The source path of the item
	 */
	@NotEmpty
	@ValidNewContentPath
	protected String sourcePath;

	protected boolean includeChildren;

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public boolean isIncludeChildren() {
		return includeChildren;
	}

	public void setIncludeChildren(boolean includeChildren) {
		this.includeChildren = includeChildren;
	}
}
