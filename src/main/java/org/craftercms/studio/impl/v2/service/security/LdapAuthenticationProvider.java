/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
 */

package org.craftercms.studio.impl.v2.service.security;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationSystemException;
import org.craftercms.studio.api.v1.exception.security.BadCredentialsException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.GroupDAO;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.dal.UserGroup;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.service.security.AuthenticationChain;
import org.craftercms.studio.api.v2.service.security.BaseAuthenticationProvider;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.model.AuthenticationType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.CommunicationException;
import org.springframework.ldap.core.AuthenticatedLdapEntryContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQuery;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.craftercms.studio.api.v1.constant.StudioConstants.DEFAULT_ORGANIZATION_ID;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_DESCRIPTION;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ORG_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_IDS;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class LdapAuthenticationProvider extends BaseAuthenticationProvider {

    private final static Logger logger = LoggerFactory.getLogger(LdapAuthenticationProvider.class);

    private String ldapUrl;
    private String ldapPassword;
    private String ldapUsername;
    private String ldapBaseContext;

    private String usernameLdapAttribute;
    private String firstNameLdapAttribute;
    private String lastNameLdapAttribute;
    private String groupNameLdapAttribute;
    private String groupNameLdapAttributeRegex;
    private int groupNameLdapAttributeMatchIndex;
    private String emailLdapAttribute;

    @Override
    public boolean doAuthenticate(HttpServletRequest request, HttpServletResponse response,
                                  AuthenticationChain authenticationChain, String username, String password)
            throws AuthenticationSystemException, BadCredentialsException {

        LdapContextSource lcs = new LdapContextSource();
        lcs.setUrl(ldapUrl);
        lcs.setUserDn(ldapUsername);
        lcs.setPassword(ldapPassword);
        lcs.setBase(ldapBaseContext);
        lcs.setDirObjectFactory(DefaultDirObjectFactory.class);
        lcs.afterPropertiesSet();
        LdapTemplate ldapTemplate = new LdapTemplate(lcs);

        // Mapper for user data if user is successfully authenticated
        AuthenticatedLdapEntryContextMapper<User> mapper = (dirContext, ldapEntryIdentification) -> {
            try {
                // User entry - extract attributes
                DirContextOperations dirContextOperations =
                        (DirContextOperations) dirContext.lookup(ldapEntryIdentification.getRelativeName());
                Attributes attributes = dirContextOperations.getAttributes();
                Attribute emailAttrib = attributes.get(emailLdapAttribute);
                Attribute firstNameAttrib = attributes.get(firstNameLdapAttribute);
                Attribute lastNameAttrib = attributes.get(lastNameLdapAttribute);
                Attribute groupNameAttrib = attributes.get(groupNameLdapAttribute);


                User user = new User();
                user.setEnabled(true);
                user.setUsername(username);
                user.setPassword(UUID.randomUUID().toString());

                if (emailAttrib != null && emailAttrib.get() != null) {
                    user.setEmail(emailAttrib.get().toString());
                } else {
                    logger.warn("No LDAP attribute " + emailLdapAttribute + " found for username " + username +
                                 ". User will not be imported into DB.");
                    return null;
                }
                if (firstNameAttrib != null && firstNameAttrib.get() != null) {
                    user.setFirstName(firstNameAttrib.get().toString());
                } else {
                    logger.warn("No LDAP attribute " + firstNameLdapAttribute + " found for username " + username);
                }
                if (lastNameAttrib != null && lastNameAttrib.get() != null) {
                    user.setLastName(lastNameAttrib.get().toString());
                } else {
                    logger.warn("No LDAP attribute " + lastNameLdapAttribute + " found for username " + username);
                }


                extractGroupsFromAttribute(user, groupNameLdapAttribute, groupNameAttrib);


                return user;
            } catch (NamingException e) {
                logger.debug("Error getting details from LDAP for username " + username, e);

                return null;
            }
        };

        // Create ldap query to authenticate user
        LdapQuery ldapQuery = query().where(usernameLdapAttribute).is(username);
        User user;
        try {
            user = ldapTemplate.authenticate(ldapQuery, password, mapper);
        } catch (EmptyResultDataAccessException e) {
            logger.debug("User " + username + " not found with external security provider.");

            return false;
        } catch (CommunicationException e) {
            logger.debug("Failed to connect with external security provider", e);

            return false;
        }  catch (AuthenticationException e) {
            logger.debug("Authentication failed with the LDAP system (bad credentials)", e);

            throw new BadCredentialsException();
        } catch (Exception e) {
            logger.debug("Unexpected exception when authenticating with the LDAP system", e);

            return false;
        }

        if (user != null) {
            // When user authenticated against LDAP, upsert user data into studio database
            UserServiceInternal userServiceInternal = authenticationChain.getUserServiceInternal();
            ActivityService activityService = authenticationChain.getActivityService();
            StudioConfiguration studioConfiguration = authenticationChain.getStudioConfiguration();
            try {
                if (userServiceInternal.userExists(-1, username)) {
                    try {
                        userServiceInternal.updateUser(user);
                    } catch (UserNotFoundException e) {
                        // Shouldn't happen
                        throw new IllegalStateException(e);
                    }

                    ActivityService.ActivityType activityType = ActivityService.ActivityType.UPDATED;
                    Map<String, String> extraInfo = new HashMap<>();
                    extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
                    activityService.postActivity(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE),
                                                 user.getUsername(), user.getUsername(),
                                                 activityType, ActivityService.ActivitySource.API, extraInfo);

                } else {
                    try {
                        userServiceInternal.createUser(user);
                        ActivityService.ActivityType activityType = ActivityService.ActivityType.CREATED;
                        Map<String, String> extraInfo = new HashMap<>();
                        extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
                        activityService.postActivity(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE),
                                                     user.getUsername(), user.getUsername(),
                                                     activityType, ActivityService.ActivitySource.API, extraInfo);
                    } catch (UserAlreadyExistsException e) {
                        logger.debug("Error adding user " + username + " from external authentication provider",
                                     e);
                        throw new AuthenticationSystemException("Error adding user " + username +
                                " from external authentication provider", e);
                    }
                }
            } catch (ServiceLayerException e) {
                logger.debug("Unknown service error", e);

                throw  new AuthenticationSystemException("Unknown service error" , e);
            }

            for (UserGroup userGroup : user.getGroups()) {
                upsertUserGroup(userGroup.getGroup().getGroupName(), user.getUsername(), authenticationChain);
            }

            String token = createToken(user, authenticationChain);
            storeAuthentication(new Authentication(username, token, AuthenticationType.LDAP));

            return true;
        } else {
            logger.debug("Failed to retrieve LDAP user details");

            throw new AuthenticationSystemException("Failed to retrieve LDAP user details");
        }
    }

    private void extractGroupsFromAttribute(User user, String groupNameAttribName, Attribute groupNameAttrib) throws
                                                                                                              NamingException {
        if (groupNameAttrib != null && groupNameAttrib.size() > 0) {
            NamingEnumeration groupAttribValues = groupNameAttrib.getAll();
            while (groupAttribValues.hasMore()) {
                Object groupNameObj = groupAttribValues.next();
                if (groupNameObj != null) {
                    String groupName = extractGroupNameFromAttributeValue(groupNameObj.toString());
                    if (StringUtils.isNotEmpty(groupName)) {
                        addGroupToUser(user, groupName);
                    }
                }
            }
        } else {
            logger.debug("No LDAP attribute " + groupNameAttribName + " found for username " + user.getUsername());
        }
    }

    private String extractGroupNameFromAttributeValue(String groupAttributeValue) {
        Pattern pattern = Pattern.compile(groupNameLdapAttributeRegex);
        Matcher matcher = pattern.matcher(groupAttributeValue);
        if (matcher.matches()) {
            return matcher.group(groupNameLdapAttributeMatchIndex);
        }

        return StringUtils.EMPTY;
    }

    private void addGroupToUser(User user, String groupName) {
        Group group = new Group();
        group.setGroupName(groupName);
        group.setGroupDescription("Externally managed group");
        group.setOrganization(null);

        UserGroup userGroup = new UserGroup();
        userGroup.setGroup(group);
        if (user.getGroups() == null) {
            user.setGroups(new ArrayList<UserGroup>());
        }
        user.getGroups().add(userGroup);
    }

    protected boolean upsertUserGroup(String groupName, String username, AuthenticationChain authenticationChain) {
        UserDAO userDao = authenticationChain.getUserDao();
        GroupDAO groupDao = authenticationChain.getGroupDao();
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

                ActivityService.ActivityType activityType = ActivityService.ActivityType.ADD_USER_TO_GROUP;
                Map<String, String> extraInfo = new HashMap<>();
                extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
                activityService.postActivity("", "LDAP", username + " > " + groupName, activityType,
                                             ActivityService.ActivitySource.API, extraInfo);
            } catch (Exception e) {
                logger.debug("Unknown database error", e);
            }
        }
        return true;
    }

    public String getLdapUrl() {
        return ldapUrl;
    }

    public void setLdapUrl(String ldapUrl) {
        this.ldapUrl = ldapUrl;
    }

    public String getLdapPassword() {
        return ldapPassword;
    }

    public void setLdapPassword(String ldapPassword) {
        this.ldapPassword = ldapPassword;
    }

    public String getLdapUsername() {
        return ldapUsername;
    }

    public void setLdapUsername(String ldapUsername) {
        this.ldapUsername = ldapUsername;
    }

    public String getLdapBaseContext() {
        return ldapBaseContext;
    }

    public void setLdapBaseContext(String ldapBaseContext) {
        this.ldapBaseContext = ldapBaseContext;
    }

    public String getUsernameLdapAttribute() {
        return usernameLdapAttribute;
    }

    public void setUsernameLdapAttribute(String usernameLdapAttribute) {
        this.usernameLdapAttribute = usernameLdapAttribute;
    }

    public String getFirstNameLdapAttribute() {
        return firstNameLdapAttribute;
    }

    public void setFirstNameLdapAttribute(String firstNameLdapAttribute) {
        this.firstNameLdapAttribute = firstNameLdapAttribute;
    }

    public String getLastNameLdapAttribute() {
        return lastNameLdapAttribute;
    }

    public void setLastNameLdapAttribute(String lastNameLdapAttribute) {
        this.lastNameLdapAttribute = lastNameLdapAttribute;
    }

    public String getGroupNameLdapAttribute() {
        return groupNameLdapAttribute;
    }

    public void setGroupNameLdapAttribute(String groupNameLdapAttribute) {
        this.groupNameLdapAttribute = groupNameLdapAttribute;
    }

    public String getGroupNameLdapAttributeRegex() {
        return groupNameLdapAttributeRegex;
    }

    public void setGroupNameLdapAttributeRegex(String groupNameLdapAttributeRegex) {
        this.groupNameLdapAttributeRegex = groupNameLdapAttributeRegex;
    }

    public int getGroupNameLdapAttributeMatchIndex() {
        return groupNameLdapAttributeMatchIndex;
    }

    public void setGroupNameLdapAttributeMatchIndex(int groupNameLdapAttributeMatchIndex) {
        this.groupNameLdapAttributeMatchIndex = groupNameLdapAttributeMatchIndex;
    }

    public String getEmailLdapAttribute() {
        return emailLdapAttribute;
    }

    public void setEmailLdapAttribute(String emailLdapAttribute) {
        this.emailLdapAttribute = emailLdapAttribute;
    }
}
