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

package org.craftercms.studio.impl.v2.security;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.security.SitePermissionMappings;
import org.craftercms.studio.api.v2.security.AvailableActionsResolver;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * Default implementation of {@link AvailableActionsResolver}
 */
public class AvailableActionsResolverImpl implements AvailableActionsResolver {

	private static final Logger logger = LoggerFactory.getLogger(AvailableActionsResolverImpl.class);

	private final UserServiceInternal userServiceInternal;
	private final PermissionMappingsProvider permissionMappingsProvider;

	public AvailableActionsResolverImpl(UserServiceInternal userServiceInternal,
										PermissionMappingsProvider permissionMappingsProvider) {
		this.userServiceInternal = userServiceInternal;
		this.permissionMappingsProvider = permissionMappingsProvider;
	}

	@Override
	public long getContentItemAvailableActions(String username, String siteId, String path)
		throws ServiceLayerException, UserNotFoundException {
		SitePermissionMappings sitePermissionMappings = permissionMappingsProvider.getPermissionMappings(siteId);
		return calculateAvailableActions(username, path, sitePermissionMappings);
	}

	@Override
	public long getSiteWideActions(String siteId, String username) throws ServiceLayerException, UserNotFoundException {
		List<Group> groups = userServiceInternal.getUserGroups(-1, username);
		SitePermissionMappings sitePermissionMappings = permissionMappingsProvider.getPermissionMappings(siteId);
		return sitePermissionMappings.getSiteWideAvailableActions(username, groups);
	}

	private long calculateAvailableActions(String username, String path,
										   SitePermissionMappings sitePermissionMappings)
		throws ServiceLayerException, UserNotFoundException {
		long toReturn = 0L;
		List<Group> groups = userServiceInternal.getUserGroups(-1, username);
		if (isNotEmpty(groups)) {
			toReturn = sitePermissionMappings.getAvailableActions(username, groups, path);
		}
		return toReturn;
	}

}
