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

import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.dal.UserTO;
import org.craftercms.studio.api.v2.service.security.GroupService;
import org.craftercms.studio.api.v2.service.security.SecurityProvider;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.model.AuthenticatedUser;
import org.craftercms.studio.model.Group;
import org.craftercms.studio.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAMES;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SORT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAME;

public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private UserDAO userDAO;
    private GroupService groupService;
    private SecurityProvider securityProvider;

    @Override
    public List<User> getAllUsersForSite(long orgId, String siteId, int offset, int limit, String sort) {
        List<String> groupNames = groupService.getSiteGroups(siteId);
        return securityProvider.getAllUsersForSite(orgId, groupNames, offset, limit, sort);
    }

    @Override
    public List<User> getAllUsers(int offset, int limit, String sort) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        params.put(SORT, sort);
        List<UserTO> userTOs = userDAO.getAllUsers(params);
        List<User> users = new ArrayList<User>();
        userTOs.forEach(userTO -> {
            User u = new User();
            u.setId(userTO.getId());
            u.setUsername(userTO.getUsername());
            u.setFirstName(userTO.getFirstName());
            u.setLastName(userTO.getLastName());
            u.setEmail(userTO.getEmail());
            u.setEnabled(userTO.isEnabled());
            u.setExternallyManaged(userTO.getExternallyManaged() != 0);
            users.add(u);
        });
        return users;
    }

    @Override
    public int getAllUsersForSiteTotal(long orgId, String siteId) {
        List<String> groupNames = groupService.getSiteGroups(siteId);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_NAMES, groupNames);
        return userDAO.getAllUsersForSiteTotal(params);
    }

    @Override
    public int getAllUsersTotal() {
        return userDAO.getAllUsersTotal();
    }

    @Override
    public void createUser(User user) throws UserAlreadyExistsException {
        securityProvider.createUser(user);
    }

    @Override
    public void updateUser(User user) {
        securityProvider.updateUser(user);
    }

    @Override
    public void deleteUsers(List<Long> userIds, List<String> usernames) {
        securityProvider.deleteUsers(userIds, usernames);
    }

    @Override
    public User getUserByIdOrUsername(long userId, String username) {
        return securityProvider.getUserByIdOrUsername(userId, username);
    }

    @Override
    public void enableUsers(List<Long> userIds, List<String> usernames, boolean enabled) {
        securityProvider.enableUsers(userIds, usernames, enabled);
    }

    @Override
    public List<Group> getUserGroups(long userId, String username) {
        return securityProvider.getUserGroups(userId, username);
    }

    @Override
    public boolean isUserMemberOfGroup(String username, String groupName) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_NAME, groupName);
        params.put(USERNAME, username);
        int result = userDAO.isUserMemberOfGroup(params);
        return result > 0;
    }

    @Override
    public AuthenticatedUser getAuthenticatedUser() {
        Authentication authentication = securityProvider.getAuthentication();
        String username = authentication.getUsername();
        User user = securityProvider.getUserByIdOrUsername(0, username);
        AuthenticatedUser authUser = new AuthenticatedUser(user);

        authUser.setAuthenticationType(authentication.getAuthenticationType());

        return authUser;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public GroupService getGroupService() {
        return groupService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public SecurityProvider getSecurityProvider() {
        return securityProvider;
    }

    public void setSecurityProvider(SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }
}
