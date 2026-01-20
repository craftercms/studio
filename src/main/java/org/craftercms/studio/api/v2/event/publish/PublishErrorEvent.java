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

package org.craftercms.studio.api.v2.event.publish;

import org.craftercms.studio.api.v2.event.SiteAwareEvent;
import org.craftercms.studio.api.v2.event.SiteBroadcastEvent;

/**
 * Event triggered when an error occurs during the publishing process
 */
public class PublishErrorEvent extends SiteAwareEvent implements SiteBroadcastEvent {

	private final long packageId;
	private final Exception exception;

	public PublishErrorEvent(final String siteId, final long packageId) {
		this(siteId, packageId, null);
	}

	public PublishErrorEvent(final String siteId, final long packageId, final Exception exception) {
		super(siteId);
		this.packageId = packageId;
		this.exception = exception;
	}

	public long getPackageId() {
		return packageId;
	}

	public Exception getException() {
		return exception;
	}

	@Override
	public String getEventType() {
		return "PUBLISH_ERROR_EVENT";
	}
}
