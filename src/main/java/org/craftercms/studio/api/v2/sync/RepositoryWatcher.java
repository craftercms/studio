/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.sync;

import org.craftercms.studio.api.v1.exception.SiteNotFoundException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The RepositoryWatcher is responsible for watching the file system for changes
 * to registered sites' repositories.
 */
public interface RepositoryWatcher {

    /**
     * Register a site to be watched.
     * @param siteId The site ID.
     * @param sitePath The path to the site's repository.
     */
    void registerSite(String siteId, Path sitePath) throws SiteNotFoundException, IOException;

    /**
     * De-register a site from being watched.
     * @param siteId The site ID.
     */
    void deregisterSite(String siteId);

    /**
     * Start watching the registered sites.
     * This method is here to allow async processing by Spring.
     */
    void startWatching() throws InterruptedException;

}
