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

package org.craftercms.studio.impl.v2.service.security;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.commons.crypto.CryptoUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.dal.GroupDAL;
import org.craftercms.studio.api.v2.dal.UserDAL;
import org.craftercms.studio.api.v2.dal.UserMapper;
import org.craftercms.studio.api.v2.service.security.GroupService;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.model.Group;
import org.craftercms.studio.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.EMAIL;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ENABLED;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.EXTERNALLY_MANAGED;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.FIRST_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAMES;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LAST_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LOCALE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PASSWORD;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SORT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.TIMEZONE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAMES;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_IDS;

public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private UserMapper userMapper;
    private GroupService groupService;

    @Override
    public List<User> getAllUsersForSite(int orgId, String siteId, int offset, int limit, String sort) {
        List<String> groupNames = groupService.getSiteGroups(siteId);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_NAMES, groupNames);
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        params.put(SORT, "");
        List<UserDAL> userDALs = userMapper.getAllUsersForSite(params);
        List<User> users = new ArrayList<User>();
        userDALs.forEach(userDAL -> {
            User u = new User();
            u.setId(userDAL.getId());
            u.setUsername(userDAL.getUsername());
            u.setFirstName(userDAL.getFirstName());
            u.setLastName(userDAL.getLastName());
            u.setEmail(userDAL.getEmail());
            u.setEnabled(userDAL.isEnabled());
            u.setExternallyManaged(userDAL.getExternallyManaged() != 0);
            users.add(u);
        });

        return users;
    }

    @Override
    public void createUser(User user) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USERNAME, user.getUsername());
        String hashedPassword = CryptoUtils.hashPassword(user.getPassword());
        params.put(PASSWORD, hashedPassword);
        params.put(FIRST_NAME, user.getFirstName());
        params.put(LAST_NAME, user.getLastName());
        params.put(EMAIL, user.getEmail());
        params.put(EXTERNALLY_MANAGED, user.isExternallyManaged() ? 1 : 0);
        params.put(TIMEZONE, "");
        params.put(LOCALE, "");
        params.put(ENABLED, user.isEnabled() ? 1 : 0);
        userMapper.createUser(params);
    }

    @Override
    public void updateUser(User user) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USER_ID, user.getId());
        params.put(FIRST_NAME, user.getFirstName());
        params.put(LAST_NAME, user.getLastName());
        params.put(EMAIL, user.getEmail());
        params.put(TIMEZONE, "");
        params.put(LOCALE, "");
        userMapper.updateUser(params);
    }

    @Override
    public void deleteUsers(List<Integer> userIds, List<String> usernames) {
        List<Integer> allUserIds = new ArrayList<Integer>();
        allUserIds.addAll(userIds);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USERNAMES, usernames);
        if (CollectionUtils.isNotEmpty(usernames)) {
            allUserIds.addAll(userMapper.getUserIdsForUsernames(params));
        }
        if (CollectionUtils.isNotEmpty(allUserIds)) {
            params = new HashMap<String, Object>();
            params.put(USER_IDS, allUserIds);
            userMapper.deleteUsers(params);
        }
    }

    @Override
    public User getUserByIdOrUsername(int userId, String username) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USER_ID, userId);
        params.put(USERNAME, username);
        UserDAL uDAL = userMapper.getUserByIdOrUsername(params);
        User user = new User();
        user.setId(uDAL.getId());
        user.setUsername(uDAL.getUsername());
        user.setFirstName(uDAL.getFirstName());
        user.setLastName(uDAL.getLastName());
        user.setEmail(uDAL.getEmail());
        user.setEnabled(uDAL.isEnabled());
        user.setExternallyManaged(uDAL.getExternallyManaged() != 0);
        return user;
    }

    @Override
    public void enableUsers(List<Integer> userIds, List<String> usernames, boolean enabled) {
        List<Integer> allUserIds = new ArrayList<Integer>();
        allUserIds.addAll(userIds);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USERNAMES, usernames);
        if (CollectionUtils.isNotEmpty(usernames)) {
            allUserIds.addAll(userMapper.getUserIdsForUsernames(params));
        }
        if (CollectionUtils.isNotEmpty(allUserIds)) {
            params = new HashMap<String, Object>();
            params.put(USER_IDS, allUserIds);
            params.put(ENABLED, enabled ? 1 : 0);
            userMapper.enableUsers(params);
        }
    }

    @Override
    public List<Group> getUserGroups(int userId, String username) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USER_ID, userId);
        params.put(USERNAME, username);
        List<GroupDAL> gDALs = userMapper.getUserGroups(params);
        List<Group> userGroups = new ArrayList<Group>();
        gDALs.forEach(g -> {
            Group group = new Group();
            group.setId(g.getId());
            group.setName(g.getGroupName());
            group.setDesc(g.getGroupDescription());
            userGroups.add(group);
        });
        return userGroups;
    }

    public UserMapper getUserMapper() {
        return userMapper;
    }

    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public GroupService getGroupService() {
        return groupService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }
}
