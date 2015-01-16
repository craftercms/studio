/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2014 Crafter Software Corporation.
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

import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;

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
    ContentTypeConfigTO getContentTypeForContent(String site, String path) throws ServiceException;

    /**
     * check if the user is allowed to access the content type with the given user roles
     *
     * @param userRoles
     * @param item
     * @return
     */
    boolean isUserAllowed(Set<String> userRoles, ContentTypeConfigTO item);

}
