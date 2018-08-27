/*
 * Copyright (C) 2007-2018 Crafter Software Corporation.
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

package org.craftercms.studio.api.v1.webdav;

import org.craftercms.studio.api.v1.exception.WebDavException;

/**
 * Reads the configuration file and loads instances of {@link WebDavProfile}.
 * @author joseross
 */
public interface WebDavProfileReader {

    /**
     * Parses the configuration file for the specified profile.
     * @param site the name of the site
     * @param profileId the id of the profile
     * @return the {@link WebDavProfile} object
     * @throws WebDavException if there is any error loading the profile
     */
    WebDavProfile getProfile(String site, String profileId) throws WebDavException;

}
