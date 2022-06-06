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
package org.craftercms.studio.impl.v2.utils.spring.context;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.spring.context.SystemStatusProvider;
import org.craftercms.studio.impl.v2.utils.spring.event.CleanupRepositoriesEvent;
import org.craftercms.studio.impl.v2.utils.spring.event.StartUpgradeEvent;
import org.craftercms.studio.impl.v2.utils.spring.event.BootstrapFinishedEvent;
import org.craftercms.studio.impl.v2.utils.spring.event.StartClusterSetupEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Central point to control the event-based bootstrap process.
 * <p>Note: All methods in this class should use the {@link Order} annotation with the default value to ensure the
 * events are triggered in the right order</p>
 *
 * @author joseross
 * @since 4.0
 */
public class BootstrapManager implements SystemStatusProvider {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapManager.class);

    /**
     * Flag used to indicate if the bootstrap process has finished
     */
    private final AtomicBoolean systemReady = new AtomicBoolean(false);

    @Override
    public boolean isSystemReady() {
        return systemReady.get();
    }

    // the condition is needed to avoid a repeated event from a child app context
    @Order(2)
    @EventListener(value = ContextRefreshedEvent.class, condition = "event.applicationContext.parent == null")
    public Object onContextRefresh() {
        logger.info("Beans created and ready to be used");
        logger.info("Starting clean up repositories ...");
        return new CleanupRepositoriesEvent(this);
    }

    @Order
    @EventListener(value = CleanupRepositoriesEvent.class)
    public Object onCleanUpRepositories() {
        logger.info("Clean up repositories finished");
        logger.info("Starting upgrade ...");
        return new StartUpgradeEvent(this);
    }

    @Order
    @EventListener(StartUpgradeEvent.class)
    public Object onStartUpgrade() {
        logger.info("Upgrade complete");
        logger.info("Starting cluster setup ...");
        return new StartClusterSetupEvent(this);
    }

    @Order
    @EventListener(StartClusterSetupEvent.class)
    public Object onStartClusterSetup() {
        logger.info("Cluster setup complete");
        return new BootstrapFinishedEvent(this);
    }

    @Order
    @EventListener(BootstrapFinishedEvent.class)
    public void onBootstrapFinished() {
        logger.info("Bootstrap process finished");
        systemReady.set(true);
    }

}
