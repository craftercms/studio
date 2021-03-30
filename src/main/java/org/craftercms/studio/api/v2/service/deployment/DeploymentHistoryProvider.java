/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.service.deployment;

import org.craftercms.studio.api.v1.dal.DeploymentSyncHistory;
import org.craftercms.studio.api.v1.util.filter.DmFilterWrapper;

import java.time.ZonedDateTime;
import java.util.List;

public interface DeploymentHistoryProvider {

    /**
     * Get deployment history for given site
     *
     * @param site site id
     * @param environmentNames list of environment names
     * @param fromDate date from
     * @param toDate date to
     * @param dmFilterWrapper filter wrapper
     * @param filterType filter items by type
     * @param numberOfItems number of items in result set
     * @return deployment history
     */
    List<DeploymentSyncHistory> getDeploymentHistory(String site, List<String> environmentNames, ZonedDateTime fromDate,
                                                     ZonedDateTime toDate, DmFilterWrapper dmFilterWrapper,
                                                     String filterType, int numberOfItems);
}
