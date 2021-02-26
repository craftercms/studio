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

package org.craftercms.studio.impl.v2.security.authentication.headers;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.GroupDAO;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.dal.UserGroup;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.AuthenticatedUser;
import org.craftercms.studio.model.AuthenticationType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

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

/**
 * Implementation of {@link AuthenticationProvider} to support Studio's header authentications
 *
 * @author joseross
 * @since 4.0
 */
public class HeadersAuthenticationProvider implements AuthenticationProvider {

    private final static Logger logger = LoggerFactory.getLogger(HeadersAuthenticationProvider.class);

    private String secureKeyHeader;
    private String secureKeyHeaderValue;
    private String usernameHeader;
    private String firstNameHeader;
    private String lastNameHeader;
    private String emailHeader;
    private String groupsHeader;

    protected StudioConfiguration studioConfiguration;
    protected SiteService siteService;
    protected UserServiceInternal userServiceInternal;
    protected AuditServiceInternal auditServiceInternal;
    protected UserDAO userDao;
    protected GroupDAO groupDao;

    public HeadersAuthenticationProvider(StudioConfiguration studioConfiguration, SiteService siteService,
                                         UserServiceInternal userServiceInternal,
                                         AuditServiceInternal auditServiceInternal, UserDAO userDao,
                                         GroupDAO groupDao) {
        this.studioConfiguration = studioConfiguration;
        this.siteService = siteService;
        this.userServiceInternal = userServiceInternal;
        this.auditServiceInternal = auditServiceInternal;
        this.userDao = userDao;
        this.groupDao = groupDao;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        logger.debug("Authenticating user using authentication headers.");

        var requestContext = RequestContext.getCurrent();
        if (requestContext != null) {
            var request = requestContext.getRequest();
            String secureKeyHeader = request.getHeader(this.secureKeyHeader);
            logger.debug("Verifying authentication header secure key.");
            if (StringUtils.equals(secureKeyHeader, secureKeyHeaderValue)) {
                String usernameHeaderValue = request.getHeader(usernameHeader);
                String firstName = request.getHeader(firstNameHeader);
                String lastName = request.getHeader(lastNameHeader);
                String email = request.getHeader(emailHeader);
                String groups = request.getHeader(groupsHeader);

                User user;

                try {
                    SiteFeed siteFeed =
                            siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
                    if (userServiceInternal.userExists(-1, usernameHeaderValue)) {
                        user = userServiceInternal.getUserByIdOrUsername(-1, usernameHeaderValue);
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

                                throw new AuthenticationServiceException(
                                        "Error updating user " + usernameHeaderValue + " with data from " +
                                                "external authentication provider", e);
                            }
                        }
                    } else {
                        logger.debug("User does not exist in studio db. Adding user " + usernameHeader);
                        try {
                            user = new User();
                            user.setUsername(usernameHeaderValue);
                            user.setPassword(UUID.randomUUID().toString());
                            user.setFirstName(firstName);
                            user.setLastName(lastName);
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

                            throw new AuthenticationServiceException(
                                    "Error adding user " + usernameHeaderValue + " from external " +
                                            "authentication provider", e);
                        }
                    }
                } catch (ServiceLayerException e) {
                    logger.debug("Unknown service error", e);
                    throw new AuthenticationServiceException("Unknown service error" , e);
                } catch (UserNotFoundException e) {
                    // should never happen
                    throw new IllegalStateException("User nor found", e);
                }

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
                            upsertUserGroup(g.getGroupName(), usernameHeaderValue);
                        } catch (Exception e) {
                            logger.debug("Error updating user group " + g.getGroupName() +
                                    " with data from authentication headers", e);
                        }
                    }
                }

                var authenticatedUser = new AuthenticatedUser(user);
                authenticatedUser.setAuthenticationType(AuthenticationType.AUTH_HEADERS);

                // Use a custom token instead of Spring's preauth to avoid conflict with tokens
                return new HeadersAuthenticationToken(authenticatedUser);
            }
        }

        logger.debug("Unable to authenticate user using authentication headers");

        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(PreAuthenticatedAuthenticationToken.class);
    }

    protected boolean upsertUserGroup(String groupName, String username)
            throws SiteNotFoundException {
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

    public void setSecureKeyHeader(String secureKeyHeader) {
        this.secureKeyHeader = secureKeyHeader;
    }

    public void setSecureKeyHeaderValue(String secureKeyHeaderValue) {
        this.secureKeyHeaderValue = secureKeyHeaderValue;
    }

    public void setUsernameHeader(String usernameHeader) {
        this.usernameHeader = usernameHeader;
    }

    public void setFirstNameHeader(String firstNameHeader) {
        this.firstNameHeader = firstNameHeader;
    }

    public void setLastNameHeader(String lastNameHeader) {
        this.lastNameHeader = lastNameHeader;
    }

    public void setEmailHeader(String emailHeader) {
        this.emailHeader = emailHeader;
    }

    public void setGroupsHeader(String groupsHeader) {
        this.groupsHeader = groupsHeader;
    }


}
