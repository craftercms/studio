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
package org.craftercms.studio.api.v2.service.content;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.model.contentType.ContentTypeUsage;

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
     * @param siteId the id of the site
     * @param contentType the id of the content-type
     * @return the usage
     * @throws ServiceLayerException if there is any error finding the items
     */
    ContentTypeUsage getContentTypeUsage(String siteId, String contentType) throws ServiceLayerException;

    /**
     * Deletes all files related to a given content-type
     *
     * @param siteId the id of the site
     * @param contentType the id of the content-type
     * @param deleteDependencies indicates if all dependencies should be deleted
     * @throws ServiceLayerException if there is any error deleting the files
     * @throws AuthenticationException if there is any error authenticating the user
     * @throws DeploymentException if there is any error publishing the changes
     */
    void deleteContentType(String siteId, String contentType, boolean deleteDependencies)
            throws ServiceLayerException, AuthenticationException, DeploymentException, UserNotFoundException;

}
