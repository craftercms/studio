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

import org.craftercms.studio.api.v2.dal.item.ContentItem;

import java.util.List;

/**
 * Children result for a get children operation.
 */
public class GetChildrenResult {

	protected int total;
	protected int offset;
	protected int limit;
	private ContentItem levelDescriptor;
	private List<ContentItem> children;

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public ContentItem getLevelDescriptor() {
		return levelDescriptor;
	}

	public void setLevelDescriptor(ContentItem levelDescriptor) {
		this.levelDescriptor = levelDescriptor;
	}

	public List<ContentItem> getChildren() {
		return children;
	}

	public void setChildren(List<ContentItem> children) {
		this.children = children;
	}
}
