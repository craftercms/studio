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

package org.craftercms.studio.api.v2.service.cluster;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.dal.ClusterMember;

import java.util.List;

public interface ClusterManagementService {

    /**
     * Get all cluster members
     *
     * @return List of cluster members
     */
    List<ClusterMember> getAllMemebers();


    /**
     * Get member by local address
     * @param localAddress
     * @return cluster member
     */
    @HasPermission(type = DefaultPermission.class, action = "read_cluster")
    ClusterMember getMemberByLocalAddress(String localAddress);

    /**
     * Remove member from cluster
     *
     * @param memberIds List of ids of member to remover from cluster
     * @return True if success, otherwise false
     */
    boolean removeMembers(List<Long> memberIds) throws SiteNotFoundException;

    /**
     * Set current member as primary
     * @return True if success, otherwise false
     */
    @HasPermission(type = DefaultPermission.class, action = "update_cluster")
    boolean setClusterPrimary();
}
