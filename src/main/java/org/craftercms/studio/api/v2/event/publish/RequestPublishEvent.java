/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

import java.util.Collection;
import java.util.List;

/**
 * Event triggered to request the publisher to publish package that is ready to be processed.
 */
public class RequestPublishEvent extends SiteAwareEvent {

	private final Collection<Long> packageIds;

	public RequestPublishEvent(final String siteId, final Long packageId) {
		this(siteId, List.of(packageId));
	}

	public RequestPublishEvent(final String siteId, final Collection<Long> packageIds) {
		super(siteId);
		this.packageIds = packageIds;
	}

	public Collection<Long> getPackageIds() {
		return packageIds;
	}
}
