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

package org.craftercms.studio.impl.v1.service.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.constant.StudioXmlConstants;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.ebus.RepositoryEventContext;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.PasswordDoesNotMatchException;
import org.craftercms.studio.api.v1.exception.security.UserExternallyManagedException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.job.CronJobContext;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ContentTypeService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.security.UserDetailsManager;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v1.to.PermissionsConfigTO;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.security.AuthenticationChain;
import org.craftercms.studio.api.v2.service.security.GroupService;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.SessionTokenUtils;
import org.craftercms.studio.impl.v2.service.security.Authentication;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_EMAIL;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_EXTERNALLY_MANAGED;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_FIRSTNAME;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_LASTNAME;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_USERNAME;
import static org.craftercms.studio.api.v1.constant.StudioConstants.ADMIN_ROLE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.HTTP_SESSION_ATTRIBUTE_AUTHENTICATION;
import static org.craftercms.studio.api.v1.constant.StudioConstants.MODULE_STUDIO;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SECURITY_AUTHENTICATION_TYPE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SYSTEM_ADMIN_GROUP;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_LOGOUT;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_USER;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_PERMISSION_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_ROLE_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_PERMISSION_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.MAIL_FROM_DEFAULT;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.MAIL_SMTP_AUTH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_CIPHER_ALGORITHM;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_CIPHER_KEY;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_CIPHER_TYPE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_SESSION_TIMEOUT;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_TYPE;

/**
 * @author Dejan Brkic
 */
public class SecurityServiceImpl implements SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityServiceImpl.class);

    protected ContentTypeService contentTypeService;
    protected ContentService contentService;
    protected GeneralLockService generalLockService;
    protected StudioConfiguration studioConfiguration;
    protected JavaMailSender emailService;
    protected JavaMailSender emailServiceNoAuth;
    protected UserDetailsManager userDetailsManager;
    protected ObjectFactory<FreeMarkerConfig> freeMarkerConfig;
    protected GroupService groupService;
    protected UserServiceInternal userServiceInternal;
    protected AuthenticationChain authenticationChain;
    protected ConfigurationService configurationService;
    protected AuditServiceInternal auditServiceInternal;
    protected SiteService siteService;

    @Override
    @ValidateParams
    public String authenticate(@ValidateStringParam(name = "username") String username,
                               @ValidateStringParam(name = "password") String password) throws Exception {
        RequestContext requestContext = RequestContext.getCurrent();
        HttpServletRequest request = requestContext.getRequest();
        HttpServletResponse response = requestContext.getResponse();
        authenticationChain.doAuthenticate(request, response, username, password);
        return getCurrentToken();
    }

    @Override
    @ValidateParams
    public boolean validateTicket(@ValidateStringParam(name = "ticket") String ticket) {
        if (ticket == null) {
            ticket = getCurrentToken();
        }
        boolean valid = false;
        if (StringUtils.isNotEmpty(ticket)) valid = true;
        return valid;
    }

    @Override
    public String getCurrentUser() {
        String username = null;
        RequestContext context = RequestContext.getCurrent();

        if(context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            Authentication auth = (Authentication) httpSession.getAttribute(HTTP_SESSION_ATTRIBUTE_AUTHENTICATION);

            if (auth != null) {
                username = auth.getUsername();
            }
        } else {
            CronJobContext cronJobContext = CronJobContext.getCurrent();

            if (cronJobContext != null) {
                username = cronJobContext.getCurrentUser();
            } else {
                RepositoryEventContext repositoryEventContext = RepositoryEventContext.getCurrent();
                if (repositoryEventContext != null) {
                    username = repositoryEventContext.getCurrentUser();
                }
            }
        }

        return username;
    }

    @Override
    public String getCurrentToken() {
        String ticket = null;
        RequestContext context = RequestContext.getCurrent();

        if (context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            Authentication auth = (Authentication) httpSession.getAttribute(HTTP_SESSION_ATTRIBUTE_AUTHENTICATION);

            if (auth != null) {
                ticket = auth.getToken();
            }
        } else {
            ticket = getJobOrEventTicket();
        }

        if (ticket == null) {
            ticket = "NOTICKET";
        }

        return ticket;
    }

    @Override
    @ValidateParams
    public Map<String,Object> getUserProfile(@ValidateStringParam(name = "user") String user)
            throws ServiceLayerException, UserNotFoundException {
        Map<String, Object> toRet = new HashMap<String, Object>();
        User u = userServiceInternal.getUserByIdOrUsername(-1, user);
        if (u != null) {
            toRet.put(KEY_USERNAME, user);
            toRet.put(KEY_FIRSTNAME, u.getFirstName());
            toRet.put(KEY_LASTNAME, u.getLastName());
            toRet.put(KEY_EMAIL, u.getEmail());
            toRet.put(KEY_EXTERNALLY_MANAGED, u.isExternallyManaged());
            String authenticationType = studioConfiguration.getProperty(SECURITY_TYPE);
            toRet.put(SECURITY_AUTHENTICATION_TYPE, authenticationType);
        }
        return toRet;
    }

    @Override
    public Map<String, Object> getUserProfileByGitName(
            @ValidateStringParam(name = "firstNameLastName") String gitName)
            throws ServiceLayerException, UserNotFoundException {
        Map<String, Object> toRet = new HashMap<String, Object>();
        User u = userServiceInternal.getUserByGitName(gitName);
        if (u != null) {
            toRet.put(KEY_USERNAME, u.getUsername());
            toRet.put(KEY_FIRSTNAME, u.getFirstName());
            toRet.put(KEY_LASTNAME, u.getLastName());
            toRet.put(KEY_EMAIL, u.getEmail());
            toRet.put(KEY_EXTERNALLY_MANAGED, u.isExternallyManaged());
            String authenticationType = studioConfiguration.getProperty(SECURITY_TYPE);
            toRet.put(SECURITY_AUTHENTICATION_TYPE, authenticationType);
        } else {
            throw new UserNotFoundException("User " + gitName + " not found");
        }
        return toRet;
    }

    @Override
    @ValidateParams
    public Set<String> getUserPermissions(@ValidateStringParam(name = "site") final String site,
                                          @ValidateSecurePathParam(name = "path") String path,
                                          List<String> groups) {
        return this.getUserPermissions(site, path, getCurrentUser(), groups);
    }

    @Override
    @ValidateParams
    public Set<String> getUserPermissions(@ValidateStringParam(name = "site") final String site,
                                          @ValidateSecurePathParam(name = "path") String path,
                                          @ValidateStringParam(name = "user") String user, List<String> groups) {
        Set<String> permissions = new HashSet<String>();
        if (StringUtils.isNotEmpty(site)) {
            PermissionsConfigTO rolesConfig = loadConfiguration(site, getRoleMappingsFileName());
            PermissionsConfigTO permissionsConfig = loadConfiguration(site, getPermissionsFileName());
            Set<String> roles = new HashSet<String>();
            addUserRoles(roles, site, user);
            addGroupRoles(roles, site, groups, rolesConfig);
            // resolve the permission
            permissions = populateUserPermissions(site, path, roles, permissionsConfig);
            // check if the user is allowed to edit the content

            if (path.indexOf("/site") == 0) { // If it's content a file
                try {
                    ContentTypeConfigTO config = contentTypeService.getContentTypeForContent(site, path);
                    boolean isAllowed = contentTypeService.isUserAllowed(roles, config);
                    if (!isAllowed) {
                        logger.debug("The user is not allowed to access " + site + ":" + path
                                + ". adding permission: " + StudioConstants.PERMISSION_VALUE_NOT_ALLOWED);
                        // If no default role is set
                        permissions.add(StudioConstants.PERMISSION_VALUE_NOT_ALLOWED);
                        return permissions;
                    }
                } catch (ServiceLayerException e) {
                    logger.debug("Error while getting the content type of " + path
                            + ". skipping user role checking on the content.");
                }
            }
        }

        PermissionsConfigTO globalRolesConfig = loadGlobalRolesConfiguration();
        PermissionsConfigTO globalPermissionsConfig = loadGlobalPermissionsConfiguration();
        Set<String> roles = new HashSet<String>();
        addGlobalUserRoles(user, roles, globalRolesConfig);
        addGlobalGroupRoles(roles, groups, globalRolesConfig);
        permissions.addAll(populateUserGlobalPermissions(path, roles, globalPermissionsConfig));
        return permissions;
    }

    protected void addGlobalUserRoles(String user, Set<String> roles, PermissionsConfigTO rolesConfig) {
        try {
            List<Group> groups = userServiceInternal.getUserGroups(-1, user);
            if (rolesConfig != null && groups != null) {
                Map<String, List<String>> rolesMap = rolesConfig.getRoles();
                for (Group group : groups) {
                    String groupName = group.getGroupName();
                    List<String> userRoles = rolesMap.get(groupName);
                    if (roles != null && userRoles != null) {
                        roles.addAll(userRoles);
                    }
                }
            }
        } catch (ServiceLayerException | UserNotFoundException e) {
            logger.error("Unable to retrieve user groups for user {0}", user);
        }
    }

    protected void addGlobalGroupRoles(Set<String> roles, List<String> groups, PermissionsConfigTO rolesConfig) {
        if (groups != null) {
            Map<String, List<String>> rolesMap = rolesConfig.getRoles();
            for (String group : groups) {
                List<String> groupRoles = rolesMap.get(group);
                if (groupRoles != null) {
                    logger.debug("Adding roles by group " + group + ": " + roles);
                    roles.addAll(groupRoles);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected Set<String> populateUserGlobalPermissions(String path, Set<String> roles,
                                                  PermissionsConfigTO permissionsConfig) {
        Set<String> permissions = new HashSet<String>();
        if (roles != null && !roles.isEmpty()) {
            for (String role : roles) {
                Map<String, Map<String, List<Node>>> permissionsMap = permissionsConfig.getPermissions();
                Map<String, List<Node>> siteRoles = permissionsMap.get("###GLOBAL###");
                if (siteRoles == null || siteRoles.isEmpty()) {
                    siteRoles = permissionsMap.get("*");
                }
                if (siteRoles != null && !siteRoles.isEmpty()) {
                    List<Node> ruleNodes = siteRoles.get(role);
                    if (ruleNodes == null || ruleNodes.isEmpty()) {
                        ruleNodes = siteRoles.get("*");
                    }
                    if (ruleNodes != null && !ruleNodes.isEmpty()) {
                        for (Node ruleNode : ruleNodes) {
                            String regex = ruleNode.valueOf(StudioXmlConstants.DOCUMENT_ATTR_REGEX);
                            if (path.matches(regex)) {
                                logger.debug("Global permissions found by matching " + regex + " for " + role);

                                List<Node> permissionNodes =
                                        ruleNode.selectNodes(StudioXmlConstants.DOCUMENT_ELM_ALLOWED_PERMISSIONS);
                                for (Node permissionNode : permissionNodes) {
                                    String permission = permissionNode.getText().toLowerCase();
                                    logger.debug("adding global permissions " + permission + " to " + path
                                            + " for " + role);
                                    permissions.add(permission);
                                }
                            }
                        }
                    } else {
                        logger.debug("No default role is set. adding default permission: "
                                + StudioConstants.PERMISSION_VALUE_READ);
                        // If no default role is set
                        permissions.add(StudioConstants.PERMISSION_VALUE_READ);
                    }
                } else {
                    logger.debug("No default site is set. adding default permission: "
                            + StudioConstants.PERMISSION_VALUE_READ);
                    // If no default site is set
                    permissions.add(StudioConstants.PERMISSION_VALUE_READ);
                }
            }
        } else {
            logger.debug("No user or group matching found. adding default permission: "
                    + StudioConstants.PERMISSION_VALUE_READ);
            // If user or group did not match the roles-mapping file
            permissions.add(StudioConstants.PERMISSION_VALUE_READ);
        }
        return permissions;
    }

    /* Derives a key based off the site and filename */
    protected String getPermissionsKey(String site, String filename) {
        return new StringBuffer(site).append(":").append(filename).toString();
    }

    /**
     * add user roles
     *
     * @param roles
     * @param site
     * @param user
     */
    protected void addUserRoles(Set<String> roles, String site, String user) {
        if (!StringUtils.isEmpty(user)) {
            Set<String> userRoles = this.getUserRoles(site, user);
            logger.debug("Adding roles by user: " + userRoles);
            roles.addAll(userRoles);
        }
    }

    @Override
    @ValidateParams
    public Set<String> getUserRoles(@ValidateStringParam(name = "site") final String site) {
        return getUserRoles(site, getCurrentUser());
    }

    @Override
    @ValidateParams
    public Set<String> getUserRoles(@ValidateStringParam(name = "site") final String site,
                                    @ValidateStringParam(name = "user") String user) {
        return getUserRoles(site, user, false);
    }


    @Override
    @ValidateParams
    public Set<String> getUserRoles(@ValidateStringParam(name = "site") final String site,
                                    @ValidateStringParam(name = "user") String user, boolean includeGlobal) {
        try {
            // TODO: We should replace this with userService.getUserSiteRoles, but that one is protected by permissions.
            // TODO: When the UserService is refactored to use UserServiceInternal, we could use that method and
            // TODO: remove this one
            List<Group> groups = userServiceInternal.getUserGroups(-1, user);
            if (groups != null && groups.size() > 0) {
                logger.debug("Groups for " + user + " in " + site + ": " + groups);

                PermissionsConfigTO rolesConfig = loadConfiguration(site, getRoleMappingsFileName());
                Set<String> userRoles = new HashSet<String>();
                if (rolesConfig != null) {
                    Map<String, List<String>> rolesMap = rolesConfig.getRoles();
                    for (Group group : groups) {
                        String groupName = group.getGroupName();
                        if (StringUtils.equals(groupName, SYSTEM_ADMIN_GROUP)) {
                            Collection<List<String>> mapValues = rolesMap.values();
                            mapValues.forEach(valueList -> {
                                userRoles.addAll(valueList);
                            });
                            break;
                        } else {
                            List<String> roles = rolesMap.get(groupName);
                            if (roles != null) {
                                userRoles.addAll(roles);
                            }
                        }
                    }
                }
                if (includeGlobal) {
                    PermissionsConfigTO globalRolesConfig = loadGlobalRolesConfiguration();
                    addGlobalUserRoles(user, userRoles, globalRolesConfig);
                    List<String> groupNames = groups.stream().map(x -> x.getGroupName()).collect(Collectors.toList());
                    addGlobalGroupRoles(userRoles, groupNames, globalRolesConfig);
                }
                return userRoles;
            } else {
                logger.debug("No groups found for " + user + " in " + site);
            }
        } catch (ServiceLayerException | UserNotFoundException e) {
            logger.error("Error while getting groups for user {0}", e);
        }

        return new HashSet<>(0);
    }

    /**
     * get roles by groups
     *
     * @param site
     * @param groups
     * @param rolesConfig
     */
    protected void addGroupRoles(Set<String> roles, String site, List<String> groups,
                                 PermissionsConfigTO rolesConfig) {
        if (groups != null) {
            Map<String, List<String>> rolesMap = rolesConfig.getRoles();
            for (String group : groups) {
                List<String> groupRoles = rolesMap.get(group);
                if (groupRoles != null) {
                    logger.debug("Adding roles by group " + group + ": " + roles);
                    roles.addAll(groupRoles);
                }
            }
        }
    }

    /**
     * populate user permissions
     *
     * @param site
     * @param path
     * @param roles
     * @param permissionsConfig
     */
    @SuppressWarnings("unchecked")
    protected Set<String> populateUserPermissions(String site, String path, Set<String> roles,
                                                  PermissionsConfigTO permissionsConfig) {
        Set<String> permissions = new HashSet<String>();
        if (roles != null && !roles.isEmpty()) {
            for (String role : roles) {
                Map<String, Map<String, List<Node>>> permissionsMap = permissionsConfig.getPermissions();
                Map<String, List<Node>> siteRoles = permissionsMap.get(site);
                if (siteRoles == null || siteRoles.isEmpty()) {
                    siteRoles = permissionsMap.get("*");
                }
                if (siteRoles != null && !siteRoles.isEmpty()) {
                    List<Node> ruleNodes = siteRoles.get(role);
                    if (ruleNodes == null || ruleNodes.isEmpty()) {
                        ruleNodes = siteRoles.get("*");
                    }
                    if (ruleNodes != null && !ruleNodes.isEmpty()) {
                        for (Node ruleNode : ruleNodes) {
                            String regex = ruleNode.valueOf(StudioXmlConstants.DOCUMENT_ATTR_REGEX);
                            if (path.matches(regex)) {
                                logger.debug("Permissions found by matching " + regex + " for " + role
                                        + " in " + site);

                                List<Node> permissionNodes = ruleNode.selectNodes(
                                        StudioXmlConstants.DOCUMENT_ELM_ALLOWED_PERMISSIONS);
                                for (Node permissionNode : permissionNodes) {
                                    String permission = permissionNode.getText().toLowerCase();
                                    logger.debug("adding permissions " + permission + " to " + path + " for "
                                            + role + " in " + site);
                                    permissions.add(permission);
                                }
                            }
                        }
                    } else {
                        logger.debug("No default role is set. adding default permission: "
                                + StudioConstants.PERMISSION_VALUE_READ);
                        // If no default role is set
                        permissions.add(StudioConstants.PERMISSION_VALUE_READ);
                    }
                } else {
                    logger.debug("No default site is set. adding default permission: "
                            + StudioConstants.PERMISSION_VALUE_READ);
                    // If no default site is set
                    permissions.add(StudioConstants.PERMISSION_VALUE_READ);
                }
            }
        } else {
            logger.debug("No user or group matching found. adding default permission: "
                    + StudioConstants.PERMISSION_VALUE_READ);
            // If user or group did not match the roles-mapping file
            permissions.add(StudioConstants.PERMISSION_VALUE_READ);
        }
        return permissions;
    }

    protected PermissionsConfigTO loadConfiguration(String site, String filename) {
        Document document = null;
        PermissionsConfigTO config = null;
        try {
            document = configurationService.getConfigurationAsDocument(site, MODULE_STUDIO, filename,
                    studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE));
        } catch (DocumentException | IOException e) {
            logger.error("Permission mapping not found for " + site + ":" + filename);
        }
        if (document != null) {
            config = new PermissionsConfigTO();
            config.setMapping(document);
            Element root = document.getRootElement();

            // roles file
            loadRoles(root, config);

            // permissions file
            loadPermissions(site, root, config);

            config.setKey(site + ":" + filename);
            config.setLastUpdated(ZonedDateTime.now(ZoneOffset.UTC));

        } else {
            logger.error("Permission mapping not found for " + site + ":" + filename);
        }
        return config;
    }

    @SuppressWarnings("unchecked")
    protected void loadRoles(Element root, PermissionsConfigTO config) {
        if (root.getName().equals(StudioXmlConstants.DOCUMENT_ROLE_MAPPINGS)) {
            Map<String, List<String>> rolesMap = new HashMap<String, List<String>>();

            List<Node> userNodes = root.selectNodes(StudioXmlConstants.DOCUMENT_ELM_USER_NODE);
            rolesMap = getRoles(userNodes, rolesMap);

            List<Node> groupNodes = root.selectNodes(StudioXmlConstants.DOCUMENT_ELM_GROUPS_NODE);
            rolesMap = getRoles(groupNodes, rolesMap);

            config.setRoles(rolesMap);
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, List<String>> getRoles(List<Node> nodes, Map<String, List<String>> rolesMap) {
        for (Node node : nodes) {
            String name = node.valueOf(StudioXmlConstants.DOCUMENT_ATTR_PERMISSIONS_NAME);
            if (!StringUtils.isEmpty(name)) {
                List<Node> roleNodes = node.selectNodes(StudioXmlConstants.DOCUMENT_ELM_PERMISSION_ROLE);
                List<String> roles = new ArrayList<String>();
                for (Node roleNode : roleNodes) {
                    roles.add(roleNode.getText());
                }
                rolesMap.put(name, roles);
            }
        }
        return rolesMap;
    }

    @SuppressWarnings("unchecked")
    protected void loadPermissions(String siteId, Element root, PermissionsConfigTO config) {
        if (root.getName().equals(StudioXmlConstants.DOCUMENT_PERMISSIONS)) {
            Map<String, Map<String, List<Node>>> permissionsMap = new HashMap<String, Map<String, List<Node>>>();

            //backwards compatibility for nested <site>
            Element permissionsRoot = root;
            Element siteNode = (Element) permissionsRoot.selectSingleNode(StudioXmlConstants.DOCUMENT_ELM_SITE);
            if(siteNode != null) {
                permissionsRoot = siteNode;
            }

            List<Node> roleNodes = permissionsRoot.selectNodes(StudioXmlConstants.DOCUMENT_ELM_PERMISSION_ROLE);
            Map<String, List<Node>> rules = new HashMap<String, List<Node>>();
            for (Node roleNode : roleNodes) {
                String roleName = roleNode.valueOf(StudioXmlConstants.DOCUMENT_ATTR_PERMISSIONS_NAME);
                List<Node> ruleNodes = roleNode.selectNodes(StudioXmlConstants.DOCUMENT_ELM_PERMISSION_RULE);
                rules.put(roleName, ruleNodes);
            }
            permissionsMap.put(siteId, rules);

            config.setPermissions(permissionsMap);
        }
    }


    protected PermissionsConfigTO loadGlobalPermissionsConfiguration() {
        String globalPermissionsConfigPath = getGlobalConfigPath() + FILE_SEPARATOR + getGlobalPermissionsFileName();
        Document document = null;
        PermissionsConfigTO config = null;
        try {
            document = contentService.getContentAsDocument(StringUtils.EMPTY, globalPermissionsConfigPath);
        } catch (DocumentException e) {
            logger.error("Global permission mapping not found (path: {0})", globalPermissionsConfigPath);
        }
        if (document != null) {
            config = new PermissionsConfigTO();
            config.setMapping(document);
            Element root = document.getRootElement();

            // permissions file
            loadPermissions("###GLOBAL###", root, config);

            String globalPermissionsKey = "###GLOBAL###:" + getGlobalPermissionsFileName();
            config.setKey(globalPermissionsKey);
            config.setLastUpdated(ZonedDateTime.now(ZoneOffset.UTC));

        } else {
            logger.error("Global permission mapping not found (path: {0})", globalPermissionsConfigPath);
        }
        return config;
    }

    protected PermissionsConfigTO loadGlobalRolesConfiguration() {
        String globalRolesConfigPath = getGlobalConfigPath() + FILE_SEPARATOR + getGlobalRoleMappingsFileName();
        Document document = null;
        PermissionsConfigTO config = null;
        try {
            document = contentService.getContentAsDocument(StringUtils.EMPTY, globalRolesConfigPath);
        } catch (DocumentException e) {
            logger.error("Global roles mapping not found (path: {0})", globalRolesConfigPath);
        }
        if (document != null) {
            config = new PermissionsConfigTO();
            config.setMapping(document);
            Element root = document.getRootElement();

            // roles file
            loadRoles(root, config);

            String globalRolesKey = "###GLOBAL###:" + getGlobalRoleMappingsFileName();
            config.setKey(globalRolesKey);
            config.setLastUpdated(ZonedDateTime.now(ZoneOffset.UTC));

        } else {
            logger.error("Global roles mapping not found (path: {0})", globalRolesConfigPath);
        }
        return config;
    }


    @Override
    @ValidateParams
    public void reloadConfiguration(@ValidateStringParam(name = "site") String site) {
        PermissionsConfigTO permissionsConfigTO = loadConfiguration(site, getPermissionsFileName());
        PermissionsConfigTO rolesConfigTO = loadConfiguration(site, getRoleMappingsFileName());
    }

    @Override
    public void reloadGlobalConfiguration() {
        PermissionsConfigTO permissionsConfigTO = loadGlobalPermissionsConfiguration();
        PermissionsConfigTO rolesConfigTO = loadGlobalRolesConfiguration();
    }

    @Override
    public boolean logout() throws SiteNotFoundException {
        String username = getCurrentUser();
        RequestContext context = RequestContext.getCurrent();
        if (username != null && context != null) {
            HttpServletRequest httpServletRequest = context.getRequest();
            String ipAddress = httpServletRequest.getRemoteAddr();

            SiteFeed siteFeed = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
            AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
            auditLog.setOperation(OPERATION_LOGOUT);
            auditLog.setActorId(username);
            auditLog.setSiteId(siteFeed.getId());
            auditLog.setPrimaryTargetId(username);
            auditLog.setPrimaryTargetType(TARGET_TYPE_USER);
            auditLog.setPrimaryTargetValue(username);
            auditServiceInternal.insertAuditLog(auditLog);

            logger.info("User " + username + " logged out from IP: " + ipAddress);
        }
        return true;
    }


    @Override
    public int getAllUsersTotal() throws ServiceLayerException {
        return userServiceInternal.getAllUsersTotal();
    }


    @Override
    @ValidateParams
    public boolean validateToken(@ValidateStringParam(name = "token") String token) throws UserNotFoundException,
        UserExternallyManagedException, ServiceLayerException {
        boolean toRet = false;
        String decryptedToken = decryptToken(token);
        if (StringUtils.isNotEmpty(decryptedToken)) {
            StringTokenizer tokenElements = new StringTokenizer(decryptedToken, "|");
            if (tokenElements.countTokens() == 3) {
                String username = tokenElements.nextToken();
                User userProfile = userServiceInternal.getUserByIdOrUsername(-1, username);
                if (userProfile == null) {
                    logger.info("User profile not found for " + username);
                    throw new UserNotFoundException();
                } else {
                    if (userProfile.isExternallyManaged()) {
                        throw new UserExternallyManagedException();
                    } else {
                        long tokenTimestamp = Long.parseLong(tokenElements.nextToken());
                        if (tokenTimestamp < System.currentTimeMillis()) {
                            toRet = false;
                        } else {
                            toRet = true;
                        }
                    }
                }
            }
        }
        return toRet;
    }

    private String decryptToken(String token) {
        try {
            SecretKeySpec key = new SecretKeySpec(studioConfiguration.getProperty(SECURITY_CIPHER_KEY).getBytes(),
                    studioConfiguration.getProperty(SECURITY_CIPHER_TYPE));
            Cipher cipher = Cipher.getInstance(studioConfiguration.getProperty(SECURITY_CIPHER_ALGORITHM));
            byte[] tokenBytes = Base64.getDecoder().decode(token.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(key.getEncoded()));
            byte[] decrypted = cipher.doFinal(tokenBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            logger.error("Error while decrypting forgot password token", e);
            return null;
        }
    }

    @Override
    @ValidateParams
    public boolean changePassword(@ValidateStringParam(name = "username") String username,
                                  @ValidateStringParam(name = "current") String current,
                                  @ValidateStringParam(name = "newPassword") String newPassword)
        throws PasswordDoesNotMatchException, UserExternallyManagedException, ServiceLayerException {
        return userServiceInternal.changePassword(username, current, newPassword);
    }

    @Override
    @ValidateParams
    public Map<String, Object> setUserPassword(@ValidateStringParam(name = "token") String token,
                                               @ValidateStringParam(name = "newPassword") String newPassword)
        throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException {
        Map<String, Object> toRet = new HashMap<String, Object>();
        toRet.put("username", StringUtils.EMPTY);
        toRet.put("success", false);
        if (validateToken(token)) {
            String username = getUsernameFromToken(token);
            if (StringUtils.isNotEmpty(username)) {
                toRet.put("username", username);
                User user = userServiceInternal.getUserByIdOrUsername(-1, username);
                if (user != null ) {
                    if (user.isEnabled()) {
                        toRet.put("success", userServiceInternal.setUserPassword(username, newPassword));
                    }
                } else {
                    throw new UserNotFoundException("User not found");
                }
            } else {
                throw new UserNotFoundException("User not found");
            }
        }
        return toRet;
    }

    private String getUsernameFromToken(String token) {
        String toRet = StringUtils.EMPTY;
        String decryptedToken = decryptToken(token);
        if (StringUtils.isNotEmpty(decryptedToken)) {
            StringTokenizer tokenElements = new StringTokenizer(decryptedToken, "|");
            if (tokenElements.countTokens() == 3) {
                toRet = tokenElements.nextToken();
            }
        }
        return toRet;
    }

    @Override
    @ValidateParams
    public boolean resetPassword(@ValidateStringParam(name = "username") String username,
                                 @ValidateStringParam(name = "newPassword") String newPassword)
        throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException {
        String currentUser = getCurrentUser();
        if (isAdmin(currentUser)) {
            return userServiceInternal.setUserPassword(username, newPassword);
        } else {
            return false;
        }
    }

    private boolean isAdmin(String username) throws ServiceLayerException, UserNotFoundException {
        List<Group> userGroups = userServiceInternal.getUserGroups(-1, username);
        boolean toRet = false;
        if (CollectionUtils.isNotEmpty(userGroups)) {
            for (Group group : userGroups) {
                if (StringUtils.equalsIgnoreCase(group.getGroupName(), SYSTEM_ADMIN_GROUP)) {
                    toRet = true;
                    break;
                }
            }
        }
        return toRet;
    }

    @Override
    @ValidateParams
    public boolean isSiteAdmin(@ValidateStringParam(name = "username") String username, String site) {

        boolean toRet = false;
        try {
            if (userServiceInternal.isUserMemberOfGroup(username, SYSTEM_ADMIN_GROUP)) {
                return true;
            }

            List<Group> groups = userServiceInternal.getUserGroups(-1, username);

            if (CollectionUtils.isNotEmpty(groups)) {
                Map<String, List<String>> roleMappings = configurationService.geRoleMappings(site);

                if (MapUtils.isNotEmpty(roleMappings)) {
                    for (Group group : groups) {
                        String groupName = group.getGroupName();
                        List<String> roles = roleMappings.get(groupName);
                        if (roles.contains(ADMIN_ROLE)) {
                            toRet = true;
                        }
                    }
                }
            }

        } catch (ServiceLayerException | UserNotFoundException e) {
            logger.warn("Error getting user memberships", e);
        }
        return toRet;
    }

    @Override
    @ValidateParams
    public boolean userExists(@ValidateStringParam(name = "username") String username) throws ServiceLayerException {
        return userServiceInternal.userExists(-1, username);
    }

    @Override
    public boolean validateSession(HttpServletRequest request) throws ServiceLayerException {
        HttpSession httpSession = request.getSession();
        String authToken = getCurrentToken();
        String userName = getCurrentUser();

        if (userName != null) {

            UserDetails userDetails = this.userDetailsManager.loadUserByUsername(userName);

            if (SessionTokenUtils.validateToken(authToken, userDetails.getUsername())) {
                return true;
            }

        }

        httpSession.removeAttribute(HTTP_SESSION_ATTRIBUTE_AUTHENTICATION);
        httpSession.invalidate();
        return false;
    }

    @Override
    public Authentication getAuthentication() {
        Authentication auth = null;
        RequestContext context = RequestContext.getCurrent();

        if (context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            auth = (Authentication) httpSession.getAttribute(HTTP_SESSION_ATTRIBUTE_AUTHENTICATION);
        }

        return auth;
    }


    protected String getJobOrEventTicket() {
        String ticket = null;
        CronJobContext cronJobContext = CronJobContext.getCurrent();

        if (cronJobContext != null) {
            ticket = cronJobContext.getAuthenticationToken();
        } else {
            RepositoryEventContext repositoryEventContext = RepositoryEventContext.getCurrent();
            if (repositoryEventContext != null) {
                ticket = repositoryEventContext.getAuthenticationToken();
            }
        }

        return ticket;
    }

    public String getRoleMappingsFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME);
    }

    public String getPermissionsFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_PERMISSION_MAPPINGS_FILE_NAME);
    }

    public String getGlobalConfigPath() {
        return studioConfiguration.getProperty(CONFIGURATION_GLOBAL_CONFIG_BASE_PATH);
    }

    public String getGlobalRoleMappingsFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_GLOBAL_ROLE_MAPPINGS_FILE_NAME);
    }

    public String getGlobalPermissionsFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_GLOBAL_PERMISSION_MAPPINGS_FILE_NAME);
    }

    public int getSessionTimeout() {
        int toReturn = Integer.parseInt(studioConfiguration.getProperty(SECURITY_SESSION_TIMEOUT));
        return toReturn;
    }

    public boolean isAuthenticatedSMTP() {
        boolean toReturn = Boolean.parseBoolean(studioConfiguration.getProperty(MAIL_SMTP_AUTH));
        return toReturn;
    }

    public String getDefaultFromAddress() {
        return studioConfiguration.getProperty(MAIL_FROM_DEFAULT);
    }

    public String getSystemSite() {
        return studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE);
    }

    public ContentTypeService getContentTypeService() {
        return contentTypeService;
    }

    public void setContentTypeService(ContentTypeService contentTypeService) {
        this.contentTypeService = contentTypeService;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
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

    public UserDetailsManager getUserDetailsManager() {
        return userDetailsManager;
    }

    public void setUserDetailsManager(UserDetailsManager userDetailsManager) {
        this.userDetailsManager = userDetailsManager;
    }

    public ObjectFactory<FreeMarkerConfig> getFreeMarkerConfig() {
        return freeMarkerConfig;
    }

    public void setFreeMarkerConfig(ObjectFactory<FreeMarkerConfig> freeMarkerConfig) {
        this.freeMarkerConfig = freeMarkerConfig;
    }

    public GroupService getGroupService() {
        return groupService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public AuthenticationChain getAuthenticationChain() {
        return authenticationChain;
    }

    public void setAuthenticationChain(AuthenticationChain authenticationChain) {
        this.authenticationChain = authenticationChain;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public AuditServiceInternal getAuditServiceInternal() {
        return auditServiceInternal;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
