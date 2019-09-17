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

package org.craftercms.studio.impl.v2.service.cluster.internal;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.service.cluster.internal.ClusterManagementServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v2.dal.ClusterMember.State.INACTIVE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_INACTIVE_STATE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_MEMBER_IDS;

public class ClusterManagementServiceInternalImpl implements ClusterManagementServiceInternal {

    private ClusterDAO clusterDao;
    private StudioConfiguration studioConfiguration;

    @Override
    public List<ClusterMember> getAllMembers() {
        return clusterDao.getAllMembers();
    }

    @Override
    public boolean removeMembers(List<Long> memberIds) {
        if (CollectionUtils.isNotEmpty(memberIds)) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(CLUSTER_MEMBER_IDS, memberIds);
            params.put(CLUSTER_INACTIVE_STATE, INACTIVE);
            int result = clusterDao.removeMembers(params);
            return result > 0;
        } else {
            return true;
        }
    }

    public ClusterDAO getClusterDao() {
        return clusterDao;
    }

    public void setClusterDao(ClusterDAO clusterDao) {
        this.clusterDao = clusterDao;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }
}
