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

import org.craftercms.studio.api.v2.dal.security.NormalizedRole;

import java.util.List;
import java.util.Set;

/**
 * Represents a content type in the system.
 */
public class ContentType {
	protected String id;
	protected String label;
	protected Type type;
	protected Set<NormalizedRole> allowedRoles;
	protected List<DeleteDependency> deleteDependencies;
	protected List<CopyDependency> copyDependencies;
	protected boolean previewable;
	protected String imageThumbnail;
	protected boolean noThumbnail;
	protected List<String> pathIncludes;
	protected List<String> pathExcludes;
	protected boolean quickCreate;
	protected String quickCreatePath;

	public enum Type {
		PAGE, COMPONENT, UNKNOWN
	}
}
