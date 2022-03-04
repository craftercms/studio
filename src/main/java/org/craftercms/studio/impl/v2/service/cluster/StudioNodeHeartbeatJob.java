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

package org.craftercms.studio.impl.v2.service.cluster;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v2.utils.spring.context.SystemStatusProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CLUSTER_MEMBER_LOCAL_ADDRESS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_LOCAL_ADDRESS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_STATE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CLUSTERING_NODE_REGISTRATION;

public class StudioNodeHeartbeatJob implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(StudioNodeHeartbeatJob.class);

    private StudioConfiguration studioConfiguration;
    private ClusterDAO clusterDAO;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;
    private SystemStatusProvider systemStatusProvider;


    private final static ReentrantLock singleWorkerLock = new ReentrantLock();

    @Override
    public void run() {
        if (systemStatusProvider.isSystemReady()) {
            if (singleWorkerLock.tryLock()) {
                try {
                    updateHeartbeat();
                } catch (Exception error) {
                    logger.error("Error during execution of node heartbeat job", error);
                } finally {
                    singleWorkerLock.unlock();
                }
            } else {
                logger.debug("Another worker is updating heartbeat. Skipping cycle.");
            }
        } else {
            logger.debug("System not ready yet. Skipping cycle");
        }
    }

    public void updateHeartbeat() {
        HierarchicalConfiguration<ImmutableNode> registrationData = getConfiguration();
        if (registrationData != null && !registrationData.isEmpty()) {
            String localAddress = registrationData.getString(CLUSTER_MEMBER_LOCAL_ADDRESS);
            Map<String, String> params = new HashMap<String, String>();
            params.put(CLUSTER_LOCAL_ADDRESS, localAddress);
            params.put(CLUSTER_STATE, ClusterMember.State.ACTIVE.toString());
            logger.debug("Update heartbeat for cluster member with local address: " + localAddress);
            retryingDatabaseOperationFacade.updateClusterNodeHeartbeat(params);
        }
    }

    private HierarchicalConfiguration<ImmutableNode> getConfiguration() {
        return studioConfiguration.getSubConfig(CLUSTERING_NODE_REGISTRATION);
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

    public RetryingDatabaseOperationFacade getRetryingDatabaseOperationFacade() {
        return retryingDatabaseOperationFacade;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }

    public void setSystemStatusProvider(SystemStatusProvider systemStatusProvider) {
        this.systemStatusProvider = systemStatusProvider;
    }

}
