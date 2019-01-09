/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
 */

package org.craftercms.studio.api.v2.service.security.internal;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.User;

import java.util.List;

public interface GroupServiceInternal {

    Group getGroup(long groupId) throws GroupNotFoundException, ServiceLayerException;

    Group getGroupByName(String groupName) throws GroupNotFoundException, ServiceLayerException;

    List<Group> getAllGroups(long orgId, int offset, int limit, String sort) throws ServiceLayerException;

    int getAllGroupsTotal(long orgId) throws ServiceLayerException;

    Group createGroup(long orgId, String groupName,
                      String groupDescription) throws GroupAlreadyExistsException, ServiceLayerException;

    Group updateGroup(long orgId, Group group) throws GroupNotFoundException, ServiceLayerException;

    void deleteGroup(List<Long> groupIds) throws GroupNotFoundException, ServiceLayerException;

    boolean groupExists(long groupId, String groupName) throws ServiceLayerException;

    List<User> getGroupMembers(long groupId, int offset, int limit,
                               String sort) throws GroupNotFoundException, ServiceLayerException;

    int getGroupMembersTotal(long groupId) throws ServiceLayerException, GroupNotFoundException;

    List<User> addGroupMembers(long groupId, List<Long> userIds,
                               List<String> usernames) throws GroupNotFoundException, UserNotFoundException,
                                                              ServiceLayerException;

    void removeGroupMembers(long groupId, List<Long> userIds,
                            List<String> usernames) throws GroupNotFoundException, UserNotFoundException,
                                                           ServiceLayerException;

    List<String> getSiteGroups(String siteId) throws ServiceLayerException;

}
