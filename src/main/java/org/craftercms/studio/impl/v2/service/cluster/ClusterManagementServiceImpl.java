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

package org.craftercms.studio.impl.v2.service.cluster;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.exception.ClusterMemberAlreadyExistsException;
import org.craftercms.studio.api.v2.exception.ClusterMemberNotFoundException;
import org.craftercms.studio.api.v2.service.cluster.ClusterManagementService;
import org.craftercms.studio.api.v2.service.cluster.internal.ClusterManagementServiceInternal;

import java.util.List;

public class ClusterManagementServiceImpl implements ClusterManagementService {

    private ClusterManagementServiceInternal clusterManagementServiceInternal;

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_cluster")
    public List<ClusterMember> getAllMemebers() {
        return clusterManagementServiceInternal.getAllMembers();
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "update_cluster")
    public ClusterMember updateMember(ClusterMember member) throws ClusterMemberNotFoundException {
        if (clusterManagementServiceInternal.getMember(member.getId()) == null) {
            throw new ClusterMemberNotFoundException();
        }
        boolean result = clusterManagementServiceInternal.updateMember(member);
        if (result) {
            return clusterManagementServiceInternal.getMember(member.getId());
        }
        return null;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "create_cluster")
    public ClusterMember addMember(ClusterMember member) throws ClusterMemberAlreadyExistsException {
        if (clusterManagementServiceInternal.memberExists(member.getGitUrl())) {
            throw new ClusterMemberAlreadyExistsException();
        }
        boolean result = clusterManagementServiceInternal.addMember(member);
        if (result) {
            return member;
        }
        return null;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "delete_cluster")
    public boolean removeMembers(List<Long> memberIds) {
        return clusterManagementServiceInternal.removeMembers(memberIds);
    }

    public ClusterManagementServiceInternal getClusterManagementServiceInternal() {
        return clusterManagementServiceInternal;
    }

    public void setClusterManagementServiceInternal(ClusterManagementServiceInternal clusterManagementServiceInternal) {
        this.clusterManagementServiceInternal = clusterManagementServiceInternal;
    }
}
