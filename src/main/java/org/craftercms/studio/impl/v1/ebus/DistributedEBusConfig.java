/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2015 Crafter Software Corporation.
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

import org.craftercms.studio.api.v1.ebus.ClearCacheEventMessage;
import org.craftercms.studio.api.v1.ebus.DistributedEventMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.Environment;
import reactor.function.Consumer;
import reactor.tcp.TcpClient;
import reactor.tcp.TcpConnection;
import reactor.tcp.TcpServer;
import reactor.tcp.encoding.json.JsonCodec;
import reactor.tcp.netty.NettyTcpServer;
import reactor.tcp.spec.TcpServerSpec;

@Configuration
public class DistributedEBusConfig {

    @Value("${crafter.studio.ebus.tcpserver.port}")
    private int portNumber;

    @Bean
    public TcpServer<DistributedEventMessage, String> distributedEventMessageStringTcpServer() {
        Environment env = new Environment();
        TcpServer<DistributedEventMessage, String> server =
                new TcpServerSpec<DistributedEventMessage, String>(NettyTcpServer.class)
                        .env(env)
                        .dispatcher(Environment.RING_BUFFER)
                        .listen(portNumber)
                        .codec(new JsonCodec<DistributedEventMessage, String>(DistributedEventMessage.class))
                        .consume(new DistributedEBussConnectionConsumer()).get().start();
        return server;
    }
}
