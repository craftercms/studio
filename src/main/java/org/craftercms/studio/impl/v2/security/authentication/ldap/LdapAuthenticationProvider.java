/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.impl.v2.security.authentication.ldap;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.GroupDAO;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.dal.UserGroup;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.AuthenticatedUser;
import org.craftercms.studio.model.AuthenticationType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.CommunicationException;
import org.springframework.ldap.core.AuthenticatedLdapEntryContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptySet;
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
import static org.springframework.ldap.query.LdapQueryBuilder.query;

/**
 * Implementation of {@link AuthenticationProvider} to support Studio's LDAP authentication
 *
 * @author joseross
 * @since 4.0
 */
public class LdapAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(LdapAuthenticationProvider.class);

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

    protected StudioConfiguration studioConfiguration;
    protected SiteService siteService;
    protected AuditServiceInternal auditServiceInternal;
    protected UserServiceInternal userServiceInternal;
    protected UserDAO userDao;
    protected GroupDAO groupDao;
    protected RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    @ConstructorProperties({"studioConfiguration", "siteService", "auditServiceInternal", "userServiceInternal",
            "userDao", "groupDao", "retryingDatabaseOperationFacade"})
    public LdapAuthenticationProvider(StudioConfiguration studioConfiguration, SiteService siteService,
                                      AuditServiceInternal auditServiceInternal,
                                      UserServiceInternal userServiceInternal, UserDAO userDao, GroupDAO groupDao,
                                      RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.studioConfiguration = studioConfiguration;
        this.siteService = siteService;
        this.auditServiceInternal = auditServiceInternal;
        this.userServiceInternal = userServiceInternal;
        this.userDao = userDao;
        this.groupDao = groupDao;
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var username = authentication.getName();
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
                user.setExternallyManaged(true);
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
            user = ldapTemplate.authenticate(ldapQuery, authentication.getCredentials().toString(), mapper);
        } catch (EmptyResultDataAccessException e) {
            logger.debug("User " + username + " not found in LDAP server.");
            throw new UsernameNotFoundException("User " + username + " not found in LDAP server");
        } catch (CommunicationException e) {
            logger.debug("Failed to connect with LDAP server", e);
            throw new AuthenticationServiceException("Failed to connect with LDAP server", e);
        }  catch (AuthenticationException e) {
            logger.debug("Authentication failed with the LDAP system (bad credentials)", e);
            throw e;
        } catch (Exception e) {
            logger.debug("Unexpected exception when authenticating with the LDAP server", e);
            throw new AuthenticationServiceException("Unexpected exception when authenticating with the LDAP server", e);
        }

        if (user != null) {
            // When user authenticated against LDAP, upsert user data into studio database
            try {
                SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
                if (userServiceInternal.userExists(-1, username)) {
                    try {
                        userServiceInternal.updateUser(user);
                    } catch (UserNotFoundException e) {
                        // Shouldn't happen
                        throw new IllegalStateException(e);
                    }

                    AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
                    auditLog.setOperation(OPERATION_UPDATE);
                    auditLog.setSiteId(siteFeed.getId());
                    auditLog.setActorId(user.getUsername());
                    auditLog.setPrimaryTargetId(user.getUsername());
                    auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
                    auditLog.setPrimaryTargetValue(user.getUsername());
                    auditServiceInternal.insertAuditLog(auditLog);

                } else {
                    try {
                        userServiceInternal.createUser(user);
                        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
                        auditLog.setOperation(OPERATION_CREATE);
                        auditLog.setSiteId(siteFeed.getId());
                        auditLog.setActorId(user.getUsername());
                        auditLog.setPrimaryTargetId(user.getUsername());
                        auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
                        auditLog.setPrimaryTargetValue(user.getUsername());
                        auditServiceInternal.insertAuditLog(auditLog);
                    } catch (UserAlreadyExistsException e) {
                        logger.debug("Error adding user {0} from LDAP server", username);
                        throw new AuthenticationServiceException(
                                "Error adding user " + username + " from LDAP server", e);
                    }
                }
            } catch (ServiceLayerException e) {
                logger.debug("Unknown service error", e);

                throw new AuthenticationServiceException("Unknown service error" , e);
            }

            for (UserGroup userGroup : user.getGroups()) {
                upsertUserGroup(userGroup.getGroup().getGroupName(), user.getUsername());
            }

            // Now that we are done with LDAP, get the updated user from the DB
            user = userDao.getUserByIdOrUsername(Map.of(USERNAME, user.getUsername(), USER_ID, -1));
            var authenticatedUser = new AuthenticatedUser(user);
            authenticatedUser.setAuthenticationType(AuthenticationType.LDAP);

            return new UsernamePasswordAuthenticationToken(authenticatedUser, "N/A", emptySet());
        } else {
            logger.debug("Failed to retrieve LDAP user details");

            throw new AuthenticationServiceException("Failed to retrieve LDAP user details");
        }
    }

    private void extractGroupsFromAttribute(User user, String groupNameAttribName, Attribute groupNameAttrib)
            throws NamingException {
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

    protected void upsertUserGroup(String groupName, String username) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(ORG_ID, DEFAULT_ORGANIZATION_ID);
            params.put(GROUP_NAME, groupName);
            params.put(GROUP_DESCRIPTION, "Externally managed group - " + groupName);
            retryingDatabaseOperationFacade.createGroup(params);
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
                retryingDatabaseOperationFacade.addGroupMembers(params);
                SiteFeed siteFeed =
                        siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
                AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
                auditLog.setOperation(OPERATION_ADD_MEMBERS);
                auditLog.setActorId(user.getUsername());
                auditLog.setSiteId(siteFeed.getId());
                auditLog.setPrimaryTargetId(group.getGroupName() + ":" + user.getUsername());
                auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
                auditLog.setPrimaryTargetValue(user.getUsername());
                auditServiceInternal.insertAuditLog(auditLog);
            } catch (Exception e) {
                logger.debug("Unknown database error", e);
            }
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public void setLdapUrl(String ldapUrl) {
        this.ldapUrl = ldapUrl;
    }

    public void setLdapPassword(String ldapPassword) {
        this.ldapPassword = ldapPassword;
    }

    public void setLdapUsername(String ldapUsername) {
        this.ldapUsername = ldapUsername;
    }

    public void setLdapBaseContext(String ldapBaseContext) {
        this.ldapBaseContext = ldapBaseContext;
    }

    public void setUsernameLdapAttribute(String usernameLdapAttribute) {
        this.usernameLdapAttribute = usernameLdapAttribute;
    }

    public void setFirstNameLdapAttribute(String firstNameLdapAttribute) {
        this.firstNameLdapAttribute = firstNameLdapAttribute;
    }

    public void setLastNameLdapAttribute(String lastNameLdapAttribute) {
        this.lastNameLdapAttribute = lastNameLdapAttribute;
    }

    public void setGroupNameLdapAttribute(String groupNameLdapAttribute) {
        this.groupNameLdapAttribute = groupNameLdapAttribute;
    }

    public void setGroupNameLdapAttributeRegex(String groupNameLdapAttributeRegex) {
        this.groupNameLdapAttributeRegex = groupNameLdapAttributeRegex;
    }

    public void setGroupNameLdapAttributeMatchIndex(int groupNameLdapAttributeMatchIndex) {
        this.groupNameLdapAttributeMatchIndex = groupNameLdapAttributeMatchIndex;
    }

    public void setEmailLdapAttribute(String emailLdapAttribute) {
        this.emailLdapAttribute = emailLdapAttribute;
    }

}
