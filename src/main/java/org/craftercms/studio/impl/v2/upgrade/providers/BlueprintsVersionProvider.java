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
package org.craftercms.studio.impl.v2.upgrade.providers;

import org.craftercms.commons.upgrade.impl.UpgradeContext;
import org.craftercms.commons.upgrade.impl.providers.AbstractVersionProvider;

import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.VERSION_3_0_0;

/**
 * Implementation of {@link org.craftercms.commons.upgrade.VersionProvider} for the built-in blueprints.
 *
 * @author jose ross
 * @since 4.0.0
 */
public class BlueprintsVersionProvider extends AbstractVersionProvider<String> {

    @Override
    protected String doGetVersion(UpgradeContext<String> context) throws Exception {
        // The version is fixed for now so bp are always updated, in the future this should be replaced with
        // a proper implementation
        return VERSION_3_0_0;
    }

    @Override
    protected void doSetVersion(UpgradeContext<String> context, String newVersion) throws Exception {
        // do nothing for now
    }

}
