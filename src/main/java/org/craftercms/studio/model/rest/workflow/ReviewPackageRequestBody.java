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

package org.craftercms.studio.model.rest.workflow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.craftercms.studio.impl.v2.utils.SanitizerUtil;

import java.util.List;

import static org.craftercms.studio.api.v2.service.publish.PublishService.PACKAGE_COMMENT_MAX_LENGTH;

/**
 * Request body for reviewing a package (reject, approve)
 */
public class ReviewPackageRequestBody {

	@NotBlank
	@Size(max = PACKAGE_COMMENT_MAX_LENGTH)
	private String comment;
	@NotEmpty
	private List<@NotNull Long> packageIds;

	public @NotBlank String getComment() {
		return comment;
	}

	public void setComment(@NotBlank String comment) {
		this.comment = SanitizerUtil.sanitizeText(comment);
	}

	public @NotEmpty List<Long> getPackageIds() {
		return packageIds;
	}

	public void setPackageIds(@NotEmpty List<Long> packageIds) {
		this.packageIds = packageIds;
	}
}
