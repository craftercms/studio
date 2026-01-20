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

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.craftercms.commons.validation.annotations.param.ValidExistingContentPath;

import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.ALPHANUMERIC;

/**
 * Request body for a content revert operation.
 */
public class RevertRequestBody {
	@NotEmpty
	@ValidExistingContentPath
	private String path;
	@NotEmpty
	@EsapiValidatedParam(type = ALPHANUMERIC)
	@Pattern(regexp = "^[0-9a-f]{40}$")
	private String commitId;

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(final String commitId) {
		this.commitId = commitId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}
}
