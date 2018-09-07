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

import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.dal.GroupDAO;
import org.craftercms.studio.api.v2.dal.GroupTO;
import org.craftercms.studio.api.v2.service.security.internal.GroupServiceInternal;
import org.craftercms.studio.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ORG_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SORT;

public class GroupServiceInternalImpl implements GroupServiceInternal {

    private static final Logger logger = LoggerFactory.getLogger(GroupServiceInternalImpl.class);

    private GroupDAO groupDao;

    @Override
    public List<GroupTO> getAllGroups(long orgId, int offset, int limit, String sort) {
        // Prepare parameters
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ORG_ID, orgId);
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        params.put(SORT, sort);
        List<GroupTO> groups = groupDao.getAllGroupsForOrganization(params);

        return groups;
    }

    @Override
    public int getAllGroupsTotal(long orgId) {
        return 0;
    }

    @Override
    public void createGroup(long orgId, String groupName, String groupDescription) throws GroupAlreadyExistsException {

    }

    @Override
    public void updateGroup(long orgId, Group group) {

    }

    @Override
    public void deleteGroup(List<Long> groupIds) {

    }

    @Override
    public Group getGroup(long groupId) {
        return null;
    }

    @Override
    public List<User> getGroupMembers(long groupId, int offset, int limit, String sort) {
        return null;
    }

    @Override
    public void addGroupMembers(long groupId, List<Long> userIds, List<String> usernames) {

    }

    @Override
    public void removeGroupMembers(long groupId, List<Long> userIds, List<String> usernames) {

    }

    @Override
    public List<String> getSiteGroups(String siteId) {
        return null;
    }

    @Override
    public List<String> getGlobalGroups() {
        return null;
    }

    @Override
    public Group getGroupByName(String groupName) throws GroupNotFoundException {
        return null;
    }

    public GroupDAO getGroupDao() {
        return groupDao;
    }

    public void setGroupDao(GroupDAO groupDao) {
        this.groupDao = groupDao;
    }
}
