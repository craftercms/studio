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

package org.craftercms.studio.api.v1.dal;

import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface NavigationOrderSequenceMapper {

	String SITE = "site";
	String SOURCE_PATH = "sourcePath";
	String TARGET_PATH = "targetPath";

	void insert(NavigationOrderSequence navigationOrderSequence);

	void update(NavigationOrderSequence navigationOrderSequence);

	NavigationOrderSequence getPageNavigationOrderForSiteAndPath(Map params);

	void deleteSequencesForSite(Map params);

	/**
	 * Moves the navigation order from one path to another.
	 *
	 * @param site       the site id
	 * @param sourcePath the previous path to update
	 * @param targetPath the new path
	 */
	void move(@Param(SITE) String site,
			  @Param(SOURCE_PATH) String sourcePath,
			  @Param(TARGET_PATH) String targetPath);
}
