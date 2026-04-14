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

package org.craftercms.studio.model.rest.content;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.craftercms.commons.validation.annotations.param.ValidExistingContentPath;

/**
 * Request body for a content write operation
 */
public class WriteContentRequest {

	public static final int WRITE_COMMENT_MAX_LENGTH = 500;

	@NotEmpty
	@ValidExistingContentPath
	private String path;
	private String content;
	@Size(max = WRITE_COMMENT_MAX_LENGTH)
	private String comment;

	public String getContent() {
		return content;
	}

	public void setContent(final String content) {
		this.content = content;
	}

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public @Size String getComment() {
		return comment;
	}

	public void setComment(@Size String comment) {
		this.comment = comment;
	}
}
