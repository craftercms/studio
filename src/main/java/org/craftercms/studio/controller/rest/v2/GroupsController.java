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

package org.craftercms.studio.controller.rest.v2;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.exception.OrganizationNotFoundException;
import org.craftercms.studio.api.v2.service.security.GroupService;
import org.craftercms.studio.model.rest.AddGroupMembers;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

import static org.craftercms.studio.api.v1.constant.StudioConstants.DEFAULT_ORGANIZATION_ID;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_ID;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_KEYWORD;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_LIMIT;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_OFFSET;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_SORT;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_USERNAME;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_USER_ID;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.API_2;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.GROUPS;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.MEMBERS;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.PATH_PARAM_ID;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_GROUP;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_GROUPS;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_USERS;
import static org.craftercms.studio.model.rest.ApiResponse.CREATED;
import static org.craftercms.studio.model.rest.ApiResponse.DELETED;
import static org.craftercms.studio.model.rest.ApiResponse.OK;

@RequestMapping(API_2 + GROUPS)
@RestController
public class GroupsController {

    private static final Logger logger = LoggerFactory.getLogger(GroupsController.class);

    private GroupService groupService;

    /**
     * Get groups API
     *
     * @param keyword keyword parameter
     * @param offset offset parameter
     * @param limit limit parameter
     * @param sort sort parameter
     * @return Response containing list of groups
     */
    @GetMapping()
    public ResponseBody getAllGroups(
            @RequestParam(value = REQUEST_PARAM_KEYWORD, required = false) String keyword,
            @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit,
            @RequestParam(value = REQUEST_PARAM_SORT, required = false, defaultValue = StringUtils.EMPTY) String sort)
            throws ServiceLayerException, OrganizationNotFoundException {
        int total = 0;
        total = groupService.getAllGroupsTotal(DEFAULT_ORGANIZATION_ID, keyword);
        List<Group> groups = groupService.getAllGroups(DEFAULT_ORGANIZATION_ID, keyword, offset, limit, sort);

        ResponseBody responseBody = new ResponseBody();
        PaginatedResultList<Group> result = new PaginatedResultList<>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isEmpty(groups) ? 0 : groups.size());
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_GROUPS, groups);
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
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseBody createGroup(@RequestBody Group group)
            throws GroupAlreadyExistsException, ServiceLayerException, AuthenticationException {
        Group newGroup =
                groupService.createGroup(DEFAULT_ORGANIZATION_ID, group.getGroupName(), group.getGroupDescription());
        ResponseBody responseBody = new ResponseBody();
        ResultOne<Group> result = new ResultOne<>();
        result.setResponse(CREATED);
        result.setEntity(RESULT_KEY_GROUP, newGroup);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Update group API
     *
     * @param group Group to update
     * @return Response object
     */
    @PatchMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseBody updateGroup(@RequestBody Group group)
            throws ServiceLayerException, GroupNotFoundException, AuthenticationException {
        ResponseBody responseBody = new ResponseBody();
        Group updatedGroup = groupService.updateGroup(DEFAULT_ORGANIZATION_ID, group);

        ResultOne<Group> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_GROUP, updatedGroup);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Delete group API
     *
     * @param groupIds Group identifier
     * @return Response object
     */
    @DeleteMapping()
    public ResponseBody deleteGroup(@RequestParam(REQUEST_PARAM_ID) List<Long> groupIds)
            throws ServiceLayerException, GroupNotFoundException, AuthenticationException {
        groupService.deleteGroup(groupIds);

        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(DELETED);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Get group API
     *
     * @param groupId Group identifier
     * @return Response containing requested group
     */
    @GetMapping(PATH_PARAM_ID)
    public ResponseBody getGroup(@PathVariable(REQUEST_PARAM_ID) int groupId)
            throws ServiceLayerException, GroupNotFoundException {
        Group group = groupService.getGroup(groupId);

        ResponseBody responseBody = new ResponseBody();
        ResultOne<Group> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_GROUP, group);
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
    @GetMapping(PATH_PARAM_ID + MEMBERS)
    public ResponseBody getGroupMembers(
            @PathVariable(REQUEST_PARAM_ID) int groupId,
            @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit,
            @RequestParam(value = REQUEST_PARAM_SORT, required = false, defaultValue = StringUtils.EMPTY) String sort)
            throws ServiceLayerException, GroupNotFoundException {

        int total = groupService.getGroupMembersTotal(groupId);
        List<User> users = groupService.getGroupMembers(groupId, offset, limit, sort);

        ResponseBody responseBody = new ResponseBody();
        PaginatedResultList<User> result = new PaginatedResultList<>();
        result.setResponse(OK);
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(limit);
        result.setEntities(RESULT_KEY_USERS, users);
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
    @PostMapping(value = PATH_PARAM_ID + MEMBERS, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseBody addGroupMembers(@PathVariable(REQUEST_PARAM_ID) int groupId,
                                        @RequestBody AddGroupMembers addGroupMembers)
            throws ServiceLayerException, UserNotFoundException, GroupNotFoundException, AuthenticationException {

        ValidationUtils.validateAddGroupMembers(addGroupMembers);

        List<User> addedUsers = groupService.addGroupMembers(groupId, addGroupMembers.getIds(),
                                                             addGroupMembers.getUsernames());

        ResponseBody responseBody = new ResponseBody();
        ResultList<User> result = new ResultList<>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_USERS, addedUsers);
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
    @DeleteMapping(PATH_PARAM_ID + MEMBERS)
    public ResponseBody removeGroupMembers(
            @PathVariable(REQUEST_PARAM_ID) int groupId,
            @RequestParam(value = REQUEST_PARAM_USER_ID, required = false) List<Long> userIds,
            @RequestParam(value = REQUEST_PARAM_USERNAME, required = false) List<String> usernames)
            throws ServiceLayerException, UserNotFoundException, GroupNotFoundException, AuthenticationException {

        ValidationUtils.validateAnyListNonEmpty(userIds, usernames);

        groupService.removeGroupMembers(groupId, userIds != null? userIds : Collections.emptyList(),
                                        usernames != null? usernames : Collections.emptyList());

        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(DELETED);
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
