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

package org.craftercms.studio.impl.v2.security;

import com.google.common.cache.Cache;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.StudioXmlConstants;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.security.RolePermissionMappings;
import org.craftercms.studio.api.v2.dal.security.SitePermissionMappings;
import org.craftercms.studio.api.v2.security.AvailableActionsResolver;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.MODULE_STUDIO;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SYSTEM_ADMIN_GROUP;
import static org.craftercms.studio.api.v2.security.ContentItemAvailableActionsConstants.mapPermissionsToContentItemAvailableActions;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_PERMISSION_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_ROLE_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_PERMISSION_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME;

public class AvailableActionsResolverImpl implements AvailableActionsResolver {

    private static final Logger logger = LoggerFactory.getLogger(AvailableActionsResolverImpl.class);

    public static final String CACHE_KEY = ":available-actions";

    private StudioConfiguration studioConfiguration;
    private ConfigurationService configurationService;
    private UserServiceInternal userServiceInternal;
    private Cache<String, SitePermissionMappings> cache;

    public AvailableActionsResolverImpl(StudioConfiguration studioConfiguration,
                                        ConfigurationService configurationService,
                                        UserServiceInternal userServiceInternal,
                                        Cache<String, SitePermissionMappings> cache) {
        this.studioConfiguration = studioConfiguration;
        this.configurationService = configurationService;
        this.userServiceInternal = userServiceInternal;
        this.cache = cache;
    }

    private SitePermissionMappings fetchSitePermissionMappings(String site) throws ServiceLayerException {
        SitePermissionMappings sitePermissionMappings = new SitePermissionMappings();
        sitePermissionMappings.setSiteId(site);

        String globalRolesConfigPath = studioConfiguration.getProperty(CONFIGURATION_GLOBAL_CONFIG_BASE_PATH) +
                FILE_SEPARATOR + studioConfiguration.getProperty(CONFIGURATION_GLOBAL_ROLE_MAPPINGS_FILE_NAME);
        Document globalRoleMappingsDocument =
                configurationService.getGlobalConfigurationAsDocument(globalRolesConfigPath);

        String globalPermissionsConfigPath =
                studioConfiguration.getProperty(CONFIGURATION_GLOBAL_CONFIG_BASE_PATH) + FILE_SEPARATOR +
                        studioConfiguration.getProperty(CONFIGURATION_GLOBAL_PERMISSION_MAPPINGS_FILE_NAME);
        Document globalPermissionMappingsDocument =
                configurationService.getGlobalConfigurationAsDocument(globalPermissionsConfigPath);

        loadRoles(globalRoleMappingsDocument, sitePermissionMappings);
        loadPermissions(globalPermissionMappingsDocument, sitePermissionMappings);

        if (!StringUtils.equals(site, studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE))) {
            Document roleMappingsDocument = configurationService.getConfigurationAsDocument(site, MODULE_STUDIO,
                    studioConfiguration.getProperty(CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME),
                    studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE));
            Document permissionsMappingsDocument = configurationService.getConfigurationAsDocument(site, MODULE_STUDIO,
                    studioConfiguration.getProperty(CONFIGURATION_SITE_PERMISSION_MAPPINGS_FILE_NAME),
                    studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE));
            loadRoles(roleMappingsDocument, sitePermissionMappings);
            loadPermissions(permissionsMappingsDocument, sitePermissionMappings);
        }
        return sitePermissionMappings;
    }

    private SitePermissionMappings loadRoles(Document document, SitePermissionMappings sitePermissionMappings) {
        Element root = document.getRootElement();
        if (root.getName().equals(StudioXmlConstants.DOCUMENT_ROLE_MAPPINGS)) {
            Map<String, List<String>> rolesMap = new HashMap<String, List<String>>();

            List<Node> userNodes = root.selectNodes(StudioXmlConstants.DOCUMENT_ELM_USER_NODE);
            rolesMap = getRoles(userNodes, rolesMap);

            List<Node> groupNodes = root.selectNodes(StudioXmlConstants.DOCUMENT_ELM_GROUPS_NODE);
            rolesMap = getRoles(groupNodes, rolesMap);

            rolesMap.forEach(sitePermissionMappings::addGroupToRolesMapping);
        }
        return sitePermissionMappings;
    }

    private Map<String, List<String>> getRoles(List<Node> nodes, Map<String, List<String>> rolesMap) {
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

    private SitePermissionMappings loadPermissions(Document document, SitePermissionMappings sitePermissionMappings) {
        Element permissionsRoot = document.getRootElement();
        if (permissionsRoot.getName().equals(StudioXmlConstants.DOCUMENT_PERMISSIONS)) {
            Element siteNode = (Element) permissionsRoot.selectSingleNode(StudioXmlConstants.DOCUMENT_ELM_SITE);
            if(siteNode != null) {
                permissionsRoot = siteNode;
            }

            List<Node> roleNodes = permissionsRoot.selectNodes(StudioXmlConstants.DOCUMENT_ELM_PERMISSION_ROLE);
            for (Node roleNode : roleNodes) {
                String roleName = roleNode.valueOf(StudioXmlConstants.DOCUMENT_ATTR_PERMISSIONS_NAME);
                RolePermissionMappings rolePermissionMappings = new RolePermissionMappings();
                rolePermissionMappings.setRole(roleName);
                List<Node> ruleNodes = roleNode.selectNodes(StudioXmlConstants.DOCUMENT_ELM_PERMISSION_RULE);
                ruleNodes.forEach(r -> {
                    String regex = r.valueOf(StudioXmlConstants.DOCUMENT_ATTR_REGEX);
                    List<Node> permissionNodes = r.selectNodes(StudioXmlConstants.DOCUMENT_ELM_ALLOWED_PERMISSIONS);
                    List<String> permissions = new ArrayList<>();
                    permissionNodes.forEach(pn -> {
                        permissions.add(pn.getText().toLowerCase());
                    });
                    long availableActions = mapPermissionsToContentItemAvailableActions(permissions);
                    rolePermissionMappings.addRuleContentItemPermissionsMapping(regex, availableActions);
                });
                sitePermissionMappings.addRolePermissionMapping(roleName, rolePermissionMappings);
            }
        }
        return sitePermissionMappings;
    }

    @Override
    public long getContentItemAvailableActions(String username, String siteId, String path)
            throws ServiceLayerException, UserNotFoundException {
        SitePermissionMappings sitePermissionMappings = findSitePermissionMappings(siteId);
        return calculateAvailableActions(username, path, sitePermissionMappings);
    }

    private SitePermissionMappings findSitePermissionMappings(final String site) throws ServiceLayerException {
        var cacheKey = site + CACHE_KEY;
        SitePermissionMappings mappings = cache.getIfPresent(cacheKey);
        if (mappings == null) {
            logger.debug("Cache miss for {0}", cacheKey);
            mappings = fetchSitePermissionMappings(site);
            cache.put(cacheKey, mappings);
        }
        return mappings;
    }

    private long calculateAvailableActions(String username, String path,
                                           SitePermissionMappings sitePermissionMappings)
            throws ServiceLayerException, UserNotFoundException {
        long toReturn = 0L;
        List<Group> groups = userServiceInternal.getUserGroups(-1, username);
        if (CollectionUtils.isNotEmpty(groups)) {
            List<String> groupNames = groups.stream().map(g -> g.getGroupName()).collect(Collectors.toList());
            if (groupNames.contains(SYSTEM_ADMIN_GROUP)) {
                toReturn = -1L;
            } else {
                toReturn = sitePermissionMappings.getAvailableActions(username, groups, path);
            }
        }
        return toReturn;
    }

}
