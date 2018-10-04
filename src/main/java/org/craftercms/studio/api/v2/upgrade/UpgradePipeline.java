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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v2.exception.UpgradeException;

/**
 * Groups any number of {@link UpgradeOperation} instances.
 * @author joseross
 */
public interface UpgradePipeline {

    /**
     * Executes each {@link UpgradeOperation} for the given site.
     * @param site the name of the site
     * @throws UpgradeException if any of the {@link UpgradeOperation}s fails
     */
    void execute(String site) throws UpgradeException;

    /**
     * Executes each {@link UpgradeOperation} for the global repository.
     * @throws UpgradeException if any of the operations fails
     */
    default void execute() throws UpgradeException {
        execute(StringUtils.EMPTY);
    }

    /**
     * Indicates if the pipeline doesn't contain any operations.
     * @return true if there are no operations
     */
    boolean isEmpty();

}
