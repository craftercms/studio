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

package org.craftercms.studio.impl.v1.service.security;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoUtils;
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.entitlements.model.EntitlementType;
import org.craftercms.commons.entitlements.model.Module;
import org.craftercms.commons.entitlements.validator.EntitlementValidator;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.studio.api.v2.dal.GroupDAL;
import org.craftercms.studio.api.v1.dal.GroupPerSiteResult;
import org.craftercms.studio.api.v1.dal.GroupResult;
import org.craftercms.studio.api.v2.dal.SecurityMapper;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v1.dal.UserProfileResult;
import org.craftercms.studio.api.v1.ebus.RepositoryEventContext;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationSystemException;
import org.craftercms.studio.api.v1.exception.security.BadCredentialsException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.api.v1.exception.security.PasswordDoesNotMatchException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserExternallyManagedException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.job.CronJobContext;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.SessionTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.dao.DuplicateKeyException;

import javax.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_EMAIL;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_EXTERNALLY_MANAGED;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_FIRSTNAME;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_LASTNAME;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_USERNAME;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_SESSION_TIMEOUT;
import static org.craftercms.studio.impl.v1.service.security.SecurityServiceImpl.STUDIO_SESSION_TOKEN_ATRIBUTE;

public class DbSecurityProvider implements SecurityProvider {

    private static Logger logger = LoggerFactory.getLogger(DbSecurityProvider.class);

    protected SecurityMapper securityMapper;
    protected SiteFeedMapper siteFeedMapper;
    @Autowired
    protected EntitlementValidator entitlementValidator;

    protected StudioConfiguration studioConfiguration;

    @Override
    public Set<String> getUserGroups(String user) {
        Set<String> userGroups = new HashSet<String>();
        List<GroupDAL> groups = new ArrayList<>(); //securityMapper.getUserGroups(user);
        for (GroupDAL g : groups) {
            userGroups.add(g.getGroupName());
        }
        return userGroups;
    }

    @Override
    public Set<String> getUserGroupsPerSite(String user, String site) {
        Set<String> userGroups = new HashSet<String>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", user);
        params.put("siteId", site);
        List<GroupDAL> groups = new ArrayList<>();// securityMapper.getUserGroupsPerSite(params);
        for (GroupDAL g : groups) {
            userGroups.add(g.getGroupName());
        }
        return userGroups;
    }

    @Override
    public String getCurrentUser() {
        String username = null;
        RequestContext context = RequestContext.getCurrent();

        if(context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            username = (String)httpSession.getAttribute("studio_user");
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
    public Map<String, Object> getUserProfile(String user) {
        List<User> resultSet = securityMapper.getUserDetails(user);
        Map<String, Object> userProfile = new HashMap<String, Object>();
        List<Map<String, Object>> parsedRS = parseUserResultSet(resultSet);
        if (parsedRS != null && !parsedRS.isEmpty()) {
            userProfile = parsedRS.get(0);
        }
        return userProfile;
    }

    @Override
    public List<Map<String, Object>> getAllUsers(int start, int number) {
        List<User> resultSet = new ArrayList<User>();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("start", start);
        params.put("number", number);
        List<String> usernames = securityMapper.getAllUsersQuery(params);
        if (usernames != null && !usernames.isEmpty()) {
            params = new HashMap<String, Object>();
            params.put("usernames", usernames);
            //resultSet = securityMapper.getAllUsersData(params);
        }
        return parseUserResultSet(resultSet);
    }

    @Override
    public int getAllUsersTotal() {
        List<UserProfileResult> resultSet = new ArrayList<UserProfileResult>();
        Map<String, Object> params = new HashMap<String, Object>();
        return 0; //securityMapper.getAllUsersQueryTotal(params);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseUserResultSet(List<User> usersResultSet) {
        List<Map<String, Object>> toRet = new ArrayList<Map<String, Object>>();
        Map<String, Object> userProfile = new HashMap<String, Object>();
        if (usersResultSet != null && !usersResultSet.isEmpty()) {
            String lastSite = null;
            String lastUser = null;
            List<Object> sites = new ArrayList<Object>();
            Map<String, Object> site = null;
            List<Map<String, Object>> groups = null;
            for (User row : usersResultSet) {
                String username = row.getUsername();
                if (!username.equals(lastUser)) {
                    if (userProfile != null && !userProfile.isEmpty()) {
                        if (site != null) {
                            if (groups != null) {
                                site.put("groups", new ArrayList<Map<String, Object>>(groups));
                            }
                            sites.add(site);
                        }
                        userProfile.put("sites", new ArrayList(sites));
                        toRet.add(userProfile);
                    }
                    userProfile = new HashMap<String, Object>();
                    userProfile.put(KEY_USERNAME, username);
                    userProfile.put(KEY_FIRSTNAME, row.getFirstName());
                    userProfile.put(KEY_LASTNAME, row.getLastName());
                    userProfile.put(KEY_EMAIL, row.getEmail());
                    userProfile.put(KEY_EXTERNALLY_MANAGED, row.getExternallyManaged() > 0);
                    sites = new ArrayList<Object>();
                    groups = new ArrayList<Map<String, Object>>();
                    site = null;
                    lastSite = null;
                }
                /*
                String siteId = row.getSiteId();
                if (StringUtils.isNotEmpty(siteId)) {
                    if (!siteId.equals(lastSite)) {
                        if (site != null) {
                            if (groups != null) {
                                site.put("groups", new ArrayList<Map<String, Object>>(groups));
                            }
                            sites.add(site);
                        }
                        site = new HashMap<String, Object>();
                        site.put("site_id", siteId);
                        site.put("site_name", row.getSiteName());
                        groups = new ArrayList<Map<String, Object>>();
                    }
                    Map<String, Object> group = new HashMap<String, Object>();
                    group.put("group_name", row.getGroupName());
                    groups.add(group);
                    lastSite = siteId;
                }*/
                lastUser = username;
            }
            if (site != null) {
                if (groups != null) {
                    site.put("groups", new ArrayList<Map<String, Object>>(groups));
                }
                sites.add(site);
            }
            userProfile.put("sites", new ArrayList(sites));
            toRet.add(userProfile);
        }
        return toRet;
    }

    @Override
    public List<Map<String, Object>> getUsersPerSite(String site, int start, int number) throws SiteNotFoundException {
        List<Map<String, Object>> toRet = new ArrayList<Map<String, Object>>();
        if (!(siteFeedMapper.exists(site) > 0)) {
            throw new SiteNotFoundException();
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("siteId", site);
            params.put("start", start);
            params.put("number", number);
            List<String> usernames = new ArrayList<>(); //securityMapper.getUsersPerSiteQuery(params);
            if (usernames != null && !usernames.isEmpty()) {
                params = new HashMap<String, Object>();
                params.put("siteId", site);
                params.put("usernames", usernames);
                List<UserProfileResult> resultSet = new ArrayList<>(); //securityMapper.getUsersPerSiteData(params);
                Map<String, Object> userProfile = new HashMap<String, Object>();
                if (resultSet != null && !resultSet.isEmpty()) {
                    String lastUser = null;
                    List<Object> sites = new ArrayList<Object>();
                    List<Map<String, Object>> groups = null;
                    for (UserProfileResult row : resultSet) {
                        String username = row.getUsername();
                        if (!username.equals(lastUser)) {
                            if (userProfile != null && !userProfile.isEmpty()) {
                                if (groups != null) {
                                    userProfile.put("groups", groups);
                                }
                                toRet.add(userProfile);
                            }
                            userProfile = new HashMap<String, Object>();
                            userProfile.put(KEY_USERNAME, username);
                            userProfile.put(KEY_FIRSTNAME, row.getFirstName());
                            userProfile.put(KEY_LASTNAME, row.getLastName());
                            userProfile.put(KEY_EMAIL, row.getEmail());
                            userProfile.put(KEY_EXTERNALLY_MANAGED, row.getExternallyManaged() > 0);
                            groups = new ArrayList<Map<String, Object>>();
                        }
                        Map<String, Object> group = new HashMap<String, Object>();
                        group.put("group_name", row.getGroupName());
                        groups.add(group);
                        lastUser = username;
                    }
                    if (groups != null) {
                        userProfile.put("groups", groups);
                    }
                    toRet.add(userProfile);
                }
            }
        }
        return toRet;
    }

    @Override
    public int getUsersPerSiteTotal(String site) throws SiteNotFoundException {
        List<Map<String, Object>> toRet = new ArrayList<Map<String, Object>>();
        if (!(siteFeedMapper.exists(site) > 0)) {
            throw new SiteNotFoundException();
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("siteId", site);
            return 0; //securityMapper.getUsersPerSiteQueryTotal(params);
        }
    }

    @Override
    public String authenticate(String username, String password) throws BadCredentialsException,
        AuthenticationSystemException, EntitlementException {
        User user = securityMapper.getUser(username);
        if (user != null && user.isEnabled() && CryptoUtils.matchPassword(user.getPassword(), password)) {
            //byte[] randomBytes = CryptoUtils.generateRandomBytes(20);
            //String token = randomBytes.toString();
            String token = createToken(user);
            storeSessionTicket(token);
            storeSessionUsername(username);
            return token;
        } else {
            throw new BadCredentialsException();
        }
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
    public void addUserGroup(String groupName) {

    }

    @Override
    public void addUserGroup(String parentGroup, String groupName) {

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
    public boolean groupExists(String siteId, String groupName) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("groupName", groupName);
        params.put("siteId", siteId);
        Integer result =  0;//securityMapper.groupExists(params);
        return (result > 0);
    }

    @Override
    public boolean userExists(String username) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(KEY_USERNAME, username);
        Integer result = 0;//securityMapper.userExists(params);
        return (result > 0);
    }

    @Override
    public boolean userExistsInGroup(String siteId, String groupName, String username) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("groupName", groupName);
        params.put("siteId", siteId);
        params.put(KEY_USERNAME, username);
        Integer result = 0;//securityMapper.userExistsInGroup(params);
        return (result > 0);
    }

    @Override
    public boolean addUserToGroup(String siteId, String groupName, String user)
            throws UserAlreadyExistsException, UserNotFoundException, GroupNotFoundException, SiteNotFoundException {
        if (!(siteFeedMapper.exists(siteId) > 0)) {
            throw new SiteNotFoundException();
        } else if (!groupExists(siteId, groupName)) {
            throw new GroupNotFoundException();
        } else if (!userExists(user)) {
            throw new UserNotFoundException();
        } else if (userExistsInGroup(siteId, groupName, user)) {
            throw new UserAlreadyExistsException();
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("groupName", groupName);
            params.put("siteId", siteId);
            //Group group = securityMapper.getGroupObject(params);
            params = new HashMap<String, Object>();
            params.put(KEY_USERNAME, user);
            //params.put("groupId", group.getId());
            //securityMapper.addUserToGroup(params);
            return true;
        }
    }

    @Override
    public boolean removeUserFromGroup(String siteId, String groupName, String user) throws UserNotFoundException,
            GroupNotFoundException, SiteNotFoundException {
        if (!(siteFeedMapper.exists(siteId) > 0)) {
            throw new SiteNotFoundException();
        } else if (!groupExists(siteId, groupName)) {
            throw new GroupNotFoundException();
        } else if (!userExists(user) || !userExistsInGroup(siteId, groupName, user)) {
            throw new UserNotFoundException();
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("groupName", groupName);
            params.put("siteId", siteId);
            //Group group = securityMapper.getGroupObject(params);
            params = new HashMap<String, Object>();
            params.put(KEY_USERNAME, user);
            //params.put("groupId", group.getId());
            //securityMapper.removeUserFromGroup(params);
            return true;
        }
    }

    @Override
    public boolean logout() {
        storeSessionTicket(null);
        storeSessionUsername(null);
        return true;
    }

    @Override
    public void addContentWritePermission(String path, String group) {

    }

    @Override
    public void addConfigWritePermission(String path, String group) {

    }

    @Override
    public boolean createUser(String username, String password, String firstName, String lastName, String email,
                              boolean externallyManaged) throws UserAlreadyExistsException, EntitlementException {
        if (userExists(username)) {
            logger.error("Not able to create user " + username + ", already exists.");
            throw new UserAlreadyExistsException("User already exists.");
        } else {
            long start = 0;
            if(logger.getLevel().equals(Logger.LEVEL_DEBUG)) {
                start = System.currentTimeMillis();
                logger.debug("Starting entitlement validation");
            }
            entitlementValidator.validateEntitlement(Module.STUDIO, EntitlementType.USER, getAllUsersTotal(), 1);
            if(logger.getLevel().equals(Logger.LEVEL_DEBUG)) {
                logger.debug("Validation completed, duration : {0} ms", System.currentTimeMillis() - start);
            }
            String hashedPassword = CryptoUtils.hashPassword(password);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(KEY_USERNAME, username);
            params.put("password", hashedPassword);
            params.put("firstname", firstName);
            params.put("lastname", lastName);
            params.put(KEY_EMAIL, email);
            params.put("externallyManaged", externallyManaged ? 1 : 0);
            try {
                //securityMapper.createUser(params);
            } catch (DuplicateKeyException e) {
                logger.error("Error creating user " + username, e);
                throw new UserAlreadyExistsException("User already exists.", e);
            }
        }
        return true;
    }

    @Override
    public boolean deleteUser(String username) throws UserNotFoundException {
        if (!userExists(username)) {
            throw new UserNotFoundException();
        } else {
            Map<String, String> params = new HashMap<String, String>();
            params.put(KEY_USERNAME, username);
            //securityMapper.deleteUser(params);
            return true;
        }
    }

    @Override
    public boolean updateUser(String username, String firstName, String lastName, String email)
            throws UserNotFoundException, UserExternallyManagedException {
        if (!userExists(username)) {
            throw new UserNotFoundException();
        } else {
            User user = securityMapper.getUser(username);
            if (user.getExternallyManaged() > 0) {
                throw new UserExternallyManagedException();
            } else {
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_USERNAME, username);
                params.put("firstname", firstName);
                params.put("lastname", lastName);
                params.put(KEY_EMAIL, email);
                //securityMapper.updateUser(params);
                return true;
            }
        }
    }

    @Override
    public boolean enableUser(String username, boolean enabled)
            throws UserNotFoundException, UserExternallyManagedException {
        if (!userExists(username)) {
            throw new UserNotFoundException();
        } else {
            User user = securityMapper.getUser(username);
            if (user.getExternallyManaged() > 0) {
                throw new UserExternallyManagedException();
            } else {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put(KEY_USERNAME, username);
                params.put("enabled", enabled ? 1 : 0);
                //securityMapper.enableUser(params);
                return true;
            }
        }
    }

    @Override
    public Map<String, Object> getUserStatus(String user) throws UserNotFoundException {
        if (!userExists(user)) {
            throw new UserNotFoundException();
        } else {
            User u = securityMapper.getUser(user);
            Map<String, Object> userStatus = new HashMap<String, Object>();
            if (u != null) {
                userStatus.put(KEY_USERNAME, u.getUsername());
                userStatus.put("enabled", u.isEnabled());

            }
            return userStatus;
        }
    }

    @Override
    public boolean createGroup(String groupName, String description, String siteId, boolean externallyManaged)
            throws GroupAlreadyExistsException, SiteNotFoundException {

        // Get the site first
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", siteId);
        SiteFeed site = siteFeedMapper.getSite(params);

        // If we found the site, use it, otherwise it's a SiteNotFoundException
        if (site != null) {
            params = new HashMap<String, Object>();
            params.put("name", groupName);
            params.put("description", description);
            params.put("siteId", site.getId());
            params.put("externallyManaged", externallyManaged ? 1 : 0);
            try {
                //securityMapper.createGroup(params);
            } catch (DuplicateKeyException e) {
                logger.error("Error creating group " + groupName, e);
                throw new GroupAlreadyExistsException("Group already exists.", e);
            }
            return true;
        } else {
            throw new SiteNotFoundException();
        }
    }

    @Override
    public Map<String, Object> getGroup(String site, String group)
            throws GroupNotFoundException, SiteNotFoundException {
        if (!(siteFeedMapper.exists(site) > 0)) {
            throw new SiteNotFoundException();
        } else if (!groupExists(site, group)) {
            throw new GroupNotFoundException();
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("groupName", group);
            params.put("siteId", site);
            return null;// securityMapper.getGroup(params);
        }
    }

    @Override
    public List<Map<String, Object>> getAllGroups(int start, int number) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("start", start);
        params.put("number", number);
        List<Long> groupIds = new ArrayList<>(); //securityMapper.getAllGroupsQuery(params);
        List<GroupResult> resultSet = new ArrayList<GroupResult>();
        if (groupIds != null && !groupIds.isEmpty()) {
            params = new HashMap<String, Object>();
            params.put("groupids", groupIds);
            //resultSet = securityMapper.getAllGroupsData(params);
        }
        return parseGroupResultSet(resultSet);
    }

    private  List<Map<String, Object>> parseGroupResultSet(List<GroupResult> resultSet) {
        List<Map<String, Object>> toRet = new ArrayList<Map<String, Object>>();
        if (resultSet != null && !resultSet.isEmpty()) {
            String lastSite = null;
            Map<String, Object> site = null;
            List<Map<String, Object>> groups = null;
            for (GroupResult row : resultSet) {
                String siteId = row.getSiteId();
                if (!siteId.equals(lastSite)) {
                    if (site != null) {
                        if (groups != null) {
                            site.put("groups", groups);
                        }
                        toRet.add(site);
                    }
                    site = new HashMap<String, Object>();
                    site.put("site_id", siteId);
                    groups = new ArrayList<Map<String, Object>>();
                }
                Map<String, Object> group = new HashMap<String, Object>();
                group.put("group_name", row.getGroupName());
                group.put("description", row.getGroupDescription());
                groups.add(group);
                lastSite = siteId;
            }
            if (site != null) {
                if (groups != null) {
                    site.put("groups", groups);
                }
                toRet.add(site);
            }
        }
        return toRet;
    }

    @Override
    public List<Map<String, Object>> getGroupsPerSite(String site, int start, int number)
            throws SiteNotFoundException {
        if (!(siteFeedMapper.exists(site) > 0)) {
            throw new SiteNotFoundException();
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("site", site);
            params.put("start", start);
            params.put("number", number);
            List<Long> groupIds = new ArrayList<>();//securityMapper.getGroupsPerSiteQuery(params);
            List<GroupPerSiteResult> resultSet = new ArrayList<GroupPerSiteResult>();
            if (groupIds != null && !groupIds.isEmpty()) {
                params = new HashMap<String, Object>();
                params.put("site", site);
                params.put("groupids", groupIds);
                //resultSet = securityMapper.getGroupsPerSiteData(params);
            }
            return parseGroupsPerSiteResultSet(resultSet);
        }
    }

    @Override
    public int getGroupsPerSiteTotal(String site) throws SiteNotFoundException {
        if (!(siteFeedMapper.exists(site) > 0)) {
            throw new SiteNotFoundException();
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("site", site);
            return 0;// securityMapper.getGroupsPerSiteQueryTotal(params);
        }
    }

    private  List<Map<String, Object>> parseGroupsPerSiteResultSet(List<GroupPerSiteResult> resultSet) {
        List<Map<String, Object>> toRet = new ArrayList<Map<String, Object>>();
        if (resultSet != null && !resultSet.isEmpty()) {
            String lastGroup = null;
            Map<String, Object> group = null;
            List<Map<String, Object>> users = null;
            for (GroupPerSiteResult row : resultSet) {
                String groupName = row.getGroupName();
                if (!groupName.equals(lastGroup)) {
                    if (group != null) {
                        if (users != null) {
                            group.put("users", users);
                        }
                        toRet.add(group);
                    }
                    group = new HashMap<String, Object>();
                    group.put("group_name", groupName);
                    group.put("description", row.getGroupDescription());
                    users = new ArrayList<Map<String, Object>>();
                }
                if (StringUtils.isNotEmpty(row.getUsername())) {
                    Map<String, Object> user = new HashMap<String, Object>();
                    user.put(KEY_USERNAME, row.getUsername());
                    user.put(KEY_FIRSTNAME, row.getFirstName());
                    user.put(KEY_LASTNAME, row.getLastName());
                    user.put(KEY_EMAIL, row.getEmail());
                    user.put(KEY_EXTERNALLY_MANAGED, row.getExternallyManaged() > 0);
                    users.add(user);
                }
                lastGroup = groupName;
            }
            if (group != null) {
                if (users != null) {
                    group.put("users", users);
                }
                toRet.add(group);
            }
        }
        return toRet;
    }

    @Override
    public List<Map<String, Object>> getUsersPerGroup(String site, String group, int start, int number)
            throws GroupNotFoundException, SiteNotFoundException {
        if (!(siteFeedMapper.exists(site) > 0)) {
            throw new SiteNotFoundException();
        } else if (!groupExists(site, group)) {
            throw new GroupNotFoundException();
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("siteId", site);
            params.put("groupName", group);
            params.put("start", start);
            params.put("number", number);
            List<User> resultSet = new ArrayList<>(); //securityMapper.getUsersPerGroup(params);
            List<Map<String, Object>> toRet = new ArrayList<Map<String, Object>>();
            if (resultSet != null && !resultSet.isEmpty()) {
                for (User u : resultSet) {
                    Map<String, Object> userProfile = new HashMap<String, Object>();
                    userProfile.put(KEY_USERNAME, u.getUsername());
                    userProfile.put(KEY_FIRSTNAME, u.getFirstName());
                    userProfile.put(KEY_LASTNAME, u.getLastName());
                    userProfile.put(KEY_EMAIL, u.getEmail());
                    userProfile.put(KEY_EXTERNALLY_MANAGED, u.getExternallyManaged() > 0);
                    toRet.add(userProfile);
                }
            }
            return toRet;
        }
    }

    @Override
    public int getUsersPerGroupTotal(String site, String group) throws GroupNotFoundException, SiteNotFoundException {
        if (!(siteFeedMapper.exists(site) > 0)) {
            throw new SiteNotFoundException();
        } else if (!groupExists(site, group)) {
            throw new GroupNotFoundException();
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("siteId", site);
            params.put("groupName", group);
            return securityMapper.getUsersPerGroupTotal(params);
        }
    }

    @Override
    public boolean updateGroup(String siteId, String groupName, String description)
            throws GroupNotFoundException, SiteNotFoundException {
        if (!(siteFeedMapper.exists(siteId) > 0)) {
            throw new SiteNotFoundException();
        } else if (!groupExists(siteId, groupName)) {
            throw new GroupNotFoundException();
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("siteId", siteId);
            SiteFeed site = siteFeedMapper.getSite(params);
            params = new HashMap<String, Object>();
            params.put("groupName", groupName);
            params.put("siteId", site.getId());
            params.put("description", description);
            securityMapper.updateGroup(params);
            return true;
        }
    }

    @Override
    public boolean deleteGroup(String siteId, String groupName) throws GroupNotFoundException, SiteNotFoundException {
        if (!(siteFeedMapper.exists(siteId) > 0)) {
            throw new SiteNotFoundException();
        } else if (!groupExists(siteId, groupName)) {
            throw new GroupNotFoundException();
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("siteId", siteId);
            SiteFeed site = siteFeedMapper.getSite(params);
            params = new HashMap<String, Object>();
            params.put("groupName", groupName);
            params.put("siteId", site.getId());
            securityMapper.deleteGroup(params);
            return true;
        }
    }

    @Override
    public boolean changePassword(String username, String current, String newPassword)
            throws PasswordDoesNotMatchException, UserExternallyManagedException {
        User user = securityMapper.getUser(username);
        if (user.getExternallyManaged() > 0) {
            throw new UserExternallyManagedException();
        } else {
            if (CryptoUtils.matchPassword(user.getPassword(), current)) {
                String hashedPassword = CryptoUtils.hashPassword(newPassword);
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_USERNAME, username);
                params.put("password", hashedPassword);
                securityMapper.setUserPassword(params);
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
            User user = securityMapper.getUser(username);
            if (user.getExternallyManaged() > 0) {
                throw new UserExternallyManagedException();
            } else {
                String hashedPassword = CryptoUtils.hashPassword(newPassword);
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_USERNAME, username);
                params.put("password", hashedPassword);
                securityMapper.setUserPassword(params);
                return true;
            }
        }
    }

    @Override
    public boolean isSystemUser(String username) throws UserNotFoundException {
        if (!userExists(username)) {
            throw new UserNotFoundException();
        } else {
            Map<String, String> params = new HashMap<String, String>();
            params.put(KEY_USERNAME, username);
            int result = securityMapper.isSystemUser(params);
            return result > 0;
        }
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public SecurityMapper getSecurityMapper() {
        return securityMapper;
    }

    public void setSecurityMapper(SecurityMapper securityMapper) {
        this.securityMapper = securityMapper;
    }

    public SiteFeedMapper getSiteFeedMapper() {
        return siteFeedMapper;
    }

    public void setSiteFeedMapper(SiteFeedMapper siteFeedMapper) {
        this.siteFeedMapper = siteFeedMapper;
    }
}
