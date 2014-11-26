/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.alfresco.dm.service.api;

import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ContentTypeConfigTO;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Provides wcm conten types related services
 *
 * @author hyanghee
 * @author Dejan Brkic
 *
 */
public interface DmContentTypeService {

    /**
     * merge the content at the given path with the latest template if the version provided is different from the tempalte's version
     *
     * @param path
     * @param contentType
     * @param version
     * 			the version of the current content at the given path
     * @param content
     * 			current content
     * @return merged document as stream
     */
    public InputStream mergeLastestTemplate(String site, String path, String contentType, String version, InputStream content);

    /**
     * change the content type of the content at the given path
     *
     * @param site
     * @param sub
     * @param path
     * @param contentType
     * @throws org.craftercms.cstudio.alfresco.service.exception.ServiceException
     */
    public void changeContentType(String site, String sub, String path, String contentType) throws ServiceException;

    /**
     * return all content types available in the given site
     *
     * @param site
     * @return content types
     */
    public List<ContentTypeConfigTO> getAllContentTypes(String site);

    /**
     * get all content types from search configuration
     *
     * @param site
     * @param user
     * @return all searchable content types
     * @throws ServiceException
     */
    public List<ContentTypeConfigTO> getAllSearchableContentTypes(String site, String user) throws ServiceException;

    /**
     * get allowed content types that can be created at the given path
     *
     * @param site
     * @param relativepath
     * @return allowed content types
     * @throws ServiceException
     */
    public List<ContentTypeConfigTO> getAllowedContentTypes(String site, String relativepath) throws ServiceException;

    /**
     * get a content type by the given site and the content path
     *
     * @param site
     * @param sub
     * @param path
     * @return content type
     */
    public ContentTypeConfigTO getContentTypeByRelativePath(String site, String sub, String path) throws ServiceException;

    /**
     * check if the user is allowed to access the content type with the given user roles
     *
     * @param userRoles
     * @param item
     * @return
     */
    public boolean isUserAllowed(Set<String> userRoles, ContentTypeConfigTO item);

    /**
     * get a content type by the given site and type name
     *
     * @param site
     * @param type
     * @return content type
     */
    public ContentTypeConfigTO getContentType(String site, String type);
}
