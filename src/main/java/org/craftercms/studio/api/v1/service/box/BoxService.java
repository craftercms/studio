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

package org.craftercms.studio.api.v1.service.box;

import org.craftercms.studio.api.v1.exception.BoxException;

/**
 * Defines the operations available for handling files in Box
 */
public interface BoxService {

    /**
     * Gets an access token to allow direct access to the Box folder.
     * @param site the name of the site to search for the configuration file
     * @param profileId the name of the profile to search
     * @return the value of the access token
     * @throws BoxException
     */
    String getAccessToken(String site, String profileId) throws BoxException;

    /**
     * Builds a local URL for the given asset
     * @param profileId the name of the profile to use
     * @param fileId the id of the file
     * @param filename the name of the file
     * @return the local URL for the file
     */
    String getUrl(String site, String profileId, String fileId, String filename) throws BoxException;

}
