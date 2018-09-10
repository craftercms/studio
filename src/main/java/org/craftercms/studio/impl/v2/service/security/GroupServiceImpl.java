/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */

package org.craftercms.studio.impl.v2.service.security;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.studio.api.v1.constant.StudioXmlConstants;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.GroupTO;
import org.craftercms.studio.api.v2.dal.GroupDAO;
import org.craftercms.studio.api.v2.service.security.GroupService;
import org.craftercms.studio.api.v2.service.security.SecurityProvider;
import org.craftercms.studio.model.Group;
import org.craftercms.studio.model.User;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELM_GROUPS_NODE;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ROLE_MAPPINGS;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_GLOBAL_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_GLOBAL_ROLE_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ORG_ID;

public class GroupServiceImpl implements GroupService {

    private static final Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);

    private GroupDAO groupDAO;
    private StudioConfiguration studioConfiguration;
    private ContentService contentService;
    private SecurityProvider securityProvider;

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_groups")
    public List<Group> getAllGroups(long orgId, int offset, int limit, String sort) throws ServiceLayerException {
        return securityProvider.getAllGroups(orgId, offset, limit, sort);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_groups")
    public int getAllGroupsTotal(long orgId) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ORG_ID, orgId);
        try {
            return groupDAO.getAllGroupsForOrganizationTotal(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "create_groups")
    public void createGroup(long orgId, String groupName, String groupDescription) throws GroupAlreadyExistsException,
        ServiceLayerException {
        securityProvider.createGroup(orgId, groupName, groupDescription);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "update_groups")
    public void updateGroup(long orgId, Group group) throws ServiceLayerException {
        securityProvider.updateGroup(orgId, group);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "delete_groups")
    public void deleteGroup(List<Long> groupIds) throws ServiceLayerException {
        securityProvider.deleteGroup(groupIds);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_groups")
    public Group getGroup(long groupId) throws ServiceLayerException {
        return securityProvider.getGroup(groupId);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "read_groups")
    public List<User> getGroupMembers(long groupId, int offset, int limit, String sort) throws ServiceLayerException {
        return securityProvider.getGroupMembers(groupId, offset, limit, sort);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "update_groups")
    public void addGroupMembers(long groupId, List<Long> userIds, List<String> usernames) throws ServiceLayerException {
        securityProvider.addGroupMembers(groupId, userIds, usernames);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "update_groups")
    public void removeGroupMembers(long groupId, List<Long> userIds, List<String> usernames)
        throws ServiceLayerException {
        securityProvider.removeGroupMembers(groupId, userIds, usernames);
    }

    // TODO: All methods under this one (and including this one) should be part of the internal service
    @Override
    public List<String> getSiteGroups(String siteId) {
        Map<String, List<String>> groupRoleMapping = loadGroupMappings(siteId);
        List<String> toRet = new ArrayList<String>();
        toRet.addAll(groupRoleMapping.keySet());
        return toRet;
    }

    private Map<String, List<String>> loadGroupMappings(String siteId) {
        Map<String, List<String>> groupRoleMap = new HashMap<String, List<String>>();
        String siteConfigPath =
                studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH)
                        .replaceFirst(PATTERN_SITE, siteId);
        String siteGroupRoleMappingConfigPath =
                siteConfigPath + FILE_SEPARATOR +
                studioConfiguration.getProperty(CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME);
        Document document = null;
        try {
            document = contentService.getContentAsDocument(siteId, siteGroupRoleMappingConfigPath);
            Element root = document.getRootElement();
            if (root.getName().equals(DOCUMENT_ROLE_MAPPINGS)) {
                List<Node> groupNodes = root.selectNodes(DOCUMENT_ELM_GROUPS_NODE);
                for (Node node : groupNodes) {
                    String name = node.valueOf(StudioXmlConstants.DOCUMENT_ATTR_PERMISSIONS_NAME);
                    if (!StringUtils.isEmpty(name)) {
                        List<Node> roleNodes = node.selectNodes(StudioXmlConstants.DOCUMENT_ELM_PERMISSION_ROLE);
                        List<String> roles = new ArrayList<String>();
                        for (Node roleNode : roleNodes) {
                            roles.add(roleNode.getText());
                        }
                        groupRoleMap.put(name, roles);
                    }
                }
            }
        } catch (DocumentException e) {
            logger.error("Error while reading group role mappings file for site " + siteId + " - "
                    + siteGroupRoleMappingConfigPath);
        }
        return groupRoleMap;
    }

    @Override
    public List<String> getGlobalGroups() {
        Map<String, List<String>> groupRoleMapping = loadGlobalGroupMappings();
        List<String> toRet = new ArrayList<String>();
        toRet.addAll(groupRoleMapping.keySet());
        return toRet;
    }

    private Map<String, List<String>> loadGlobalGroupMappings() {
        Map<String, List<String>> groupRoleMap = new HashMap<String, List<String>>();
        String siteConfigPath =
                studioConfiguration.getProperty(CONFIGURATION_GLOBAL_CONFIG_BASE_PATH);
        String siteGroupRoleMappingConfigPath =
                siteConfigPath + FILE_SEPARATOR +
                        studioConfiguration.getProperty(CONFIGURATION_GLOBAL_ROLE_MAPPINGS_FILE_NAME);
        Document document = null;
        try {
            document = contentService.getContentAsDocument(StringUtils.EMPTY, siteGroupRoleMappingConfigPath);
            Element root = document.getRootElement();
            if (root.getName().equals(DOCUMENT_ROLE_MAPPINGS)) {
                List<Node> groupNodes = root.selectNodes(DOCUMENT_ELM_GROUPS_NODE);
                for (Node node : groupNodes) {
                    String name = node.valueOf(StudioXmlConstants.DOCUMENT_ATTR_PERMISSIONS_NAME);
                    if (!StringUtils.isEmpty(name)) {
                        List<Node> roleNodes = node.selectNodes(StudioXmlConstants.DOCUMENT_ELM_PERMISSION_ROLE);
                        List<String> roles = new ArrayList<String>();
                        for (Node roleNode : roleNodes) {
                            roles.add(roleNode.getText());
                        }
                        groupRoleMap.put(name, roles);
                    }
                }
            }
        } catch (DocumentException e) {
            logger.error("Error while reading global group role mappings file "
                    + siteGroupRoleMappingConfigPath);
        }
        return groupRoleMap;
    }

    @Override
    public Group getGroupByName(String groupName) throws GroupNotFoundException, ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_NAME, groupName);
        GroupTO groupTO;
        try {
            groupTO = groupDAO.getGroupByName(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
        if (groupTO != null) {
            Group g = new Group();
            g.setId(groupTO.getId());
            g.setName(groupTO.getGroupName());
            g.setDesc(groupTO.getGroupDescription());
            return g;
        } else {
            throw new GroupNotFoundException();
        }
    }

    public GroupDAO getGroupDAO() {
        return groupDAO;
    }

    public void setGroupDAO(GroupDAO groupDAO) {
        this.groupDAO = groupDAO;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public SecurityProvider getSecurityProvider() {
        return securityProvider;
    }

    public void setSecurityProvider(SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }
}
