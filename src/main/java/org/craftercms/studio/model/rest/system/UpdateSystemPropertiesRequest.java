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

package org.craftercms.studio.model.rest.system;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.craftercms.commons.validation.annotations.param.ValidateNoTagsParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;

import java.util.Map;

import static org.craftercms.studio.api.v2.service.system.SystemPropertiesService.PROPERTY_NAME_ALLOWED_PATTERN;

/**
 * Request object for setting system properties.
 */
public class UpdateSystemPropertiesRequest {

	@NotEmpty
	protected Map<@Size(max = 50) @ValidateNoTagsParam @ValidateStringParam(whitelistedPatterns = PROPERTY_NAME_ALLOWED_PATTERN) String, @Size(max = 4000) String> properties;

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(final Map<String, String> properties) {
		this.properties = properties;
	}
}
