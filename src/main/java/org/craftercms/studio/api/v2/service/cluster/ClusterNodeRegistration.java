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

package org.craftercms.studio.api.v2.service.cluster;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.dal.ClusterMember;

public interface ClusterNodeRegistration {

    /**
     * Check if cluster node is registered
     *
     * @param localIp local IP of cluster node
     *
     * @return true if cluster node is already registered, otherwise false
     */
    boolean isRegistered(String localIp);

    /**
     * Register cluster node
     *
     * @param clusterMember Cluster member data
     *
     * @return true if registration was successful
     *
     * @throws ServiceLayerException
     */
    boolean registerClusterNode(ClusterMember clusterMember) throws ServiceLayerException;

    /**
     * Remove node from cluster
     *
     * @param localIp local IP address of node to be removed
     *
     * @return true if node was removed, otherwise false
     */
    boolean removeClusterNode(String localIp);
}
