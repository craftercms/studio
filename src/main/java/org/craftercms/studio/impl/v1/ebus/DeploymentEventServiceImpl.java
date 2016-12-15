/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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

package org.craftercms.studio.impl.v1.ebus;

import org.craftercms.studio.api.v1.ebus.DeploymentEventListener;
import org.craftercms.studio.api.v1.ebus.DeploymentEventMessage;
import org.craftercms.studio.api.v1.ebus.DeploymentEventService;
import org.craftercms.studio.api.v1.ebus.RepositoryEventContext;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.jgroups.JChannel;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;

import java.util.ArrayList;
import java.util.List;

public class DeploymentEventServiceImpl implements DeploymentEventService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentEventServiceImpl.class);

    public DeploymentEventServiceImpl() throws Exception {
        JChannel channel = new JChannel();
        rpcDispatcher = new RpcDispatcher(channel, this);
        channel.connect("StudioCluster");
    }

    @Override
    public void deploymentEvent(DeploymentEventMessage message) {

        try {
            MethodCall call = new MethodCall(getClass().getMethod("onDeploymentEvent", DeploymentEventMessage.class));
            call.setArgs(message);
            rpcDispatcher.callRemoteMethods(null, call, RequestOptions.ASYNC());
        } catch (NoSuchMethodException e) {
            logger.error("Error invoking deployment event", e);
        } catch (Exception e) {
            logger.error("Error invoking deployment event", e);
        }
    }

    @Override
    public void onDeploymentEvent(DeploymentEventMessage message) {
        RepositoryEventContext context = message.getRepositoryEventContext();
        RepositoryEventContext.setCurrent(context);
        for (DeploymentEventListener listener :
                listeners) {
            listener.onDeploymentEvent(message);
        }
        RepositoryEventContext.setCurrent(null);
    }

    @Override
    public void subscribe(DeploymentEventListener listener) {
        listeners.add(listener);
    }

    public RpcDispatcher getRpcDispatcher() { return rpcDispatcher; }
    public void setRpcDispatcher(RpcDispatcher rpcDispatcher) { this.rpcDispatcher = rpcDispatcher; }

    protected RpcDispatcher rpcDispatcher;
    protected List<DeploymentEventListener> listeners = new ArrayList<DeploymentEventListener>();
}
