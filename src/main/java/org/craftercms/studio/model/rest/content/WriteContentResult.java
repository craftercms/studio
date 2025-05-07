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

import org.craftercms.studio.api.v1.service.content.DmContentLifeCycleService;

import java.util.List;

public class WriteContentResult {
	private final List<WriteContentResultItem> items;

	public WriteContentResult(final List<WriteContentResultItem> items) {
		this.items = items;
	}

	public List<WriteContentResultItem> getItems() {
		return items;
	}

	public record WriteContentResultItem(String path,
										 DmContentLifeCycleService.ContentLifeCycleOperation operation,
										 boolean amended) {
	}
}
