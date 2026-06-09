/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.api.v1.exception;

public class ContentNotFoundException extends ServiceLayerException {

	/**
	 *
	 */
	protected static final long serialVersionUID = 8988159536378832232L;

	protected final String path;

	protected final String site;

	public ContentNotFoundException(String path, String site, String message) {
		this(path, site, message, null);
	}

	public ContentNotFoundException(String path, String site, String message, Exception e) {
		super(message, e);
		this.path = path;
		this.site = site;
	}

	public String getPath() {
		return path;
	}

	public String getSite() {
		return site;
	}
}
