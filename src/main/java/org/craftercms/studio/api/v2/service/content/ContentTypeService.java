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
package org.craftercms.studio.api.v2.service.content;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.QuickCreateItem;
import org.craftercms.studio.model.contentType.ContentType;
import org.craftercms.studio.model.contentType.ContentTypeUsage;
import org.springframework.core.io.Resource;

import java.util.Collection;
import java.util.List;

/**
 * Defines all operations related to content-types
 *
 * @author joseross
 * @since 4.0
 */
public interface ContentTypeService {

	/**
	 * Finds all items related to a given content-type
	 *
	 * @param siteId      the id of the site
	 * @param contentType the id of the content-type
	 * @return the usage
	 * @throws ServiceLayerException if there is any error finding the items
	 */
	ContentTypeUsage getContentTypeUsage(String siteId, String contentType) throws ServiceLayerException;

	/**
	 * Finds the preview image for a given content-type
	 *
	 * @param siteId        the id of the site
	 * @param contentTypeId the id of the content-type
	 * @return the preview image file as a pair of path and resource
	 * @throws ServiceLayerException if there is any error finding the items
	 */
	ImmutablePair<String, Resource> getContentTypePreviewImage(String siteId, String contentTypeId) throws ServiceLayerException;

	/**
	 * Deletes all files related to a given content-type
	 *
	 * @param siteId             the id of the site
	 * @param contentType        the id of the content-type
	 * @param deleteDependencies indicates if all dependencies should be deleted
	 * @throws ServiceLayerException   if there is any error deleting the files
	 * @throws AuthenticationException if there is any error authenticating the user
	 */
	void deleteContentType(String siteId, String contentType, boolean deleteDependencies)
		throws ServiceLayerException, AuthenticationException, UserNotFoundException;

	/**
	 * Get the form-controller.js file for a given content-type
	 *
	 * @param siteId        the id of the site
	 * @param contentTypeId the id of the content-type
	 * @return the form-controller.js file as a pair of path and resource, if exists
	 * @throws org.craftercms.studio.api.v1.exception.ContentNotFoundException if the form-controller.js file does not exist for the given content type
	 */
	ImmutablePair<String, Resource> getContentTypeFormController(String siteId, String contentTypeId) throws ServiceLayerException;

	/**
	 * Builds the path of the Groovy controller for a given content type id
	 *
	 * @param contentTypeId the id of the content type
	 * @return the path of the controller or null
	 */
	String getContentTypeControllerPath(String contentTypeId);

	/**
	 * Extracts the path of the Freemarker template for a given content type id
	 *
	 * @param siteId        the id of the site
	 * @param contentTypeId the id of the content type
	 * @return the path of the template or null
	 * @throws ServiceLayerException if there is any error reading the content type definition
	 */
	String getContentTypeTemplatePath(String siteId, String contentTypeId) throws ServiceLayerException;

	/**
	 * Get all content types for the given site.
	 *
	 * @param siteId the id of the site
	 * @return a collection of content types including their config files (config.xml, form-definition.xml)
	 * @throws ServiceLayerException if there is any error getting the content types
	 */
	Collection<String> getAllModelDefinitions(String siteId) throws ServiceLayerException;

	/**
	 * Get list of content types marked as quick creatable for given site
	 *
	 * @param siteId site identifier
	 * @return List of quick creatable content types
	 * @throws ServiceLayerException if there is any error getting the content types
	 */
	List<QuickCreateItem> getQuickCreatableContentTypes(String siteId) throws ServiceLayerException;

	/**
	 * Get all content types for the given site.
	 * @param siteId the id of the site
	 * @return a collection of content types
	 * @throws SiteNotFoundException if the site with the given id does not exist
	 */
	Collection<ContentType> getAllContentTypes(String siteId) throws ServiceLayerException;

	/**
	 * Get the content type configuration for a given content type id
	 *
	 * @param siteId        the id of the site
	 * @param contentTypeId the id of the content type
	 * @return the content type configuration
	 */
	ContentType getContentType(String siteId, String contentTypeId) throws ServiceLayerException;

	/**
	 * Get a collection of the ids of the content types allowed for the given site and path
	 *
	 * @param siteId the id of the site
	 * @param path   the path of the content item to be created
	 * @return a collection of the ids of the content types allowed for the given site and path
	 */
	Collection<String> getAllowedContentTypes(String siteId, String path) throws ServiceLayerException;

	/**
	 * Checks if a given content type is allowed for the given site and path
	 *
	 * @param siteId        the id of the site
	 * @param path          the path of the content item to be created
	 * @param contentTypeId the id of the content type to check
	 * @return true if the content type is allowed for the given site and path, false otherwise
	 * @throws ServiceLayerException if there is any error checking if the content type is allowed
	 */
	boolean isContentTypeAllowed(String siteId, String path, String contentTypeId) throws ServiceLayerException;
}
