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
package org.craftercms.studio.api.v1.service.configuration;

import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;

/**
 * provides content type configuration
 * 
 * @author hyanghee
 *
 */
public interface ContentTypesConfig {

	/**
	 * get content type configuration for the given site and the content type
	 * @param site
	 * @param contentType
	 * @return content type configuration
	 */
	ContentTypeConfigTO getContentTypeConfig(String site, String contentType);

	/**
	 * load configuration from the noderRef given
	 * 
	 * @param site
	 * @param contentType
	 * @return content type configuration
	 */
	ContentTypeConfigTO loadConfiguration(String site, String contentType);

    ContentTypeConfigTO reloadConfiguration(String site, String contentType);

}
