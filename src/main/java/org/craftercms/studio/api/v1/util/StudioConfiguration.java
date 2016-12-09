/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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

package org.craftercms.studio.api.v1.util;

public interface StudioConfiguration {
    String STUDIO_CONFIG_OVERRIDE_CONFIG = "studio.config.override.config";
    String REPO_BASE_PATH = "studio.repo.basePath";
    String GLOBAL_REPO_PATH = "studio.repo.globalRepoPath";
    String SITES_REPOS_PATH = "studio.repo.sitesRepoBasePath";
    String SANDBOX_PATH = "studio.repo.siteSandboxPath";
    String PUBLISHED_PATH = "studio.repo.sitePublishedPath";
    String BLUE_PRINTS_PATH = "studio.repo.blueprintsPath";
    String BOOTSTRAP_REPO = "studio.repo.bootstrapRepo";

    void loadConfig();

    String getProperty(String key);
}
