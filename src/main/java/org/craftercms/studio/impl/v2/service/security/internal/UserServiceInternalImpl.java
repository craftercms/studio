/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

import com.google.common.cache.Cache;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.*;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.exception.PasswordRequirementsFailedException;
import org.craftercms.studio.api.v2.service.security.internal.AccessTokenServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.GroupServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.beans.ConstructorProperties;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toMap;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_PASSWORD_REQUIREMENTS_MINIMUM_COMPLEXITY;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_REPO_USER_USERNAME;

public class UserServiceInternalImpl implements UserServiceInternal {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceInternalImpl.class);

    private final UserDAO userDao;
    private final GroupServiceInternal groupServiceInternal;
    private final StudioConfiguration studioConfiguration;
    private final SiteService siteService;
    private final AccessTokenServiceInternal accessTokenService;
    private final SecurityService securityService;
    private final RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;
    private final Cache<String, User> userCache;
    private final Zxcvbn zxcvbn;

    @ConstructorProperties({"userDao", "groupServiceInternal", "studioConfiguration", "siteService", "securityService",
            "accessTokenService", "retryingDatabaseOperationFacade", "userCache", "zxcvbn"})
    public UserServiceInternalImpl(UserDAO userDao, GroupServiceInternal groupServiceInternal,
                                   StudioConfiguration studioConfiguration, SiteService siteService,
                                   SecurityService securityService, AccessTokenServiceInternal accessTokenService,
                                   RetryingDatabaseOperationFacade retryingDatabaseOperationFacade,
                                   Cache<String, User> userCache, Zxcvbn zxcvbn) {
        this.userDao = userDao;
        this.groupServiceInternal = groupServiceInternal;
        this.studioConfiguration = studioConfiguration;
        this.siteService = siteService;
        this.securityService = securityService;
        this.accessTokenService= accessTokenService;
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
        this.userCache = userCache;
        this.zxcvbn = zxcvbn;
    }

    protected void invalidateCache(String username) {
        userCache.invalidate(username);
    }

    protected void invalidateCache(List<String> usernames) {
        userCache.invalidateAll(usernames);
    }

    protected void invalidateCache(Collection<User> users) {
        invalidateCache(users.stream().map(User::getUsername).collect(Collectors.toList()));
    }

    @NonNull
    @Override
    public User getUserByIdOrUsername(long userId, String username)
            throws ServiceLayerException, UserNotFoundException {
        Map<String, Object> params = new HashMap<>();
        params.put(USER_ID, userId);
        params.put(USERNAME, username);
        User user;

        try {
            user = userDao.getUserByIdOrUsername(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }

        if (user == null) {
            throw new UserNotFoundException("No user found for username '" + username + "' or id '" + userId + "'");
        }

        return user;
    }

    @Override
    public List<User> getUsersByIdOrUsername(List<Long> userIds, List<String> usernames)
            throws ServiceLayerException, UserNotFoundException {
        List<User> users = new LinkedList<>();
        for(long userId : userIds) {
            users.add(getUserByIdOrUsername(userId, StringUtils.EMPTY));
        }
        for (String username : usernames) {
            if (username != null) {
                Optional<User> user = users.stream().filter(u -> u.getUsername().equals(username)).findFirst();
                if (user.isEmpty()) {
                    users.add(getUserByIdOrUsername(-1, username));
                }
            }
        }

        return users;
    }

    @Override
    public List<User> getAllUsersForSite(long orgId, List<String> groupNames, String keyword, int offset, int limit,
                                         String sort)
            throws ServiceLayerException {
        try {
            return userDao.getAllUsersForSite(groupNames, keyword, offset, limit, sort);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public List<User> getAllUsers(String keyword, int offset, int limit, String sort) throws ServiceLayerException {
        try {
            return userDao.getAllUsers(keyword, offset, limit, sort);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public int getAllUsersForSiteTotal(long orgId, String siteId, String keyword) throws ServiceLayerException {
        List<String> groupNames = groupServiceInternal.getSiteGroups(siteId);
        try {
            return userDao.getAllUsersForSiteTotal(groupNames, keyword);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public int getAllUsersTotal(String keyword) throws ServiceLayerException {
        try {
            return userDao.getAllUsersTotal(keyword);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public User createUser(User user) throws UserAlreadyExistsException, ServiceLayerException {
        if (userExists(-1, user.getUsername())) {
            throw new UserAlreadyExistsException(format("User '%s' already exists", user.getUsername()));
        }
        if (user.isExternallyManaged() || verifyPasswordRequirements(user.getPassword())) {
            Map<String, Object> params = new HashMap<>();
            params.put(USERNAME, user.getUsername());
            params.put(PASSWORD, CryptoUtils.hashPassword(user.getPassword()));
            params.put(FIRST_NAME, user.getFirstName());
            params.put(LAST_NAME, user.getLastName());
            params.put(EMAIL, user.getEmail());
            params.put(EXTERNALLY_MANAGED, user.getExternallyManagedAsInt());
            params.put(TIMEZONE, StringUtils.EMPTY);
            params.put(LOCALE, StringUtils.EMPTY);
            params.put(ENABLED, user.getEnabledAsInt());

            try {
                retryingDatabaseOperationFacade.retry(() -> userDao.createUser(params));

                user.setId((Long) params.get(ID));

                return user;
            } catch (Exception e) {
                throw new ServiceLayerException("Unknown database error", e);
            }
        } else {
            throw new PasswordRequirementsFailedException();
        }
    }

    @Override
    public boolean userExists(long userId, String username) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<>();
        params.put(USER_ID, userId);
        params.put(USERNAME, username);

        try {
            Integer result = userDao.userExists(params);
            return (result > 0);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public void updateUser(User user) throws UserNotFoundException, ServiceLayerException {
        long userId = user.getId();
        String username = user.getUsername() != null ? user.getUsername() : StringUtils.EMPTY;

        User oldUser = getUserByIdOrUsername(userId, username);

        Map<String, Object> params = new HashMap<>();
        params.put(USER_ID, oldUser.getId());
        params.put(FIRST_NAME, user.getFirstName());
        params.put(LAST_NAME, user.getLastName());
        params.put(EMAIL, user.getEmail());
        params.put(ENABLED, user.isEnabled());
        params.put(TIMEZONE, StringUtils.EMPTY);
        params.put(LOCALE, StringUtils.EMPTY);

        try {
            retryingDatabaseOperationFacade.retry(() -> userDao.updateUser(params));
            invalidateCache(oldUser.getUsername());
            // Force a re-authentication if the user is currently logged-in
            accessTokenService.deleteRefreshToken(oldUser);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public void deleteUsers(List<Long> userIds, List<String> usernames)
            throws UserNotFoundException, ServiceLayerException {
        List<User> users = getUsersByIdOrUsername(userIds, usernames);

        var ids = users.stream().map(User::getId).collect(Collectors.toList());
        Map<String, Object> params = new HashMap<>();
        params.put(USER_IDS, ids);

        try {
            retryingDatabaseOperationFacade.retry(() -> userDao.deleteUsers(params));
            invalidateCache(users);
            // Cleanup user properties...
            retryingDatabaseOperationFacade.retry(() -> userDao.deleteUserPropertiesByUserIds(ids));
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public List<User> enableUsers(List<Long> userIds, List<String> usernames, boolean enabled)
            throws ServiceLayerException, UserNotFoundException {
        List<User> users = getUsersByIdOrUsername(userIds, usernames);

        Map<String, Object> params = new HashMap<>();
        params.put(USER_IDS, users.stream().map(User::getId).collect(Collectors.toList()));
        params.put(ENABLED, enabled? 1: 0);

        try {
            retryingDatabaseOperationFacade.retry(() -> userDao.enableUsers(params));
            invalidateCache(users);
            return getUsersByIdOrUsername(userIds, usernames);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public List<Group> getUserGroups(long userId, String username)
            throws UserNotFoundException, ServiceLayerException {
        if (!userExists(userId, username)) {
            throw new UserNotFoundException("No user found for username '" + username + "' or id '" + userId + "'");
        }

        Map<String, Object> params = new HashMap<>();
        params.put(USER_ID, userId);
        params.put(USERNAME, username);

        try {
            return userDao.getUserGroups(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public boolean changePassword(String username, String current, String newPassword)
            throws PasswordDoesNotMatchException, UserExternallyManagedException, ServiceLayerException {
        Map<String, Object> params = new HashMap<>();
        params.put(USER_ID, -1);
        params.put(USERNAME, username);
        try {
            User user = userDao.getUserByIdOrUsername(params);
            if (user.isExternallyManaged()) {
                throw new UserExternallyManagedException();
            }
            if (!CryptoUtils.matchPassword(user.getPassword(), current)) {
                throw new PasswordDoesNotMatchException();
            }
            if (!verifyPasswordRequirements(newPassword)) {
                throw new PasswordRequirementsFailedException();
            }
            String hashedPassword = CryptoUtils.hashPassword(newPassword);
            HashMap<String, Object> setPasswordParams = new HashMap<>();
            setPasswordParams.put(USERNAME, username);
            setPasswordParams.put(PASSWORD, hashedPassword);
            retryingDatabaseOperationFacade.retry(() -> userDao.setUserPassword(setPasswordParams));
            invalidateCache(username);
            return true;
        } catch (RuntimeException e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public boolean setUserPassword(String username, String newPassword) throws UserNotFoundException,
            ServiceLayerException {
        if (!userExists(-1, username)) {
            throw new UserNotFoundException();
        }
        if (!verifyPasswordRequirements(newPassword)) {
            throw new PasswordRequirementsFailedException("User password does not fulfill requirements");
        }
        Map<String, Object> params = new HashMap<>();
        params.put(USER_ID, -1);
        params.put(USERNAME, username);
        try {
            User user = userDao.getUserByIdOrUsername(params);
            if (user.isExternallyManaged()) {
                throw new UserExternallyManagedException();
            } else {
                String hashedPassword = CryptoUtils.hashPassword(newPassword);
                HashMap<String, Object> setPasswordParams = new HashMap<>();
                setPasswordParams.put(USERNAME, username);
                setPasswordParams.put(PASSWORD, hashedPassword);
                retryingDatabaseOperationFacade.retry(() -> userDao.setUserPassword(setPasswordParams));
                invalidateCache(username);
                return true;
            }
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    private boolean verifyPasswordRequirements(String password) {
        Strength strength = zxcvbn.measure(password);

        return strength.getScore() >= getPasswordRequirementMinimumComplexity();
    }

    private int getPasswordRequirementMinimumComplexity() {
        return Integer.parseInt(studioConfiguration.getProperty(SECURITY_PASSWORD_REQUIREMENTS_MINIMUM_COMPLEXITY));
    }

    @Override
    public User getUserByGitName(String gitName) throws ServiceLayerException, UserNotFoundException {
        User user =  userDao.getUserByGitName(gitName);
        if (Objects.isNull(user)) {
            logger.info("Git user '{}' was not found in the database", gitName);
            user = getUserByIdOrUsername(-1, GIT_REPO_USER_USERNAME);
        }
        return user;
    }

    protected Map<String, String> getUserProperties(User user, long siteId) {
        return userDao.getUserProperties(user.getId(), siteId).stream()
                .collect(toMap(UserProperty::getKey, UserProperty::getValue));
    }

    protected String getGlobalSiteName() {
        return studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE);
    }

    protected String getActualSiteId(String siteId) {
        return StringUtils.isEmpty(siteId)? getGlobalSiteName() : siteId;
    }

    @Override
    public Map<String, Map<String, String>> getUserProperties(String siteId)
            throws ServiceLayerException {
        var actualSiteId = getActualSiteId(siteId);
        var dbSiteId = siteService.getSite(actualSiteId).getId();
        var username = securityService.getCurrentUser();
        try {
            var user = getUserByIdOrUsername(0, username);
            // TODO: Properly support multiple sites when needed
            return singletonMap(siteId, getUserProperties(user, dbSiteId));
        } catch (UserNotFoundException e) {
            // This should never happen...
            logger.error("Failed to get the current user with username '{}' in site '{}'", username, siteId, e);
            return null;
        }
    }

    @Override
    public Map<String, String> updateUserProperties(String siteId, Map<String, String> propertiesToUpdate)
            throws ServiceLayerException {
        var actualSiteId = getActualSiteId(siteId);
        var dbSiteId = siteService.getSite(actualSiteId).getId();
        var username = securityService.getCurrentUser();
        try {
            var user = getUserByIdOrUsername(0, username);
            retryingDatabaseOperationFacade.retry(() -> userDao.updateUserProperties(user.getId(), dbSiteId, propertiesToUpdate));

            return getUserProperties(user, dbSiteId);
        } catch (UserNotFoundException e) {
            // This should never happen...
            logger.error("Failed to get the current user with username '{}' in site '{}'", username, siteId, e);
            return null;
        }
    }

    @Override
    public Map<String, String> deleteUserProperties(String siteId,
                                                    List<String> propertiesToDelete)
            throws ServiceLayerException {
        var actualSiteId = getActualSiteId(siteId);
        var dbSiteId = siteService.getSite(actualSiteId).getId();
        var username = securityService.getCurrentUser();
        try {
            var user = getUserByIdOrUsername(0, username);
            retryingDatabaseOperationFacade.retry(() -> userDao.deleteUserProperties(user.getId(), dbSiteId, propertiesToDelete));

            return getUserProperties(user, dbSiteId);
        } catch (UserNotFoundException e) {
            // This should never happen...
            logger.error("Failed to get the current user with username '{}' in site '{}'", username, siteId, e);
            return null;
        }
    }

    @Override
    public AuthenticatedUser getCurrentUser() throws AuthenticationException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return (AuthenticatedUser) authentication.getPrincipal();
        } else {
            throw new AuthenticationException("User should be authenticated");
        }
    }

    @Override
    public boolean isSystemAdmin(String username) {
        return securityService.isSystemAdmin(username);
    }

}
