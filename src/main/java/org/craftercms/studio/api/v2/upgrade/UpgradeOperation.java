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

package org.craftercms.studio.api.v2.upgrade;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.craftercms.studio.api.v2.exception.UpgradeException;

/**
 * Defines the basic operations for a single upgrade.
 * @author joseross
 */
public interface UpgradeOperation {

    /**
     * Initializes the instance with the given configuration.
     * @param sourceVersion the starting version
     * @param targetVersion the target version
     * @param config operation configuration
     */
    void init(String sourceVersion, String targetVersion, HierarchicalConfiguration<ImmutableNode> config);

    /**
     * Performs a single upgrade operation.
     * @param site the name of the site
     * @throws UpgradeException if there is any error performing the upgrade
     */
    void execute(String site) throws UpgradeException;

}
