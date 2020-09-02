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

package org.craftercms.studio.impl.v2.upgrade.operations.db;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.commons.entitlements.validator.DbIntegrityValidator;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

public class PopulateItemTableUpgradeOperation extends DbScriptUpgradeOperation{

    public static final String CONFIG_KEY_INTEGRITY = "clearExistingData";

    protected boolean clearExistingData;

    public PopulateItemTableUpgradeOperation(StudioConfiguration studioConfiguration, String scriptFolder,
                                             DbIntegrityValidator integrityValidator) {
        super(studioConfiguration, scriptFolder, integrityValidator);
    }

    @Override
    public void doInit(HierarchicalConfiguration config) {
        super.doInit(config);
        clearExistingData
    }
}
