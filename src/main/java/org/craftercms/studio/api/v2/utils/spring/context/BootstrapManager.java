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
package org.craftercms.studio.api.v2.utils.spring.context;

import org.craftercms.studio.impl.v2.utils.spring.event.CleanupRepositoriesEvent;
import org.craftercms.studio.impl.v2.utils.spring.event.StartSitesBootstrapEvent;
import org.craftercms.studio.impl.v2.utils.spring.event.StartSitesUpgradeEvent;
import org.craftercms.studio.impl.v2.utils.spring.event.StartSystemUpgradeEvent;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * This is interface is needed so the BootstrapManager implementations
 * can handle events asynchronously
 */
public interface BootstrapManager {

	/**
	 * Handles the {@link ContextRefreshedEvent}. This is needed to trigger the bootstrap process after the context is fully initialized.
	 *
	 * @return the next event to be published
	 */
	Object onContextRefresh();

	/**
	 * Handles the {@link StartSitesBootstrapEvent}. This is meant to trigger the sites bootstrap
	 * asynchronously.
	 */
	void onSitesBootstrapEvent();

	/**
	 * Handles the {@link CleanupRepositoriesEvent} and chains the next event to be published after the repositories are cleaned up.
	 *
	 * @return the next event to be published
	 */
	Object onCleanUpRepositories();

	/**
	 * Handles the {@link StartSitesUpgradeEvent}. This is meant to trigger the sites upgrade
	 */
	void onStartSitesUpgradeEvent();

	/**
	 * Handles the {@link StartSystemUpgradeEvent} and chains the next event to be published after the system upgrade is done.
	 *
	 * @return the next event to be published
	 */
	Object onStartSystemUpgrade();

	/**
	 * Handles the end of the bootstrap process. This is meant to trigger any finalization needed after the bootstrap process is finished.
	 */
	void onBootstrapFinished();
}
