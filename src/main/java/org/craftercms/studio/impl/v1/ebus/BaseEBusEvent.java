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

import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;

public abstract class BaseEBusEvent {

    public BaseEBusEvent() throws Exception {
        JChannel channel = new JChannel();
        rpcDispatcher = new RpcDispatcher(channel, this);
        channel.connect("StudioCluster");
    }

    @Override
    protected void finalize() throws Throwable {
        if (rpcDispatcher != null) {
            rpcDispatcher.close();
            rpcDispatcher = null;
        }
        super.finalize();
    }

    public RpcDispatcher getRpcDispatcher() { return rpcDispatcher; }
    public void setRpcDispatcher(RpcDispatcher rpcDispatcher) { this.rpcDispatcher = rpcDispatcher; }

    protected RpcDispatcher rpcDispatcher;
}
