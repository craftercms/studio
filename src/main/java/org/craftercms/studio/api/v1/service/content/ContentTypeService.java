/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
     * @param site
     * @param path
     * @return content type
     */
    ContentTypeConfigTO getContentTypeForContent(String site, String path) throws ServiceLayerException;

    /**
     * check if the user is allowed to access the content type with the given user roles
     *
     * @param userRoles
     * @param item
     * @return true if user has permissions to access the content type
     */
    boolean isUserAllowed(Set<String> userRoles, ContentTypeConfigTO item);

    /**
     * get a content type by the given site and type name
     *
     * @param site
     * @param type
     * @return content type
     */
    ContentTypeConfigTO getContentType(String site, String type);

    ContentTypeConfigTO getContentTypeByRelativePath(String site, String relativePath) throws ServiceLayerException;

    List<ContentTypeConfigTO> getAllContentTypes(String site, boolean searchable);

    List<ContentTypeConfigTO> getAllowedContentTypesForPath(String site, String relativePath)
        throws ServiceLayerException;

    boolean changeContentType(String site, String path, String contentType) throws ServiceLayerException;

    void reloadConfiguration(String site);

    String getConfigPath();
}

