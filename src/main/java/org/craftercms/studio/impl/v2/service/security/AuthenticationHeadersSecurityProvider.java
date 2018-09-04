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
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.exception.security.AuthenticationSystemException;
import org.craftercms.studio.api.v1.exception.security.BadCredentialsException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v2.dal.GroupDAO;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.dal.UserGroup;
import org.craftercms.studio.model.User;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_HEADERS_EMAIL;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_HEADERS_ENABLED;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_HEADERS_FIRST_NAME;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_HEADERS_GROUPS;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_HEADERS_LAST_NAME;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_HEADERS_SECURE_KEY_HEADER_NAME;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_HEADERS_SECURE_KEY_HEADER_VALUE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_HEADERS_USERNAME;

public class AuthenticationHeadersSecurityProvider extends DbWithLdapExtensionSecurityProvider {

    private final static Logger logger = LoggerFactory.getLogger(AuthenticationHeadersSecurityProvider.class);

    @Override
    public String authenticate(String username, String password) throws BadCredentialsException,
        AuthenticationSystemException, EntitlementException {
        if (isAuthenticationHeadersEnabled()) {
            logger.debug("Authenticating user using authentication headers.");

            RequestContext requestContext = RequestContext.getCurrent();
            if (requestContext != null) {
                HttpServletRequest request = requestContext.getRequest();
                String securekeyHeader = request.getHeader(
                        studioConfiguration.getProperty(AUTHENTICATION_HEADERS_SECURE_KEY_HEADER_NAME));
                String secureKey = studioConfiguration.getProperty(AUTHENTICATION_HEADERS_SECURE_KEY_HEADER_VALUE);
                logger.debug("Verifying authentication header secure key.");
                if (StringUtils.equals(securekeyHeader, secureKey)) {
                    String usernameHeader = request.getHeader(
                            studioConfiguration.getProperty(AUTHENTICATION_HEADERS_USERNAME));
                    String firstName = request.getHeader(
                            studioConfiguration.getProperty(AUTHENTICATION_HEADERS_FIRST_NAME));
                    String lastName = request.getHeader(
                            studioConfiguration.getProperty(AUTHENTICATION_HEADERS_LAST_NAME));
                    String email = request.getHeader(studioConfiguration.getProperty(AUTHENTICATION_HEADERS_EMAIL));
                    String groups = request.getHeader(studioConfiguration.getProperty(AUTHENTICATION_HEADERS_GROUPS));

                    if (userExists(usernameHeader)) {
                        if (StringUtils.isNoneEmpty(firstName, lastName, email)) {
                            logger.debug("If user already exists in studio DB, update details.");
                            try {
                                boolean success = updateUserInternal(usernameHeader, firstName, lastName, email);
                                if (success) {
                                    ActivityService.ActivityType activityType = ActivityService.ActivityType.UPDATED;
                                    Map<String, String> extraInfo = new HashMap<String, String>();
                                    extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
                                    activityService.postActivity(getSystemSite(), usernameHeader, usernameHeader,
                                            activityType, ActivityService.ActivitySource.API, extraInfo);
                                }
                            } catch (UserNotFoundException e) {
                                logger.error("Error updating user " + username
                                        + " with data from authentication headers", e);

                                throw new AuthenticationSystemException("Error updating user " + username +
                                        " with data from external authentication provider", e);
                            }
                        }
                    } else {
                        logger.debug("User does not exist in studio db. Adding user " + usernameHeader);
                        try {
                            User user = new User();
                            user.setUsername(usernameHeader);
                            user.setPassword(password);
                            user.setFirstName(firstName);
                            user.setLastName(firstName);
                            user.setEmail(email);
                            user.setExternallyManaged(true);
                            user.setEnabled(true);
                            boolean success = createUser(user);
                            if (success) {
                                ActivityService.ActivityType activityType = ActivityService.ActivityType.CREATED;
                                Map<String, String> extraInfo = new HashMap<String, String>();
                                extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
                                activityService.postActivity(getSystemSite(), usernameHeader, usernameHeader,
                                        activityType, ActivityService.ActivitySource.API, extraInfo);
                            }
                        } catch (UserAlreadyExistsException e) {
                            logger.error("Error adding user " + username + " from authentication headers", e);

                            throw new AuthenticationSystemException("Error adding user " + username
                                    + " from external authentication provider", e);
                        }
                    }

                    UserDAO userDao = new UserDAO();
                    userDao.setUsername(usernameHeader);
                    userDao.setFirstName(firstName);
                    userDao.setLastName(lastName);
                    userDao.setEmail(email);
                    userDao.setGroups(new ArrayList<UserGroup>());

                    logger.debug("Update user groups in database.");
                    if (StringUtils.isNoneEmpty(groups)) {
                        String[] groupsArray = groups.split(",");
                        for (int i = 0; i < groupsArray.length; i++) {
                            GroupDAO g = new GroupDAO();
                            g.setGroupName(StringUtils.trim(groupsArray[i]));
                            g.setGroupDescription("Externally managed group");
                            g.setOrganization(null);
                            UserGroup ug = new UserGroup();
                            ug.setGroup(g);
                            userDao.getGroups().add(ug);
                            upsertUserGroup(g.getGroupName(), usernameHeader);
                        }

                    }

                    String token = createToken(userDao);
                    storeSessionTicket(token);
                    storeSessionUsername(username);
                    return token;
                }
            }
            logger.debug("Unable to authenticate user using authentication headers. " +
                    "Switching to other security provider(s).");
            return super.authenticate(username, password);
        } else {
            logger.debug("Authentication using headers disabled. Switching to other security provider(s).");
            return super.authenticate(username, password);
        }
    }

    protected boolean isAuthenticationHeadersEnabled() {
        String enabledString = studioConfiguration.getProperty(AUTHENTICATION_HEADERS_ENABLED);
        return Boolean.parseBoolean(enabledString);
    }
}
