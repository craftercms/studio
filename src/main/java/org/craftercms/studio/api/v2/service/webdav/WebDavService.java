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

package org.craftercms.studio.api.v2.service.webdav;

import java.io.InputStream;
import java.util.List;

import org.craftercms.commons.config.profiles.ConfigurationProfileNotFoundException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.WebDavException;
import org.craftercms.studio.api.v1.webdav.WebDavItem;

/**
 * Defines the operations available for a WebDAV server.
 * @author joseross
 * @since 3.1.4
 */
public interface WebDavService {

    /**
     * Lists resources in the specified path.
     * @param siteId the id of the site
     * @param profileId the id of the profile
     * @param path the relative path to list
     * @param type mime type used for filtering
     * @return list of resources found
     * @throws WebDavException if there is an error connecting to the server or listing the resources
     * @throws ConfigurationProfileNotFoundException if the profile is not found
     */
    List<WebDavItem> list(String siteId, String profileId, String path, String type) throws WebDavException, SiteNotFoundException, ConfigurationProfileNotFoundException;

    /**
     * Uploads a file in the specified path.
     * @param siteId the id of the site
     * @param profileId the id of the profile
     * @param path the relative path to upload the file
     * @param filename the name of the file to upload
     * @param content stream providing the content of the file
     * @return the uploaded item
     * @throws WebDavException if there is an error connecting to the server or uploading the file
     * @throws ConfigurationProfileNotFoundException if the profile is not found
     */
    WebDavItem upload(String siteId, String profileId, String path, String filename, InputStream content) throws
            WebDavException, SiteNotFoundException, ConfigurationProfileNotFoundException;

}
