/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.model.rest.publish;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;

import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.ALPHANUMERIC;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.ALPHANUMERIC_LOWERCASE_PATTERN;

/**
 * Request for publish package recalculation
 */
public class RecalculatePublishPackageRequest {
	@NotEmpty
	@Size(max = 20)
	@EsapiValidatedParam(type = ALPHANUMERIC)
	@Pattern(regexp = ALPHANUMERIC_LOWERCASE_PATTERN)
	private String publishingTarget;

	public String getPublishingTarget() {
		return publishingTarget;
	}

	public void setPublishingTarget(String publishingTarget) {
		this.publishingTarget = publishingTarget;
	}
}
