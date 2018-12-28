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

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CLUSTER_MEMBER_LOCAL_ADDRESS;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CLUSTERING_INACTIVITY_CHECK_TIME_LIMIT;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CLUSTERING_NODE_REGISTRATION;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_LOCAL_ADDRESS;

public class StudioNodeActivityCheckJob implements Runnable{

    private final static Logger logger = LoggerFactory.getLogger(StudioNodeActivityCheckJob.class);

    private StudioConfiguration studioConfiguration;
    private ClusterDAO clusterDAO;

    private final static ReentrantLock singleWorkerLock = new ReentrantLock();

    @Override
    public void run() {
        if (singleWorkerLock.tryLock()) {
            try {
                List<ClusterMember> inactiveMembers = getInactiveMembers();
                updateInactiveMembersState(inactiveMembers);
            } finally {
                singleWorkerLock.unlock();
            }
        } else {
            logger.debug("Another worker is checking cluster nodes activity. Skipping cycle.");
        }
    }

    private List<ClusterMember> getInactiveMembers() {
        HierarchicalConfiguration<ImmutableNode> registrationData = getConfiguration();
        long millis = TimeUnit.MINUTES.toMillis(getInactivityPeriod());
        String localAddress = registrationData.getString(CLUSTER_MEMBER_LOCAL_ADDRESS);
        Map<String, String> params = new HashMap<String, String>();
        params.put(CLUSTER_LOCAL_ADDRESS, localAddress);
        logger.debug("Update heartbeat for cluster member with local address: " + localAddress);
        clusterDAO.updateHeartbeat(params);
    }

    private HierarchicalConfiguration<ImmutableNode> getConfiguration() {
        return studioConfiguration.getSubConfig(CLUSTERING_NODE_REGISTRATION);
    }

    private int getInactivityPeriod() {
        return Integer.parseInt(studioConfiguration.getProperty(CLUSTERING_INACTIVITY_CHECK_TIME_LIMIT));
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public ClusterDAO getClusterDAO() {
        return clusterDAO;
    }

    public void setClusterDAO(ClusterDAO clusterDAO) {
        this.clusterDAO = clusterDAO;
    }
}
