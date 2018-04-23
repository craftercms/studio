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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.commons.validation.annotations.param.*;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.constant.StudioXmlConstants;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.*;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ContentTypeService;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.security.UserDetailsManager;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v1.to.PermissionsConfigTO;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.SessionTokenUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_EMAIL;
import static org.craftercms.studio.api.v1.constant.SecurityConstants.KEY_EXTERNALLY_MANAGED;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SECURITY_AUTHENTICATION_TYPE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SECURITY_AUTHENTICATION_TYPE_DB;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SECURITY_AUTHENTICATION_TYPE_HEADERS;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SECURITY_AUTHENTICATION_TYPE_LDAP;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.*;

/**
 * @author Dejan Brkic
 */
public class SecurityServiceImpl implements SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityServiceImpl.class);

    protected SecurityProvider securityProvider;
    protected ContentTypeService contentTypeService;
    protected ActivityService activityService;
    protected ContentService contentService;
    protected GeneralLockService generalLockService;
    protected StudioConfiguration studioConfiguration;
    protected JavaMailSender emailService;
    protected JavaMailSender emailServiceNoAuth;
    protected UserDetailsManager userDetailsManager;
    protected ObjectFactory<FreeMarkerConfig> freeMarkerConfig;

    @Override
    @ValidateParams
    public String authenticate(@ValidateStringParam(name = "username") String username, @ValidateStringParam(name = "password") String password) throws BadCredentialsException, AuthenticationSystemException {
        String toRet = securityProvider.authenticate(username, password);
        if (StringUtils.isNotEmpty(toRet)) {
            RequestContext requestContext = RequestContext.getCurrent();
            HttpServletRequest httpServletRequest = requestContext.getRequest();
            String ipAddress = httpServletRequest.getRemoteAddr();

            ActivityService.ActivityType activityType = ActivityService.ActivityType.LOGIN;
            Map<String, String> extraInfo = new HashMap<String, String>();
            extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
            activityService.postActivity(getSystemSite(), username, ipAddress, activityType, ActivityService.ActivitySource.UI, extraInfo);

            logger.info("User " + username + " logged in from IP: " + ipAddress);
        }
        return toRet;
    }

    @Override
    @ValidateParams
    public boolean validateTicket(@ValidateStringParam(name = "token") String token) {
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
    @ValidateParams
    public Map<String,Object> getUserProfile(@ValidateStringParam(name = "user") String user) {
        Map<String, Object> toRet = securityProvider.getUserProfile(user);
        String authenticationType = studioConfiguration.getProperty(SECURITY_TYPE);
        toRet.put(SECURITY_AUTHENTICATION_TYPE, authenticationType);
        return toRet;
    }

    @Override
    @ValidateParams
    public Set<String> getUserPermissions(@ValidateStringParam(name = "site") final String site, @ValidateSecurePathParam(name = "path") String path, @ValidateStringParam(name = "user") String user, List<String> groups) {
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
                        logger.debug("The user is not allowed to access " + site + ":" + path + ". adding permission: " + StudioConstants.PERMISSION_VALUE_NOT_ALLOWED);
                        // If no default role is set
                        permissions.add(StudioConstants.PERMISSION_VALUE_NOT_ALLOWED);
                        return permissions;
                    }
                } catch (ServiceException e) {
                    logger.debug("Error while getting the content type of " + path + ". skipping user role checking on the content.");
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

                                List<Node> permissionNodes = ruleNode.selectNodes(StudioXmlConstants.DOCUMENT_ELM_ALLOWED_PERMISSIONS);
                                for (Node permissionNode : permissionNodes) {
                                    String permission = permissionNode.getText().toLowerCase();
                                    logger.debug("adding global permissions " + permission + " to " + path + " for " + role);
                                    permissions.add(permission);
                                }
                            }
                        }
                    } else {
                        logger.debug("No default role is set. adding default permission: " + StudioConstants.PERMISSION_VALUE_READ);
                        // If no default role is set
                        permissions.add(StudioConstants.PERMISSION_VALUE_READ);
                    }
                } else {
                    logger.debug("No default site is set. adding default permission: " + StudioConstants.PERMISSION_VALUE_READ);
                    // If no default site is set
                    permissions.add(StudioConstants.PERMISSION_VALUE_READ);
                }
            }
        } else {
            logger.debug("No user or group matching found. adding default permission: " + StudioConstants.PERMISSION_VALUE_READ);
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
    public Set<String> getUserRoles(@ValidateStringParam(name = "site") final String site, @ValidateStringParam(name = "user") String user) {

        Set<String> groups = securityProvider.getUserGroups(user);
        if (groups != null && groups.size() > 0) {
            logger.debug("Groups for " + user + " in " + site + ": " + groups);

            PermissionsConfigTO rolesConfig = loadConfiguration(site, getRoleMappingsFileName());
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
                                logger.debug("Permissions found by matching " + regex + " for " + role + " in " + site);

                                List<Node> permissionNodes = ruleNode.selectNodes(StudioXmlConstants.DOCUMENT_ELM_ALLOWED_PERMISSIONS);
                                for (Node permissionNode : permissionNodes) {
                                    String permission = permissionNode.getText().toLowerCase();
                                    logger.debug("adding permissions " + permission + " to " + path + " for " + role + " in " + site);
                                    permissions.add(permission);
                                }
                            }
                        }
                    } else {
                        logger.debug("No default role is set. adding default permission: " + StudioConstants.PERMISSION_VALUE_READ);
                        // If no default role is set
                        permissions.add(StudioConstants.PERMISSION_VALUE_READ);
                    }
                } else {
                    logger.debug("No default site is set. adding default permission: " + StudioConstants.PERMISSION_VALUE_READ);
                    // If no default site is set
                    permissions.add(StudioConstants.PERMISSION_VALUE_READ);
                }
            }
        } else {
            logger.debug("No user or group matching found. adding default permission: " + StudioConstants.PERMISSION_VALUE_READ);
            // If user or group did not match the roles-mapping file
            permissions.add(StudioConstants.PERMISSION_VALUE_READ);
        }
        return permissions;
    }

    protected PermissionsConfigTO loadConfiguration(String site, String filename) {
        String siteConfigPath = getConfigPath().replaceFirst(StudioConstants.PATTERN_SITE, site);
        String siteConfigFullPath = siteConfigPath + FILE_SEPARATOR + filename;
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

    @Override
    @ValidateParams
    public void addUserGroup(@ValidateStringParam(name = "groupName") String groupName) {
        securityProvider.addUserGroup(groupName);
    }

    @Override
    @ValidateParams
    public void addUserGroup(@ValidateStringParam(name = "parentGroup") String parentGroup, @ValidateStringParam(name = "groupName") String groupName) {
        securityProvider.addUserGroup(parentGroup, groupName);
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
    public boolean logout() {
        String username = getCurrentUser();
        boolean toRet = securityProvider.logout();
        RequestContext context = RequestContext.getCurrent();
        if (context != null) {
            HttpServletRequest httpServletRequest = context.getRequest();
            String ipAddress = httpServletRequest.getRemoteAddr();

            HttpSession httpSession = httpServletRequest.getSession();
            httpSession.removeAttribute(STUDIO_SESSION_TOKEN_ATRIBUTE);
            httpSession.invalidate();

            ActivityService.ActivityType activityType = ActivityService.ActivityType.LOGOUT;
            Map<String, String> extraInfo = new HashMap<String, String>();
            extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
            activityService.postActivity(getSystemSite(), username, ipAddress, activityType, ActivityService.ActivitySource.UI, extraInfo);

            logger.info("User " + username + " logged out from IP: " + ipAddress);
        }
        return toRet;
    }

    @Override
    @ValidateParams
    public boolean createUser(@ValidateNoTagsParam(name = "username") String username, @ValidateStringParam(name = "password") String password, @ValidateNoTagsParam(name = "firstName") String firstName, @ValidateNoTagsParam(name = "lastName") String lastName, @ValidateNoTagsParam(name = "email") String email) throws UserAlreadyExistsException {
        boolean toRet = securityProvider.createUser(username, password, firstName, lastName, email, false);
        if (toRet) {
            ActivityService.ActivityType activityType = ActivityService.ActivityType.CREATED;
            String user = getCurrentUser();
            Map<String, String> extraInfo = new HashMap<String, String>();
            extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
            activityService.postActivity(getSystemSite(), user, username, activityType, ActivityService.ActivitySource.UI, extraInfo);
        }
        return toRet;
    }

    @Override
    @ValidateParams
    public boolean deleteUser(@ValidateStringParam(name = "username") String username) throws UserNotFoundException, DeleteUserNotAllowedException {
        if (!isDeleteUserAllowed(username)) {
            throw new DeleteUserNotAllowedException();
        } else {
            boolean toRet = securityProvider.deleteUser(username);
            if (toRet) {
                ActivityService.ActivityType activityType = ActivityService.ActivityType.DELETED;
                String user = getCurrentUser();
                Map<String, String> extraInfo = new HashMap<String, String>();
                extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
                activityService.postActivity(getSystemSite(), user, username, activityType, ActivityService.ActivitySource.UI, extraInfo);
            }
            return toRet;
        }
    }

    private boolean isDeleteUserAllowed(String username) throws UserNotFoundException {
        boolean toRet = true;

        // check if it is current user - not allowed to delete current user
        if (toRet) {
            toRet = !StringUtils.equals(username, getCurrentUser());
        }

        // check if it is system user - not allowed to delete system user
        if (toRet) {
            toRet = !securityProvider.isSystemUser(username);
        }

        // check if it is admin - not allowed to delete admin
        if (toRet) {
            toRet = !isAdmin(username);
        }

        return toRet;
    }

    @Override
    @ValidateParams
    public boolean updateUser(@ValidateStringParam(name = "username") String username, @ValidateNoTagsParam(name = "firstName") String firstName, @ValidateNoTagsParam(name = "lastName") String lastName, @ValidateNoTagsParam(name = "email") String email) throws UserNotFoundException, UserExternallyManagedException {
        boolean toRet = securityProvider.updateUser(username, firstName, lastName, email);
        if (toRet) {
            ActivityService.ActivityType activityType = ActivityService.ActivityType.UPDATED;
            String user = getCurrentUser();
            Map<String, String> extraInfo = new HashMap<String, String>();
            extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
            activityService.postActivity(getSystemSite(), user, username, activityType, ActivityService.ActivitySource.UI, extraInfo);
        }
        return toRet;
    }

    @Override
    @ValidateParams
    public boolean enableUser(@ValidateStringParam(name = "username") String username, boolean enabled) throws UserNotFoundException, UserExternallyManagedException {
        return securityProvider.enableUser(username, enabled);
    }

    @Override
    @ValidateParams
    public Map<String, Object> getUserStatus(@ValidateStringParam(name = "username") String username) throws UserNotFoundException {
        return securityProvider.getUserStatus(username);
    }

    @Override
    @ValidateParams
    public List<Map<String, Object>> getAllUsers(@ValidateIntegerParam(name = "start") int start, @ValidateIntegerParam(name = "number") int number) {
        return securityProvider.getAllUsers(start, number);
    }

    @Override
    public int getAllUsersTotal() {
        return securityProvider.getAllUsersTotal();
    }

    @Override
    @ValidateParams
    public List<Map<String, Object>> getUsersPerSite(@ValidateStringParam(name = "site") String site, @ValidateIntegerParam(name = "start") int start, @ValidateIntegerParam(name = "number") int number) throws SiteNotFoundException {
        return securityProvider.getUsersPerSite(site, start, number);
    }

    @Override
    @ValidateParams
    public int getUsersPerSiteTotal(@ValidateStringParam(name = "site") String site) throws SiteNotFoundException {
        return securityProvider.getUsersPerSiteTotal(site);
    }

    @Override
    @ValidateParams
    public boolean createGroup(@ValidateNoTagsParam(name = "groupName") String groupName, @ValidateNoTagsParam(name = "description") String description, @ValidateStringParam(name = "siteId") String siteId) throws GroupAlreadyExistsException, SiteNotFoundException {
        return securityProvider.createGroup(groupName, description, siteId, false);
    }

    @Override
    @ValidateParams
    public Map<String, Object> getGroup(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "group") String group) throws GroupNotFoundException, SiteNotFoundException {
        return securityProvider.getGroup(site, group);
    }

    @Override
    @ValidateParams
    public List<Map<String, Object>> getAllGroups(@ValidateIntegerParam(name = "start") int start, @ValidateIntegerParam(name = "number") int number) {
        return securityProvider.getAllGroups(start, number);
    }

    @Override
    @ValidateParams
    public List<Map<String, Object>> getGroupsPerSite(@ValidateStringParam(name = "site") String site, @ValidateIntegerParam(name = "start") int start, @ValidateIntegerParam(name = "number") int number) throws SiteNotFoundException {
        return securityProvider.getGroupsPerSite(site, start, number);
    }

    @Override
    @ValidateParams
    public int getGroupsPerSiteTotal(@ValidateStringParam(name = "site") String site) throws SiteNotFoundException {
        return securityProvider.getGroupsPerSiteTotal(site);
    }

    @Override
    @ValidateParams
    public List<Map<String, Object>> getUsersPerGroup(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "group") String group, @ValidateIntegerParam(name = "start") int start, @ValidateIntegerParam(name = "number") int number) throws
            GroupNotFoundException, SiteNotFoundException {
        return securityProvider.getUsersPerGroup(site, group, start, number);
    }

    @Override
    @ValidateParams
    public int getUsersPerGroupTotal(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "group") String group) throws
            GroupNotFoundException, SiteNotFoundException {
        return securityProvider.getUsersPerGroupTotal(site, group);
    }

    @Override
    @ValidateParams
    public boolean updateGroup(@ValidateStringParam(name = "siteId") String siteId, @ValidateNoTagsParam(name = "groupName") String groupName, @ValidateNoTagsParam(name = "description") String description) throws GroupNotFoundException, SiteNotFoundException {
        return securityProvider.updateGroup(siteId, groupName, description);
    }

    @Override
    @ValidateParams
    public boolean deleteGroup(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "group") String group) throws GroupNotFoundException, SiteNotFoundException {
        return securityProvider.deleteGroup(site, group);
    }

    @Override
    @ValidateParams
    public boolean addUserToGroup(@ValidateStringParam(name = "siteId") String siteId, @ValidateStringParam(name = "groupName") String groupName, @ValidateStringParam(name = "username") String username) throws
            UserAlreadyExistsException, UserNotFoundException, GroupNotFoundException, SiteNotFoundException {
        boolean toRet = securityProvider.addUserToGroup(siteId, groupName, username);
        if (toRet) {
            ActivityService.ActivityType activityType = ActivityService.ActivityType.ADD_USER_TO_GROUP;
            String user = getCurrentUser();
            Map<String, String> extraInfo = new HashMap<String, String>();
            extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
            activityService.postActivity(siteId, user, username + " > " + groupName , activityType, ActivityService.ActivitySource.UI, extraInfo);
        }
        return toRet;
    }

    @Override
    @ValidateParams
    public boolean removeUserFromGroup(@ValidateStringParam(name = "siteId") String siteId, @ValidateStringParam(name = "groupName") String groupName, @ValidateStringParam(name = "username") String username) throws
            UserNotFoundException, GroupNotFoundException, SiteNotFoundException {
        boolean toRet = securityProvider.removeUserFromGroup(siteId, groupName, username);
        if (toRet) {
            ActivityService.ActivityType activityType = ActivityService.ActivityType.REMOVE_USER_FROM_GROUP;
            String user = getCurrentUser();
            Map<String, String> extraInfo = new HashMap<String, String>();
            extraInfo.put(DmConstants.KEY_CONTENT_TYPE, StudioConstants.CONTENT_TYPE_USER);
            activityService.postActivity(siteId, user, username + " X " + groupName , activityType, ActivityService.ActivitySource.UI, extraInfo);
        }
        return toRet;
    }

    @Override
    @ValidateParams
    public Map<String, Object> forgotPassword(@ValidateStringParam(name = "username") String username) throws ServiceException, UserNotFoundException, UserExternallyManagedException {
        logger.debug("Getting user profile for " + username);
        Map<String, Object> userProfile = securityProvider.getUserProfile(username);
        boolean success = false;
        String message = StringUtils.EMPTY;
        if (userProfile == null || userProfile.isEmpty()) {
            logger.info("User profile not found for " + username);
            throw new UserNotFoundException();
        } else {
            if (Boolean.parseBoolean(userProfile.get(KEY_EXTERNALLY_MANAGED).toString())) {
                throw new UserExternallyManagedException();
            } else {
                if (userProfile.get(KEY_EMAIL) != null) {
                    String email = userProfile.get(KEY_EMAIL).toString();

                    logger.debug("Creating security token for forgot password");
                    long timestamp = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(Long.parseLong(studioConfiguration
                            .getProperty(SECURITY_FORGOT_PASSWORD_TOKEN_TIMEOUT)));
                    String salt = studioConfiguration.getProperty(SECURITY_CIPHER_SALT);

                    String token = username + "|" + timestamp + "|" + salt;
                    String hashedToken = encryptToken(token);
                    logger.debug("Sending forgot password email to " + email);
                    try {
                        sendForgotPasswordEmail(email, hashedToken);
                    } catch (MessagingException | IOException | TemplateException e) {
                        throw new ServiceException("Error while sending forgot password email", e);
                    }
                    success = true;
                    message = "OK";
                } else {
                    logger.info("User " + username + " does not have assigned email with account");
                    throw new ServiceException("User " + username + " does not have assigned email with account");
                }
            }
        }
        Map<String, Object> toRet = new HashMap<String, Object>();
        toRet.put("success", success);
        toRet.put("message", message);
        return toRet;
    }

    @Override
    @ValidateParams
    public boolean validateToken(@ValidateStringParam(name = "token") String token) throws UserNotFoundException, UserExternallyManagedException {
        boolean toRet = false;
        String decryptedToken = decryptToken(token);
        if (StringUtils.isNotEmpty(decryptedToken)) {
            StringTokenizer tokenElements = new StringTokenizer(decryptedToken, "|");
            if (tokenElements.countTokens() == 3) {
                String username = tokenElements.nextToken();
                Map<String, Object> userProfile = securityProvider.getUserProfile(username);
                if (userProfile == null || userProfile.isEmpty()) {
                    logger.info("User profile not found for " + username);
                    throw new UserNotFoundException();
                } else {
                    if (Boolean.parseBoolean(userProfile.get(KEY_EXTERNALLY_MANAGED).toString())) {
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

    private String encryptToken(String token) {
        try {
            SecretKeySpec key = new SecretKeySpec(studioConfiguration.getProperty(SECURITY_CIPHER_KEY).getBytes(), studioConfiguration.getProperty(SECURITY_CIPHER_TYPE));
            Cipher cipher = Cipher.getInstance(studioConfiguration.getProperty(SECURITY_CIPHER_ALGORITHM));
            byte[] tokenBytes = token.getBytes(StandardCharsets.UTF_8);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(key.getEncoded()));
            byte[] encrypted = cipher.doFinal(tokenBytes);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            logger.error("Error while encrypting forgot password token", e);
            return null;
        }
    }

    private String decryptToken(String token) {
        try {
            SecretKeySpec key = new SecretKeySpec(studioConfiguration.getProperty(SECURITY_CIPHER_KEY).getBytes(), studioConfiguration.getProperty(SECURITY_CIPHER_TYPE));
            Cipher cipher = Cipher.getInstance(studioConfiguration.getProperty(SECURITY_CIPHER_ALGORITHM));
            byte[] tokenBytes = Base64.getDecoder().decode(token.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(key.getEncoded()));
            byte[] decrypted = cipher.doFinal(tokenBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            logger.error("Error while decrypting forgot password token", e);
            return null;
        }
    }

    private void sendForgotPasswordEmail(String emailAddress, String token) throws MessagingException, IOException, TemplateException {
        try {
            Template emailTemplate = freeMarkerConfig.getObject().getConfiguration().getTemplate(studioConfiguration.getProperty(SECURITY_FORGOT_PASSWORD_EMAIL_TEMPLATE));

            Writer out = new StringWriter();
            Map<String, Object> model = new HashMap<String, Object>();
            RequestContext context = RequestContext.getCurrent();
            HttpServletRequest request = context.getRequest();
            String authoringUrl = request.getRequestURL().toString().replace(request.getPathInfo(), "");
            String serviceUrl = studioConfiguration.getProperty(SECURITY_RESET_PASSWORD_SERVICE_URL);
            model.put("authoringUrl", authoringUrl);
            model.put("serviceUrl", serviceUrl);
            model.put("token", token);
            if (emailTemplate != null) {
                emailTemplate.process(model, out);
            }

            MimeMessage mimeMessage = emailService.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);

            messageHelper.setFrom(getDefaultFromAddress());
            messageHelper.setTo(emailAddress);
            messageHelper.setSubject(studioConfiguration.getProperty(SECURITY_FORGOT_PASSWORD_MESSAGE_SUBJECT));
            messageHelper.setText(out.toString(), true);
            logger.info("Sending password recovery message to " + emailAddress);
            if (isAuthenticatedSMTP()) {
                emailService.send(mimeMessage);
            } else {
                emailServiceNoAuth.send(mimeMessage);
            }
            logger.info("Password recovery message successfully sent to " + emailAddress);
        } catch (MessagingException | IOException | TemplateException e) {
            logger.error("Failed to send password recovery message to " + emailAddress, e);
            throw e;
        }
    }

    @Override
    @ValidateParams
    public boolean changePassword(@ValidateStringParam(name = "username") String username, @ValidateStringParam(name = "current") String current, @ValidateStringParam(name = "newPassword") String newPassword) throws PasswordDoesNotMatchException, UserExternallyManagedException {
        return securityProvider.changePassword(username, current, newPassword);
    }

    @Override
    @ValidateParams
    public Map<String, Object> setUserPassword(@ValidateStringParam(name = "token") String token, @ValidateStringParam(name = "newPassword") String newPassword) throws UserNotFoundException, UserExternallyManagedException {
        Map<String, Object> toRet = new HashMap<String, Object>();
        toRet.put("username", StringUtils.EMPTY);
        toRet.put("success", false);
        if (validateToken(token)) {
            String username = getUsernameFromToken(token);
            if (StringUtils.isNotEmpty(username)) {
                toRet.put("username", username);
                Map<String, Object> userStatus = securityProvider.getUserStatus(username);
                if (userStatus != null && !userStatus.isEmpty()) {
                    boolean enabled = (Boolean)userStatus.get("enabled");
                    if (enabled) {
                        toRet.put("success", securityProvider.setUserPassword(username, newPassword));
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
    public boolean resetPassword(@ValidateStringParam(name = "username") String username, @ValidateStringParam(name = "newPassword") String newPassword) throws UserNotFoundException, UserExternallyManagedException {
        String currentUser = getCurrentUser();
        if (isAdmin(currentUser)) {
            return securityProvider.setUserPassword(username, newPassword);
        } else {
            return false;
        }
    }

    private boolean isAdmin(String username) {
        Set<String> userGroups = securityProvider.getUserGroups(username);
        boolean toRet = false;
        if (CollectionUtils.isNotEmpty(userGroups)) {
            for (String group : userGroups) {
                if (StringUtils.equalsIgnoreCase(group, studioConfiguration.getProperty(SECURITY_GLOBAL_ADMIN_GROUP))) {
                    toRet = true;
                    break;
                }
            }
        }
        return toRet;
    }

    @Override
    @ValidateParams
    public boolean isSiteAdmin(@ValidateStringParam(name = "username") String username) {
        Set<String> userGroups = securityProvider.getUserGroups(username);
        boolean toRet = false;
        if (CollectionUtils.isNotEmpty(userGroups)) {
            for (String group : userGroups) {
                if (StringUtils.equalsIgnoreCase(group, studioConfiguration.getProperty(CONFIGURATION_SITE_DEFAULT_ADMIN_GROUP))) {
                    toRet = true;
                    break;
                }
            }
        }
        return toRet;
    }

    @Override
    @ValidateParams
    public boolean userExists(@ValidateStringParam(name = "username") String username) {
        return securityProvider.userExists(username);
    }

    @Override
    public boolean validateSession(HttpServletRequest request) {
        HttpSession httpSession = request.getSession();
        String authToken = (String)httpSession.getAttribute(STUDIO_SESSION_TOKEN_ATRIBUTE);
        String userName = securityProvider.getCurrentUser();

        if (userName != null) {

            UserDetails userDetails = this.userDetailsManager.loadUserByUsername(userName);

            return (SessionTokenUtils.validateToken(authToken, userDetails.getUsername()));

        }

        return false;
    }

    public String getConfigPath() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH);
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

    public SecurityProvider getSecurityProvider() { return securityProvider; }
    public void setSecurityProvider(SecurityProvider securityProvider) { this.securityProvider = securityProvider; }

    public ContentTypeService getContentTypeService() { return contentTypeService; }
    public void setContentTypeService(ContentTypeService contentTypeService) { this.contentTypeService = contentTypeService; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

    public JavaMailSender getEmailService() { return emailService; }
    public void setEmailService(JavaMailSender emailService) { this.emailService = emailService; }

    public JavaMailSender getEmailServiceNoAuth() { return emailServiceNoAuth; }
    public void setEmailServiceNoAuth(JavaMailSender emailServiceNoAuth) { this.emailServiceNoAuth = emailServiceNoAuth; }

    public UserDetailsManager getUserDetailsManager() { return userDetailsManager; }
    public void setUserDetailsManager(UserDetailsManager userDetailsManager) { this.userDetailsManager = userDetailsManager; }

    public ObjectFactory<FreeMarkerConfig> getFreeMarkerConfig() { return freeMarkerConfig; }
    public void setFreeMarkerConfig(ObjectFactory<FreeMarkerConfig> freeMarkerConfig) { this.freeMarkerConfig = freeMarkerConfig; }

    public ActivityService getActivityService() { return activityService; }
    public void setActivityService(ActivityService activityService) { this.activityService = activityService; }
}
