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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.StudioXmlConstants;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.GroupDAL;
import org.craftercms.studio.api.v2.dal.GroupMapper;
import org.craftercms.studio.api.v2.dal.UserDAL;
import org.craftercms.studio.api.v2.service.security.GroupService;
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
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_DESCRIPTION;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ORG_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SORT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAMES;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_IDS;

public class GroupServiceImpl implements GroupService {

    private static final Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);

    private GroupMapper groupMapper;
    private StudioConfiguration studioConfiguration;
    private ContentService contentService;

    @Override
    public List<Group> getAllGroups(int orgId, int offset, int limit, String sort) {
        // Prepare parameters
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ORG_ID, orgId);
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        params.put(SORT, sort);
        List<GroupDAL> groups = groupMapper.getAllGroupsForOrganization(params);

        List<Group> toRet = new ArrayList<Group>();
        groups.forEach(g -> {
            Group group = new Group();
            group.setId(g.getId());
            group.setDesc(g.getGroupDescription());
            group.setName(g.getGroupName());
            toRet.add(group);
        });

        return toRet;
    }

    @Override
    public void createGroup(int orgId, String groupName, String groupDescription) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ORG_ID, orgId);
        params.put(GROUP_NAME, groupName);
        params.put(GROUP_DESCRIPTION, groupDescription);
        groupMapper.createGroup(params);
    }

    @Override
    public void updateGroup(int orgId, Group group) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ID, group.getId());
        params.put(ORG_ID, orgId);
        params.put(GROUP_NAME, group.getName());
        params.put(GROUP_DESCRIPTION, group.getDesc());
        groupMapper.updateGroup(params);
    }

    @Override
    public void deleteGroup(int groupId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ID, groupId);
        groupMapper.deleteGroup(params);
    }

    @Override
    public Group getGroup(int groupId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_ID, groupId);
        GroupDAL gDAL = groupMapper.getGroup(params);
        Group toRet = new Group();
        toRet.setId(gDAL.getId());
        toRet.setName(gDAL.getGroupName());
        toRet.setDesc(gDAL.getGroupDescription());
        return toRet;
    }

    @Override
    public List<User> getGroupMembers(int groupId, int offset, int limit, String sort) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_ID, groupId);
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        params.put(SORT, sort);
        List<UserDAL> uDALs = groupMapper.getGroupMembers(params);
        List<User> toRet = new ArrayList<User>();
        uDALs.forEach(u -> {
            User user = new User();
            user.setId(u.getId());
            user.setUsername(u.getUsername());
            user.setFirstName(u.getFirstName());
            user.setLastName(u.getLastName());
            user.setEmail(u.getEmail());
            user.setEnabled(u.isEnabled());
            user.setExternallyManaged(u.getExternallyManaged() != 0);
            toRet.add(user);
        });
        return toRet;
    }

    @Override
    public void addGroupMembers(int groupId, List<Integer> userIds, List<String> usernames) {
        List<Integer> allUserIds = new ArrayList<Integer>();
        allUserIds.addAll(userIds);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USERNAMES, usernames);
        if (CollectionUtils.isNotEmpty(usernames)) {
            allUserIds.addAll(groupMapper.getUserIdsForUsernames(params));
        }
        if (CollectionUtils.isNotEmpty(allUserIds)) {
            params = new HashMap<String, Object>();
            params.put(USER_IDS, allUserIds);
            params.put(GROUP_ID, groupId);
            groupMapper.addGroupMembers(params);
        }
    }

    @Override
    public void removeGroupMembers(int groupId, List<Integer> userIds, List<String> usernames) {
        List<Integer> allUserIds = new ArrayList<Integer>();
        allUserIds.addAll(userIds);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USERNAMES, usernames);
        if (CollectionUtils.isNotEmpty(usernames)) {
            allUserIds.addAll(groupMapper.getUserIdsForUsernames(params));
        }
        if (CollectionUtils.isNotEmpty(allUserIds)) {
            params = new HashMap<String, Object>();
            params.put(USER_IDS, allUserIds);
            params.put(GROUP_ID, groupId);
            groupMapper.removeGroupMembers(params);
        }
    }

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

    public GroupMapper getGroupMapper() {
        return groupMapper;
    }

    public void setGroupMapper(GroupMapper groupMapper) {
        this.groupMapper = groupMapper;
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
}
