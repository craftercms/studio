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

import org.craftercms.studio.api.v1.ebus.DistributedEventMessage;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import reactor.core.Reactor;
import reactor.event.Event;
import reactor.function.Consumer;

public class DistributedEBusConsumer implements Consumer<DistributedEventMessage> {

    private final static Logger LOGGER = LoggerFactory.getLogger(DistributedEBusConsumer.class);

    @Override
    public void accept(DistributedEventMessage distributedEventMessage) {
        LOGGER.debug("Received distributed event message.");
        distributedReactor.notify(distributedEventMessage.getEventKey(), Event.wrap(distributedEventMessage.getMessage()));
    }

    public Reactor getDistributedReactor() { return distributedReactor; }
    public void setDistributedReactor(Reactor distributedReactor) { this.distributedReactor = distributedReactor; }

    protected Reactor distributedReactor;
}
