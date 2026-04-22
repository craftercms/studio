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

package org.craftercms.studio.model.contentType;

import java.util.Collection;

/**
 * Represents the includes and excludes for content type paths.
 *
 * @param includes the included path patterns for the content type
 * @param excludes the excluded path patterns for the content type
 */
public record PathIncludeExcludes(Collection<PathPattern> includes,
								  Collection<PathPattern> excludes) {
}
