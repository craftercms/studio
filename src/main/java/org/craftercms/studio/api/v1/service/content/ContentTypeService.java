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

package org.craftercms.studio.api.v1.service.content;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;

import java.util.List;
import java.util.Set;

/**
 * @author Dejan Brkic
 */
public interface ContentTypeService {

    /**
     * get a content type by the given site and the content path
     *
     * @param site site identifier
     * @param path path of the content
     * @return content type
     *
     * @throws ServiceLayerException general service error
     */
    ContentTypeConfigTO getContentTypeForContent(String site, String path) throws ServiceLayerException;

    /**
     * check if the user is allowed to access the content type with the given user roles
     *
     * @param userRoles user roles
     * @param item content type
     * @return true if user has permissions to access the content type
     */
    boolean isUserAllowed(Set<String> userRoles, ContentTypeConfigTO item);

    /**
     * get a content type by the given site and type name
     *
     * @param site site identifier
     * @param type content type name
     * @return content type
     */
    ContentTypeConfigTO getContentType(String site, String type);

    ContentTypeConfigTO getContentTypeByRelativePath(String site, String relativePath) throws ServiceLayerException;

    List<ContentTypeConfigTO> getAllContentTypes(String site, boolean searchable);

    List<ContentTypeConfigTO> getAllowedContentTypesForPath(String site, String relativePath);

    boolean changeContentType(String site, String path, String contentType) throws ServiceLayerException, UserNotFoundException;

    String getConfigPath();
}

