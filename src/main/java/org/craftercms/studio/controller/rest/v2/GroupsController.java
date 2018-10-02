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

package org.craftercms.studio.controller.rest.v2;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.dal.GroupTO;
import org.craftercms.studio.api.v2.dal.UserTO;
import org.craftercms.studio.api.v2.exception.OrganizationNotFoundException;
import org.craftercms.studio.api.v2.service.security.GroupService;
import org.craftercms.studio.model.rest.AddGroupMembers;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.PaginatedResultList;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.ResultOne;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

import static org.craftercms.studio.api.v1.constant.StudioConstants.DEFAULT_ORGANIZATION_ID;

@RestController
public class GroupsController {

    private static final Logger logger = LoggerFactory.getLogger(GroupsController.class);

    private GroupService groupService;

    /**
     * Get groups API
     *
     * @param offset offset parameter
     * @param limit limit parameter
     * @param sort sort parameter
     * @return Response containing list of groups
     */
    @GetMapping("/api/2/groups")
    public ResponseBody getAllGroups(
            @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(value = "sort", required = false, defaultValue = StringUtils.EMPTY) String sort
            ) throws ServiceLayerException, OrganizationNotFoundException {
        int total = 0;
        total = groupService.getAllGroupsTotal(DEFAULT_ORGANIZATION_ID);
        List<GroupTO> groups = groupService.getAllGroups(DEFAULT_ORGANIZATION_ID, offset, limit, sort);

        ResponseBody responseBody = new ResponseBody();
        PaginatedResultList<GroupTO> result = new PaginatedResultList<>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isEmpty(groups) ? 0 : groups.size());
        result.setResponse(ApiResponse.OK);
        result.setEntities(groups);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Create group API
     *
     * @param group Group to create
     * @return Response object
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/api/2/groups", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseBody createGroup(@RequestBody GroupTO group) throws GroupAlreadyExistsException,
        ServiceLayerException {
        GroupTO newGroup =
                groupService.createGroup(DEFAULT_ORGANIZATION_ID, group.getGroupName(), group.getGroupDescription());
        ResponseBody responseBody = new ResponseBody();
        ResultOne<GroupTO> result = new ResultOne<>();
        result.setResponse(ApiResponse.CREATED);
        result.setEntity(newGroup);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Update group API
     *
     * @param group Group to update
     * @return Response object
     */
    @PatchMapping(value = "/api/2/groups", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseBody updateGroup(@RequestBody GroupTO group) throws ServiceLayerException, GroupNotFoundException {
        ResponseBody responseBody = new ResponseBody();
        GroupTO updatedGroup = groupService.updateGroup(DEFAULT_ORGANIZATION_ID, group);

        ResultOne<GroupTO> result = new ResultOne<>();
        result.setResponse(ApiResponse.OK);
        result.setEntity(updatedGroup);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Delete group API
     *
     * @param groupIds Group identifier
     * @return Response object
     */
    @DeleteMapping("/api/2/groups")
    public ResponseBody deleteGroup(@RequestParam("id") List<Long> groupIds) throws ServiceLayerException {
        groupService.deleteGroup(groupIds);

        ResponseBody responseBody = new ResponseBody();
        ResultOne result = new ResultOne();
        result.setResponse(ApiResponse.DELETED);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Get group API
     *
     * @param groupId Group identifier
     * @return Response containing requested group
     */
    @GetMapping("/api/2/groups/{groupId}")
    public ResponseBody getGroup(@PathVariable("groupId") int groupId) throws ServiceLayerException {
        GroupTO group = groupService.getGroup(groupId);

        ResponseBody responseBody = new ResponseBody();
        ResultOne<GroupTO> result = new ResultOne<>();
        result.setResponse(ApiResponse.OK);
        result.setEntity(group);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Get group members API
     *
     * @param groupId Group identifier
     * @param offset Result set offset
     * @param limit Result set limit
     * @param sort Sort order
     * @return Response containing list od users
     */
    @GetMapping("/api/2/groups/{groupId}/members")
    public ResponseBody getGroupMembers(
        @PathVariable("groupId") int groupId,
        @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
        @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
        @RequestParam(value = "sort", required = false, defaultValue = StringUtils.EMPTY) String sort)
        throws ServiceLayerException {

        List<UserTO> users = groupService.getGroupMembers(groupId, offset, limit, sort);

        ResponseBody responseBody = new ResponseBody();
        ResultList<UserTO> result = new ResultList<>();
        result.setResponse(ApiResponse.OK);
        result.setEntities(users);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Add group members API
     *
     * @param groupId Group identifiers
     * @param addGroupMembers Add members request body (json representation)
     * @return Response object
     */
    @PostMapping(value = "/api/2/groups/{groupId}/members", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseBody addGroupMembers(@PathVariable("groupId") int groupId,
                                        @RequestBody AddGroupMembers addGroupMembers) throws ServiceLayerException,
                                                                                             UserNotFoundException {

        ValidationUtils.validateAddGroupMembers(addGroupMembers);

        List<UserTO> addedUsers = groupService.addGroupMembers(groupId, addGroupMembers.getUserIds(),
            addGroupMembers.getUsernames());

        ResponseBody responseBody = new ResponseBody();
        ResultList<UserTO> result = new ResultList<>();
        result.setResponse(ApiResponse.OK);
        result.setEntities(addedUsers);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Remove group members API
     *
     * @param groupId Group identifier
     * @param userIds List of user identifiers
     * @param usernames List of usernames
     * @return Response object
     */
    @DeleteMapping("/api/2/groups/{groupId}/members")
    public ResponseBody removeGroupMembers(@PathVariable("groupId") int groupId,
                                   @RequestParam(value = "userId", required = false) List<Long> userIds,
                                   @RequestParam(value = "username", required = false) List<String> usernames)
        throws ServiceLayerException, UserNotFoundException {

        ValidationUtils.validateAnyListNonEmpty(userIds, usernames);

        groupService.removeGroupMembers(groupId, userIds != null? userIds : Collections.emptyList(),
                                        usernames != null? usernames : Collections.emptyList());

        ResponseBody responseBody = new ResponseBody();
        ResultOne result = new ResultOne();
        result.setResponse(ApiResponse.DELETED);
        responseBody.setResult(result);
        return responseBody;
    }

    public GroupService getGroupService() {
        return groupService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }
}
