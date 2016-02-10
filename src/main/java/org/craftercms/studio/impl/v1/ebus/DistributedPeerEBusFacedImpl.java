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
 */

package org.craftercms.studio.impl.v1.ebus;

import org.apache.commons.collections.ListUtils;
import org.craftercms.studio.api.v1.ebus.DistributedEventMessage;
import org.craftercms.studio.api.v1.ebus.DistributedPeerEBusFacade;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.tcp.TcpClient;
import reactor.tcp.TcpConnection;
import reactor.tcp.TcpServer;
import reactor.tcp.encoding.json.JsonCodec;
import reactor.tcp.netty.NettyTcpClient;
import reactor.tcp.spec.TcpClientSpec;

import java.util.List;

public class DistributedPeerEBusFacedImpl implements DistributedPeerEBusFacade {

    private final static Logger logger = LoggerFactory.getLogger(DistributedPeerEBusFacedImpl.class);

    @Override
    public void notifyCluster(DistributedEventMessage message) {
        if (peerList != null && peerList.size() > 0) {
            for (String peer : peerList) {
                String[] t = peer.split(":");
                if (t.length != 2) {
                    logger.error("Invalid distributed event peer configuration: " + peer);
                    continue;
                }
                String host = t[0];
                int port = Integer.parseInt(t[1]);
                TcpClient<String, DistributedEventMessage> client = getTcpClient(host, port);
                try {
                    TcpConnection<String, DistributedEventMessage> conn = client.open().await();
                    conn.send(message);
                    conn.close();
                } catch (InterruptedException e) {
                    logger.error("Error connecting to distributed peer: " + peer, e);
                } catch (RuntimeException e) {
                    logger.error("Error connecting to distributed peer: " + peer, e);
                }
            }
        }

    }

    private TcpClient<String, DistributedEventMessage> getTcpClient(String host, int port) {
        Environment env = new Environment();
        TcpClient<String, DistributedEventMessage> client =
                new TcpClientSpec<String, DistributedEventMessage>(NettyTcpClient.class)
                        .env(env)
                        .dispatcher(Environment.RING_BUFFER)
                        .connect(host, port)
                        .codec(new JsonCodec<String, DistributedEventMessage>(String.class))
                        .get();
        return client;
    }

    public Reactor getDistributedReactor() { return distributedReactor; }
    public void setDistributedReactor(Reactor distributedReactor) { this.distributedReactor = distributedReactor; }

    public List<String> getPeerList() { return peerList; }
    public void setPeerList(List<String> peerList) { this.peerList = peerList; }

    protected Reactor distributedReactor;
    protected List<String> peerList;
}
