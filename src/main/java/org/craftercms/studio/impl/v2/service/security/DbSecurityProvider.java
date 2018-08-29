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
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoUtils;
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.studio.api.v1.ebus.RepositoryEventContext;
import org.craftercms.studio.api.v1.exception.security.AuthenticationSystemException;
import org.craftercms.studio.api.v1.exception.security.BadCredentialsException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.PasswordDoesNotMatchException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserExternallyManagedException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.job.CronJobContext;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.GroupDAO;
import org.craftercms.studio.api.v2.dal.GroupMapper;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.dal.UserMapper;
import org.craftercms.studio.api.v2.service.security.SecurityProvider;
import org.craftercms.studio.impl.v1.util.SessionTokenUtils;
import org.craftercms.studio.model.Group;
import org.craftercms.studio.model.User;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.HTTP_SESSION_ATTRIBUTE_STUDIO_USER;
import static org.craftercms.studio.api.v1.service.security.SecurityService.STUDIO_SESSION_TOKEN_ATRIBUTE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_SESSION_TIMEOUT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.EMAIL;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ENABLED;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.EXTERNALLY_MANAGED;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.FIRST_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_DESCRIPTION;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_IDS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAMES;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LAST_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LOCALE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ORG_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PASSWORD;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SORT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.TIMEZONE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAMES;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_IDS;

public class DbSecurityProvider implements SecurityProvider {

    protected UserMapper userMapper;
    protected GroupMapper groupMapper;
    protected StudioConfiguration studioConfiguration;

    @Override
    public List<User> getAllUsersForSite(long orgId, List<String> groupNames, int offset, int limit, String sort) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_NAMES, groupNames);
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        params.put(SORT, "");
        List<UserDAO> userDAOS = userMapper.getAllUsersForSite(params);
        List<User> users = new ArrayList<User>();
        userDAOS.forEach(userDAO -> {
            User u = new User();
            u.setId(userDAO.getId());
            u.setUsername(userDAO.getUsername());
            u.setFirstName(userDAO.getFirstName());
            u.setLastName(userDAO.getLastName());
            u.setEmail(userDAO.getEmail());
            u.setEnabled(userDAO.isEnabled());
            u.setExternallyManaged(userDAO.getExternallyManaged() != 0);
            users.add(u);
        });

        return users;
    }

    @Override
    public boolean createUser(User user) throws UserAlreadyExistsException {
        if (userExists(user.getUsername())) {
            throw new UserAlreadyExistsException();
        }
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
        return true;
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
    public void deleteUsers(List<Long> userIds, List<String> usernames) {
        List<Long> allUserIds = new ArrayList<Long>();
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
    public User getUserByIdOrUsername(long userId, String username) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USER_ID, userId);
        params.put(USERNAME, username);
        UserDAO uDAO = userMapper.getUserByIdOrUsername(params);
        User user = new User();
        if (uDAO != null) {
            user.setId(uDAO.getId());
            user.setUsername(uDAO.getUsername());
            user.setFirstName(uDAO.getFirstName());
            user.setLastName(uDAO.getLastName());
            user.setEmail(uDAO.getEmail());
            user.setEnabled(uDAO.isEnabled());
            user.setExternallyManaged(uDAO.getExternallyManaged() != 0);
        }
        return user;
    }

    @Override
    public void enableUsers(List<Long> userIds, List<String> usernames, boolean enabled) {
        List<Long> allUserIds = new ArrayList<Long>();
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
    public List<Group> getUserGroups(long userId, String username) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USER_ID, userId);
        params.put(USERNAME, username);
        List<GroupDAO> gDAOs = userMapper.getUserGroups(params);
        List<Group> userGroups = new ArrayList<Group>();
        gDAOs.forEach(g -> {
            Group group = new Group();
            group.setId(g.getId());
            group.setName(g.getGroupName());
            group.setDesc(g.getGroupDescription());
            userGroups.add(group);
        });
        return userGroups;
    }

    @Override
    public List<Group> getAllGroups(long orgId, int offset, int limit, String sort) {
        // Prepare parameters
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ORG_ID, orgId);
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        params.put(SORT, sort);
        List<GroupDAO> groups = groupMapper.getAllGroupsForOrganization(params);

        List<Group> toRet = new ArrayList<Group>();
        groups.forEach(g -> {
            Group group = new Group();
            group.setId(g.getId());
            group.setDesc(g.getGroupDescription());
            group.setName(g.getGroupName());
            toRet.add(group);
        });

        return toRet;
    }

    @Override
    public void createGroup(long orgId, String groupName, String groupDescription) throws GroupAlreadyExistsException {
        if (groupExists(groupName)) {
            throw new GroupAlreadyExistsException();
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ORG_ID, orgId);
        params.put(GROUP_NAME, groupName);
        params.put(GROUP_DESCRIPTION, groupDescription);
        groupMapper.createGroup(params);
    }

    @Override
    public void updateGroup(long orgId, Group group) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ID, group.getId());
        params.put(ORG_ID, orgId);
        params.put(GROUP_NAME, group.getName());
        params.put(GROUP_DESCRIPTION, group.getDesc());
        groupMapper.updateGroup(params);
    }

    @Override
    public void deleteGroup(List<Long> groupIds) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_IDS, groupIds);
        groupMapper.deleteGroup(params);
    }

    @Override
    public Group getGroup(long groupId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_ID, groupId);
        GroupDAO gDAL = groupMapper.getGroup(params);
        Group toRet = new Group();
        toRet.setId(gDAL.getId());
        toRet.setName(gDAL.getGroupName());
        toRet.setDesc(gDAL.getGroupDescription());
        return toRet;
    }

    @Override
    public List<User> getGroupMembers(long groupId, int offset, int limit, String sort) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_ID, groupId);
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        params.put(SORT, sort);
        List<UserDAO> uDAOs = groupMapper.getGroupMembers(params);
        List<User> toRet = new ArrayList<User>();
        uDAOs.forEach(u -> {
            User user = new User();
            user.setId(u.getId());
            user.setUsername(u.getUsername());
            user.setFirstName(u.getFirstName());
            user.setLastName(u.getLastName());
            user.setEmail(u.getEmail());
            user.setEnabled(u.isEnabled());
            user.setExternallyManaged(u.getExternallyManaged() != 0);
            toRet.add(user);
        });
        return toRet;
    }

    @Override
    public boolean addGroupMembers(long groupId, List<Long> userIds, List<String> usernames) {
        List<Long> allUserIds = new ArrayList<Long>();
        allUserIds.addAll(userIds);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USERNAMES, usernames);
        if (CollectionUtils.isNotEmpty(usernames)) {
            allUserIds.addAll(groupMapper.getUserIdsForUsernames(params));
        }
        if (CollectionUtils.isNotEmpty(allUserIds)) {
            params = new HashMap<String, Object>();
            params.put(USER_IDS, allUserIds);
            params.put(GROUP_ID, groupId);
            int result = groupMapper.addGroupMembers(params);
            return result > 0;
        }
        return false;
    }

    @Override
    public void removeGroupMembers(long groupId, List<Long> userIds, List<String> usernames) {
        List<Long> allUserIds = new ArrayList<Long>();
        allUserIds.addAll(userIds);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USERNAMES, usernames);
        if (CollectionUtils.isNotEmpty(usernames)) {
            allUserIds.addAll(groupMapper.getUserIdsForUsernames(params));
        }
        if (CollectionUtils.isNotEmpty(allUserIds)) {
            params = new HashMap<String, Object>();
            params.put(USER_IDS, allUserIds);
            params.put(GROUP_ID, groupId);
            groupMapper.removeGroupMembers(params);
        }
    }

    @Override
    public int getAllUsersTotal() {
        return userMapper.getAllUsersTotal();
    }

    @Override
    public String getCurrentUser() {
        String username = null;
        RequestContext context = RequestContext.getCurrent();

        if(context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            username = (String)httpSession.getAttribute(HTTP_SESSION_ATTRIBUTE_STUDIO_USER);
        } else {
            CronJobContext cronJobContext = CronJobContext.getCurrent();

            if (cronJobContext != null) {
                username = cronJobContext.getCurrentUser();
            } else {
                RepositoryEventContext repositoryEventContext = RepositoryEventContext.getCurrent();
                if (repositoryEventContext != null) {
                    username = repositoryEventContext.getCurrentUser();
                }
            }
        }

        return username;
    }

    @Override
    public String authenticate(String username, String password)
            throws BadCredentialsException, AuthenticationSystemException, EntitlementException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USER_ID, -1);
        params.put(USERNAME, username);
        UserDAO user = userMapper.getUserByIdOrUsername(params);
        if (user != null && user.isEnabled() && CryptoUtils.matchPassword(user.getPassword(), password)) {
            String token = createToken(user);
            storeSessionTicket(token);
            storeSessionUsername(username);
            return token;
        } else {
            throw new BadCredentialsException();
        }
    }

    protected String createToken(UserDAO user) {
        int timeout = Integer.parseInt(studioConfiguration.getProperty(SECURITY_SESSION_TIMEOUT));
        String token = SessionTokenUtils.createToken(user.getUsername(), timeout);
        return token;
    }

    protected String createToken(User user) {
        int timeout = Integer.parseInt(studioConfiguration.getProperty(SECURITY_SESSION_TIMEOUT));
        String token = SessionTokenUtils.createToken(user.getUsername(), timeout);
        return token;
    }

    protected void storeSessionTicket(String ticket) {
        RequestContext context = RequestContext.getCurrent();

        if(context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            httpSession.setAttribute(STUDIO_SESSION_TOKEN_ATRIBUTE, ticket);
        }
    }

    protected void storeSessionUsername(String username) {
        RequestContext context = RequestContext.getCurrent();

        if(context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            httpSession.setAttribute("studio_user", username);
        }
    }

    @Override
    public boolean validateTicket(String ticket) {
        if (ticket == null) {
            ticket = getCurrentToken();
        }
        boolean valid = false;
        if (StringUtils.isNotEmpty(ticket)) valid = true;
        return valid;
    }

    @Override
    public String getCurrentToken() {
        String ticket;
        RequestContext context = RequestContext.getCurrent();

        if (context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            ticket = (String)httpSession.getAttribute(STUDIO_SESSION_TOKEN_ATRIBUTE);
        } else {
            ticket = getJobOrEventTicket();
        }

        if (ticket == null) {
            ticket = "NOTICKET";
        }

        return ticket;
    }

    protected String getJobOrEventTicket() {
        String ticket = null;
        CronJobContext cronJobContext = CronJobContext.getCurrent();

        if (cronJobContext != null) {
            ticket = cronJobContext.getAuthenticationToken();
        } else {
            RepositoryEventContext repositoryEventContext = RepositoryEventContext.getCurrent();
            if (repositoryEventContext != null) {
                ticket = repositoryEventContext.getAuthenticationToken();
            }
        }

        return ticket;
    }

    @Override
    public boolean logout() {
        storeSessionTicket(null);
        storeSessionUsername(null);
        return true;
    }

    @Override
    public boolean changePassword(String username, String current, String newPassword)
            throws PasswordDoesNotMatchException, UserExternallyManagedException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USER_ID, -1);
        params.put(USERNAME, username);
        UserDAO user = userMapper.getUserByIdOrUsername(params);
        if (user.getExternallyManaged() > 0) {
            throw new UserExternallyManagedException();
        } else {
            if (CryptoUtils.matchPassword(user.getPassword(), current)) {
                String hashedPassword = CryptoUtils.hashPassword(newPassword);
                params = new HashMap<String, Object>();
                params.put(USERNAME, username);
                params.put(PASSWORD, hashedPassword);
                userMapper.setUserPassword(params);
                return true;
            } else {
                throw new PasswordDoesNotMatchException();
            }
        }
    }

    @Override
    public boolean setUserPassword(String username, String newPassword)
            throws UserNotFoundException, UserExternallyManagedException {
        if (!userExists(username)) {
            throw new UserNotFoundException();
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(USER_ID, -1);
            params.put(USERNAME, username);
            UserDAO user = userMapper.getUserByIdOrUsername(params);
            if (user.getExternallyManaged() > 0) {
                throw new UserExternallyManagedException();
            } else {
                String hashedPassword = CryptoUtils.hashPassword(newPassword);
                params = new HashMap<String, Object>();
                params.put(USERNAME, username);
                params.put(PASSWORD, hashedPassword);
                userMapper.setUserPassword(params);
                return true;
            }
        }
    }

    @Override
    public boolean userExists(String username) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USERNAME, username);
        Integer result = userMapper.userExists(params);
        return (result > 0);
    }

    @Override
    public boolean groupExists(String groupName) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_NAME, groupName);
        Integer result = groupMapper.groupExists(params);
        return (result > 0);
    }

    public UserMapper getUserMapper() {
        return userMapper;
    }

    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public GroupMapper getGroupMapper() {
        return groupMapper;
    }

    public void setGroupMapper(GroupMapper groupMapper) {
        this.groupMapper = groupMapper;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }
}
