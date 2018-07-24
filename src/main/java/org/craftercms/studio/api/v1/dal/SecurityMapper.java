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

package org.craftercms.studio.api.v1.dal;

import java.util.List;
import java.util.Map;

public interface SecurityMapper {

    User getUser(String username);

    List<Group> getUserGroups(String username);

    List<Group> getUserGroupsPerSite(Map params);

    void createUser(Map params);

    void deleteUser(Map params);

    void updateUser(Map params);

    void enableUser(Map params);

    void createGroup(Map params);

    List<UserProfileResult> getUserDetails(String username);

    List<String> getAllUsersQuery(Map params);

    int getAllUsersQueryTotal(Map params);

    List<UserProfileResult> getAllUsersData(Map params);

    List<String> getUsersPerSiteQuery(Map params);

    int getUsersPerSiteQueryTotal(Map params);

    List<UserProfileResult> getUsersPerSiteData(Map params);

    Map<String, Object> getGroup(Map params);

    List<Long> getAllGroupsQuery(Map params);

    List<GroupResult> getAllGroupsData(Map params);

    List<Long> getGroupsPerSiteQuery(Map params);

    int getGroupsPerSiteQueryTotal(Map<String, Object> params);

    List<GroupPerSiteResult> getGroupsPerSiteData(Map params);

    List<User> getUsersPerGroup(Map params);

    int getUsersPerGroupTotal(Map params);

    Integer userExistsInGroup(Map params);

    Integer userExists(Map params);

    Integer groupExists(Map params);

    void updateGroup(Map params);

    void deleteGroup(Map params);

    Group getGroupObject(Map params);

    void addUserToGroup(Map params);

    void removeUserFromGroup(Map params);

    void setUserPassword(Map params);

    int isSystemUser(Map params);
}
