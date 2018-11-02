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

package org.craftercms.studio.api.v2.service.cluster;

import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.exception.ClusterMemberAlreadyExistsException;
import org.craftercms.studio.api.v2.exception.ClusterMemberNotFoundException;

import java.util.List;

public interface ClusterManagementService {

    /**
     * Get all cluster members
     *
     * @return List of cluster members
     */
    List<ClusterMember> getAllMemebers();

    /**
     * Update given cluster member
     *
     * @param member Cluster member to update
     * @return Updated cluster member
     */
    ClusterMember updateMember(ClusterMember member) throws ClusterMemberNotFoundException;

    /**
     * Add new member to the cluster
     *
     * @param member Member to add to the cluster
     * @return Cluster member
     */
    ClusterMember addMember(ClusterMember member) throws ClusterMemberAlreadyExistsException;

    /**
     * Remove member from cluster
     *
     * @param memberIds List of ids of member to remover from cluster
     * @return True if success, otherwise false
     */
    boolean removeMembers(List<Long> memberIds);
}
