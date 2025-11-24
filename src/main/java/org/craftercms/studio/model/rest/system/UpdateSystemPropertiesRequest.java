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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.craftercms.commons.validation.annotations.param.ValidateNoTagsParam;

import java.util.List;

/**
 * Request object for setting system properties.
 */
public class UpdateSystemPropertiesRequest {

	@NotEmpty
	protected List<@Valid SystemProperty> properties;

	public List<SystemProperty> getProperties() {
		return properties;
	}

	public void setProperties(final List<SystemProperty> properties) {
		this.properties = properties;
	}

	/**
	 * Represents a single system property.
	 */
	public static class SystemProperty {
		@Size(max = 50)
		@ValidateNoTagsParam
		private String name;
		@Size(max = 4000)
		private String value;

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(final String value) {
			this.value = value;
		}
	}
}
