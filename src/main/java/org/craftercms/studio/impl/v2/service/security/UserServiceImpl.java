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

import freemarker.template.Template;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.entitlements.model.EntitlementType;
import org.craftercms.commons.entitlements.validator.EntitlementValidator;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.commons.security.exception.PermissionException;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.api.v1.exception.security.PasswordDoesNotMatchException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserExternallyManagedException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.AuditLogParameter;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.api.v2.service.security.internal.GroupServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.system.InstanceService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.AuthenticatedUser;
import org.craftercms.studio.model.Site;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toSet;
import static org.craftercms.studio.api.v1.constant.StudioConstants.REMOVE_SYSTEM_ADMIN_MEMBER_LOCK;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SYSTEM_ADMIN_GROUP;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_CREATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_DELETE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_DISABLE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_ENABLE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_UPDATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_USER;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.MAIL_FROM_DEFAULT;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.MAIL_SMTP_AUTH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_CIPHER_SALT;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_FORGOT_PASSWORD_EMAIL_TEMPLATE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_FORGOT_PASSWORD_MESSAGE_SUBJECT;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_FORGOT_PASSWORD_TOKEN_TIMEOUT;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_RESET_PASSWORD_SERVICE_URL;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_REPO_USER_USERNAME;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CREATE_USERS;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_DELETE_USERS;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_READ_USERS;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_UPDATE_USERS;

public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private UserServiceInternal userServiceInternal;
    private ConfigurationService configurationService;
    private GroupServiceInternal groupServiceInternal;
    private SiteService siteService;
    private EntitlementValidator entitlementValidator;
    private GeneralLockService generalLockService;
    private SecurityService securityService;
    private StudioConfiguration studioConfiguration;
    private AuditServiceInternal auditServiceInternal;
    private ObjectFactory<FreeMarkerConfig> freeMarkerConfig;
    private JavaMailSender emailService;
    private JavaMailSender emailServiceNoAuth;
    private InstanceService instanceService;
    private TextEncryptor encryptor;
    private org.craftercms.studio.api.v2.service.security.SecurityService securityServiceV2;
    private SessionRegistry sessionRegistry;

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
    public List<User> getAllUsersForSite(long orgId, String siteId, String keyword, int offset, int limit, String sort)
            throws ServiceLayerException {
        List<String> groupNames = groupServiceInternal.getSiteGroups(siteId);
        return userServiceInternal.getAllUsersForSite(orgId, groupNames, keyword, offset, limit, sort);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
    public List<User> getAllUsers(String keyword, int offset, int limit, String sort) throws ServiceLayerException {
        return userServiceInternal.getAllUsers(keyword, offset, limit, sort);
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
    public User createUser(User user) throws UserAlreadyExistsException, ServiceLayerException, AuthenticationException {
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
        return toRet;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_UPDATE_USERS)
    public void updateUser(User user) throws ServiceLayerException, UserNotFoundException, AuthenticationException {
        userServiceInternal.updateUser(user);
        SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_UPDATE);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(getCurrentUser().getUsername());
        auditLog.setPrimaryTargetId(user.getUsername());
        auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
        auditLog.setPrimaryTargetValue(user.getUsername());
        auditServiceInternal.insertAuditLog(auditLog);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_DELETE_USERS)
    public void deleteUsers(List<Long> userIds, List<String> usernames)
            throws ServiceLayerException, AuthenticationException, UserNotFoundException {
        User currentUser = getCurrentUser();

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

            logger.debug("Searching for current sessions for users: {0}", toDelete);
            Set<AuthenticatedUser> principals = sessionRegistry.getAllPrincipals().stream()
                    .map(principal -> (AuthenticatedUser) principal)
                    .filter(authenticatedUser -> toDelete.stream()
                                                    .anyMatch(user -> authenticatedUser.getId() == user.getId()))
                    .collect(toSet());
            principals.forEach(principal -> {
                // Invalidate any open session
                List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                sessions.forEach(session -> {
                    logger.debug("Invalidating session {0} for user {1}",
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
            List<AuditLogParameter> parameters = new ArrayList<AuditLogParameter>();
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
    public List<User> enableUsers(List<Long> userIds, List<String> usernames,
                                  boolean enabled) throws ServiceLayerException, UserNotFoundException, AuthenticationException {
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
        List<AuditLogParameter> paramters = new ArrayList<AuditLogParameter>();
        for (User u : users) {
            AuditLogParameter paramter = new AuditLogParameter();
            paramter.setTargetId(Long.toString(u.getId()));
            paramter.setTargetType(TARGET_TYPE_USER);
            paramter.setTargetValue(u.getUsername());
            paramters.add(paramter);
        }
        auditLog.setParameters(paramters);
        auditServiceInternal.insertAuditLog(auditLog);
        return users;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_READ_USERS)
    public List<Site> getUserSites(long userId, String username) throws ServiceLayerException, UserNotFoundException {
        List<Site> sites = new ArrayList<>();
        Set<String> allSites = siteService.getAllAvailableSites();
        List<Group> userGroups = userServiceInternal.getUserGroups(userId, username);
        boolean isSysAdmin = userGroups.stream().anyMatch(group -> group.getGroupName().equals(SYSTEM_ADMIN_GROUP));

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

                    sites.add(site);
                } catch (SiteNotFoundException e) {
                    logger.error("Site not found: {0}", e, siteId);
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

        if (CollectionUtils.isNotEmpty(groups)) {
            Map<String, List<String>> roleMappings = configurationService.getRoleMappings(site);
            Set<String> userRoles = new LinkedHashSet<>();

            if (MapUtils.isNotEmpty(roleMappings)) {
                for (Group group : groups) {
                    String groupName = group.getGroupName();
                    if (groupName.equals(SYSTEM_ADMIN_GROUP)) {
                        // If sysadmin, return all roles
                        Collection<List<String>> roleSets = roleMappings.values();

                        for (List<String> roleSet : roleSets) {
                            userRoles.addAll(roleSet);
                        }

                        break;
                    } else {
                        List<String> roles = roleMappings.get(groupName);
                        if (CollectionUtils.isNotEmpty(roles)) {
                            userRoles.addAll(roles);
                        }
                    }
                }
            }

            return new ArrayList<>(userRoles);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public AuthenticatedUser getCurrentUser() throws AuthenticationException, ServiceLayerException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return (AuthenticatedUser) authentication.getPrincipal();
        } else {
            throw new AuthenticationException("User should be authenticated");
        }
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
    public boolean forgotPassword(String username) throws ServiceLayerException, UserNotFoundException,
            UserExternallyManagedException {
        logger.debug("Getting user profile for " + username);
        User user = userServiceInternal.getUserByIdOrUsername(-1, username);
        boolean success = false;
        if (user == null) {
            logger.info("User profile not found for " + username);
            throw new UserNotFoundException();
        } else {
            if (user.isExternallyManaged()) {
                throw new UserExternallyManagedException();
            } else {
                if (user.getEmail() != null) {
                    String email = user.getEmail();

                    logger.debug("Creating security token for forgot password");
                    long timestamp = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(
                            Long.parseLong(studioConfiguration .getProperty(SECURITY_FORGOT_PASSWORD_TOKEN_TIMEOUT)));
                    String salt = studioConfiguration.getProperty(SECURITY_CIPHER_SALT);
                    String studioId = instanceService.getInstanceId();
                    String token = username + "|" + studioId + "|" + timestamp + "|" + salt;
                    String hashedToken = encryptToken(token);
                    logger.debug("Sending forgot password email to " + email);
                    sendForgotPasswordEmail(email, hashedToken);
                    success = true;
                } else {
                    logger.info("User " + username + " does not have assigned email with account");
                    throw new ServiceLayerException("User " + username + " does not have assigned email with account");
                }
            }
        }
        return success;
    }

    private String encryptToken(String token) {
        try {
            String hashedToken = encryptor.encrypt(token);
            return Base64.getEncoder().encodeToString(hashedToken.getBytes(StandardCharsets.UTF_8));
        } catch (CryptoException e) {
            logger.error("Error while encrypting forgot password token", e);
            return null;
        }
    }

    private String decryptToken(String hashedToken) {
        try {
            byte[] hashedTokenBytes = Base64.getDecoder().decode(hashedToken.getBytes(StandardCharsets.UTF_8));
            return encryptor.decrypt(new String(hashedTokenBytes, StandardCharsets.UTF_8));
        } catch (CryptoException e) {
            logger.error("Error while decrypting forgot password token", e);
            return null;
        }
    }

    private void sendForgotPasswordEmail(String emailAddress, String token) {
        try {
            Template emailTemplate = freeMarkerConfig.getObject().getConfiguration().getTemplate(
                    studioConfiguration.getProperty(SECURITY_FORGOT_PASSWORD_EMAIL_TEMPLATE));

            Writer out = new StringWriter();
            Map<String, Object> model = new HashMap<String, Object>();
            RequestContext context = RequestContext.getCurrent();
            HttpServletRequest request = context.getRequest();
            String authoringUrl = request.getRequestURL().toString().replace(request.getPathInfo(), "");
            String serviceUrl = studioConfiguration.getProperty(SECURITY_RESET_PASSWORD_SERVICE_URL);
            model.put("authoringUrl", authoringUrl);
            model.put("serviceUrl", serviceUrl);
            model.put("token", token);
            if (emailTemplate != null) {
                emailTemplate.process(model, out);
            }

            MimeMessage mimeMessage = emailService.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);

            messageHelper.setFrom(studioConfiguration.getProperty(MAIL_FROM_DEFAULT));
            messageHelper.setTo(emailAddress);
            messageHelper.setSubject(studioConfiguration.getProperty(SECURITY_FORGOT_PASSWORD_MESSAGE_SUBJECT));
            messageHelper.setText(out.toString(), true);
            logger.info("Sending password recovery message to " + emailAddress);
            if (isAuthenticatedSMTP()) {
                emailService.send(mimeMessage);
            } else {
                emailServiceNoAuth.send(mimeMessage);
            }
            logger.info("Password recovery message successfully sent to " + emailAddress);
        } catch (Exception e) {
            logger.error("Failed to send password recovery message to " + emailAddress, e);
        }
    }

    @Override
    public User changePassword(String username, String current, String newPassword)
            throws PasswordDoesNotMatchException, UserExternallyManagedException, ServiceLayerException,
            AuthenticationException, UserNotFoundException {
        AuthenticatedUser currentUser = getCurrentUser();
        if (currentUser != null && StringUtils.equals(username, currentUser.getUsername())) {
            boolean success = userServiceInternal.changePassword(username, current, newPassword);
            if (success) {
                return userServiceInternal.getUserByIdOrUsername(-1, username);
            } else {
                throw new ServiceLayerException("Failed to change password");
            }
        } else {
            throw new PermissionException();
        }
    }

    @Override
    public User setPassword(String token, String newPassword) throws UserNotFoundException,
            UserExternallyManagedException, ServiceLayerException {
        if (validateToken(token)) {
            String username = getUsernameFromToken(token);
            if (StringUtils.isNotEmpty(username)) {
                User user = userServiceInternal.getUserByIdOrUsername(-1, username);
                if (user != null ) {
                    if (user.isEnabled()) {
                        boolean success = userServiceInternal.setUserPassword(username, newPassword);
                        if (success) {
                            return user;
                        }
                    }
                } else {
                    throw new UserNotFoundException("User not found");
                }
            } else {
                throw new UserNotFoundException("User not found");
            }
        }
        return null;
    }

    @Override
    public boolean validateToken(String token) throws UserNotFoundException,
            UserExternallyManagedException, ServiceLayerException {
        boolean toRet = false;
        String decryptedToken = decryptToken(token);
        if (StringUtils.isNotEmpty(decryptedToken)) {
            StringTokenizer tokenElements = new StringTokenizer(decryptedToken, "|");
            if (tokenElements.countTokens() == 4) {
                String username = tokenElements.nextToken();
                User userProfile = userServiceInternal.getUserByIdOrUsername(-1, username);
                if (userProfile == null) {
                    logger.info("User profile not found for " + username);
                    throw new UserNotFoundException();
                } else {
                    if (userProfile.isExternallyManaged()) {
                        throw new UserExternallyManagedException();
                    } else {
                        String studioId = tokenElements.nextToken();
                        if (StringUtils.equals(studioId, instanceService.getInstanceId())) {
                            long tokenTimestamp = Long.parseLong(tokenElements.nextToken());
                            toRet = tokenTimestamp >= System.currentTimeMillis();
                        }
                    }
                }
            }
        }
        return toRet;
    }

    private String getUsernameFromToken(String token) {
        String toRet = StringUtils.EMPTY;
        String decryptedToken = decryptToken(token);
        if (StringUtils.isNotEmpty(decryptedToken)) {
            StringTokenizer tokenElements = new StringTokenizer(decryptedToken, "|");
            if (tokenElements.countTokens() == 4) {
                toRet = tokenElements.nextToken();
            }
        }
        return toRet;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_UPDATE_USERS)
    public boolean resetPassword(String username, String newPassword)
            throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException {
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
        Map<String, Boolean> toRet = new HashMap<String, Boolean>();
        List<String> userPermissions = getCurrentUserSitePermissions(site);
        permissions.forEach(p -> toRet.put(p, userPermissions.contains(p)));
        return toRet;
    }

    @Override
    public List<String> getCurrentUserGlobalPermissions() throws ServiceLayerException, UserNotFoundException, ExecutionException {
        String currentUser = securityService.getCurrentUser();
        List<String> roles = getUserGlobalRoles(-1, currentUser);
        return securityServiceV2.getUserPermission(StringUtils.EMPTY, currentUser, roles);
    }

    @Override
    public Map<String, Boolean> hasCurrentUserGlobalPermissions(List<String> permissions) throws ServiceLayerException, UserNotFoundException, ExecutionException {
        Map<String, Boolean> toRet = new HashMap<String, Boolean>();
        List<String> userPermissions = getCurrentUserGlobalPermissions();
        permissions.forEach(p -> toRet.put(p, userPermissions.contains(p)));
        return toRet;
    }

    private List<String> getUserGlobalRoles(long userId, String username)
            throws ServiceLayerException, UserNotFoundException {
        List<Group> groups = userServiceInternal.getUserGroups(userId, username);

        if (CollectionUtils.isNotEmpty(groups)) {
            Map<String, List<String>> roleMappings = configurationService.getGlobalRoleMappings();
            Set<String> userRoles = new LinkedHashSet<>();

            if (MapUtils.isNotEmpty(roleMappings)) {
                for (Group group : groups) {
                    String groupName = group.getGroupName();
                    List<String> roles = roleMappings.get(groupName);
                    if (CollectionUtils.isNotEmpty(roles)) {
                        userRoles.addAll(roles);
                    }
                }
            }

            return new ArrayList<>(userRoles);
        } else {
            return Collections.emptyList();
        }
    }

    private boolean isAuthenticatedSMTP() {
        return Boolean.parseBoolean(studioConfiguration.getProperty(MAIL_SMTP_AUTH));
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public GroupServiceInternal getGroupServiceInternal() {
        return groupServiceInternal;
    }

    public void setGroupServiceInternal(GroupServiceInternal groupServiceInternal) {
        this.groupServiceInternal = groupServiceInternal;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public EntitlementValidator getEntitlementValidator() {
        return entitlementValidator;
    }

    public void setEntitlementValidator(EntitlementValidator entitlementValidator) {
        this.entitlementValidator = entitlementValidator;
    }

    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public AuditServiceInternal getAuditServiceInternal() {
        return auditServiceInternal;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public ObjectFactory<FreeMarkerConfig> getFreeMarkerConfig() {
        return freeMarkerConfig;
    }

    public void setFreeMarkerConfig(ObjectFactory<FreeMarkerConfig> freeMarkerConfig) {
        this.freeMarkerConfig = freeMarkerConfig;
    }

    public JavaMailSender getEmailService() {
        return emailService;
    }

    public void setEmailService(JavaMailSender emailService) {
        this.emailService = emailService;
    }

    public JavaMailSender getEmailServiceNoAuth() {
        return emailServiceNoAuth;
    }

    public void setEmailServiceNoAuth(JavaMailSender emailServiceNoAuth) {
        this.emailServiceNoAuth = emailServiceNoAuth;
    }

    public InstanceService getInstanceService() {
        return instanceService;
    }

    public void setInstanceService(InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    public TextEncryptor getEncryptor() {
        return encryptor;
    }

    public void setEncryptor(TextEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    public org.craftercms.studio.api.v2.service.security.SecurityService getSecurityServiceV2() {
        return securityServiceV2;
    }

    public void setSecurityServiceV2(org.craftercms.studio.api.v2.service.security.SecurityService securityServiceV2) {
        this.securityServiceV2 = securityServiceV2;
    }

    public void setSessionRegistry(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

}
