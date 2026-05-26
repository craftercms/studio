/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.studio.api.v2.utils.spring.context.BootstrapManager;
import org.craftercms.studio.api.v2.utils.spring.context.SystemStatusProvider;
import org.craftercms.studio.impl.v2.utils.spring.event.*;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Central point to control the event-based bootstrap process.
 * <p>Note: All methods in this class should use the {@link Order} annotation with the default value to ensure the
 * events are triggered in the right order</p>
 *
 * @author joseross
 * @since 4.0
 */
public class BootstrapManagerImpl implements SystemStatusProvider, BootstrapManager, ApplicationEventPublisherAware {

	private static final Logger logger = LoggerFactory.getLogger(BootstrapManagerImpl.class);

	/**
	 * Flag used to indicate if the bootstrap process has finished
	 */
	private final AtomicBoolean systemReady = new AtomicBoolean(false);
	private ApplicationEventPublisher applicationEventPublisher;

	// This allows to make sure the sites upgrade process is not started before the system upgrade process is finished,
	// even though they are executed in different threads because the sites bootstrap process is async.
	private final Semaphore upgradeSemaphore = new Semaphore(0);

	@Override
	public boolean isSystemReady() {
		return systemReady.get();
	}

	// the condition is needed to avoid a repeated event from a child app context
	@Order
	@Override
	@EventListener(value = ContextRefreshedEvent.class, condition = "event.applicationContext.parent == null")
	public Object onContextRefresh() {
		logger.info("Beans created and ready to be used");
		logger.info("Start temporary files cleanup ...");
		applicationEventPublisher.publishEvent(new StartSitesBootstrapEvent(this));
		return new StartSystemUpgradeEvent(this);
	}

	@Async
	@Order
	@Override
	@EventListener(value = StartSitesBootstrapEvent.class)
	public void onSitesBootstrapEvent() {
		logger.info("Start repositories cleanup...");
		applicationEventPublisher.publishEvent(new CleanupRepositoriesEvent(this));
	}

	@Order
	@Override
	@EventListener(CleanupRepositoriesEvent.class)
	public Object onCleanUpRepositories() {
		logger.info("Successfully cleaned up repositories");
		logger.info("Waiting for system upgrade to complete before starting sites upgrade");
		upgradeSemaphore.acquireUninterruptibly();
		logger.info("Start upgrade ...");
		return new StartSitesUpgradeEvent(this);
	}

	@Order
	@Override
	@EventListener(StartSitesUpgradeEvent.class)
	public void onStartSitesUpgradeEvent() {
		logger.info("Upgrade sites complete");
	}

	@Order
	@Override
	@EventListener(StartSystemUpgradeEvent.class)
	public Object onStartSystemUpgrade() {
		logger.info("Upgrade system complete");
		upgradeSemaphore.release();
		return new BootstrapFinishedEvent(this);
	}

	@Order
	@Override
	@EventListener(BootstrapFinishedEvent.class)
	public void onBootstrapFinished() {
		logger.info("Bootstrap process finished");
		systemReady.set(true);
	}

	@Override
	public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}
}
