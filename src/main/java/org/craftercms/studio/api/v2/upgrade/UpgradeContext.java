/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */

package org.craftercms.studio.api.v2.upgrade;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.springframework.core.io.Resource;

/**
 * Provides access to the system components that support upgrades.
 * @author joseross
 */
public interface UpgradeContext {

    /**
     * Get the current version of the system.
     * @return version number
     */
    String getCurrentVersion();

    /**
     * Get the version to which the system will be upgraded.
     * @return version number
     */
    String getTargetVersion();

    /**
     * Returns a connection to the database.
     * @return the connection
     * @throws SQLException if there is an error opening the connection
     */
    Connection getConnection() throws SQLException;

    /**
     * Get the current instance of {@link ContentRepository}.
     * @return the content repository
     */
    ContentRepository getContentRepository();

    /**
     * Get a list of all existing sites.
     * @return list of names
     */
    List<String> getSites();

    /**
     * Get a value from the configuration file.
     * @param key property key
     * @return configuration value
     */
    String getProperty(String key);

    /**
     * Get a file from the servlet context.
     * @param path relative path of the file
     * @return the file
     */
    Resource getServletResource(String path);

    /**
     * Performs a write and commits the changes (if any) to the content repository.
     * @param site site id
     * @param path path of the file
     * @param content stream providing the new content
     * @param message commit message
     */
    void writeToRepo(String site, String path, InputStream content, String message);

}
