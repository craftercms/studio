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

package org.craftercms.studio.impl.v1.service.security;

import com.google.common.cache.Cache;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.constant.StudioXmlConstants;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
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
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v1.to.PermissionsConfigTO;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.security.GroupService;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_EMAIL;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_EXTERNALLY_MANAGED;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_FIRSTNAME;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_LASTNAME;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_USERNAME;
import static org.craftercms.studio.api.v1.constant.StudioConstants.ADMIN_ROLE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.MODULE_STUDIO;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SYSTEM_ADMIN_GROUP;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_PERMISSION_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_ROLE_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_PERMISSION_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.MAIL_FROM_DEFAULT;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.MAIL_SMTP_AUTH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_SESSION_TIMEOUT;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_READ;

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
    protected ObjectFactory<FreeMarkerConfig> freeMarkerConfig;
    protected GroupService groupService;
    protected UserServiceInternal userServiceInternal;
    protected ConfigurationService configurationService;
    protected AuditServiceInternal auditServiceInternal;
    protected SiteService siteService;

    protected Cache<String, PermissionsConfigTO> cache;

    @Override
    public String getCurrentUser() {
        String username = null;
        var context = SecurityContextHolder.getContext();

        if(context != null) {
            var auth = context.getAuthentication();

            if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
                username = auth.getName();
            }
        } else {
            CronJobContext cronJobContext = CronJobContext.getCurrent();

            if (cronJobContext != null) {
                username = cronJobContext.getCurrentUser();
            }
        }

        return username;
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
                                + PERMISSION_CONTENT_READ);
                        // If no default role is set
                        permissions.add(PERMISSION_CONTENT_READ);
                    }
                } else {
                    logger.debug("No default site is set. adding default permission: "
                            + PERMISSION_CONTENT_READ);
                    // If no default site is set
                    permissions.add(PERMISSION_CONTENT_READ);
                }
            }
        } else {
            logger.debug("No user or group matching found. adding default permission: "
                    + PERMISSION_CONTENT_READ);
            // If user or group did not match the roles-mapping file
            permissions.add(PERMISSION_CONTENT_READ);
        }
        return permissions;
    }

    protected PermissionsConfigTO loadConfiguration(String site, String filename) {
        var environment = studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE);
        var cacheKey = configurationService.getCacheKey(site, MODULE_STUDIO, filename, environment, "object");
        PermissionsConfigTO config = cache.getIfPresent(cacheKey);
        if (config == null) {
            try {
                Document document =
                        configurationService.getConfigurationAsDocument(site, MODULE_STUDIO, filename, environment);
                if (document != null) {
                    config = new PermissionsConfigTO();
                    config.setMapping(document);
                    Element root = document.getRootElement();

                    // roles file
                    loadRoles(root, config);

                    // permissions file
                    loadPermissions(site, root, config);

                    config.setKey(site + ":" + filename);
                    config.setLastUpdated(DateUtils.getCurrentTime());

                    cache.put(cacheKey, config);
                }
            } catch (ServiceLayerException e) {
                logger.error("Permission mapping not found for " + site + ":" + filename);
            }
        }
        return config;
    }

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
        String cacheKey = configurationService.getCacheKey(null, null, globalPermissionsConfigPath, null, "object");
        PermissionsConfigTO config = cache.getIfPresent(cacheKey);
        if (config == null) {
            try {
                Document document = configurationService.getGlobalConfigurationAsDocument(globalPermissionsConfigPath);
                if (document != null) {
                    config = new PermissionsConfigTO();
                    config.setMapping(document);
                    Element root = document.getRootElement();

                    // permissions file
                    loadPermissions("###GLOBAL###", root, config);

                    String globalPermissionsKey = "###GLOBAL###:" + getGlobalPermissionsFileName();
                    config.setKey(globalPermissionsKey);
                    config.setLastUpdated(DateUtils.getCurrentTime());

                    cache.put(cacheKey, config);
                }
            } catch (ServiceLayerException e) {
                logger.error("Global permission mapping not found (path: {0})", globalPermissionsConfigPath);
            }
        }
        return config;
    }

    protected PermissionsConfigTO loadGlobalRolesConfiguration() {
        String globalRolesConfigPath = getGlobalConfigPath() + FILE_SEPARATOR + getGlobalRoleMappingsFileName();
        Document document = null;
        PermissionsConfigTO config = null;
        try {
            document = configurationService.getGlobalConfigurationAsDocument(globalRolesConfigPath);
        } catch (ServiceLayerException e) {
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
            config.setLastUpdated(DateUtils.getCurrentTime());

        } else {
            logger.error("Global roles mapping not found (path: {0})", globalRolesConfigPath);
        }
        return config;
    }

    @Override
    public int getAllUsersTotal() throws ServiceLayerException {
        return userServiceInternal.getAllUsersTotal(null);
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
                Map<String, List<String>> roleMappings = configurationService.getRoleMappings(site);

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
    public Authentication getAuthentication() {
        var context = SecurityContextHolder.getContext();
        if (context != null) {
            return context.getAuthentication();
        }
        return null;
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

    public void setCache(Cache<String, PermissionsConfigTO> cache) {
        this.cache = cache;
    }

}
