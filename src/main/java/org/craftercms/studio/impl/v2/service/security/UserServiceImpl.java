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

package org.craftercms.studio.impl.v2.service.security;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.*;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.security.NormalizedRole;
import org.craftercms.studio.api.v2.security.HasAllPermissions;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.model.Site;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.craftercms.studio.permissions.StudioPermissionsConstants.*;

public class UserServiceImpl implements UserService {
	private UserService userServiceInternal;


	/**
	 * Check if updating users list contains any externally managed users.
	 * If matched, the operation must not be permitted.
	 */
	// TODO JM: Consider making this an annotation
	private void checkExternallyManagedUsers(List<Long> userIds, List<String> usernames) throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException {
		List<User> users = getUsersByIdOrUsername(userIds, usernames);
		if (users.stream().anyMatch(User::isExternallyManaged)) {
			throw new UserExternallyManagedException("Cannot update externally managed users.");
		}
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
	public Collection<User> getAllUsersForSite(long orgId, String siteId, String keyword, int offset, int limit, String sort)
		throws ServiceLayerException {
		return userServiceInternal.getAllUsersForSite(orgId, siteId, keyword, offset, limit, sort);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
	public Collection<User> getAllUsers(String keyword, int offset, int limit, String sort) throws ServiceLayerException {
		return userServiceInternal.getAllUsers(keyword, offset, limit, sort);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
	public int getAllUsersForSiteTotal(long orgId, String siteId, String keyword) throws ServiceLayerException {
		return userServiceInternal.getAllUsersForSiteTotal(orgId, siteId, keyword);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
	public int getAllUsersTotal(String keyword) throws ServiceLayerException {
		return userServiceInternal.getAllUsersTotal(keyword);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_CREATE_USERS)
	public User createUser(User user) throws UserAlreadyExistsException, ServiceLayerException {
		return userServiceInternal.createUser(user);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_UPDATE_USERS)
	public void updateUser(User user) throws ServiceLayerException, UserNotFoundException, UserExternallyManagedException {
		checkExternallyManagedUsers(List.of(user.getId()), List.of(user.getUsername()));
		userServiceInternal.updateUser(user);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
	public boolean userExists(String username) throws ServiceLayerException {
		return userServiceInternal.userExists(username);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
	public boolean userExists(long userId, String username) throws ServiceLayerException {
		return userServiceInternal.userExists(userId, username);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
	public User getUserByGitName(String gitName) throws ServiceLayerException, UserNotFoundException {
		return userServiceInternal.getUserByGitName(gitName);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_DELETE_USERS)
	public void deleteUsers(List<Long> userIds, List<String> usernames)
		throws ServiceLayerException, UserNotFoundException, UserExternallyManagedException, AuthenticationException, GroupNotFoundException {
		checkExternallyManagedUsers(userIds, usernames);
		userServiceInternal.deleteUsers(userIds, usernames);
	}

	@Override
	@NonNull
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
	public User getUserByIdOrUsername(long userId, String username)
		throws ServiceLayerException, UserNotFoundException {
		return userServiceInternal.getUserByIdOrUsername(userId, username);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_UPDATE_USERS)
	public List<User> enableUsers(List<Long> userIds, List<String> usernames,
					      boolean enabled) throws ServiceLayerException, UserNotFoundException, UserExternallyManagedException {
		checkExternallyManagedUsers(userIds, usernames);
		return userServiceInternal.enableUsers(userIds, usernames, enabled);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
	public List<Site> getUserSites(long userId, String username) throws ServiceLayerException, UserNotFoundException {
		return userServiceInternal.getUserSites(userId, username);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
	public List<NormalizedRole> getUserSiteRoles(long userId, String username, String site)
		throws ServiceLayerException, UserNotFoundException {
		return userServiceInternal.getUserSiteRoles(userId, username, site);
	}

	@Override
	public List<Site> getCurrentUserSites() throws AuthenticationException, ServiceLayerException {
		return userServiceInternal.getCurrentUserSites();
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
	public List<String> getCurrentUserSiteRoles(@SiteId String site) throws AuthenticationException, ServiceLayerException, UserNotFoundException {
		return userServiceInternal.getCurrentUserSiteRoles(site);
	}

	@Override
	public List<User> getUsersByIdOrUsername(List<Long> userIds, List<String> usernames) throws ServiceLayerException, UserNotFoundException {
		return userServiceInternal.getUsersByIdOrUsername(userIds, usernames);
	}

	@Override
	public List<Group> getUserGroups(long userId, String username, boolean filterExternallyManagedGroups) throws UserNotFoundException, ServiceLayerException {
		return userServiceInternal.getUserGroups(userId, username, filterExternallyManagedGroups);
	}

	@Override
	public Collection<NormalizedRole> getUserGlobalRoles(String username) throws ServiceLayerException, UserNotFoundException {
		return userServiceInternal.getUserGlobalRoles(username);
	}

	@Override
	@HasAllPermissions(type = DefaultPermission.class, actions = {PERMISSION_READ_GROUPS, PERMISSION_READ_USERS})
	public List<Group> getUserGroups(long userId, String username) throws UserNotFoundException, ServiceLayerException {
		return userServiceInternal.getUserGroups(userId, username);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
	public boolean isSystemAdmin(String username) {
		return userServiceInternal.isSystemAdmin(username);
	}

	@Override
	public void forgotPassword(final String username) throws ServiceLayerException {
		userServiceInternal.forgotPassword(username);
	}

	@Override
	public String getForgotPasswordToken(final String username) throws ServiceLayerException {
		return userServiceInternal.getForgotPasswordToken(username);
	}

	@Override
	public User changePassword(String username, String current, String newPassword)
		throws PasswordDoesNotMatchException, UserExternallyManagedException, ServiceLayerException,
		AuthenticationException, UserNotFoundException {
		return userServiceInternal.changePassword(username, current, newPassword);
	}

	@Override
	public User setPassword(final String token, final String newPassword) throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException {
		return userServiceInternal.setPassword(token, newPassword);
	}

	@Override
	public boolean validateToken(final String token) throws UserNotFoundException,
		UserExternallyManagedException, ServiceLayerException {
		return userServiceInternal.validateToken(token);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_UPDATE_USERS)
	public boolean resetPassword(String username, String newPassword)
		throws UserNotFoundException, ServiceLayerException, UserExternallyManagedException {
		return userServiceInternal.resetPassword(username, newPassword);
	}

	@Override
	public Map<String, Map<String, String>> getUserProperties(String siteId) throws ServiceLayerException {
		return userServiceInternal.getUserProperties(siteId);
	}

	@Override
	public Map<String, String> updateUserProperties(String siteId, Map<String, String> propertiesToUpdate)
		throws ServiceLayerException {
		return userServiceInternal.updateUserProperties(siteId, propertiesToUpdate);
	}

	@Override
	public Map<String, String> deleteUserProperties(String siteId, List<String> propertiesToDelete)
		throws ServiceLayerException {
		return userServiceInternal.deleteUserProperties(siteId, propertiesToDelete);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
	public List<String> getCurrentUserSitePermissions(@SiteId String site)
		throws ServiceLayerException, UserNotFoundException, ExecutionException {
		return userServiceInternal.getCurrentUserSitePermissions(site);
	}

	@Override
	public Map<String, Boolean> hasCurrentUserSitePermissions(final String site, final Collection<String> permissions)
		throws ServiceLayerException, UserNotFoundException, ExecutionException {
		return userServiceInternal.hasCurrentUserSitePermissions(site, permissions);
	}

	@Override
	public List<String> getCurrentUserGlobalPermissions() throws ServiceLayerException, UserNotFoundException, ExecutionException {
		return userServiceInternal.getCurrentUserGlobalPermissions();
	}

	@Override
	public Map<String, Boolean> hasCurrentUserGlobalPermissions(List<String> permissions) throws ServiceLayerException, UserNotFoundException, ExecutionException {
		return userServiceInternal.hasCurrentUserGlobalPermissions(permissions);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
	public boolean isSiteMember(String username, String siteId) {
		return userServiceInternal.isSiteMember(username, siteId);
	}

	@SuppressWarnings("unused")
	public void setUserServiceInternal(final UserService userServiceInternal) {
		this.userServiceInternal = userServiceInternal;
	}

}
