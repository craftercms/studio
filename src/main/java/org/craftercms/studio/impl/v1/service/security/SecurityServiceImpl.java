/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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

package org.craftercms.studio.impl.v1.service.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.commons.lang.Callback;
import org.craftercms.core.service.CacheService;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.constant.CStudioXmlConstants;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ContentTypeService;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v1.to.PermissionsConfigTO;
import org.craftercms.studio.impl.v1.service.StudioCacheContext;
import org.craftercms.studio.impl.v1.util.SessionTokenUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;

import javax.servlet.http.HttpSession;

/**
 * @author Dejan Brkic
 */
public class SecurityServiceImpl implements SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityServiceImpl.class);

    private final static String STUDIO_SESSION_TOKEN_ATRIBUTE = "studioSessionToken";

    @Override
    public String authenticate(String username, String password) {
        String toRet = securityProvider.authenticate(username, password);
        String sessionToken = SessionTokenUtils.createToken(username, sessionTimeout);
        RequestContext context = RequestContext.getCurrent();
        if (context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            httpSession.setAttribute(STUDIO_SESSION_TOKEN_ATRIBUTE, sessionToken);
        }
        return toRet;
    }

    @Override
    public boolean validateTicket(String token) {
        return securityProvider.validateTicket(token);
    }

    @Override
    public String getCurrentUser() {
        return securityProvider.getCurrentUser();
    }

    @Override
    public String getCurrentToken() {
        return securityProvider.getCurrentToken();
    }

    @Override
    public Map<String,String> getUserProfile(String user) {
        return securityProvider.getUserProfile(user);
    }

    @Override
    public Set<String> getUserPermissions(final String site, String path, String user, List<String> groups) {

        Set<String> permissions = new HashSet<String>();

        StudioCacheContext cacheContext = new StudioCacheContext(site, true);
        CacheService cacheService = cacheTemplate.getCacheService();
        generalLockService.lock(cacheContext.getId());
        try {
            if (!cacheService.hasScope(cacheContext)) {
                cacheService.addScope(cacheContext);
            }
        } finally {
            generalLockService.unlock(cacheContext.getId());
        }
        if (StringUtils.isNotEmpty(site)) {
            PermissionsConfigTO rolesConfig = cacheTemplate.getObject(cacheContext, new Callback<PermissionsConfigTO>() {
                @Override
                public PermissionsConfigTO execute() {
                    return loadConfiguration(site, roleMappingsFileName);
                }
            }, site, configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site), roleMappingsFileName);
            PermissionsConfigTO permissionsConfig = cacheTemplate.getObject(cacheContext, new Callback<PermissionsConfigTO>() {
                @Override
                public PermissionsConfigTO execute() {
                    return loadConfiguration(site, permissionsFileName);
                }
            }, site, configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site), permissionsFileName);
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
                        logger.debug("The user is not allowed to access " + site + ":" + path + ". adding permission: " + CStudioConstants.PERMISSION_VALUE_NOT_ALLOWED);
                        // If no default role is set
                        permissions.add(CStudioConstants.PERMISSION_VALUE_NOT_ALLOWED);
                        return permissions;
                    }
                } catch (ServiceException e) {
                    logger.debug("Error while getting the content type of " + path + ". skipping user role checking on the content.");
                }
            }
        }

        StudioCacheContext globalCacheContext = new StudioCacheContext("###GLOBAL###", true);
        PermissionsConfigTO globalRolesConfig = cacheTemplate.getObject(globalCacheContext, new Callback<PermissionsConfigTO>() {
            @Override
            public PermissionsConfigTO execute() {
                return loadGlobalRolesConfiguration();
            }
        }, "###GLOBAL###", globalConfigPath, globalRoleMappingsFileName);
        PermissionsConfigTO globalPermissionsConfig = cacheTemplate.getObject(globalCacheContext, new Callback<PermissionsConfigTO>() {
            @Override
            public PermissionsConfigTO execute() {
                return loadGlobalPermissionsConfiguration();
            }
        }, "###GLOBAL###", globalConfigPath, globalPermissionsFileName);
        Set<String> roles = new HashSet<String>();
        addGlobalUserRoles(user, roles, globalRolesConfig);
        addGlobalGroupRoles(roles, groups, globalRolesConfig);
        permissions.addAll(populateUserGlobalPermissions(path, roles, globalPermissionsConfig));
        return permissions;
    }

    protected void addGlobalUserRoles(String user, Set<String> roles, PermissionsConfigTO rolesConfig) {
        Set<String> groups = securityProvider.getUserGroups(user);
        if (rolesConfig != null && groups != null) {
            Map<String, List<String>> rolesMap = rolesConfig.getRoles();
            for (String group : groups) {
                String groupName = group.replaceFirst("GROUP_", "");
                List<String> userRoles = rolesMap.get(groupName);
                if (roles != null && userRoles != null) {
                    roles.addAll(userRoles);
                }
            }
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
                            String regex = ruleNode.valueOf(CStudioXmlConstants.DOCUMENT_ATTR_REGEX);
                            if (path.matches(regex)) {
                                logger.debug("Global permissions found by matching " + regex + " for " + role);

                                List<Node> permissionNodes = ruleNode.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_ALLOWED_PERMISSIONS);
                                for (Node permissionNode : permissionNodes) {
                                    String permission = permissionNode.getText().toLowerCase();
                                    logger.debug("adding global permissions " + permission + " to " + path + " for " + role);
                                    permissions.add(permission);
                                }
                            }
                        }
                    } else {
                        logger.debug("No default role is set. adding default permission: " + CStudioConstants.PERMISSION_VALUE_READ);
                        // If no default role is set
                        permissions.add(CStudioConstants.PERMISSION_VALUE_READ);
                    }
                } else {
                    logger.debug("No default site is set. adding default permission: " + CStudioConstants.PERMISSION_VALUE_READ);
                    // If no default site is set
                    permissions.add(CStudioConstants.PERMISSION_VALUE_READ);
                }
            }
        } else {
            logger.debug("No user or group matching found. adding default permission: " + CStudioConstants.PERMISSION_VALUE_READ);
            // If user or group did not match the roles-mapping file
            permissions.add(CStudioConstants.PERMISSION_VALUE_READ);
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

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.craftercms.crafter.alfresco.service.api.PermissionService#getUserRoles
	 * (java.lang.String, java.lang.String)
	 */
    @Override
    public Set<String> getUserRoles(final String site, String user) {

        Set<String> groups = securityProvider.getUserGroups(user);
        if (groups != null && groups.size() > 0) {
            logger.debug("Groups for " + user + " in " + site + ": " + groups);
            // determine whether to refresh the config
            //checkForUpdate(site);
            // get the config files from the permissionsConfigMap based on the key
            StudioCacheContext cacheContext = new StudioCacheContext(site, true);
            CacheService cacheService = cacheTemplate.getCacheService();
            generalLockService.lock(cacheContext.getId());
            try {
                if (!cacheService.hasScope(cacheContext)) {
                    cacheService.addScope(cacheContext);
                }
            } finally {
                generalLockService.unlock(cacheContext.getId());
            }
            PermissionsConfigTO rolesConfig = cacheTemplate.getObject(cacheContext, new Callback<PermissionsConfigTO>() {
                @Override
                public PermissionsConfigTO execute() {
                    return loadConfiguration(site, roleMappingsFileName);
                }
            }, site, configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site), roleMappingsFileName);
            Set<String> userRoles = new HashSet<String>();
            if (rolesConfig != null) {
                Map<String, List<String>> rolesMap = rolesConfig.getRoles();
                for (String group : groups) {
                    String groupName = group.replaceFirst("GROUP_", "");
                    List<String> roles = rolesMap.get(groupName);
                    if (roles != null) {
                        userRoles.addAll(roles);
                    }
                }
            }
            return userRoles;
        } else {
            logger.debug("No groups found for " + user + " in " + site);
        }
        return new HashSet<String>(0);
    }

    /**
     * get roles by groups
     *
     * @param site
     * @param groups
     * @param rolesConfig
     * @return
     */
    protected void addGroupRoles(Set<String> roles, String site, List<String> groups, PermissionsConfigTO rolesConfig) {
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
                            String regex = ruleNode.valueOf(CStudioXmlConstants.DOCUMENT_ATTR_REGEX);
                            if (path.matches(regex)) {
                                logger.debug("Permissions found by matching " + regex + " for " + role + " in " + site);

                                List<Node> permissionNodes = ruleNode.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_ALLOWED_PERMISSIONS);
                                for (Node permissionNode : permissionNodes) {
                                    String permission = permissionNode.getText().toLowerCase();
                                    logger.debug("adding permissions " + permission + " to " + path + " for " + role + " in " + site);
                                    permissions.add(permission);
                                }
                            }
                        }
                    } else {
                        logger.debug("No default role is set. adding default permission: " + CStudioConstants.PERMISSION_VALUE_READ);
                        // If no default role is set
                        permissions.add(CStudioConstants.PERMISSION_VALUE_READ);
                    }
                } else {
                    logger.debug("No default site is set. adding default permission: " + CStudioConstants.PERMISSION_VALUE_READ);
                    // If no default site is set
                    permissions.add(CStudioConstants.PERMISSION_VALUE_READ);
                }
            }
        } else {
            logger.debug("No user or group matching found. adding default permission: " + CStudioConstants.PERMISSION_VALUE_READ);
            // If user or group did not match the roles-mapping file
            permissions.add(CStudioConstants.PERMISSION_VALUE_READ);
        }
        return permissions;
    }

    protected PermissionsConfigTO loadConfiguration(String site, String filename) {
        String siteConfigPath = configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site);
        String siteConfigFullPath = siteConfigPath + "/" + filename;
        Document document = null;
        PermissionsConfigTO config = null;
        try {
            document = contentService.getContentAsDocument(site, siteConfigFullPath);
        } catch (DocumentException e) {
            logger.error("Permission mapping not found for " + site + ":" + filename);
        }
        if (document != null) {
            config = new PermissionsConfigTO();
            config.setMapping(document);
            Element root = document.getRootElement();

            // roles file
            loadRoles(root, config);

            // permissions file
            loadPermissions(root, config);

            config.setKey(site + ":" + filename);
            config.setLastUpdated(new Date());

        } else {
            logger.error("Permission mapping not found for " + site + ":" + filename);
        }
        return config;
    }

    @SuppressWarnings("unchecked")
    protected void loadRoles(Element root, PermissionsConfigTO config) {
        if (root.getName().equals(CStudioXmlConstants.DOCUMENT_ROLE_MAPPINGS)) {
            Map<String, List<String>> rolesMap = new HashMap<String, List<String>>();

            List<Node> userNodes = root.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_USER_NODE);
            rolesMap = getRoles(userNodes, rolesMap);

            List<Node> groupNodes = root.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_GROUPS_NODE);
            rolesMap = getRoles(groupNodes, rolesMap);

            config.setRoles(rolesMap);
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, List<String>> getRoles(List<Node> nodes, Map<String, List<String>> rolesMap) {
        for (Node node : nodes) {
            String name = node.valueOf(CStudioXmlConstants.DOCUMENT_ATTR_PERMISSIONS_NAME);
            if (!StringUtils.isEmpty(name)) {
                List<Node> roleNodes = node.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_PERMISSION_ROLE);
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
    protected void loadPermissions(Element root, PermissionsConfigTO config) {
        if (root.getName().equals(CStudioXmlConstants.DOCUMENT_PERMISSIONS)) {
            Map<String, Map<String, List<Node>>> permissionsMap = new HashMap<String, Map<String, List<Node>>>();
            List<Node> siteNodes = root.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_SITE);
            for (Node siteNode : siteNodes) {
                String siteId = siteNode.valueOf(CStudioXmlConstants.DOCUMENT_ATTR_SITE_ID);
                if (!StringUtils.isEmpty(siteId)) {
                    List<Node> roleNodes = siteNode.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_PERMISSION_ROLE);
                    Map<String, List<Node>> rules = new HashMap<String, List<Node>>();
                    for (Node roleNode : roleNodes) {
                        String roleName = roleNode.valueOf(CStudioXmlConstants.DOCUMENT_ATTR_PERMISSIONS_NAME);
                        List<Node> ruleNodes = roleNode.selectNodes(CStudioXmlConstants.DOCUMENT_ELM_PERMISSION_RULE);
                        rules.put(roleName, ruleNodes);
                    }
                    permissionsMap.put(siteId, rules);
                }
            }
            config.setPermissions(permissionsMap);
        }
    }

    @Override
    public void addUserGroup(String groupName) {
        securityProvider.addUserGroup(groupName);
    }

    @Override
    public void addUserGroup(String parentGroup, String groupName) {
        securityProvider.addUserGroup(parentGroup, groupName);
    }

    @Override
    public void addUserToGroup(String groupName, String user) {
        securityProvider.addUserToGroup(groupName, user);
    }


    protected PermissionsConfigTO loadGlobalPermissionsConfiguration() {
        String globalPermissionsConfigPath = globalConfigPath + "/" + globalPermissionsFileName;
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
            loadPermissions(root, config);

            String globalPermissionsKey = "###GLOBAL###:" + globalPermissionsFileName;
            config.setKey(globalPermissionsKey);
            config.setLastUpdated(new Date());

        } else {
            logger.error("Global permission mapping not found (path: {0})", globalPermissionsConfigPath);
        }
        return config;
    }

    protected PermissionsConfigTO loadGlobalRolesConfiguration() {
        String globalRolesConfigPath = globalConfigPath + "/" + globalRoleMappingsFileName;
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

            String globalRolesKey = "###GLOBAL###:" + globalRoleMappingsFileName;
            config.setKey(globalRolesKey);
            config.setLastUpdated(new Date());

        } else {
            logger.error("Global roles mapping not found (path: {0})", globalRolesConfigPath);
        }
        return config;
    }


    @Override
    public void reloadConfiguration(String site) {
        CacheService cacheService = cacheTemplate.getCacheService();
        StudioCacheContext cacheContext = new StudioCacheContext(site, true);
        Object permissionsKey = cacheTemplate.getKey(site, configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site), permissionsFileName);
        Object rolesKey = cacheTemplate.getKey(site, configPath.replaceFirst(CStudioConstants.PATTERN_SITE, site), roleMappingsFileName);
        generalLockService.lock(cacheContext.getId());
        try {
            if (cacheService.hasScope(cacheContext)) {
                cacheService.remove(cacheContext, permissionsKey);
                cacheService.remove(cacheContext, rolesKey);
            } else {
                cacheService.addScope(cacheContext);
            }
        } finally {
            generalLockService.unlock(cacheContext.getId());
        }
        PermissionsConfigTO permissionsConfigTO = loadConfiguration(site, permissionsFileName);
        PermissionsConfigTO rolesConfigTO = loadConfiguration(site, roleMappingsFileName);
        cacheService.put(cacheContext, permissionsKey, permissionsConfigTO);
        cacheService.put(cacheContext, rolesKey, rolesConfigTO);
    }

    @Override
    public void reloadGlobalConfiguration() {
        CacheService cacheService = cacheTemplate.getCacheService();
        StudioCacheContext cacheContext = new StudioCacheContext("###GLOBAL###", true);
        Object permissionsKey = cacheTemplate.getKey("###GLOBAL###", globalConfigPath, globalPermissionsFileName);
        Object rolesKey = cacheTemplate.getKey("###GLOBAL###", globalConfigPath, globalRoleMappingsFileName);
        generalLockService.lock(cacheContext.getId());
        try {
            if (cacheService.hasScope(cacheContext)) {
                cacheService.remove(cacheContext, permissionsKey);
                cacheService.remove(cacheContext, rolesKey);
            } else {
                cacheService.addScope(cacheContext);
            }
        } finally {
            generalLockService.unlock(cacheContext.getId());
        }
        PermissionsConfigTO permissionsConfigTO = loadGlobalPermissionsConfiguration();
        PermissionsConfigTO rolesConfigTO = loadGlobalRolesConfiguration();
        cacheService.put(cacheContext, permissionsKey, permissionsConfigTO);
        cacheService.put(cacheContext, rolesKey, rolesConfigTO);
    }

    @Override
    public boolean logout() {
        boolean toRet = securityProvider.logout();
        RequestContext context = RequestContext.getCurrent();
        if (context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            httpSession.removeAttribute(STUDIO_SESSION_TOKEN_ATRIBUTE);
        }
        return toRet;
    }

    public String getRoleMappingsFileName() { return roleMappingsFileName; }
    public void setRoleMappingsFileName(String roleMappingsFileName) { this.roleMappingsFileName = roleMappingsFileName; }

    public String getPermissionsFileName() { return permissionsFileName; }
    public void setPermissionsFileName(String permissionsFileName) { this.permissionsFileName = permissionsFileName; }

    public String getGlobalConfigPath() { return globalConfigPath; }
    public void setGlobalConfigPath(String globalConfigPath) { this.globalConfigPath = globalConfigPath; }

    public String getGlobalRoleMappingsFileName() { return globalRoleMappingsFileName; }
    public void setGlobalRoleMappingsFileName(String globalRoleMappingsFileName) { this.globalRoleMappingsFileName = globalRoleMappingsFileName; }

    public String getGlobalPermissionsFileName() {  return globalPermissionsFileName; }
    public void setGlobalPermissionsFileName(String globalPermissionsFileName) { this.globalPermissionsFileName = globalPermissionsFileName; }

    public SecurityProvider getSecurityProvider() { return securityProvider; }
    public void setSecurityProvider(SecurityProvider securityProvider) { this.securityProvider = securityProvider; }

    public ContentTypeService getContentTypeService() { return contentTypeService; }
    public void setContentTypeService(ContentTypeService contentTypeService) { this.contentTypeService = contentTypeService; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public String getConfigPath() { return configPath; }
    public void setConfigPath(String configPath) { this.configPath = configPath; }

    public CacheTemplate getCacheTemplate() { return cacheTemplate; }
    public void setCacheTemplate(CacheTemplate cacheTemplate) { this.cacheTemplate = cacheTemplate; }

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }

    public int getSessionTimeout() { return sessionTimeout; }
    public void setSessionTimeout(int sessionTimeout) { this.sessionTimeout = sessionTimeout; }

    protected String roleMappingsFileName;
    protected String permissionsFileName;
    protected String globalConfigPath;
    protected String globalRoleMappingsFileName;
    protected String globalPermissionsFileName;
    protected SecurityProvider securityProvider;
    protected ContentTypeService contentTypeService;
    protected ContentService contentService;
    protected String configPath;
    protected CacheTemplate cacheTemplate;
    protected GeneralLockService generalLockService;
    protected int sessionTimeout;
}
