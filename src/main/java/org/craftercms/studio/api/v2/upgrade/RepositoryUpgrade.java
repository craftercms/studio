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

/**
 * Defines basic operations to check and upgrade the content repository.
 * @author joseross
 */
public interface RepositoryUpgrade {

    /**
     * Checks if an upgrade is needed in the global repository.
     * @param currentVersion version from which to upgrade
     * @return true if an upgrade should be executed
     */
    default boolean shouldUpgradeGlobalRepo(String currentVersion) {
        return false;
    }

    /**
     * Performs an upgrade on the global repository,
     * @param currentVersion version from which to upgrade
     */
    default void upgradeGlobalRepo(String currentVersion) {
        // Defaults to nothing
    }

    /**
     * Utility method to check the global repository and upgrade if needed.
     * @param currentVersion version from which to upgrade
     */
    default void checkGlobalRepo(String currentVersion) {
        if(shouldUpgradeGlobalRepo(currentVersion)) {
            upgradeGlobalRepo(currentVersion);
        }
    }

    /**
     * Checks if an upgrade is needed in the repository of a given site.
     * @param site the name of the site
     * @param currentVersion version from which to upgrade
     * @return
     */
    default boolean shouldUpgradeRepo(String site, String currentVersion) {
        return false;
    }

    /**
     * Performs an upgrade on the repository of a given site.
     * @param site the name of the site
     * @param currentVersion version from which to upgrade
     */
    default void upgradeRepo(String site, String currentVersion) {
        // Defaults to nothing
    }

    /**
     * Utility method to check and upgrade the repository of a given site.
     * @param site the name of the site
     * @param currentVersion version from which to upgrade
     */
    default void checkRepo(String site, String currentVersion) {
        if(shouldUpgradeRepo(site, currentVersion)) {
            upgradeRepo(site, currentVersion);
        }
    }

}
