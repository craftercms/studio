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

import org.craftercms.studio.api.v2.utils.spring.context.SiteBootstrapStateProvider;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation of {@link SiteBootstrapStateProvider} that keeps the state in memory.
 */
public class SiteBootstrapStateProviderImpl implements SiteBootstrapStateProvider {

	private final Set<String> readySites = Collections.synchronizedSet(new HashSet<>());

	protected Set<String> getReadySites() {
		return readySites;
	}

	@Override
	public void markSiteAsReady(String siteId) {
		getReadySites().add(siteId);
	}

	@Override
	public void unmarkSiteAsReady(String siteId) {
		getReadySites().remove(siteId);
	}

	@Override
	public boolean isSiteReady(String siteId) {
		return getReadySites().contains(siteId);
	}
}
