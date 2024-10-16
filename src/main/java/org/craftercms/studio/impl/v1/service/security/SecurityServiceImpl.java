/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.constant.StudioXmlConstants;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.job.CronJobContext;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ContentTypeService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v1.to.PermissionsConfigTO;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.security.NormalizedGroup;
import org.craftercms.studio.api.v2.dal.security.NormalizedRole;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.security.GroupService;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static org.craftercms.studio.api.v1.constant.SecurityConstants.*;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.*;
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

        if (context != null) {
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
    @Valid
    public Map<String,Object> getUserProfile(@ValidateStringParam String user)
            throws ServiceLayerException, UserNotFoundException {
        Map<String, Object> toRet = new HashMap<>();
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
            @ValidateStringParam String gitName)
            throws ServiceLayerException, UserNotFoundException {
        Map<String, Object> toRet = new HashMap<>();
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
    @Valid
    public Set<String> getUserPermissions(@ValidateStringParam final String site, @ValidateSecurePathParam String path,
                                          @ValidateStringParam String user) {
        Set<String> permissions = new HashSet<>();
        if (StringUtils.isNotEmpty(site)) {
            PermissionsConfigTO permissionsConfig = loadConfiguration(site, getPermissionsFileName());
            Set<NormalizedRole> roles = new HashSet<>();
            addUserRoles(roles, site, user);
            // resolve the permission
            permissions = populateUserPermissions(site, path, roles, permissionsConfig);
            logger.trace("Check if the user is allowed to edit the content in site '{}' path '{}' user '{}' ", site, path, user);

            // TODO: SJ: refactor the code below if it's still in use, otherwise remove
            if (path.indexOf("/site") == 0) { // If it's content a file
                try {
                    ContentTypeConfigTO config = contentTypeService.getContentTypeForContent(site, path);
                    boolean isAllowed = contentTypeService.isUserAllowed(roles, config);
                    if (!isAllowed) {
                        logger.trace("User '{}' is not permitted to access site '{}' path '{}', add " +
                                "permission '{}'", user, site, path, StudioConstants.PERMISSION_VALUE_NOT_ALLOWED);
                        // If no default role is set
                        permissions.add(StudioConstants.PERMISSION_VALUE_NOT_ALLOWED);
                        return permissions;
                    }
                } catch (ServiceLayerException e) {
                    // TODO: SJ: Is this really a debug?
                    logger.debug("Failed to get content type in site '{}' path '{}'. Skip user role check for " +
                                    "user '{}'", site, path, user, e);
                }
            }
        }

        PermissionsConfigTO globalRolesConfig = loadGlobalRolesConfiguration();
        PermissionsConfigTO globalPermissionsConfig = loadGlobalPermissionsConfiguration();
        Set<NormalizedRole> roles = new HashSet<>();
        addGlobalUserRoles(user, roles, globalRolesConfig);
        permissions.addAll(populateUserGlobalPermissions(path, roles, globalPermissionsConfig));
        return permissions;
    }

    protected void addGlobalUserRoles(String user, Set<NormalizedRole> roles, PermissionsConfigTO rolesConfig) {
        try {
            List<Group> groups = userServiceInternal.getUserGroups(-1, user);
            if (rolesConfig != null && groups != null) {
                Map<NormalizedGroup, List<NormalizedRole>> rolesMap = rolesConfig.getRoles();
                for (Group group : groups) {
                    List<NormalizedRole> userRoles = rolesMap.get(new NormalizedGroup(group.getGroupName()));
                    if (roles != null && userRoles != null) {
                        roles.addAll(userRoles);
                    }
                }
            }
        } catch (ServiceLayerException | UserNotFoundException e) {
            logger.error("Failed to get user groups for user '{}'",
                    user, e);
        }
    }

    protected void addGlobalGroupRoles(Set<NormalizedRole> roles, List<String> groups, PermissionsConfigTO rolesConfig) {
        if (groups != null) {
            Map<NormalizedGroup, List<NormalizedRole>> rolesMap = rolesConfig.getRoles();
            for (String group : groups) {
                NormalizedGroup normalizedGroup = new NormalizedGroup(group);
                List<NormalizedRole> groupRoles = rolesMap.get(normalizedGroup);
                if (groupRoles != null) {
                    logger.debug("Add roles to group'{}':'{}'", normalizedGroup, roles);
                    roles.addAll(groupRoles);
                }
            }
        }
    }

    protected Set<String> populateUserGlobalPermissions(String path, Set<NormalizedRole> roles,
                                                  PermissionsConfigTO permissionsConfig) {
        Set<String> permissions = new HashSet<>();
        if (roles != null && !roles.isEmpty()) {
            for (NormalizedRole role : roles) {
                // TODO: SJ: Avoid string literals
                Map<String, Map<NormalizedRole, List<Node>>> permissionsMap = permissionsConfig.getPermissions();
                Map<NormalizedRole, List<Node>> siteRoles = permissionsMap.get("###GLOBAL###");
                if (siteRoles == null || siteRoles.isEmpty()) {
                    siteRoles = permissionsMap.get("*");
                }
                if (siteRoles != null && !siteRoles.isEmpty()) {
                    List<Node> ruleNodes = siteRoles.get(role);
                    if (ruleNodes == null || ruleNodes.isEmpty()) {
                        ruleNodes = siteRoles.get(new NormalizedRole("*"));
                    }
                    if (ruleNodes != null && !ruleNodes.isEmpty()) {
                        for (Node ruleNode : ruleNodes) {
                            String regex = ruleNode.valueOf(StudioXmlConstants.DOCUMENT_ATTR_REGEX);
                            if (path.matches(regex)) {
                                logger.trace("Global permissions found by matching regex '{}' in role '{}'",
                                        regex, role);

                                List<Node> permissionNodes =
                                        ruleNode.selectNodes(StudioXmlConstants.DOCUMENT_ELM_ALLOWED_PERMISSIONS);
                                for (Node permissionNode : permissionNodes) {
                                    String permission = permissionNode.getText().toLowerCase();
                                    logger.debug("Add the global permissions '{}' to role '{}' path '{}'",
                                            permission, role, path);
                                    permissions.add(permission);
                                }
                            }
                        }
                    } else {
                        // If no default role is set
                        logger.debug("No default role is set. Add the default permission '{}'",
                                StudioConstants.PERMISSION_VALUE_READ);
                        permissions.add(StudioConstants.PERMISSION_VALUE_READ);
                    }
                } else {
                    // If no default site is set
                    logger.debug("No default site is set. Add the default permission '{}'",
                            StudioConstants.PERMISSION_VALUE_READ);
                    permissions.add(StudioConstants.PERMISSION_VALUE_READ);
                }
            }
        } else {
            // If user or group did not match the roles-mapping file
            logger.debug("No user or group match found. Add the default permission '{}'",
                    StudioConstants.PERMISSION_VALUE_READ);
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
    protected void addUserRoles(Set<NormalizedRole> roles, String site, String user) {
        if (!StringUtils.isEmpty(user)) {
            Set<NormalizedRole> userRoles = this.getUserRoles(site, user);
            logger.debug("Add the roles '{}' to user '{}' in site '{}'", userRoles, user, site);
            roles.addAll(userRoles);
        }
    }

    @Override
    @Valid
    public Set<String> getUserRoles(@ValidateStringParam final String site) {
        return getUserRoles(site, getCurrentUser())
                .stream()
                .map(NormalizedRole::toString)
                .collect(Collectors.toSet());
    }

    @Override
    @Valid
    public Set<NormalizedRole> getUserRoles(@ValidateStringParam final String site,
                                    @ValidateStringParam String user) {
        return getUserRoles(site, user, false);
    }


    @Override
    @Valid
    public Set<NormalizedRole> getUserRoles(@ValidateStringParam final String site,
                                    @ValidateStringParam String user, boolean includeGlobal) {
        try {
            // TODO: We should replace this with userService.getUserSiteRoles, but that one is protected by permissions.
            // TODO: When the UserService is refactored to use UserServiceInternal, we could use that method and
            // TODO: remove this one
            List<Group> groups = userServiceInternal.getUserGroups(-1, user);
            if (groups != null && !groups.isEmpty()) {
                logger.debug("Get groups for user '{}' in site '{}' groups '{}'", user, site, groups);

                PermissionsConfigTO rolesConfig = loadConfiguration(site, getRoleMappingsFileName());
                Set<NormalizedRole> userRoles = new HashSet<>();
                if (rolesConfig != null) {
                    Map<NormalizedGroup, List<NormalizedRole>> rolesMap = rolesConfig.getRoles();
                    for (Group group : groups) {
                        if (isSystemAdmin(user)) {
                            Collection<List<NormalizedRole>> mapValues = rolesMap.values();
                            mapValues.forEach(userRoles::addAll);
                            break;
                        } else {
                            List<NormalizedRole> roles = rolesMap.get(new NormalizedGroup(group.getGroupName()));
                            if (roles != null) {
                                userRoles.addAll(roles);
                            }
                        }
                    }
                }
                if (includeGlobal) {
                    PermissionsConfigTO globalRolesConfig = loadGlobalRolesConfiguration();
                    addGlobalUserRoles(user, userRoles, globalRolesConfig);
                    List<String> groupNames = groups.stream().map(Group::getGroupName).collect(Collectors.toList());
                    addGlobalGroupRoles(userRoles, groupNames, globalRolesConfig);
                }
                return userRoles;
            } else {
                logger.debug("No groups found for user '{}' in site '{}'", user, site);
            }
        } catch (ServiceLayerException | UserNotFoundException e) {
            logger.error("Failed to get groups for user '{}' in site '{}'", user, site, e);
        }

        return new HashSet<>(0);
    }

    /**
     * populate user permissions
     *
     * @param site
     * @param path
     * @param roles
     * @param permissionsConfig
     */
    protected Set<String> populateUserPermissions(String site, String path, Set<NormalizedRole> roles,
                                                  PermissionsConfigTO permissionsConfig) {
        Set<String> permissions = new HashSet<>();
        if (CollectionUtils.isEmpty(roles)) {
            // User has no access to the site
            return permissions;
        }
        Map<String, Map<NormalizedRole, List<Node>>> permissionsMap = permissionsConfig.getPermissions();
        Map<NormalizedRole, List<Node>> siteRoles = permissionsMap.getOrDefault(site, permissionsMap.get("*"));
        if (MapUtils.isEmpty(siteRoles)) {
            logger.debug("No default role is set site '{}' path '{}'. Add the default permission '{}'",
                    site, path, PERMISSION_CONTENT_READ);
            // This site has no role configured
            permissions.add(PERMISSION_CONTENT_READ);
            return permissions;
        }
        for (NormalizedRole role : roles) {
            List<Node> ruleNodes = siteRoles.getOrDefault(role, siteRoles.get(new NormalizedRole("*")));
            if (CollectionUtils.isEmpty(ruleNodes)) {
                logger.debug("No default role is set site '{}' path '{}'. Add the default permission '{}'",
                        site, path, PERMISSION_CONTENT_READ);
                // No rule for this role
                permissions.add(PERMISSION_CONTENT_READ);
            }
            for (Node ruleNode : ruleNodes) {
                String regex = ruleNode.valueOf(StudioXmlConstants.DOCUMENT_ATTR_REGEX);
                if (path.matches(regex)) {
                    logger.debug("Permissions found in site '{}' matching regex '{}' for role '{}'",
                            site, regex, role);

                    List<Node> permissionNodes = ruleNode.selectNodes(
                            StudioXmlConstants.DOCUMENT_ELM_ALLOWED_PERMISSIONS);
                    for (Node permissionNode : permissionNodes) {
                        String permission = permissionNode.getText().toLowerCase();
                        logger.trace("Add permission '{}' to site '{}' path '{}' role '{}'",
                                permission, site, path, role);
                        permissions.add(permission);
                    }
                }
            }
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
                logger.error("Failed to load the permission mappings from site '{}' path '{}'",
                        site, filename, e);
            }
        }
        return config;
    }

    protected void loadRoles(Element root, PermissionsConfigTO config) {
        if (root.getName().equals(StudioXmlConstants.DOCUMENT_ROLE_MAPPINGS)) {
            Map<NormalizedGroup, List<NormalizedRole>> rolesMap = new HashMap<>();

            List<Node> userNodes = root.selectNodes(StudioXmlConstants.DOCUMENT_ELM_USER_NODE);
            rolesMap = getRoles(userNodes, rolesMap);

            List<Node> groupNodes = root.selectNodes(StudioXmlConstants.DOCUMENT_ELM_GROUPS_NODE);
            rolesMap = getRoles(groupNodes, rolesMap);

            config.setRoles(rolesMap);
        }
    }

    protected Map<NormalizedGroup, List<NormalizedRole>> getRoles(List<Node> nodes, Map<NormalizedGroup, List<NormalizedRole>> rolesMap) {
        for (Node node : nodes) {
            String groupName = node.valueOf(StudioXmlConstants.DOCUMENT_ATTR_NAME);
            if (!StringUtils.isEmpty(groupName)) {
                List<Node> roleNodes = node.selectNodes(StudioXmlConstants.DOCUMENT_ELM_PERMISSION_ROLE);
                List<NormalizedRole> roles = new ArrayList<>();
                for (Node roleNode : roleNodes) {
                    roles.add(new NormalizedRole(roleNode.getText()));
                }
                rolesMap.put(new NormalizedGroup(groupName), roles);
            }
        }
        return rolesMap;
    }

    protected void loadPermissions(String siteId, Element root, PermissionsConfigTO config) {
        if (root.getName().equals(StudioXmlConstants.DOCUMENT_PERMISSIONS)) {
            Map<String, Map<NormalizedRole, List<Node>>> permissionsMap = new HashMap<>();

            //backwards compatibility for nested <site>
            Element permissionsRoot = root;
            Element siteNode = (Element) permissionsRoot.selectSingleNode(StudioXmlConstants.DOCUMENT_ELM_SITE);
            if (siteNode != null) {
                permissionsRoot = siteNode;
            }

            List<Node> roleNodes = permissionsRoot.selectNodes(StudioXmlConstants.DOCUMENT_ELM_PERMISSION_ROLE);
            Map<NormalizedRole, List<Node>> rules = new HashMap<>();
            for (Node roleNode : roleNodes) {
                String roleName = roleNode.valueOf(StudioXmlConstants.DOCUMENT_ATTR_NAME);
                List<Node> ruleNodes = roleNode.selectNodes(StudioXmlConstants.DOCUMENT_ELM_PERMISSION_RULE);
                rules.put(new NormalizedRole(roleName), ruleNodes);
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
                logger.error("Failed to load the global permission mapping path '{}'",
                        globalPermissionsConfigPath, e);
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
            logger.error("Failed to load the global roles mapping path '{}'",
                    globalRolesConfigPath, e);
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
            logger.error("The global roles mapping file was not found at path '{}'",
                    globalRolesConfigPath);
        }
        return config;
    }

    @Override
    public int getAllUsersTotal() throws ServiceLayerException {
        return userServiceInternal.getAllUsersTotal(null);
    }

    @Override
    @Valid
    public boolean isSystemAdmin(@ValidateStringParam String username) {
        List<NormalizedRole> roles;
        try {
            roles = getUserGlobalRoles(-1, username);
        } catch (UserNotFoundException e) {
            logger.info("User '{}' is not a site member", username, e);
            return false;
        } catch (ServiceLayerException e) {
            logger.warn("Failed to get site membership for user '{}'", username, e);
            return false;
        }

        boolean toRet = false;
        if (CollectionUtils.isNotEmpty(roles)) {
            for (NormalizedRole role : roles) {
                if (role.equals(SYSTEM_ADMIN_NORMALIZED_ROLE)) {
                    toRet = true;
                    break;
                }
            }
        }
        return toRet;
    }

    @Override
    @Valid
    public boolean isSiteAdmin(@ValidateStringParam String username, String site) {

        boolean toRet = false;
        try {
            if (isSystemAdmin(username)) {
                return true;
            }

            List<Group> groups = userServiceInternal.getUserGroups(-1, username);

            if (CollectionUtils.isNotEmpty(groups)) {
                Map<NormalizedGroup, List<NormalizedRole>> roleMappings = configurationService.getRoleMappings(site);

                if (MapUtils.isNotEmpty(roleMappings)) {
                    for (Group group : groups) {
                        List<NormalizedRole> roles = roleMappings.get(new NormalizedGroup(group.getGroupName()));
                        if (roles != null && roles.contains(ADMIN_NORMALIZED_ROLE)) {
                            toRet = true;
                        }
                    }
                }
            }
        } catch (ServiceLayerException | UserNotFoundException e) {
            logger.warn("Failed to get user membership for username '{}' site '{}'",
                    username, site, e);
        }
        return toRet;
    }

    @Override
    @Valid
    public boolean userExists(@ValidateStringParam String username) throws ServiceLayerException {
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

    @Override
    @Valid
    public List<NormalizedRole> getUserGlobalRoles(long userId, @ValidateStringParam String username)
            throws ServiceLayerException, UserNotFoundException {
        List<Group> groups = userServiceInternal.getUserGroups(userId, username);

        if (CollectionUtils.isEmpty(groups)) {
            return Collections.emptyList();
        }

        Map<NormalizedGroup, List<NormalizedRole>> roleMappings = configurationService.getGlobalRoleMappings();
        Set<NormalizedRole> userRoles = new LinkedHashSet<>();

        if (MapUtils.isNotEmpty(roleMappings)) {
            for (Group group : groups) {
                List<NormalizedRole> roles = roleMappings.get(new NormalizedGroup(group.getGroupName()));
                if (CollectionUtils.isNotEmpty(roles)) {
                    userRoles.addAll(roles);
                }
            }
        }

        return new ArrayList<>(userRoles);
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
        return Integer.parseInt(studioConfiguration.getProperty(SECURITY_SESSION_TIMEOUT));
    }

    public boolean isAuthenticatedSMTP() {
        return Boolean.parseBoolean(studioConfiguration.getProperty(MAIL_SMTP_AUTH));
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
