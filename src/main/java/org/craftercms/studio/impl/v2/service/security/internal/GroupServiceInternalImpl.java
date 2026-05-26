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

package org.craftercms.studio.impl.v2.service.security.internal;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.GroupDAO;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.service.security.GroupService;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class GroupServiceInternalImpl implements GroupService {

	private GroupDAO groupDao;
	private UserService userService;
	private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

	@Override
	public Group getGroup(long groupId) throws GroupNotFoundException, ServiceLayerException {
		Group group;
		try {
			group = groupDao.getGroup(groupId);
		} catch (Exception e) {
			throw new ServiceLayerException("Unknown database error", e);
		}

		if (group != null) {
			return group;
		} else {
			throw new GroupNotFoundException("No group found for id '" + groupId + "'");
		}
	}

	@Override
	public List<Group> getGroups(List<Long> groupIds) throws GroupNotFoundException, ServiceLayerException {
		List<Group> groups;
		try {
			groups = groupDao.getGroups(groupIds);
		} catch (Exception e) {
			throw new ServiceLayerException("Unknown database error", e);
		}

		if (groups != null) {
			return groups;
		} else {
			throw new GroupNotFoundException("No group found for id '" + groupIds + "'");
		}
	}

	@Override
	public Group getGroupByName(String groupName) throws GroupNotFoundException, ServiceLayerException {
		Group group;
		try {
			group = groupDao.getGroupByName(groupName);
		} catch (Exception e) {
			throw new ServiceLayerException("Unknown database error", e);
		}

		if (group != null) {
			return group;
		} else {
			throw new GroupNotFoundException("No group found for name '" + groupName + "'");
		}
	}

	@Override
	public boolean groupExists(long groupId, String groupName) throws ServiceLayerException {
		try {
			Integer result = groupDao.groupExists(groupId, groupName);
			return (result > 0);
		} catch (Exception e) {
			throw new ServiceLayerException("Unknown database error", e);
		}
	}

	@Override
	public List<Group> getAllGroups(String keyword, int offset, int limit, String sort)
			throws ServiceLayerException {
		try {
			return groupDao.getAllGroups(keyword, offset, limit, sort);
		} catch (Exception e) {
			throw new ServiceLayerException("Unknown database error", e);
		}
	}

	@Override
	public int getAllGroupsTotal(String keyword) throws ServiceLayerException {
		try {
			return groupDao.getAllGroupsTotal(keyword);
		} catch (Exception e) {
			throw new ServiceLayerException("Unknown database error", e);
		}
	}

	@Override
	public Group createGroup(String groupName, String groupDescription, boolean externallyManaged)
			throws GroupAlreadyExistsException, ServiceLayerException {
		if (groupExists(-1, groupName)) {
			throw new GroupAlreadyExistsException("Group '" + groupName + "' already exists");
		}
		try {
			retryingDatabaseOperationFacade.retry(() -> groupDao.createGroup(groupName, groupDescription, externallyManaged));
			return groupDao.getGroupByName(groupName);
		} catch (Exception e) {
			throw new ServiceLayerException("Unknown database error", e);
		}
	}

	@Override
	public Group updateGroup(Group updatedGroup) throws GroupNotFoundException, ServiceLayerException {
		Group group = groupDao.getGroup(updatedGroup.getId());
		if (group == null) {
			throw new GroupNotFoundException(format("No group found for id '%d'", updatedGroup.getId()));
		}
		group.setGroupDescription(updatedGroup.getGroupDescription());
		try {
			retryingDatabaseOperationFacade.retry(() -> groupDao.updateGroup(group));
			return group;
		} catch (Exception e) {
			throw new ServiceLayerException("Unknown database error", e);
		}
	}

	@Override
	public void deleteGroup(List<Long> groupIds) throws GroupNotFoundException, ServiceLayerException {
		for (Long groupId : groupIds) {
			if (!groupExists(groupId, StringUtils.EMPTY)) {
				throw new GroupNotFoundException("No group found for id '" + groupId + "'");
			}
		}

		try {
			retryingDatabaseOperationFacade.retry(() -> groupDao.deleteGroups(groupIds));
		} catch (Exception e) {
			throw new ServiceLayerException("Unknown database error", e);
		}
	}

	@Override
	public List<User> getGroupMembers(long groupId, int offset, int limit, String sort)
			throws GroupNotFoundException, ServiceLayerException {
		if (!groupExists(groupId, StringUtils.EMPTY)) {
			throw new GroupNotFoundException("No group found for id '" + groupId + "'");
		}

		try {
			return groupDao.getGroupMembers(groupId, offset, limit, sort);
		} catch (Exception e) {
			throw new ServiceLayerException("Unknown database error", e);
		}
	}

	@Override
	public int getGroupMembersTotal(final long groupId) throws GroupNotFoundException, ServiceLayerException {
		if (!groupExists(groupId, StringUtils.EMPTY)) {
			throw new GroupNotFoundException("No group found for id '" + groupId + "'");
		}
		try {
			return groupDao.getGroupMembersTotal(groupId);
		} catch (Exception e) {
			throw new ServiceLayerException("Unknown database error", e);
		}
	}

	@Override
	public List<User> addGroupMembers(long groupId, List<Long> userIds, List<String> usernames, boolean externallyManaged)
			throws GroupNotFoundException, UserNotFoundException, ServiceLayerException {
		if (!groupExists(groupId, StringUtils.EMPTY)) {
			throw new GroupNotFoundException("No group found for id '" + groupId + "'");
		}

		List<User> users = userService.getUsersByIdOrUsername(userIds, usernames);
		try {
			retryingDatabaseOperationFacade.retry(() -> groupDao.addGroupMembers(groupId,
					users.stream().map(User::getId).collect(Collectors.toList()), externallyManaged));

			return users;
		} catch (Exception e) {
			throw new ServiceLayerException("Unknown database error", e);
		}
	}

	@Override
	public void removeGroupMembers(long groupId, List<Long> userIds, List<String> usernames)
			throws GroupNotFoundException, UserNotFoundException, ServiceLayerException {
		if (!groupExists(groupId, StringUtils.EMPTY)) {
			throw new GroupNotFoundException("No group found for id '" + groupId + "'");
		}
		List<User> users = userService.getUsersByIdOrUsername(userIds, usernames);
		try {
			retryingDatabaseOperationFacade.retry(() -> groupDao.removeGroupMembers(groupId,
					users.stream().map(User::getId).collect(Collectors.toList())));
		} catch (Exception e) {
			throw new ServiceLayerException("Unknown database error", e);
		}
	}

	public void setGroupDao(GroupDAO groupDao) {
		this.groupDao = groupDao;
	}

	@Lazy
	@Autowired
	@Qualifier("userServiceInternal")
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
		this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
	}
}
