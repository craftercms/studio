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

package org.craftercms.studio.model.publish;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.craftercms.studio.api.v2.dal.publish.PublishItemMetadata;

/**
 * Represents an item of a publish package being calculated.
 */
public class CalculatedPublishItem {
	private String path;
	private PublishItemMetadata metadata;

	public void setMetadata(PublishItemMetadata metadata) {
		this.metadata = metadata;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@JsonUnwrapped
	public PublishItemMetadata getMetadata() {
		return metadata;
	}

	public String getPath() {
		return path;
	}
}
