/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.impl.v2.monitor;

import org.craftercms.studio.api.v2.notification.StudioNotificationSender;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.repository.job.RepositoryCleanupJob;

/**
 * Dummy DiskMonitor for testing purposes.
 * This is just an extension of {@link DiskMonitor} that does not initialize the diskStatus
 */
public class DummyDiskMonitor extends DiskMonitor {

	public DummyDiskMonitor(RepositoryCleanupJob gitGCJob,
							StudioNotificationSender notificationSender) {
		super(gitGCJob, notificationSender, ".", 10, 20);
	}

	@Override
	public void afterPropertiesSet() {
		// Do not initialize diskStatus in tests
	}
}
