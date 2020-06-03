/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.upgrade;

import org.craftercms.studio.api.v2.exception.UpgradeException;

import java.util.List;

/**
 * Manages the current version and applies the required upgrades.
 * @author joseross
 */
public interface UpgradeManager {

    /**
     * Executes all required upgrades for the system.
     * @throws UpgradeException if any of the upgrades fails
     */
    void upgradeDatabaseAndConfiguration() throws UpgradeException;

    /**
     * Executes all required upgrades for the given site.
     * @param site name of the site
     * @throws UpgradeException if any of the upgrades fails
     */
    void upgradeSite(String site) throws UpgradeException;

    /**
     * Executes the upgrades for all managed configurations in the given site.
     * @param site name of the site
     * @throws UpgradeException if any of the upgrades fails
     */
    void upgradeSiteConfiguration(String site) throws UpgradeException;

    /**
     * Executes the upgrades for all existing sites.
     * @throws UpgradeException if any of the upgrades fails
     */
    void upgradeExistingSites() throws UpgradeException;

    /**
     * Executes all required upgrades for the blueprints.
     * @throws UpgradeException if any of the upgrades fails
     */
    void upgradeBlueprints() throws UpgradeException;

    /**
     * Returns all existing environments for the given site
     * @param site the id of the site
     * @return the list of environments
     */
    List<String> getExistingEnvironments(String site);

}
