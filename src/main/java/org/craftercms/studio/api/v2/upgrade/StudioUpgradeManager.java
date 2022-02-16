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

package org.craftercms.studio.api.v2.upgrade;

import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.upgrade.UpgradeManager;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;

import java.util.List;

/**
 * Extension of {@link UpgradeManager} that adds Studio specific operations.
 * @author joseross
 * @since 3.1.0
 */
public interface StudioUpgradeManager extends UpgradeManager<String> {

    /**
     * Executes all required upgrades for the system.
     * @throws UpgradeException if any of the upgrades fails
     */
    void upgradeDatabaseAndConfiguration() throws UpgradeException, ConfigurationException;

    /**
     * Executes the upgrades for all managed configurations in the given site.
     * @param context the context for the upgrades
     * @throws UpgradeException if any of the upgrades fails
     */
    void upgradeSiteConfiguration(StudioUpgradeContext context) throws UpgradeException;

    /**
     * Executes the upgrades for all existing sites.
     * @throws UpgradeException if any of the upgrades fails
     */
    void upgradeExistingSites() throws UpgradeException;

    /**
     * Executes all required upgrades for the blueprints.
     * @throws UpgradeException if any of the upgrades fails
     */
    void upgradeBlueprints() throws UpgradeException, ConfigurationException;

    /**
     * Returns all existing environments for the given site
     * @param site the id of the site
     * @return the list of environments
     */
    List<String> getExistingEnvironments(String site);

}
