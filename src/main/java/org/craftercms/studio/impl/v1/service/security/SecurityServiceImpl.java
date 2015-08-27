/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2014 Crafter Software Corporation.
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

import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.constant.CStudioXmlConstants;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.ConfigurableServiceBase;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ContentTypeService;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v1.to.PermissionsConfigTO;
import org.craftercms.studio.api.v1.to.TimeStamped;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.Map;
import java.io.File;
import java.util.*;

/**
 * @author Dejan Brkic
 */
public class SecurityServiceImpl extends ConfigurableServiceBase implements SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityServiceImpl.class);

    @Override
    public String authenticate(String username, String password) {
        return securityProvider.authenticate(username, password);
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
    public Map<String,String> getUserProfile(String user) {
        return securityProvider.getUserProfile(user);
    }

    @Override
    public Set<String> getUserPermissions(String site, String path, String user, List<String> groups) {
        // determine whether to refresh the config
        //checkForUpdate(site);

        Set<String> permissions = new HashSet<String>();

        if (StringUtils.isNotEmpty(site)) {
            // get the config files from the permissionsConfigMap based on the key
            PermissionsConfigTO rolesConfig = permissionsConfigMap.get(getPermissionsKey(site, roleMappingsFileName));
            PermissionsConfigTO permissionsConfig = permissionsConfigMap.get(getPermissionsKey(site, permissionsFileName));
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

        PermissionsConfigTO globalRolesConfig = permissionsConfigMap.get("###GLOBAL###:"+globalRoleMappingsFileName);
        PermissionsConfigTO globalPermissionsConfig = permissionsConfigMap.get("###GLOBAL###:"+globalPermissionsFileName);
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
    public Set<String> getUserRoles(String site, String user) {

        Set<String> groups = securityProvider.getUserGroups(user);
        if (groups != null && groups.size() > 0) {
            logger.debug("Groups for " + user + " in " + site + ": " + groups);
            // determine whether to refresh the config
            //checkForUpdate(site);
            // get the config files from the permissionsConfigMap based on the key
            PermissionsConfigTO rolesConfig = permissionsConfigMap.get(getPermissionsKey(site, roleMappingsFileName));
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

    @Override
    protected void loadConfiguration(String key) {
        String siteConfigPath = configPath.replaceFirst(CStudioConstants.PATTERN_SITE, getSiteFromKey(key));
        String siteConfigFullPath = siteConfigPath + "/" + getFilenameFromKey(key);
        Document document = null;
        try {
            document = contentService.getContentAsDocument(siteConfigFullPath);
        } catch (DocumentException e) {
            logger.error("Permission mapping not found for " + key);
        }
        if (document != null) {
            PermissionsConfigTO config = new PermissionsConfigTO();
            config.setMapping(document);
            Element root = document.getRootElement();

            // roles file
            loadRoles(root, config);

            // permissions file
            loadPermissions(root, config);

            config.setKey(key);
            config.setLastUpdated(new Date());

            permissionsConfigMap.put(key, config);
        } else {
            logger.error("Permission mapping not found for " + key);
        }
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

    protected String getSiteFromKey(String key) {
        if (key.contains(":")) {
            return key.substring(0, key.indexOf(":"));
        } else {
            return key;
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

    protected String getFilenameFromKey(String key) {
        return key.substring(key.indexOf(":") + 1);
    }

    @Override
    protected TimeStamped getConfigurationById(String key) {
        return permissionsConfigMap.get(key);
    }

    @Override
    protected void removeConfiguration(String key) {
        if (!StringUtils.isEmpty(key)) {
            permissionsConfigMap.remove(key);
        }
    }

    /*
	 * Checks for updates to both the role & permission mappings files
	 * (non-Javadoc)
	 *
	 * @see
	 * org.craftercms.crafter.alfresco.service.impl.ConfigurableServiceBase#
	 * checkForUpdate(java.lang.String)
	 */
    @Override
    protected void checkForUpdate(String site) {
        if (StringUtils.isNotEmpty(site)) {
            super.checkForUpdate(getPermissionsKey(site, roleMappingsFileName));
            super.checkForUpdate(getPermissionsKey(site, permissionsFileName));
        }
        if (isGlobalPermissionsConfigUpdated()) {
            loadGlobalPermissionsConfiguration();
        }
        if (isGlobalRolesConfigUpdated()) {
            loadGlobalRolesConfiguration();
        }
    }

    protected boolean isGlobalRolesConfigUpdated() {
        String globalRolesKey = "###GLOBAL###:" + globalRoleMappingsFileName;
        TimeStamped config = getConfigurationById(globalRolesKey);
        if (config == null) {
            return true;
        } else {
            String siteConfigFullPath =  globalConfigPath + "/" + globalRoleMappingsFileName;
            if (contentRepository.contentExists(siteConfigFullPath)) {
                Date modifiedDate = contentRepository.getModifiedDate(siteConfigFullPath);
                if (modifiedDate == null) {
                    return false;
                } else {
                    return modifiedDate.after(config.getLastUpdated());
                }
            } else {
                removeConfiguration(globalRolesKey);
                return true;
            }
        }
    }

    protected boolean isGlobalPermissionsConfigUpdated() {
        String globalPermissionsKey = "###GLOBAL###:" + globalPermissionsFileName;
        TimeStamped config = getConfigurationById(globalPermissionsKey);
        if (config == null) {
            return true;
        } else {
            String siteConfigFullPath =  globalConfigPath + "/" + globalPermissionsFileName;
            if (contentRepository.contentExists(siteConfigFullPath)) {
                Date modifiedDate = contentRepository.getModifiedDate(siteConfigFullPath);
                if (modifiedDate == null) {
                    return false;
                } else {
                    return modifiedDate.after(config.getLastUpdated());
                }
            } else {
                removeConfiguration(globalPermissionsKey);
                return true;
            }
        }
    }

    protected void loadGlobalPermissionsConfiguration() {
        String globalPermissionsConfigPath = globalConfigPath + "/" + globalPermissionsFileName;
        Document document = null;
        try {
            document = contentService.getContentAsDocument(globalPermissionsConfigPath);
        } catch (DocumentException e) {
            logger.error("Global permission mapping not found (path: {0})", globalPermissionsConfigPath);
        }
        if (document != null) {
            PermissionsConfigTO config = new PermissionsConfigTO();
            config.setMapping(document);
            Element root = document.getRootElement();

            // permissions file
            loadPermissions(root, config);

            String globalPermissionsKey = "###GLOBAL###:" + globalPermissionsFileName;
            config.setKey(globalPermissionsKey);
            config.setLastUpdated(new Date());

            permissionsConfigMap.put(globalPermissionsKey, config);
        } else {
            logger.error("Global permission mapping not found (path: {0})", globalPermissionsConfigPath);
        }
    }

    protected void loadGlobalRolesConfiguration() {
        String globalRolesConfigPath = globalConfigPath + "/" + globalRoleMappingsFileName;
        Document document = null;
        try {
            document = contentService.getContentAsDocument(globalRolesConfigPath);
        } catch (DocumentException e) {
            logger.error("Global roles mapping not found (path: {0})", globalRolesConfigPath);
        }
        if (document != null) {
            PermissionsConfigTO config = new PermissionsConfigTO();
            config.setMapping(document);
            Element root = document.getRootElement();

            // roles file
            loadRoles(root, config);

            String globalRolesKey = "###GLOBAL###:" + globalRoleMappingsFileName;
            config.setKey(globalRolesKey);
            config.setLastUpdated(new Date());

            permissionsConfigMap.put(globalRolesKey, config);
        } else {
            logger.error("Global roles mapping not found (path: {0})", globalRolesConfigPath);
        }
    }

    @Override
    protected String getConfigFullPath(String key) {
        if (!StringUtils.isEmpty(key)) {
            // key is a combination of site,content-type
            String [] keys = key.split(":");
            if (keys.length == 2) {
                String site = keys[0];
                String fileName = keys[1];
                String siteConfigPath = configPath.replaceAll(CStudioConstants.PATTERN_SITE, site);
                String siteConfigFullPath = siteConfigPath + "/" + fileName;
                return siteConfigFullPath;
            } else {
                logger.error("Invalid content type config key provided: " + key + " site, content type is expected.");

            }
        } else {
            logger.error("Key cannot be empty. site, content type is expected.");

        }
        return null;
    }

    @Override
    public void reloadConfiguration(String site) {
        loadConfiguration(getPermissionsKey(site, permissionsFileName));
        loadConfiguration(getPermissionsKey(site, roleMappingsFileName));

    }

    @Override
    public void reloadGlobalConfiguration() {
        loadGlobalPermissionsConfiguration();
        loadGlobalRolesConfiguration();
    }

    @Override
    public boolean logout() {
        return securityProvider.logout();
    }

    @Override
    public void register() {
        getServicesManager().registerService(SecurityService.class, this);
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

    protected Map<String, PermissionsConfigTO> permissionsConfigMap = new HashMap<String, PermissionsConfigTO>();
    protected String roleMappingsFileName;
    protected String permissionsFileName;
    protected String globalConfigPath;
    protected String globalRoleMappingsFileName;
    protected String globalPermissionsFileName;
    protected SecurityProvider securityProvider;
    protected ContentTypeService contentTypeService;
    protected ContentService contentService;
}
