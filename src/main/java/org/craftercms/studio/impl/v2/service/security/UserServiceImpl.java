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

package org.craftercms.studio.impl.v2.service.security;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.entitlements.model.EntitlementType;
import org.craftercms.commons.entitlements.validator.EntitlementValidator;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.*;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.AuditLogParameter;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.exception.security.ActionsDeniedException;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.api.v2.service.security.internal.GroupServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.system.InstanceService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.security.password.ForgotPasswordTaskFactory;
import org.craftercms.studio.model.AuthenticatedUser;
import org.craftercms.studio.model.Site;
import org.craftercms.studio.model.rest.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.joinWith;
import static org.craftercms.studio.api.v1.constant.StudioConstants.REMOVE_SYSTEM_ADMIN_MEMBER_LOCK;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SYSTEM_ADMIN_GROUP;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.*;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_REPO_USER_USERNAME;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.*;

public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String TOKEN_DELIMITER = "|";

    private UserServiceInternal userServiceInternal;
    private ConfigurationService configurationService;
    private GroupServiceInternal groupServiceInternal;
    private SiteService siteService;
    private EntitlementValidator entitlementValidator;
    private GeneralLockService generalLockService;
    private SecurityService securityService;
    private StudioConfiguration studioConfiguration;
    private AuditServiceInternal auditServiceInternal;
    private InstanceService instanceService;
    private TextEncryptor encryptor;
    private org.craftercms.studio.api.v2.service.security.SecurityService securityServiceV2;
    private SessionRegistry sessionRegistry;
    private TaskExecutor taskExecutor;
    private ObjectFactory<ForgotPasswordTaskFactory> forgotPasswordTaskFactory;

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
    public List<UserResponse> getAllUsersForSite(long orgId, String siteId, String keyword, int offset, int limit, String sort)
            throws ServiceLayerException {
        List<String> groupNames = groupServiceInternal.getSiteGroups(siteId);
        List<User> users = userServiceInternal.getAllUsersForSite(orgId, groupNames, keyword, offset, limit, sort);
        return users.stream().map(user -> new UserResponse(user)).collect(Collectors.toList());
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
    public List<UserResponse> getAllUsers(String keyword, int offset, int limit, String sort) throws ServiceLayerException {
        List<User> users = userServiceInternal.getAllUsers(keyword, offset, limit, sort);
        return users.stream().map(user -> new UserResponse(user)).collect(Collectors.toList());
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
    public int getAllUsersForSiteTotal(long orgId, String siteId, String keyword) throws ServiceLayerException {
        return userServiceInternal.getAllUsersForSiteTotal(orgId, siteId, keyword);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
    public int getAllUsersTotal(String keyword) throws ServiceLayerException {
        return userServiceInternal.getAllUsersTotal(keyword);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CREATE_USERS)
    public UserResponse createUser(User user) throws UserAlreadyExistsException, ServiceLayerException, AuthenticationException {
        try {
            entitlementValidator.validateEntitlement(EntitlementType.USER, 1);
        } catch (EntitlementException e) {
            throw new ServiceLayerException("Unable to complete request due to entitlement limits. Please contact " +
                                            "your system administrator.", e);
        }
        User toRet = userServiceInternal.createUser(user);
        SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_CREATE);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(getCurrentUser().getUsername());
        auditLog.setPrimaryTargetId(user.getUsername());
        auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
        auditLog.setPrimaryTargetValue(user.getUsername());
        auditServiceInternal.insertAuditLog(auditLog);
        return new UserResponse(toRet);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_UPDATE_USERS)
    public void updateUser(User user) throws ServiceLayerException, UserNotFoundException, AuthenticationException, UserExternallyManagedException {
        checkExternallyManagedUsers(Arrays.asList(user.getId()), Arrays.asList(user.getUsername()));

        userServiceInternal.updateUser(user);
        User updatedUser = userServiceInternal.getUserByIdOrUsername(user.getId(), StringUtils.EMPTY);
        SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_UPDATE);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(getCurrentUser().getUsername());
        auditLog.setPrimaryTargetId(updatedUser.getUsername());
        auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
        auditLog.setPrimaryTargetValue(updatedUser.getUsername());
        auditServiceInternal.insertAuditLog(auditLog);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_DELETE_USERS)
    public void deleteUsers(List<Long> userIds, List<String> usernames)
            throws ServiceLayerException, AuthenticationException, UserNotFoundException, UserExternallyManagedException {
        User currentUser = getCurrentUser();

        checkExternallyManagedUsers(userIds, usernames);

        if (CollectionUtils.containsAny(userIds, List.of(currentUser.getId())) ||
                CollectionUtils.containsAny(usernames, List.of(currentUser.getUsername()))) {
            throw new ServiceLayerException("Cannot delete self.");
        }

        User gitRepoUser = userServiceInternal.getUserByIdOrUsername(-1, GIT_REPO_USER_USERNAME);
        if (CollectionUtils.containsAny(userIds, List.of(gitRepoUser.getId())) ||
                CollectionUtils.containsAny(usernames, List.of(gitRepoUser.getUsername()))) {
            throw new ServiceLayerException("Cannot delete generic Git Repo User.");
        }

        generalLockService.lock(REMOVE_SYSTEM_ADMIN_MEMBER_LOCK);
        try {
            try {
                Group g = groupServiceInternal.getGroupByName(SYSTEM_ADMIN_GROUP);
                List<User> members =
                        groupServiceInternal.getGroupMembers(g.getId(), 0, Integer.MAX_VALUE, StringUtils.EMPTY);
                if (CollectionUtils.isNotEmpty(members)) {
                    List<User> membersAfterRemove = new LinkedList<>(members);
                    members.forEach(m -> {
                        if (CollectionUtils.isNotEmpty(userIds)) {
                            if (userIds.contains(m.getId())) {
                                membersAfterRemove.remove(m);
                            }
                        }
                        if (CollectionUtils.isNotEmpty(usernames)) {
                            if (usernames.contains(m.getUsername())) {
                                membersAfterRemove.remove(m);
                            }
                        }
                    });
                    if (CollectionUtils.isEmpty(membersAfterRemove)) {
                        throw new ServiceLayerException("Removing all members of the System Admin group is not allowed." +
                                " We must have at least one system administrator.");
                    }
                }
            } catch (GroupNotFoundException e) {
                throw new ServiceLayerException("The System Admin group is not found.", e);
            }

            List<User> toDelete = userServiceInternal.getUsersByIdOrUsername(userIds, usernames);
            userServiceInternal.deleteUsers(userIds, usernames);

            logger.debug("Search the current sessions for deleted users '{}'", toDelete);
            Set<AuthenticatedUser> principals = sessionRegistry.getAllPrincipals().stream()
                    .map(principal -> (AuthenticatedUser) principal)
                    .filter(authenticatedUser -> toDelete.stream()
                                                    .anyMatch(user -> authenticatedUser.getId() == user.getId()))
                    .collect(toSet());
            principals.forEach(principal -> {
                // Invalidate any open session
                List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                sessions.forEach(session -> {
                    logger.debug("Invalidate the session '{}' for user '{}'",
                                    session.getSessionId(), principal.getUsername());
                    session.expireNow();
                });
            });

            SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
            AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
            auditLog.setOperation(OPERATION_DELETE);
            auditLog.setSiteId(siteFeed.getId());
            auditLog.setActorId(getCurrentUser().getUsername());
            auditLog.setPrimaryTargetId(siteFeed.getSiteId());
            auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
            auditLog.setPrimaryTargetValue(siteFeed.getName());
            List<AuditLogParameter> parameters = new ArrayList<>();
            for (User deletedUser : toDelete) {
                AuditLogParameter parameter = new AuditLogParameter();
                parameter.setTargetId(Long.toString(deletedUser.getId()));
                parameter.setTargetType(TARGET_TYPE_USER);
                parameter.setTargetValue(deletedUser.getUsername());
                parameters.add(parameter);
            }
            auditLog.setParameters(parameters);
            auditServiceInternal.insertAuditLog(auditLog);
        } finally {
            generalLockService.unlock(REMOVE_SYSTEM_ADMIN_MEMBER_LOCK);
        }
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
    public User getUserByIdOrUsername(long userId, String username)
            throws ServiceLayerException, UserNotFoundException {
        return userServiceInternal.getUserByIdOrUsername(userId, username);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_UPDATE_USERS)
    public List<UserResponse> enableUsers(List<Long> userIds, List<String> usernames,
                                  boolean enabled) throws ServiceLayerException, UserNotFoundException, AuthenticationException, UserExternallyManagedException {
        checkExternallyManagedUsers(userIds, usernames);

        List<User> users = userServiceInternal.enableUsers(userIds, usernames, enabled);
        SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setSiteId(siteFeed.getId());
        if (enabled) {
            auditLog.setOperation(OPERATION_ENABLE);
        } else {
            auditLog.setOperation(OPERATION_DISABLE);
        }
        auditLog.setActorId(getCurrentUser().getUsername());
        auditLog.setPrimaryTargetId(siteFeed.getSiteId());
        auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
        auditLog.setPrimaryTargetValue(siteFeed.getName());
        List<AuditLogParameter> parameters = new ArrayList<>();
        for (User u : users) {
            AuditLogParameter parameter = new AuditLogParameter();
            parameter.setTargetId(Long.toString(u.getId()));
            parameter.setTargetType(TARGET_TYPE_USER);
            parameter.setTargetValue(u.getUsername());
            parameters.add(parameter);
        }
        auditLog.setParameters(parameters);
        auditServiceInternal.insertAuditLog(auditLog);

        return users.stream().map(user -> new UserResponse(user)).collect(Collectors.toList());
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
    public List<Site> getUserSites(long userId, String username) throws ServiceLayerException, UserNotFoundException {
        List<Site> sites = new ArrayList<>();
        Set<String> allSites = siteService.getAllAvailableSites();
        List<Group> userGroups = userServiceInternal.getUserGroups(userId, username);
        boolean isSysAdmin = securityService.isSystemAdmin(username);

        // Iterate all sites. If the user has any of the site groups, it has access to the site
        for (String siteId : allSites) {
            List<String> siteGroups = groupServiceInternal.getSiteGroups(siteId);
            if (isSysAdmin || userGroups.stream().anyMatch(userGroup -> siteGroups.contains(userGroup.getGroupName()))) {
                try {
                    SiteFeed siteFeed = siteService.getSite(siteId);
                    Site site = new Site();
                    site.setSiteId(siteFeed.getSiteId());
                    site.setUuid(siteFeed.getSiteUuid());
                    site.setName(siteFeed.getName());
                    site.setDesc(siteFeed.getDescription());
                    site.setState(siteFeed.getState());

                    sites.add(site);
                } catch (SiteNotFoundException e) {
                    logger.error("Site '{}' was not found while getting user sites for user '{}'",
                            siteId, username, e);
                }
            }
        }

        return sites;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
    public List<String> getUserSiteRoles(long userId, String username, String site)
            throws ServiceLayerException, UserNotFoundException {
        List<Group> groups = userServiceInternal.getUserGroups(userId, username);

        if (CollectionUtils.isEmpty(groups)) {
            return Collections.emptyList();
        }

        Map<String, List<String>> roleMappings = configurationService.getRoleMappings(site);
        Set<String> userRoles = new LinkedHashSet<>();

        if (MapUtils.isEmpty(roleMappings)) {
            return Collections.emptyList();
        }

        if (securityService.isSystemAdmin(username)) {
            // If system_admin, return all roles
            Collection<List<String>> roleSets = roleMappings.values();
            for (List<String> roleSet : roleSets) {
                userRoles.addAll(roleSet);
            }
        } else {
            for (Group group : groups) {
                String groupName = group.getGroupName();
                List<String> roles = roleMappings.get(groupName);
                if (CollectionUtils.isNotEmpty(roles)) {
                    userRoles.addAll(roles);
                }
            }
        }

        return new ArrayList<>(userRoles);
    }

    @Override
    public AuthenticatedUser getCurrentUser() throws AuthenticationException {
        return userServiceInternal.getCurrentUser();
    }

    @Override
    public List<Site> getCurrentUserSites() throws AuthenticationException, ServiceLayerException {
        var authentication = securityService.getAuthentication();
        if (authentication != null) {
            try {
                return getUserSites(-1, authentication.getName());
            } catch (UserNotFoundException e) {
                // Shouldn't happen
                throw new IllegalStateException(e);
            }
        } else {
            throw new AuthenticationException("User should be authenticated");
        }
    }

    @Override
    public List<String> getCurrentUserSiteRoles(String site) throws AuthenticationException, ServiceLayerException {
        var authentication = securityService.getAuthentication();
        if (authentication != null) {
            try {
                return getUserSiteRoles(-1, authentication.getName(), site);
            } catch (UserNotFoundException e) {
                // Shouldn't happen
                throw new IllegalStateException(e);
            }
        } else {
            throw new AuthenticationException("User should be authenticated");
        }
    }

    @Override
    public void forgotPassword(String username) {
        try {
            ForgotPasswordTaskFactory taskFactory = forgotPasswordTaskFactory.getObject();
            taskExecutor.execute(taskFactory.prepareTask(username));
        } catch (Exception e) {
            logger.error("Failed to get forgot password task for username '{}'", username, e);
        }
    }

    @Override
    public String getForgotPasswordToken(final String username) {
        long timestamp = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(
                Long.parseLong(studioConfiguration.getProperty(SECURITY_FORGOT_PASSWORD_TOKEN_TIMEOUT)));
        String salt = studioConfiguration.getProperty(SECURITY_CIPHER_SALT);
        String studioId = instanceService.getInstanceId();
        String token = joinWith(TOKEN_DELIMITER, username, studioId, timestamp, salt);

        return encryptToken(token);
    }

    private String encryptToken(String token) {
        try {
            String hashedToken = encryptor.encrypt(token);
            return Base64.getEncoder().encodeToString(hashedToken.getBytes(StandardCharsets.UTF_8));
        } catch (CryptoException e) {
            logger.error("Failed to encrypt the forgot password token", e);
            return null;
        }
    }

    protected String decryptToken(String hashedToken) {
        try {
            byte[] hashedTokenBytes = Base64.getDecoder().decode(hashedToken.getBytes(StandardCharsets.UTF_8));
            return encryptor.decrypt(new String(hashedTokenBytes, StandardCharsets.UTF_8));
        } catch (CryptoException e) {
            logger.error("Failed to decrypt the forgot password token", e);
            return null;
        }
    }

    @Override
    public UserResponse changePassword(String username, String current, String newPassword)
            throws PasswordDoesNotMatchException, UserExternallyManagedException, ServiceLayerException,
            AuthenticationException, UserNotFoundException {
        AuthenticatedUser currentUser = getCurrentUser();
        if (currentUser == null || !StringUtils.equals(username, currentUser.getUsername())) {
            throw new ActionsDeniedException("Cannot change password: current logged in user does not match provided username");
        }
        boolean success = userServiceInternal.changePassword(username, current, newPassword);
        if (success) {
            User user = userServiceInternal.getUserByIdOrUsername(-1, username);
            return new UserResponse(user);
        }
        throw new ServiceLayerException("Failed to change password");
    }

    @Override
    public UserResponse setPassword(String token, String newPassword) throws UserNotFoundException,
            UserExternallyManagedException, ServiceLayerException {
        if (!validateToken(token)) {
            return null;
        }
        String username = getUsernameFromToken(token);
        if (!StringUtils.isNotEmpty(username)) {
            throw new UserNotFoundException("User not found");
        }
        User user = userServiceInternal.getUserByIdOrUsername(-1, username);
        if (!user.isEnabled()) {
            return null;
        }
        boolean success = userServiceInternal.setUserPassword(username, newPassword);
        if (success) {
            return new UserResponse(user);
        }
        return null;
    }

    @Override
    public boolean validateToken(String token) throws UserNotFoundException,
            UserExternallyManagedException, ServiceLayerException {
        String decryptedToken = decryptToken(token);
        if (StringUtils.isEmpty(decryptedToken)) {
            logger.warn("Failed to validate forgot password token. The decrypted token is empty.");
            return false;
        }

        return validateDecryptedToken(decryptedToken);
    }

    protected boolean validateDecryptedToken(String decryptedToken)
            throws UserNotFoundException, ServiceLayerException, UserExternallyManagedException {
        StringTokenizer tokenElements = new StringTokenizer(decryptedToken, TOKEN_DELIMITER);
        if (tokenElements.countTokens() != 4) {
            logger.warn("Failed to validate forgot password token. Found '{}' elements when expecting 4.",
                    tokenElements.countTokens());
            return false;
        }

        String username = tokenElements.nextToken();
        User userProfile = userServiceInternal.getUserByIdOrUsername(-1, username);
        if (userProfile == null) {
            logger.warn("Failed to validate forgot password token. User profile not found for username '{}'",
                    username);
            throw new UserNotFoundException();
        }

        if (userProfile.isExternallyManaged()) {
            logger.warn("Failed to validate forgot password token. User '{}' is externally managed and therefore " +
                    "the password is not managed by us.", username);
            throw new UserExternallyManagedException();
        }

        String studioId = tokenElements.nextToken();
        if (!StringUtils.equals(studioId, instanceService.getInstanceId())) {
            logger.warn("Failed to validate forgot password token. Token's Studio instance ID is '{}' and " +
                            "does not match the current value '{}'",
                            studioId, instanceService.getInstanceId());
            return false;
        }

        long tokenTimestamp = Long.parseLong(tokenElements.nextToken());
        boolean isExpired = tokenTimestamp < System.currentTimeMillis();
        if (isExpired) {
            logger.info("Failed to validate forgot password token. The token timestamp '{}' is in the past.",
                    tokenTimestamp);
        }

        return !isExpired;
    }

    private String getUsernameFromToken(String token) {
        String toRet = StringUtils.EMPTY;
        String decryptedToken = decryptToken(token);
        if (StringUtils.isNotEmpty(decryptedToken)) {
            StringTokenizer tokenElements = new StringTokenizer(decryptedToken, TOKEN_DELIMITER);
            if (tokenElements.countTokens() == 4) {
                toRet = tokenElements.nextToken();
            }
        }
        return toRet;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_UPDATE_USERS)
    public boolean resetPassword(String username, String newPassword)
            throws UserNotFoundException, ServiceLayerException {
        return userServiceInternal.setUserPassword(username, newPassword);
    }

    @Override
    public Map<String, Map<String, String>> getUserProperties(String siteId) throws ServiceLayerException {
        return userServiceInternal.getUserProperties(siteId);
    }

    @Override
    public Map<String, String> updateUserProperties(String siteId, Map<String, String> propertiesToUpdate)
            throws ServiceLayerException {
        return userServiceInternal.updateUserProperties(siteId, propertiesToUpdate);
    }

    @Override
    public Map<String, String> deleteUserProperties(String siteId, List<String> propertiesToDelete)
            throws ServiceLayerException {
        return userServiceInternal.deleteUserProperties(siteId, propertiesToDelete);
    }

    @Override
    public List<String> getCurrentUserSitePermissions(String site)
            throws ServiceLayerException, UserNotFoundException, ExecutionException {
        String currentUser = securityService.getCurrentUser();
        List<String> roles = getUserSiteRoles(-1, currentUser, site);
        return securityServiceV2.getUserPermission(site, currentUser, roles);
    }

    @Override
    public Map<String, Boolean> hasCurrentUserSitePermissions(String site, List<String> permissions)
            throws ServiceLayerException, UserNotFoundException, ExecutionException {
        Map<String, Boolean> toRet = new HashMap<>();
        List<String> userPermissions = getCurrentUserSitePermissions(site);
        permissions.forEach(p -> toRet.put(p, userPermissions.contains(p)));
        return toRet;
    }

    @Override
    public List<String> getCurrentUserGlobalPermissions() throws ServiceLayerException, UserNotFoundException, ExecutionException {
        String currentUser = securityService.getCurrentUser();
        List<String> roles = securityService.getUserGlobalRoles(-1, currentUser);
        return securityServiceV2.getUserPermission(StringUtils.EMPTY, currentUser, roles);
    }

    @Override
    public Map<String, Boolean> hasCurrentUserGlobalPermissions(List<String> permissions) throws ServiceLayerException, UserNotFoundException, ExecutionException {
        Map<String, Boolean> toRet = new HashMap<>();
        List<String> userPermissions = getCurrentUserGlobalPermissions();
        permissions.forEach(p -> toRet.put(p, userPermissions.contains(p)));
        return toRet;
    }

    /**
     * Check if updating users list contains any externally managed users.
     * If matched, the operation must not be permitted.
     * @param userIds
     * @param usernames
     * @throws UserNotFoundException
     * @throws ServiceLayerException
     */
    private void checkExternallyManagedUsers(List<Long> userIds, List<String> usernames) throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException {
        List<User> users = userServiceInternal.getUsersByIdOrUsername(userIds, usernames);
        if (users.stream().anyMatch(user -> user.isExternallyManaged())) {
            throw new UserExternallyManagedException("Cannot update externally managed users.");
        }
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setGroupServiceInternal(GroupServiceInternal groupServiceInternal) {
        this.groupServiceInternal = groupServiceInternal;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setEntitlementValidator(EntitlementValidator entitlementValidator) {
        this.entitlementValidator = entitlementValidator;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public void setInstanceService(InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    public void setEncryptor(TextEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    public void setSecurityServiceV2(org.craftercms.studio.api.v2.service.security.SecurityService securityServiceV2) {
        this.securityServiceV2 = securityServiceV2;
    }

    public void setSessionRegistry(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public void setTaskExecutor(final TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void setForgotPasswordTaskFactory(final ObjectFactory<ForgotPasswordTaskFactory> forgotPasswordTaskFactory) {
        this.forgotPasswordTaskFactory = forgotPasswordTaskFactory;
    }
}
