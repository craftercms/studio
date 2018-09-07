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
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationSystemException;
import org.craftercms.studio.api.v1.exception.security.BadCredentialsException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.PasswordDoesNotMatchException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserExternallyManagedException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.job.CronJobContext;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.GroupTO;
import org.craftercms.studio.api.v2.dal.GroupDAO;
import org.craftercms.studio.api.v2.dal.UserTO;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.model.AuthenticationType;
import org.craftercms.studio.api.v2.service.security.SecurityProvider;
import org.craftercms.studio.impl.v1.util.SessionTokenUtils;
import org.craftercms.studio.model.User;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.craftercms.studio.api.v1.constant.StudioConstants.HTTP_SESSION_ATTRIBUTE_AUTHENTICATION;
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

    protected UserDAO userDAO;
    protected GroupDAO groupDAO;
    protected StudioConfiguration studioConfiguration;

    @Override
    public List<User> getAllUsersForSite(long orgId, List<String> groupNames, int offset, int limit, String sort)
        throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_NAMES, groupNames);
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        params.put(SORT, StringUtils.EMPTY);
        List<UserTO> userTOS;
        try {
            userTOS = userDAO.getAllUsersForSite(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
        List<User> users = new ArrayList<User>();
        userTOS.forEach(userTO -> {
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
    public User createUser(User user) throws UserAlreadyExistsException, ServiceLayerException {
        if (userExists(user.getUsername())) {
            throw new UserAlreadyExistsException();
        }
        Map<String, Object> params = new HashMap<>();
        params.put(USERNAME, user.getUsername());
        String hashedPassword = CryptoUtils.hashPassword(user.getPassword());
        params.put(PASSWORD, hashedPassword);
        params.put(FIRST_NAME, user.getFirstName());
        params.put(LAST_NAME, user.getLastName());
        params.put(EMAIL, user.getEmail());
        params.put(EXTERNALLY_MANAGED, user.isExternallyManaged() ? 1 : 0);
        params.put(TIMEZONE, StringUtils.EMPTY);
        params.put(LOCALE, StringUtils.EMPTY);
        params.put(ENABLED, user.isEnabled() ? 1 : 0);
        try {
            userDAO.createUser(params);

            user.setId((Long) params.get(ID));

            return user;
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public void updateUser(User user) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USER_ID, user.getId());
        params.put(FIRST_NAME, user.getFirstName());
        params.put(LAST_NAME, user.getLastName());
        params.put(EMAIL, user.getEmail());
        params.put(TIMEZONE, StringUtils.EMPTY);
        params.put(LOCALE, StringUtils.EMPTY);
        try {
            userDAO.updateUser(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public void deleteUsers(List<Long> userIds, List<String> usernames) throws ServiceLayerException {
        List<Long> allUserIds = new ArrayList<Long>();
        allUserIds.addAll(userIds);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USERNAMES, usernames);
        try {
            if (CollectionUtils.isNotEmpty(usernames)) {
                allUserIds.addAll(userDAO.getUserIdsForUsernames(params));
            }
            if (CollectionUtils.isNotEmpty(allUserIds)) {
                params = new HashMap<String, Object>();
                params.put(USER_IDS, allUserIds);
                userDAO.deleteUsers(params);
            }
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public User getUserByIdOrUsername(long userId, String username) throws ServiceLayerException, UserNotFoundException {
        Map<String, Object> params = new HashMap<>();
        params.put(USER_ID, userId);
        params.put(USERNAME, username);
        UserTO userTO;
        try {
            userTO = userDAO.getUserByIdOrUsername(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
        if(userTO == null) {
            throw new UserNotFoundException("No user found for username '" + username + "' or id '" + userId + "'");
        }
        User user = new User();
        user.setId(userTO.getId());
        user.setUsername(userTO.getUsername());
        user.setFirstName(userTO.getFirstName());
        user.setLastName(userTO.getLastName());
        user.setEmail(userTO.getEmail());
        user.setEnabled(userTO.isEnabled());
        user.setExternallyManaged(userTO.getExternallyManaged() != 0);
        return user;
    }

    @Override
    public List<User> enableUsers(List<Long> userIds, List<String> usernames, boolean enabled) throws ServiceLayerException, UserNotFoundException {
        List<User> users = findUsers(userIds, usernames);

        Map<String, Object> params = new HashMap<>();
        params.put(USER_IDS, users.stream().map(User::getId).collect(Collectors.toList()));
        params.put(ENABLED, enabled? 1: 0);
        try {
            userDAO.enableUsers(params);
            return findUsers(userIds, usernames);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public List<GroupTO> getUserGroups(long userId, String username) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USER_ID, userId);
        params.put(USERNAME, username);
        List<GroupTO> gDAOs;
        try {
            gDAOs = userDAO.getUserGroups(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
        return gDAOs;
    }

    @Override
    public List<GroupTO> getAllGroups(long orgId, int offset, int limit, String sort) throws ServiceLayerException {
        // Prepare parameters
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ORG_ID, orgId);
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        params.put(SORT, sort);
        List<GroupTO> groups;
        try {
            groups = groupDAO.getAllGroupsForOrganization(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }

        return groups;
    }

    @Override
    public GroupTO createGroup(long orgId, String groupName, String groupDescription) throws
            GroupAlreadyExistsException,
        ServiceLayerException {
        if (groupExists(groupName)) {
            throw new GroupAlreadyExistsException();
        }

        Map<String, Object> params = new HashMap<>();
        params.put(ORG_ID, orgId);
        params.put(GROUP_NAME, groupName);
        params.put(GROUP_DESCRIPTION, groupDescription);
        try {
            groupDAO.createGroup(params);

            GroupTO group = new GroupTO();
            group.setId((Long) params.get(ID));
            group.setGroupName(groupName);
            group.setGroupDescription(groupDescription);

            return group;
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public GroupTO updateGroup(long orgId, GroupTO group) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<>();
        params.put(ID, group.getId());
        params.put(ORG_ID, orgId);
        params.put(GROUP_NAME, group.getGroupName());
        params.put(GROUP_DESCRIPTION, group.getGroupDescription());
        try {
            groupDAO.updateGroup(params);
            return group;
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public void deleteGroup(List<Long> groupIds) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_IDS, groupIds);
        try {
            groupDAO.deleteGroups(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public GroupTO getGroup(long groupId) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_ID, groupId);
        GroupTO gDAL;
        try {
            gDAL = groupDAO.getGroup(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
        return gDAL;
    }

    @Override
    public List<User> getGroupMembers(long groupId, int offset, int limit, String sort) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_ID, groupId);
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        params.put(SORT, sort);
        List<UserTO> userTOs;
        try {
            userTOs = groupDAO.getGroupMembers(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
        List<User> toRet = new ArrayList<User>();
        userTOs.forEach(u -> {
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
    public List<User> addGroupMembers(long groupId, List<Long> userIds, List<String> usernames)
        throws ServiceLayerException, UserNotFoundException {
        List<User> users = findUsers(userIds, usernames);

        Map<String, Object> params = new HashMap<>();
        params.put(USER_IDS, users.stream().map(User::getId).collect(Collectors.toList()));
        params.put(GROUP_ID, groupId);
        try {
            groupDAO.addGroupMembers(params);
            return users;
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public void removeGroupMembers(long groupId, List<Long> userIds, List<String> usernames)
        throws ServiceLayerException, UserNotFoundException {
        List<User> users = findUsers(userIds, usernames);

        Map<String, Object> params = new HashMap<>();
        params.put(USER_IDS, users.stream().map(User::getId).collect(Collectors.toList()));
        params.put(GROUP_ID, groupId);
        try {
            groupDAO.removeGroupMembers(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public int getAllUsersTotal() throws ServiceLayerException {
        try {
            return userDAO.getAllUsersTotal();
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public String getCurrentUser() {
        String username = null;
        RequestContext context = RequestContext.getCurrent();

        if(context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            Authentication auth = (Authentication) httpSession.getAttribute(HTTP_SESSION_ATTRIBUTE_AUTHENTICATION);

            if (auth != null) {
                username = auth.getUsername();
            }
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
    public String authenticate(String username, String password) throws BadCredentialsException, AuthenticationSystemException, EntitlementException, UserNotFoundException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USER_ID, -1);
        params.put(USERNAME, username);
        UserTO user;
        try {
            user = userDAO.getUserByIdOrUsername(params);
        } catch (Exception e) {
            throw new AuthenticationSystemException("Unknown database error", e);
        }
        if (user != null && user.isEnabled() && CryptoUtils.matchPassword(user.getPassword(), password)) {
            String token = createToken(user);

            storeAuthentication(new Authentication(username, token, AuthenticationType.DB));

            return token;
        } else {
            throw new BadCredentialsException();
        }
    }

    protected String createToken(UserTO user) {
        int timeout = Integer.parseInt(studioConfiguration.getProperty(SECURITY_SESSION_TIMEOUT));
        String token = SessionTokenUtils.createToken(user.getUsername(), timeout);
        return token;
    }

    protected String createToken(User user) {
        int timeout = Integer.parseInt(studioConfiguration.getProperty(SECURITY_SESSION_TIMEOUT));
        String token = SessionTokenUtils.createToken(user.getUsername(), timeout);
        return token;
    }

    protected void storeAuthentication(Authentication authentication) {
        RequestContext context = RequestContext.getCurrent();
        if(context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            httpSession.setAttribute(HTTP_SESSION_ATTRIBUTE_AUTHENTICATION, authentication);
        }
    }

    protected void deleteAuthentication() {
        RequestContext context = RequestContext.getCurrent();
        if(context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            httpSession.removeAttribute(HTTP_SESSION_ATTRIBUTE_AUTHENTICATION);
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
        String ticket = null;
        RequestContext context = RequestContext.getCurrent();

        if (context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            Authentication auth = (Authentication) httpSession.getAttribute(HTTP_SESSION_ATTRIBUTE_AUTHENTICATION);

            if (auth != null) {
                ticket = auth.getToken();
            }
        } else {
            ticket = getJobOrEventTicket();
        }

        if (ticket == null) {
            ticket = "NOTICKET";
        }

        return ticket;
    }

    @Override
    public Authentication getAuthentication() {
        Authentication auth = null;
        RequestContext context = RequestContext.getCurrent();

        if (context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            auth = (Authentication) httpSession.getAttribute(HTTP_SESSION_ATTRIBUTE_AUTHENTICATION);
        }

        return auth;
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
        deleteAuthentication();

        return true;
    }

    @Override
    public boolean changePassword(String username, String current, String newPassword)
        throws PasswordDoesNotMatchException, UserExternallyManagedException, ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USER_ID, -1);
        params.put(USERNAME, username);
        try {
            UserTO user = userDAO.getUserByIdOrUsername(params);
            if (user.getExternallyManaged() > 0) {
                throw new UserExternallyManagedException();
            } else {
                if (CryptoUtils.matchPassword(user.getPassword(), current)) {
                    String hashedPassword = CryptoUtils.hashPassword(newPassword);
                    params = new HashMap<String, Object>();
                    params.put(USERNAME, username);
                    params.put(PASSWORD, hashedPassword);
                    userDAO.setUserPassword(params);
                    return true;
                } else {
                    throw new PasswordDoesNotMatchException();
                }
            }
        } catch (RuntimeException e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public boolean setUserPassword(String username, String newPassword) throws UserNotFoundException,
        UserExternallyManagedException, ServiceLayerException {
        if (!userExists(username)) {
            throw new UserNotFoundException();
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(USER_ID, -1);
            params.put(USERNAME, username);
            try {
                UserTO user = userDAO.getUserByIdOrUsername(params);
                if (user.getExternallyManaged() > 0) {
                    throw new UserExternallyManagedException();
                } else {
                    String hashedPassword = CryptoUtils.hashPassword(newPassword);
                    params = new HashMap<String, Object>();
                    params.put(USERNAME, username);
                    params.put(PASSWORD, hashedPassword);
                    userDAO.setUserPassword(params);
                    return true;
                }
            } catch (Exception e) {
                throw new ServiceLayerException("Unknown database error", e);
            }
        }
    }

    @Override
    public boolean userExists(String username) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USERNAME, username);
        try {
            Integer result = userDAO.userExists(params);
            return (result > 0);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public boolean groupExists(String groupName) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_NAME, groupName);
        try {
            Integer result = groupDAO.groupExists(params);
            return (result > 0);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    protected List<User> findUsers(List<Long> userIds, List<String> usernames) throws ServiceLayerException, UserNotFoundException {
        List<User> users = new LinkedList<>();
        for(long userId : userIds) {
            users.add(getUserByIdOrUsername(userId, Long.toString(userId)));
        }
        for(String username : usernames) {
            Optional<User> user = users.stream().filter(u -> u.getUsername().equals(username)).findFirst();
            if(!user.isPresent()) {
                users.add(getUserByIdOrUsername(-1, username));
            }
        }
        return users;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public GroupDAO getGroupDAO() {
        return groupDAO;
    }

    public void setGroupDAO(GroupDAO groupDAO) {
        this.groupDAO = groupDAO;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }
}
