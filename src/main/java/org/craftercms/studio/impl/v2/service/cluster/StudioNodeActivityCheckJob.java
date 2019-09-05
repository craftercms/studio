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

package org.craftercms.studio.impl.v2.service.cluster;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static org.craftercms.studio.api.v2.dal.ClusterMember.State.INACTIVE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_HEARTBEAT_STALE_LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_INACTIVE_STATE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_INACTIVITY_LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_MEMBER_IDS;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CLUSTERING_HEARTBEAT_STALE_TIME_LIMIT;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CLUSTERING_INACTIVITY_TIME_LIMIT;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CLUSTERING_NODE_REGISTRATION;

public class StudioNodeActivityCheckJob implements Runnable{

    private final static Logger logger = LoggerFactory.getLogger(StudioNodeActivityCheckJob.class);

    private StudioConfiguration studioConfiguration;
    private ClusterDAO clusterDao;

    private final static ReentrantLock singleWorkerLock = new ReentrantLock();

    @Override
    public void run() {
        if (singleWorkerLock.tryLock()) {
            try {
                List<ClusterMember> staleMembers = getMembersWithStaleHeartbeat();
                if (CollectionUtils.isNotEmpty(staleMembers)) {
                    setStaleMembersInactive(staleMembers);
                }

                List<ClusterMember> inactiveMembersToRemove = getInactiveMembersForRemoval();
                if (CollectionUtils.isNotEmpty(inactiveMembersToRemove)) {
                    removeInactiveMembers(inactiveMembersToRemove);
                }
            } finally {
                singleWorkerLock.unlock();
            }
        } else {
            logger.debug("Another worker is checking cluster nodes activity. Skipping cycle.");
        }
    }

    private void setStaleMembersInactive(List<ClusterMember> staleMembers) {
        staleMembers.forEach(member -> {
            member.setState(INACTIVE);
            clusterDao.updateMember(member);
        });
    }

    private List<ClusterMember> getMembersWithStaleHeartbeat() {
        HierarchicalConfiguration<ImmutableNode> registrationData = getConfiguration();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CLUSTER_HEARTBEAT_STALE_LIMIT, getHeartbeatStalePeriod());
        return clusterDao.getMembersWithStaleHeartbeat(params);
    }

    private List<ClusterMember> getInactiveMembersForRemoval() {
        HierarchicalConfiguration<ImmutableNode> registrationData = getConfiguration();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(CLUSTER_INACTIVITY_LIMIT, getInactivityPeriod() + getHeartbeatStalePeriod());
        params.put(CLUSTER_INACTIVE_STATE, INACTIVE);
        return clusterDao.getInactiveMembersWithStaleHeartbeat(params);
    }

    private void removeInactiveMembers(List<ClusterMember> inactiveMembersToRemove) {
        List<Long> idsToRemove = inactiveMembersToRemove.stream()
                .map(ClusterMember::getId)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(idsToRemove)) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(CLUSTER_MEMBER_IDS, idsToRemove);
            params.put(CLUSTER_INACTIVE_STATE, INACTIVE);
            int result = clusterDao.removeMembers(params);
        }
    }

    private HierarchicalConfiguration<ImmutableNode> getConfiguration() {
        return studioConfiguration.getSubConfig(CLUSTERING_NODE_REGISTRATION);
    }

    private int getInactivityPeriod() {
        return Integer.parseInt(studioConfiguration.getProperty(CLUSTERING_INACTIVITY_TIME_LIMIT));
    }
    private int getHeartbeatStalePeriod() {
        return Integer.parseInt(studioConfiguration.getProperty(CLUSTERING_HEARTBEAT_STALE_TIME_LIMIT));
    }


    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public ClusterDAO getClusterDao() {
        return clusterDao;
    }

    public void setClusterDao(ClusterDAO clusterDAO) {
        this.clusterDao = clusterDAO;
    }
}
