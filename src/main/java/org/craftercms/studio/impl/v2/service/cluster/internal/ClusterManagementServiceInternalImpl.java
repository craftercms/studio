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

package org.craftercms.studio.impl.v2.service.cluster.internal;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
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
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

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
            int result = retryingDatabaseOperationFacade.removeClusterMembers(params);
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

    public RetryingDatabaseOperationFacade getRetryingDatabaseOperationFacade() {
        return retryingDatabaseOperationFacade;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }
}
