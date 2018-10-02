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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationSystemException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.GroupDAO;
import org.craftercms.studio.api.v2.dal.GroupTO;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.dal.UserGroupTO;
import org.craftercms.studio.api.v2.dal.UserTO;
import org.craftercms.studio.api.v2.service.security.AuthenticationChain;
import org.craftercms.studio.api.v2.service.security.BaseAuthenticationProvider;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.model.AuthenticationType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.craftercms.studio.api.v1.constant.StudioConstants.DEFAULT_ORGANIZATION_ID;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_DESCRIPTION;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ORG_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_IDS;

public class HeadersAuthenticationProvider extends BaseAuthenticationProvider {

    private final static Logger logger = LoggerFactory.getLogger(HeadersAuthenticationProvider.class);

    private String secureKeyHeader;
    private String secureKeyHeaderValue;
    private String usernameHeader;
    private String firstNameHeader;
    private String lastNameHeader;
    private String emailHeader;
    private String groupsHeader;

    private boolean enabled;



    @Override
    public boolean doAuthenticate(HttpServletRequest request, HttpServletResponse response,
                                  AuthenticationChain authenticationChain, String username, String password) {
        if (enabled) {
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
                        ActivityService activityService = authenticationChain.getActivityService();
                        StudioConfiguration studioConfiguration = authenticationChain.getStudioConfiguration();
                        if (userServiceInternal.userExists(usernameHeaderValue)) {
                            UserTO userTO = userServiceInternal.getUserByIdOrUsername(-1, usernameHeaderValue);
                            userTO.setFirstName(firstName);
                            userTO.setLastName(lastName);
                            userTO.setEmail(email);
                            if (StringUtils.isNoneEmpty(firstName, lastName, email)) {
                                logger.debug("If user already exists in studio DB, update details.");
                                try {
                                    userServiceInternal.updateUser(userTO);

                                        ActivityService.ActivityType activityType = ActivityService.ActivityType.UPDATED;
                                        Map<String, String> extraInfo = new HashMap<String, String>();
                                        extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
                                        activityService.postActivity(
                                                studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE),
                                                usernameHeader, usernameHeader,
                                                activityType, ActivityService.ActivitySource.API, extraInfo);

                                } catch (Exception e) {
                                    logger.error("Error updating user " + usernameHeaderValue +
                                            " with data from authentication headers", e);

                                    throw new AuthenticationSystemException("Error updating user " + usernameHeaderValue +
                                            " with data from external authentication provider", e);
                                }
                            }
                        } else {
                            logger.debug("User does not exist in studio db. Adding user " + usernameHeader);
                            try {
                                UserTO user = new UserTO();
                                user.setUsername(usernameHeaderValue);
                                user.setPassword(UUID.randomUUID().toString());
                                user.setFirstName(firstName);
                                user.setLastName(firstName);
                                user.setEmail(email);
                                user.setExternallyManaged(1);
                                user.setActive(1);
                                userServiceInternal.createUser(user);
                                ActivityService.ActivityType activityType = ActivityService.ActivityType.CREATED;
                                Map<String, String> extraInfo = new HashMap<String, String>();
                                extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
                                activityService.postActivity(
                                        studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE),
                                        usernameHeaderValue, usernameHeaderValue,
                                        activityType, ActivityService.ActivitySource.API, extraInfo);
                            } catch (UserAlreadyExistsException | ServiceLayerException e) {
                                logger.error("Error adding user " + usernameHeaderValue + " from authentication headers", e);

                                throw new AuthenticationSystemException("Error adding user " + usernameHeaderValue +
                                        " from external authentication provider", e);
                            }
                        }
                    } catch (ServiceLayerException e) {
                        logger.error("Unknown service error", e);
                        return false;
                    } catch (UserNotFoundException e) {
                        logger.error("User not found", e);
                        return false;
                    } catch (AuthenticationSystemException e) {
                        logger.error("Authentication error", e);
                        return false;
                    }

                    UserTO userTO = new UserTO();
                    userTO.setUsername(usernameHeaderValue);
                    userTO.setFirstName(firstName);
                    userTO.setLastName(lastName);
                    userTO.setEmail(email);
                    userTO.setGroups(new ArrayList<UserGroupTO>());

                    logger.debug("Update user groups in database.");
                    if (StringUtils.isNoneEmpty(groups)) {
                        String[] groupsArray = groups.split(",");
                        for (int i = 0; i < groupsArray.length; i++) {
                            GroupTO g = new GroupTO();
                            g.setGroupName(StringUtils.trim(groupsArray[i]));
                            g.setGroupDescription("Externally managed group");
                            g.setOrganization(null);
                            UserGroupTO ug = new UserGroupTO();
                            ug.setGroup(g);
                            userTO.getGroups().add(ug);
                            upsertUserGroup(g.getGroupName(), usernameHeaderValue, authenticationChain);
                        }
                    }

                    String token = createToken(userTO, authenticationChain);

                    storeAuthentication(new Authentication(usernameHeaderValue, token, AuthenticationType.AUTH_HEADERS));

                    return true;
                }
            }
            logger.debug("Unable to authenticate user using authentication headers. " +
                    "Switching to other security provider(s).");
            return false;
        } else {
            logger.debug("Authentication using headers disabled. Switching to other security provider(s).");
            return false;
        }
    }

    protected boolean upsertUserGroup(String groupName, String username, AuthenticationChain authenticationChain) {
        GroupDAO groupDao = authenticationChain.getGroupDao();
        UserDAO userDao = authenticationChain.getUserDao();
        ActivityService activityService = authenticationChain.getActivityService();
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(ORG_ID, DEFAULT_ORGANIZATION_ID);
            params.put(GROUP_NAME, groupName);
            params.put(GROUP_DESCRIPTION, "Externally managed group - " + groupName);
            groupDao.createGroup(params);
        } catch (Exception e) {
            logger.warn("Error creating group", e);
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_NAME, groupName);
        GroupTO groupTO = groupDao.getGroupByName(params);
        if (groupTO != null) {
            List<String> usernames = new ArrayList<String>();
            params = new HashMap<>();
            params.put(USER_ID, -1);
            params.put(USERNAME, username);
            UserTO userTO = userDao.getUserByIdOrUsername(params);
            List<Long> users = new ArrayList<Long>();
            users.add(userTO.getId());

            params = new HashMap<>();
            params.put(USER_IDS, users);
            params.put(GROUP_ID, groupTO.getId());
            try {
                groupDao.addGroupMembers(params);

                ActivityService.ActivityType activityType = ActivityService.ActivityType.ADD_USER_TO_GROUP;
                Map<String, String> extraInfo = new HashMap<>();
                extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
                activityService.postActivity("", "LDAP", username + " > " + groupName , activityType,
                        ActivityService.ActivitySource.API, extraInfo);
            } catch (Exception e) {
                logger.error("Unknown database error", e);
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
