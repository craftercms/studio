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

import org.craftercms.studio.api.v2.exception.UpgradeException;

/**
 * Builds an {@link UpgradePipeline} with all the required operations.
 * @author joseross
 */
public interface UpgradePipelineFactory {

    /**
     * Retrieves the needed upgrade operations based on the given version.
     * @param versionProvider
     * @return the upgrade pipeline
     * @throws UpgradeException if there is any error retrieving the operations
     */
    UpgradePipeline getPipeline(VersionProvider versionProvider) throws UpgradeException;

}
