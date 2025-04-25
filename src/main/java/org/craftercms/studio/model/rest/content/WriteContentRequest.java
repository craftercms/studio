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

import jakarta.validation.constraints.NotBlank;
import org.craftercms.commons.validation.annotations.param.ValidExistingContentPath;

/**
 * Request for a write-content operation
 */
public class WriteContentRequest {

	@ValidExistingContentPath
	private String path;
	@ValidExistingContentPath
	private String oldPath;
	@NotBlank
	private String content;

	public @ValidExistingContentPath String getPath() {
		return path;
	}

	public void setPath(@ValidExistingContentPath String path) {
		this.path = path;
	}

	public @ValidExistingContentPath String getOldPath() {
		return oldPath;
	}

	public void setOldPath(@ValidExistingContentPath String oldPath) {
		this.oldPath = oldPath;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
