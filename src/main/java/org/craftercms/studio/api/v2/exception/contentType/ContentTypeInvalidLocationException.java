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

package org.craftercms.studio.api.v2.exception.contentType;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;

/**
 * Exception to be thrown when trying to write (or paste) a content type to an invalid location
 */
public class ContentTypeInvalidLocationException extends ServiceLayerException {

	public ContentTypeInvalidLocationException(String siteId, String contentType, String sourcePath, String targetPath) {
		super(String.format("Cannot copy or move content in site '%s' from '%s' to '%s': " +
					"content type '%s' of the source item is not allowed in the target location", siteId, sourcePath, targetPath, contentType));
	}
}
