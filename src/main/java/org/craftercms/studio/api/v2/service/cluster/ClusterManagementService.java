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

package org.craftercms.studio.api.v2.service.cluster;

import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.dal.ClusterMember;

import java.util.List;

public interface ClusterManagementService {

    /**
     * Get all cluster members
     *
     * @return List of cluster members
     */
    List<ClusterMember> getAllMembers();


    /**
     * Remove member from cluster
     *
     * @param memberIds List of ids of member to remover from cluster
     * @return True if success, otherwise false
     *
     * @throws SiteNotFoundException site not found
     */
    boolean removeMembers(List<Long> memberIds) throws SiteNotFoundException;
}
