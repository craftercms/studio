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

package org.craftercms.studio.impl.v2.security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.StudioXmlConstants;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.security.RolePermissionMappings;
import org.craftercms.studio.api.v2.dal.security.SitePermissionMappings;
import org.craftercms.studio.api.v2.security.AvailableActionsConstants;
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
import java.util.concurrent.ExecutionException;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.MODULE_STUDIO;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_ENVIRONMENT_ACTIVE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_PERMISSION_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_ROLE_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_PERMISSION_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME;

public class AvailableActionsResolverImpl implements AvailableActionsResolver {

    private StudioConfiguration studioConfiguration;
    private ConfigurationService configurationService;
    private UserServiceInternal userServiceInternal;

    private LoadingCache<String, SitePermissionMappings> cache;

    public AvailableActionsResolverImpl(StudioConfiguration studioConfiguration,
                                        ConfigurationService configurationService,
                                        UserServiceInternal userServiceInternal) {
        this.studioConfiguration = studioConfiguration;
        this.configurationService = configurationService;
        this.userServiceInternal = userServiceInternal;

        // init cache
        CacheLoader<String, SitePermissionMappings> cacheLoader = new CacheLoader<String, SitePermissionMappings>() {
            @Override
            public SitePermissionMappings load(String site) throws Exception {
                return fetchSitePermissionMappings(site);
            }
        };
        cache = CacheBuilder.newBuilder().build(cacheLoader);
    }

    private SitePermissionMappings fetchSitePermissionMappings(String site) {
        SitePermissionMappings sitePermissionMappings = new SitePermissionMappings();
        sitePermissionMappings.setSiteId(site);
        try {


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
        } catch (ServiceLayerException e) {
            e.printStackTrace();
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
                    long availableActions = AvailableActionsConstants.mapPermissionsToAvailableActions(permissions);
                    rolePermissionMappings.addRulePermissionsMapping(regex, availableActions);
                });
                sitePermissionMappings.addRolePermissionMapping(roleName, rolePermissionMappings);
            }
        }
        return sitePermissionMappings;
    }

    @Override
    public long getAvailableActions(String username, String site, String path)
            throws ServiceLayerException, UserNotFoundException {
        SitePermissionMappings sitePermissionMappings = null;
        try {
            sitePermissionMappings = findSitePermissionMappings(site);
        } catch (ExecutionException e) {
            throw new ServiceLayerException("Error fetching available actions from cache for site " + site, e);
        }
        return calculateAvailableActions(username, path, sitePermissionMappings);
    }

    private SitePermissionMappings findSitePermissionMappings(final String site) throws ExecutionException {
        return cache.get(site);
    }

    private long calculateAvailableActions(String username, String path,
                                           SitePermissionMappings sitePermissionMappings)
            throws ServiceLayerException, UserNotFoundException {
        List<Group> groups = userServiceInternal.getUserGroups(-1, username);
        return sitePermissionMappings.getAvailableActions(username, groups, path);
    }

    @Override
    public void invalidateAvailableActions(String site) {
        cache.invalidate(site);
    }

    @Override
    public void invalidateAvailableActions() {
        cache.invalidateAll();
    }
}
