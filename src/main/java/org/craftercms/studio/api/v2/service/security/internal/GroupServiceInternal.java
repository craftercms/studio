/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.craftercms.studio.api.v2.service.security.internal;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.GroupTO;
import org.craftercms.studio.model.User;

import java.util.List;

public interface GroupServiceInternal {
    List<GroupTO> getAllGroups(long orgId, int offset, int limit, String sort) throws ServiceLayerException;

    int getAllGroupsTotal(long orgId) throws ServiceLayerException;

    GroupTO createGroup(long orgId, String groupName, String groupDescription) throws ServiceLayerException;

    GroupTO updateGroup(long orgId, GroupTO group) throws ServiceLayerException;

    void deleteGroup(List<Long> groupIds) throws ServiceLayerException;

    GroupTO getGroup(long groupId) throws ServiceLayerException;

    List<User> getGroupMembers(long groupId, int offset, int limit, String sort) throws ServiceLayerException;

    List<User> addGroupMembers(long groupId, List<Long> userIds, List<String> usernames) throws ServiceLayerException, UserNotFoundException;

    void removeGroupMembers(long groupId, List<Long> userIds, List<String> usernames) throws ServiceLayerException;

    List<String> getSiteGroups(String siteId);

    GroupTO getGroupByName(String groupName) throws GroupNotFoundException, ServiceLayerException;

    boolean groupExists(String groupName) throws ServiceLayerException;
}
