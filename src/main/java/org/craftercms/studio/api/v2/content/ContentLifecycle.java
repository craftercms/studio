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

package org.craftercms.studio.api.v2.content;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;

/**
 * Interface for content lifecycle execution
 */
public interface ContentLifecycle {
	/**
	 * Executes a content lifecycle.
	 * Implementations will update the {@link LifecycleContent} object
	 * with the results of the execution.
	 *
	 * @param siteId           the site id
	 * @param lifecycleContent the lifecycle content object, containing the parameters and
	 *                         enabling the {@link ContentLifecycle} to alter the content
	 * @param contentLoader    content load used to read from the site content
	 */
	void execute(String siteId, LifecycleContent lifecycleContent, ContentLoader contentLoader) throws ServiceLayerException;
}
