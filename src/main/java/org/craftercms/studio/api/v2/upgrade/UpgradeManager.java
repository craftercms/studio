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

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.exception.UpgradeNotSupportedException;

/**
 * Manages the current version and applies the required upgrades.
 * @author joseross
 */
public interface UpgradeManager {

    /**
     * Provides a {@link UpgradePipeline} instance with all upgrades needed for the given version.
     * @param currentVersion current version of the system
     * @return the upgrade pipeline
     * @throws UpgradeException if there is an error configuring the pipeline
     */
    UpgradePipeline buildUpgradePipeline(String currentVersion) throws UpgradeException;

    /**
     * Reads the configuration file that defines the upgrade pipeline.
     * @return the configuration object
     * @throws UpgradeException if there is an error reading the configuration file
     */
    HierarchicalConfiguration loadUpgradeConfiguration() throws UpgradeException;


    /**
     * Provides an {@link UpgradeContext} instance for the given version.
     * @param currentVersion current version of the system
     * @return the upgrade context object
     */
    UpgradeContext buildUpgradeContext(String currentVersion);

    /**
     * Executes all required upgrades for the given version.
     * @param currentVersion current version of the system
     * @throws UpgradeException if any of the upgrades fails
     */
    void upgrade(String currentVersion) throws UpgradeException;

    /**
     * Provides the current version of the system.
     * @return the version number
     * @throws UpgradeNotSupportedException if the current version of the system is not suitable for upgrading
     */
    String getCurrentVersion() throws UpgradeNotSupportedException;

}
