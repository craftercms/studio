/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationSystemException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.GroupDAO;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.dal.UserGroup;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.security.AuthenticationChain;
import org.craftercms.studio.api.v2.service.security.BaseAuthenticationProvider;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.AuthenticationType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.craftercms.studio.api.v1.constant.StudioConstants.DEFAULT_ORGANIZATION_ID;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_ADD_MEMBERS;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_CREATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_UPDATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_USER;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_DESCRIPTION;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ORG_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_IDS;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;

public class HeadersAuthenticationProvider extends BaseAuthenticationProvider {

    private final static Logger logger = LoggerFactory.getLogger(HeadersAuthenticationProvider.class);

    private String secureKeyHeader;
    private String secureKeyHeaderValue;
    private String usernameHeader;
    private String firstNameHeader;
    private String lastNameHeader;
    private String emailHeader;
    private String groupsHeader;
    private boolean logoutEnabled;
    private String logoutUrl;

    @Override
    public boolean doAuthenticate(HttpServletRequest request, HttpServletResponse response,
                                  AuthenticationChain authenticationChain, String username, String password)
            throws AuthenticationSystemException, UserNotFoundException {
        if (isEnabled()) {
            logger.debug("Authenticating user using authentication headers.");

            RequestContext requestContext = RequestContext.getCurrent();
            if (requestContext != null) {
                String securekeyHeader = request.getHeader(secureKeyHeader);
                logger.debug("Verifying authentication header secure key.");
                if (StringUtils.equals(securekeyHeader, secureKeyHeaderValue)) {
                    String usernameHeaderValue = request.getHeader(usernameHeader);
                    String firstName = request.getHeader(firstNameHeader);
                    String lastName = request.getHeader(lastNameHeader);
                    String email = request.getHeader(emailHeader);
                    String groups = request.getHeader(groupsHeader);

                    try {
                        UserServiceInternal userServiceInternal = authenticationChain.getUserServiceInternal();
                        AuditServiceInternal auditServiceInternal = authenticationChain.getAuditServiceInternal();
                        StudioConfiguration studioConfiguration = authenticationChain.getStudioConfiguration();
                        SiteService siteService = authenticationChain.getSiteService();
                        SiteFeed siteFeed =
                                siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
                        if (userServiceInternal.userExists(-1, usernameHeaderValue)) {
                            User user = userServiceInternal.getUserByIdOrUsername(-1, usernameHeaderValue);
                            user.setFirstName(firstName);
                            user.setLastName(lastName);
                            user.setEmail(email);
                            if (StringUtils.isNoneEmpty(firstName, lastName, email)) {
                                logger.debug("If user already exists in studio DB, update details.");
                                try {
                                    userServiceInternal.updateUser(user);
                                    AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
                                    auditLog.setOperation(OPERATION_UPDATE);
                                    auditLog.setActorId(usernameHeaderValue);
                                    auditLog.setSiteId(siteFeed.getId());
                                    auditLog.setPrimaryTargetId(usernameHeaderValue);
                                    auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
                                    auditLog.setPrimaryTargetValue(user.getUsername());
                                    auditServiceInternal.insertAuditLog(auditLog);

                                } catch (Exception e) {
                                    logger.debug("Error updating user " + usernameHeaderValue +
                                                 " with data from authentication headers", e);

                                    throw new AuthenticationSystemException(
                                            "Error updating user " + usernameHeaderValue + " with data from " +
                                            "external authentication provider", e);
                                }
                            }
                        } else {
                            logger.debug("User does not exist in studio db. Adding user " + usernameHeader);
                            try {
                                User user = new User();
                                user.setUsername(usernameHeaderValue);
                                user.setPassword(UUID.randomUUID().toString());
                                user.setFirstName(firstName);
                                user.setLastName(firstName);
                                user.setEmail(email);
                                user.setExternallyManaged(true);
                                user.setEnabled(true);
                                userServiceInternal.createUser(user);
                                AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
                                auditLog.setOperation(OPERATION_CREATE);
                                auditLog.setSiteId(siteFeed.getId());
                                auditLog.setActorId(usernameHeaderValue);
                                auditLog.setPrimaryTargetId(usernameHeaderValue);
                                auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
                                auditLog.setPrimaryTargetValue(user.getUsername());
                                auditServiceInternal.insertAuditLog(auditLog);
                            } catch (UserAlreadyExistsException | ServiceLayerException e) {
                                logger.debug("Error adding user " + usernameHeaderValue + " from authentication " +
                                             "headers", e);

                                throw new AuthenticationSystemException(
                                        "Error adding user " + usernameHeaderValue + " from external " +
                                        "authentication provider", e);
                            }
                        }
                    } catch (ServiceLayerException e) {
                        logger.debug("Unknown service error", e);
                        throw  new AuthenticationSystemException("Unknown service error" , e);
                    }

                    User user = new User();
                    user.setUsername(usernameHeaderValue);
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setEmail(email);
                    user.setGroups(new ArrayList<UserGroup>());

                    logger.debug("Update user groups in database.");
                    if (StringUtils.isNoneEmpty(groups)) {
                        String[] groupsArray = groups.split(",");
                        for (int i = 0; i < groupsArray.length; i++) {
                            Group g = new Group();
                            try {
                                g.setGroupName(StringUtils.trim(groupsArray[i]));
                                g.setGroupDescription("Externally managed group");
                                g.setOrganization(null);
                                UserGroup ug = new UserGroup();
                                ug.setGroup(g);
                                user.getGroups().add(ug);
                                upsertUserGroup(g.getGroupName(), usernameHeaderValue, authenticationChain);
                            } catch (Exception e) {
                                logger.debug("Error updating user group " + g.getGroupName() +
                                        " with data from authentication headers", e);
                            }
                        }
                    }

                    String token = createToken(user, authenticationChain);
                    if (isLogoutEnabled()) {
                        storeAuthentication(new Authentication(usernameHeaderValue, token,
                                AuthenticationType.AUTH_HEADERS, logoutUrl));
                    } else {
                        storeAuthentication(new Authentication(usernameHeaderValue, token,
                                AuthenticationType.AUTH_HEADERS));
                    }
                    return true;
                }
            }

            logger.debug("Unable to authenticate user using authentication headers");

            return false;
        } else {
            logger.debug("Authentication using headers disabled");

            return false;
        }
    }

    protected boolean upsertUserGroup(String groupName, String username, AuthenticationChain authenticationChain)
            throws SiteNotFoundException {
        GroupDAO groupDao = authenticationChain.getGroupDao();
        UserDAO userDao = authenticationChain.getUserDao();
        AuditServiceInternal auditServiceInternal = authenticationChain.getAuditServiceInternal();
        SiteService siteService = authenticationChain.getSiteService();
        StudioConfiguration studioConfiguration = authenticationChain.getStudioConfiguration();
        SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(ORG_ID, DEFAULT_ORGANIZATION_ID);
            params.put(GROUP_NAME, groupName);
            params.put(GROUP_DESCRIPTION, "Externally managed group - " + groupName);
            groupDao.createGroup(params);
        } catch (Exception e) {
            logger.debug("Error creating group", e);
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_NAME, groupName);
        Group group = groupDao.getGroupByName(params);
        if (group != null) {
            List<String> usernames = new ArrayList<String>();
            params = new HashMap<>();
            params.put(USER_ID, -1);
            params.put(USERNAME, username);
            User user = userDao.getUserByIdOrUsername(params);
            List<Long> users = new ArrayList<Long>();
            users.add(user.getId());

            params = new HashMap<>();
            params.put(USER_IDS, users);
            params.put(GROUP_ID, group.getId());

            try {
                groupDao.addGroupMembers(params);
                AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
                auditLog.setOperation(OPERATION_ADD_MEMBERS);
                auditLog.setSiteId(siteFeed.getId());
                auditLog.setActorId(username);
                auditLog.setPrimaryTargetId(group.getGroupName() + ":" + user.getUsername());
                auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
                auditLog.setPrimaryTargetValue(user.getUsername());
                auditServiceInternal.insertAuditLog(auditLog);
            } catch (Exception e) {
                logger.debug("Unknown database error", e);
            }
        }
        return true;
    }

    public String getSecureKeyHeader() {
        return secureKeyHeader;
    }

    public void setSecureKeyHeader(String secureKeyHeader) {
        this.secureKeyHeader = secureKeyHeader;
    }

    public String getSecureKeyHeaderValue() {
        return secureKeyHeaderValue;
    }

    public void setSecureKeyHeaderValue(String secureKeyHeaderValue) {
        this.secureKeyHeaderValue = secureKeyHeaderValue;
    }

    public String getUsernameHeader() {
        return usernameHeader;
    }

    public void setUsernameHeader(String usernameHeader) {
        this.usernameHeader = usernameHeader;
    }

    public String getFirstNameHeader() {
        return firstNameHeader;
    }

    public void setFirstNameHeader(String firstNameHeader) {
        this.firstNameHeader = firstNameHeader;
    }

    public String getLastNameHeader() {
        return lastNameHeader;
    }

    public void setLastNameHeader(String lastNameHeader) {
        this.lastNameHeader = lastNameHeader;
    }

    public String getEmailHeader() {
        return emailHeader;
    }

    public void setEmailHeader(String emailHeader) {
        this.emailHeader = emailHeader;
    }

    public String getGroupsHeader() {
        return groupsHeader;
    }

    public void setGroupsHeader(String groupsHeader) {
        this.groupsHeader = groupsHeader;
    }

    public boolean isLogoutEnabled() {
        return logoutEnabled;
    }

    public void setLogoutEnabled(boolean logoutEnabled) {
        this.logoutEnabled = logoutEnabled;
    }

    public String geLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }
}
