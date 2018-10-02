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

package org.craftercms.studio.impl.v2.service.security.internal;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.StudioXmlConstants;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.GroupDAO;
import org.craftercms.studio.api.v2.dal.GroupTO;
import org.craftercms.studio.api.v2.dal.UserTO;
import org.craftercms.studio.api.v2.service.security.internal.GroupServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELM_GROUPS_NODE;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ROLE_MAPPINGS;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_DESCRIPTION;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_IDS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.GROUP_NAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ORG_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SORT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAMES;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_IDS;

public class GroupServiceInternalImpl implements GroupServiceInternal {

    private static final Logger logger = LoggerFactory.getLogger(GroupServiceInternalImpl.class);

    private GroupDAO groupDao;
    private UserServiceInternal userServiceInternal;
    private StudioConfiguration studioConfiguration;
    private ContentRepository contentRepository;

    @Override
    public List<GroupTO> getAllGroups(long orgId, int offset, int limit, String sort) throws ServiceLayerException {
        // Prepare parameters
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ORG_ID, orgId);
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        params.put(SORT, sort);
        List<GroupTO> groups;

        if (logger.getLevel().equals(Logger.LEVEL_DEBUG)) {
            logger.debug("Get all groups from DB using query ID: getAllGroupsForOrganization");
            logger.debug("Parameters:");
            logger.debug("\t " + ORG_ID + " = " + orgId);
            logger.debug("\t " + OFFSET + " = " + offset);
            logger.debug("\t " + LIMIT + " = " + limit);
            logger.debug("\t " + SORT + " = " + sort);
        }
        try {
            groups = groupDao.getAllGroupsForOrganization(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
        return groups;
    }

    @Override
    public int getAllGroupsTotal(long orgId) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ORG_ID, orgId);
        if (logger.getLevel().equals(Logger.LEVEL_DEBUG)) {
            logger.debug("Get all groups from DB using query ID: getAllGroupsForOrganization");
            logger.debug("Parameters:");
            logger.debug("\t " + ORG_ID + " = " + orgId);
        }
        try {
            return groupDao.getAllGroupsForOrganizationTotal(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public GroupTO createGroup(long orgId, String groupName, String groupDescription) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<>();
        params.put(ORG_ID, orgId);
        params.put(GROUP_NAME, groupName);
        params.put(GROUP_DESCRIPTION, groupDescription);
        if (logger.getLevel().equals(Logger.LEVEL_DEBUG)) {
            logger.debug("Get all groups from DB using query ID: createGroup");
            logger.debug("Parameters:");
            logger.debug("\t " + ORG_ID + " = " + orgId);
            logger.debug("\t " + GROUP_NAME + " = " + groupName);
            logger.debug("\t " + GROUP_DESCRIPTION + " = " + groupDescription);
        }
        try {
            groupDao.createGroup(params);

            GroupTO group = new GroupTO();
            group.setId((Long) params.get(ID));
            group.setGroupName(groupName);
            group.setGroupDescription(groupDescription);

            return group;
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public GroupTO updateGroup(long orgId, GroupTO group) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<>();
        params.put(ID, group.getId());
        params.put(ORG_ID, orgId);
        params.put(GROUP_NAME, group.getGroupName());
        params.put(GROUP_DESCRIPTION, group.getGroupDescription());
        if (logger.getLevel().equals(Logger.LEVEL_DEBUG)) {
            logger.debug("Get all groups from DB using query ID: createGroup");
            logger.debug("Parameters:");
            logger.debug("\t " + ID + " = " + group.getId());
            logger.debug("\t " + ORG_ID + " = " + orgId);
            logger.debug("\t " + GROUP_NAME + " = " + group.getGroupName());
            logger.debug("\t " + GROUP_DESCRIPTION + " = " + group.getGroupDescription());
        }
        try {
            groupDao.updateGroup(params);
            return group;
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public void deleteGroup(List<Long> groupIds) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_IDS, groupIds);
        try {
            groupDao.deleteGroups(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public GroupTO getGroup(long groupId) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_ID, groupId);
        GroupTO gDAL;
        try {
            gDAL = groupDao.getGroup(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
        return gDAL;
    }

    @Override
    public List<UserTO> getGroupMembers(long groupId, int offset, int limit, String sort) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_ID, groupId);
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        params.put(SORT, sort);
        List<UserTO> userTOs;
        try {
            userTOs = groupDao.getGroupMembers(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
        return userTOs;
    }

    @Override
    public List<UserTO> addGroupMembers(long groupId, List<Long> userIds, List<String> usernames)
            throws ServiceLayerException, UserNotFoundException {
        List<UserTO> users = userServiceInternal.findUsers(userIds, usernames);

        Map<String, Object> params = new HashMap<>();
        params.put(USER_IDS, users.stream().map(UserTO::getId).collect(Collectors.toList()));
        params.put(GROUP_ID, groupId);
        try {
            groupDao.addGroupMembers(params);
            return users;
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    @Override
    public void removeGroupMembers(long groupId, List<Long> userIds, List<String> usernames) throws ServiceLayerException {
        List<Long> allUserIds = new ArrayList<Long>();
        if (CollectionUtils.isNotEmpty(userIds)) {
            allUserIds.addAll(userIds);
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USERNAMES, usernames);
        try {
            if (CollectionUtils.isNotEmpty(usernames)) {
                allUserIds.addAll(groupDao.getUserIdsForUsernames(params));
            }
            if (CollectionUtils.isNotEmpty(allUserIds)) {
                params = new HashMap<String, Object>();
                params.put(USER_IDS, allUserIds);
                params.put(GROUP_ID, groupId);
                groupDao.removeGroupMembers(params);
            }
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
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
            document = getContentAsDocument(siteId, siteGroupRoleMappingConfigPath);
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

    protected Document getContentAsDocument(String site, String path) throws DocumentException {

        // TODO: SJ: Refactor in 4.x as this already exists in Crafter Core (which is part of the new Studio)
        Document retDocument = null;
        InputStream is = null;
            try {
            is = contentRepository.getContent(site, path);
        } catch (ContentNotFoundException e) {
            logger.debug("Content not found for path {0}", e, path);
        }

            if(is != null) {
            try {
                SAXReader saxReader = new SAXReader();
                try {
                    saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                    saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
                    saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                }catch (SAXException ex){
                    logger.error("Unable to turn off external entity loading, This could be a security risk.", ex);
                }
                retDocument = saxReader.read(is);
            }
            finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                }
                catch (IOException err) {
                    logger.debug("Error closing stream for path {0}", err, path);
                }
            }
        }

            return retDocument;
    }

    @Override
    public GroupTO getGroupByName(String groupName) throws GroupNotFoundException, ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_NAME, groupName);
        GroupTO groupTO;
        try {
            groupTO = groupDao.getGroupByName(params);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
        if (groupTO != null) {
            return groupTO;
        } else {
            throw new GroupNotFoundException();
        }
    }

    @Override
    public boolean groupExists(String groupName) throws ServiceLayerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GROUP_NAME, groupName);
        try {
            Integer result = groupDao.groupExists(params);
            return (result > 0);
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown database error", e);
        }
    }

    public GroupDAO getGroupDao() {
        return groupDao;
    }

    public void setGroupDao(GroupDAO groupDao) {
        this.groupDao = groupDao;
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }
}
