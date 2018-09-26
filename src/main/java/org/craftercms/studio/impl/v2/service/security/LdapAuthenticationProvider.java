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
import org.craftercms.studio.api.v1.exception.security.BadCredentialsException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
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
import org.craftercms.studio.api.v2.service.security.AuthenticationProvider;
import org.craftercms.studio.api.v2.service.security.BaseAuthenticationProvider;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.impl.v1.util.SessionTokenUtils;
import org.craftercms.studio.model.AuthenticationType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.CommunicationException;
import org.springframework.ldap.core.AuthenticatedLdapEntryContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapEntryIdentification;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQuery;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.craftercms.studio.api.v1.constant.StudioConstants.DEFAULT_ORGANIZATION_ID;
import static org.craftercms.studio.api.v1.constant.StudioConstants.HTTP_SESSION_ATTRIBUTE_AUTHENTICATION;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_LDAP_USER_ATTRIBUTE_EMAIL;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_LDAP_USER_ATTRIBUTE_FIRST_NAME;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_LDAP_USER_ATTRIBUTE_GROUP_NAME;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_LDAP_USER_ATTRIBUTE_GROUP_NAME_MATCH_INDEX;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_LDAP_USER_ATTRIBUTE_GROUP_NAME_REGEX;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_LDAP_USER_ATTRIBUTE_LAST_NAME;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_LDAP_USER_ATTRIBUTE_USERNAME;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_SESSION_TIMEOUT;
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

    private StudioConfiguration studioConfiguration;
    private UserServiceInternal userServiceInternal;
    private ActivityService activityService;

    private UserDAO userDao;
    private GroupDAO groupDAO;

    @Override
    public boolean doAuthenticate(HttpServletRequest request, HttpServletResponse response, AuthenticationChain authenticationChain) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        LdapContextSource lcs = new LdapContextSource();
        lcs.setUrl(ldapUrl);
        lcs.setUserDn(ldapUsername);
        lcs.setPassword(ldapPassword);
        lcs.setBase(ldapBaseContext);
        lcs.setDirObjectFactory(DefaultDirObjectFactory.class);
        lcs.afterPropertiesSet();
        LdapTemplate ldapTemplate = new LdapTemplate(lcs);

        // Mapper for user data if user is successfully authenticated
        AuthenticatedLdapEntryContextMapper<UserTO> mapper = new AuthenticatedLdapEntryContextMapper<UserTO>() {
            @Override
            public UserTO mapWithContext(DirContext dirContext, LdapEntryIdentification ldapEntryIdentification) {
                try {
                    // User entry - extract attributes
                    DirContextOperations dirContextOperations =
                            (DirContextOperations)dirContext.lookup(ldapEntryIdentification.getRelativeName());
                    Attributes attributes = dirContextOperations.getAttributes();
                    Attribute emailAttrib = attributes.get(emailLdapAttribute);
                    Attribute firstNameAttrib = attributes.get(firstNameLdapAttribute);
                    Attribute lastNameAttrib = attributes.get(lastNameLdapAttribute);
                    Attribute groupNameAttrib = attributes.get(groupNameLdapAttribute);


                    UserTO userTO = new UserTO();
                    userTO.setActive(1);
                    userTO.setUsername(username);
                    userTO.setPassword(UUID.randomUUID().toString());

                    if (emailAttrib != null && emailAttrib.get() != null) {
                        userTO.setEmail(emailAttrib.get().toString());
                    } else {
                        logger.error("No LDAP attribute " + emailLdapAttribute + " found for username " + username +
                                ". User will not be imported into DB.");
                        return null;
                    }
                    if (firstNameAttrib != null && firstNameAttrib.get() != null) {
                        userTO.setFirstName(firstNameAttrib.get().toString());
                    } else {
                        logger.warn("No LDAP attribute " + firstNameLdapAttribute + " found for username " + username);
                    }
                    if (lastNameAttrib != null && lastNameAttrib.get() != null) {
                        userTO.setLastName(lastNameAttrib.get().toString());
                    } else {
                        logger.warn("No LDAP attribute " + lastNameLdapAttribute + " found for username " + username);
                    }


                    extractGroupsFromAttribute(userTO, groupNameLdapAttribute, groupNameAttrib);


                    return userTO;
                } catch (NamingException e) {
                    logger.error("Error getting details from LDAP for username " + username, e);

                    return null;
                }
            }
        };

        // Create ldap query to authenticate user
        LdapQuery ldapQuery = query().where(usernameLdapAttribute).is(username);
        UserTO userTO;
        try {
            userTO = ldapTemplate.authenticate(ldapQuery, password, mapper);
        } catch (EmptyResultDataAccessException e) {
            logger.info("User " + username +
                    " not found with external security provider. Trying to authenticate against studio database");
            // When user not found try to authenticate against studio database
            return false;
        } catch (CommunicationException e) {
            logger.info("Failed to connect with external security provider. " +
                    "Trying to authenticate against studio database");
            // When user not found try to authenticate against studio database
            return false;
        } catch (AuthenticationException e) {
            logger.error("Authentication failed with the LDAP system", e);

            return false;
        } catch (Exception e) {
            logger.error("Authentication failed with the LDAP system", e);

            return false;
        }

        if (userTO != null) {
            // When user authenticated against LDAP, upsert user data into studio database
            try {
                if (userServiceInternal.userExists(username)) {
                        userServiceInternal.updateUser(userTO);

                        ActivityService.ActivityType activityType = ActivityService.ActivityType.UPDATED;
                        Map<String, String> extraInfo = new HashMap<>();
                        extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
                        activityService.postActivity(getSystemSite(), userTO.getUsername(), userTO.getUsername(),
                                activityType, ActivityService.ActivitySource.API, extraInfo);

                } else {
                    try {
                        userServiceInternal.createUser(userTO);
                        ActivityService.ActivityType activityType = ActivityService.ActivityType.CREATED;
                        Map<String, String> extraInfo = new HashMap<>();
                        extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
                        activityService.postActivity(getSystemSite(), userTO.getUsername(), userTO.getUsername(),
                                activityType, ActivityService.ActivitySource.API, extraInfo);
                    } catch (UserAlreadyExistsException e) {
                        logger.error("Error adding user " + username + " from external authentication provider",
                                e);
                    }
                }
            } catch (ServiceLayerException e) {
                logger.error("Unknown service error", e);
            }

            for (UserGroupTO userGroup : userTO.getGroups()) {
                upsertUserGroup(userGroup.getGroup().getGroupName(), userTO.getUsername());
            }

            String token = createToken(userTO);

            storeAuthentication(new Authentication(username, token, AuthenticationType.LDAP));

            return true;
        } else {
            logger.error("Failed to retrieve LDAP user details");

            return false;
        }
    }

    protected String createToken(UserTO user) {
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

    private void extractGroupsFromAttribute(UserTO user, String groupNameAttribName, Attribute groupNameAttrib) throws
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

    private void addGroupToUser(UserTO userTO, String groupName) {
        GroupTO group = new GroupTO();
        group.setGroupName(groupName);
        group.setGroupDescription("Externally managed group");
        group.setOrganization(null);

        UserGroupTO userGroup = new UserGroupTO();
        userGroup.setGroup(group);
        if (userTO.getGroups() == null) {
            userTO.setGroups(new ArrayList<UserGroupTO>());
        }
        userTO.getGroups().add(userGroup);
    }

    protected boolean upsertUserGroup(String groupName, String username) {

        try {
            Map<String, Object> params = new HashMap<>();
            params.put(ORG_ID, DEFAULT_ORGANIZATION_ID);
            params.put(GROUP_NAME, groupName);
            params.put(GROUP_DESCRIPTION, "Externally managed group - " + groupName);
            groupDAO.createGroup(params);
        } catch (Exception e) {
            logger.warn("Error creating group", e);
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_NAME, groupName);
        GroupTO groupTO = groupDAO.getGroupByName(params);
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
                groupDAO.addGroupMembers(params);

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

    private String getSystemSite() {
        return studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE);
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

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public ActivityService getActivityService() {
        return activityService;
    }

    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }

    public UserDAO getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDAO userDao) {
        this.userDao = userDao;
    }

    public GroupDAO getGroupDAO() {
        return groupDAO;
    }

    public void setGroupDAO(GroupDAO groupDAO) {
        this.groupDAO = groupDAO;
    }
}
