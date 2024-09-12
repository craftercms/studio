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

package org.craftercms.studio.impl.v2.service.security;

import com.google.common.cache.Cache;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.StudioXmlConstants;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.job.CronJobContext;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.security.SecurityService;
import org.craftercms.studio.api.v2.service.security.internal.GroupServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.MODULE_STUDIO;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.*;

public class SecurityServiceImpl implements SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityServiceImpl.class);

    private ConfigurationService configurationService;
    private StudioConfiguration studioConfiguration;
    private Cache<String, Object> configurationCache;

    protected UserServiceInternal userServiceInternal;
    protected GroupServiceInternal groupServiceInternal;

    private static final String CACHE_KEY = "user-permissions";

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getUserPermission(String siteId, String username, List<String> roles) {
        String key = siteId + ":" + CACHE_KEY + username;
        List<String> permissions = (List<String>) configurationCache.getIfPresent(key);
        if (isEmpty(permissions)) {
            logger.debug("Cache miss for key '{}'", key);
            permissions = loadUserPermission(siteId, roles);
            configurationCache.put(key, permissions);
        }
        return permissions;
    }

    private List<String> loadUserPermission(String siteId, List<String> roles) {
        Set<String> permissions;
        String configPath;
        List<String> toRet = new ArrayList<>();
        if (StringUtils.isNotEmpty(siteId)) {
            configPath = studioConfiguration.getProperty(CONFIGURATION_SITE_PERMISSION_MAPPINGS_FILE_NAME);
            permissions = getPermissionsFromConfig(siteId, configPath, roles);
            if (CollectionUtils.isNotEmpty(permissions)) {
                toRet.addAll(permissions);
            }
        }
        configPath = studioConfiguration.getProperty(CONFIGURATION_GLOBAL_CONFIG_BASE_PATH) + FILE_SEPARATOR +
                        studioConfiguration.getProperty(CONFIGURATION_GLOBAL_PERMISSION_MAPPINGS_FILE_NAME);
        permissions = getPermissionsFromConfig(StringUtils.EMPTY, configPath, roles);
        if (CollectionUtils.isNotEmpty(permissions)) {
            toRet.addAll(permissions);
        }
        return toRet;
    }

    private Set<String> getPermissionsFromConfig(String siteId, String configPath, List<String> roles) {
        Document document = null;
        Set<String> permissions = new HashSet<>();
        try {
            if (StringUtils.isEmpty(siteId)) {
                document = configurationService.getGlobalConfigurationAsDocument(configPath);
            } else {
                document = configurationService.getConfigurationAsDocument(siteId, MODULE_STUDIO, configPath,
                        studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE));
            }
        } catch (ServiceLayerException e) {
            logger.error("Permission mapping not found in site '{}' path '{}'", siteId, configPath);
        }
        if (Objects.nonNull(document)) {
            Element root = document.getRootElement();
            if (root.getName().equals(StudioXmlConstants.DOCUMENT_PERMISSIONS)) {
                //backwards compatibility for nested <site>
                Element permissionsRoot = root;
                Element siteNode = (Element) permissionsRoot.selectSingleNode(StudioXmlConstants.DOCUMENT_ELM_SITE);
                if (siteNode != null) {
                    permissionsRoot = siteNode;
                }

                List<Node> roleNodes = permissionsRoot.selectNodes(StudioXmlConstants.DOCUMENT_ELM_PERMISSION_ROLE);
                for (Node roleNode : roleNodes) {
                    String roleName = roleNode.valueOf(StudioXmlConstants.DOCUMENT_ATTR_PERMISSIONS_NAME).toLowerCase();
                    if (roles.contains(roleName)) {
                        List<Node> ruleNodes = roleNode.selectNodes(StudioXmlConstants.DOCUMENT_ELM_PERMISSION_RULE);

                        for (Node ruleNode : ruleNodes) {
                            List<Node> permissionNodes = ruleNode.selectNodes(
                                    StudioXmlConstants.DOCUMENT_ELM_ALLOWED_PERMISSIONS);
                            for (Node permissionNode : permissionNodes) {
                                String permission = permissionNode.getText().toLowerCase();
                                permissions.add(permission);
                            }
                        }
                    }
                }
            }
        }
        return permissions;
    }

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
    public Authentication getAuthentication() {
        var context = SecurityContextHolder.getContext();
        if (context != null) {
            return context.getAuthentication();
        }
        return null;
    }

    @Override
    public boolean isSiteMember(String username, String siteId) {
        try {
            if (isSystemAdmin(username)) {
                return true;
            }

            List<Group> userGroups = userServiceInternal.getUserGroups(-1, username);
            List<String> siteGroups = groupServiceInternal.getSiteGroups(siteId);
            return userGroups.stream()
                    .map(Group::getGroupName)
                    .anyMatch(siteGroups::contains);
        } catch (ServiceLayerException | UserNotFoundException e) {
            logger.error("Failed to check the groups for user '{}' in site '{}'", getAuthentication().getName(), siteId, e);
        }
        return false;
    }

    @Override
    public boolean isSystemAdmin(String username) {
        return userServiceInternal.isSystemAdmin(username);
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setConfigurationCache(Cache<String, Object> configurationCache) {
        this.configurationCache = configurationCache;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public void setGroupServiceInternal(GroupServiceInternal groupServiceInternal) {
        this.groupServiceInternal = groupServiceInternal;
    }
}
