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
import org.craftercms.studio.api.v1.util.StudioConfiguration;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CLUSTER_MEMBER_LOCAL_ADDRESS;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CLUSTERING_NODE_REGISTRATION;

public class StudioNodeHeartbeatJob implements Runnable {

    private StudioConfiguration studioConfiguration;

    @Override
    public void run() {

    }

    private void updateHeartbeat() {
        HierarchicalConfiguration<ImmutableNode> registrationData = getConfiguration();
        String localAddress = registrationData.getString(CLUSTER_MEMBER_LOCAL_ADDRESS);
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
}
