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

package org.craftercms.studio.impl.v2.security.publish;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.dal.security.SitePermissionMappings;
import org.craftercms.studio.api.v2.security.publish.PublishPackageAvailableActionResolver;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.impl.v2.security.PermissionMappingsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.ConstructorProperties;
import java.util.List;

import static org.craftercms.studio.api.v2.security.publish.PublishPackageAvailableActions.APPROVE;
import static org.craftercms.studio.api.v2.security.publish.PublishPackageAvailableActions.getPossibleActionsForPackageStates;
import static org.craftercms.studio.impl.v2.utils.security.SecurityUtils.getCurrentUser;

/**
 * Default implementation of {@link PublishPackageAvailableActionResolver}
 */
public class PublishPackageAvailableActionResolverImpl implements PublishPackageAvailableActionResolver {
	private static final Logger logger = LoggerFactory.getLogger(PublishPackageAvailableActionResolverImpl.class);

	private final PermissionMappingsProvider permissionMappingProvider;
	private final UserServiceInternal userServiceInternal;
	private final ServicesConfig servicesConfig;

	@ConstructorProperties({"permissionMappingProvider", "userServiceInternal", "servicesConfig"})
	public PublishPackageAvailableActionResolverImpl(PermissionMappingsProvider permissionMappingProvider,
													 UserServiceInternal userServiceInternal,
													 ServicesConfig servicesConfig) {
		this.permissionMappingProvider = permissionMappingProvider;
		this.userServiceInternal = userServiceInternal;
		this.servicesConfig = servicesConfig;
	}

	@Override
	public long getPublishPackageAvailableActions(PublishPackage publishPackage) throws ServiceLayerException, UserNotFoundException {
		String user = getCurrentUser();
		if (user == null) {
			logger.debug("No user is authenticated, returning 0 available actions");
			return 0;
		}
		String siteId = publishPackage.getSite().getSiteId();
		SitePermissionMappings permissionMappings = permissionMappingProvider.getPermissionMappings(siteId);
		List<Group> groups = userServiceInternal.getUserGroups(-1, user);
		boolean isSubmitter = publishPackage.getSubmitter().getUsername().equals(getCurrentUser());
		long userAllowedActions = permissionMappings.getPublishPackageAvailableActions(user, groups);
		long packageStateActions = getPossibleActionsForPackageStates(publishPackage.getPackageState(),
			publishPackage.getApprovalState());

		long allowedActions = userAllowedActions & packageStateActions;

		return applyPeerReviewConfig(allowedActions, siteId, isSubmitter);
	}

	/**
	 * Ensure the submitter of a package does not have the APPROVE action available if peer review is enabled
	 */
	private long applyPeerReviewConfig(long allowedActions, String siteId, boolean isSubmitter) {
		long result = allowedActions;
		boolean peerReviewEnabled = servicesConfig.isRequirePeerReview(siteId);
		if (isSubmitter && peerReviewEnabled) {
			result &= ~APPROVE;
		}

		return result;
	}

}
