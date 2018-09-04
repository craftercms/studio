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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.service.security.GroupService;
import org.craftercms.studio.model.AddGroupMembers;
import org.craftercms.studio.model.ApiResponse;
import org.craftercms.studio.model.Entity;
import org.craftercms.studio.model.Group;
import org.craftercms.studio.model.ResultList;
import org.craftercms.studio.model.ResultOne;
import org.craftercms.studio.model.StudioResponseBody;
import org.craftercms.studio.model.User;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public StudioResponseBody getAllGroups(
            @RequestParam("offset") Optional<Integer> offset,
            @RequestParam("limit") Optional<Integer> limit,
            @RequestParam("sort") Optional<String> sort
            ) {
        int iOffset = offset.isPresent() ? offset.get() : 0;
        int iLimit = limit.isPresent() ? limit.get() : 10;
        String sSort = sort.isPresent() ? sort.get() : StringUtils.EMPTY;
        List<Group> groups = groupService.getAllGroups(1, iOffset, iLimit, sSort);

        StudioResponseBody responseBody = new StudioResponseBody();
        ResultList result = new ResultList();
        result.setResponse(ApiResponse.CODE_0);
        List<Entity> entities = new ArrayList<Entity>();
        entities.addAll(groups);
        result.setEntities(entities);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Create group API
     *
     * @param group Group to create
     * @return Response object
     */
    @PostMapping(value = "/api/2/groups", consumes = MediaType.APPLICATION_JSON_VALUE)
    public StudioResponseBody createGroup(@RequestBody Group group) {
        try {
            groupService.createGroup(1, group.getName(), group.getDesc());
        } catch (GroupAlreadyExistsException e) {
            StudioResponseBody errorBody = new StudioResponseBody();
            ResultOne errorResult = new ResultOne();
            errorResult.setResponse(ApiResponse.CODE_4000);
            errorBody.setResult(errorResult);
            return errorBody;
        }

        StudioResponseBody responseBody = new StudioResponseBody();
        ResultOne result = new ResultOne();
        result.setResponse(ApiResponse.CODE_1);
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
    public StudioResponseBody updateGroup(@RequestBody Group group) {
        groupService.updateGroup(1, group);

        StudioResponseBody responseBody = new StudioResponseBody();
        ResultOne result = new ResultOne();
        result.setResponse(ApiResponse.CODE_0);
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
    public StudioResponseBody deleteGroup(@RequestParam("id") List<Long> groupIds) {
        groupService.deleteGroup(groupIds);

        StudioResponseBody responseBody = new StudioResponseBody();
        ResultOne result = new ResultOne();
        result.setResponse(ApiResponse.CODE_0);
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
    public StudioResponseBody getGroup(@PathVariable("groupId") int groupId) {
        Group group = groupService.getGroup(groupId);

        StudioResponseBody responseBody = new StudioResponseBody();
        ResultOne result = new ResultOne();
        result.setResponse(ApiResponse.CODE_0);
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
    public StudioResponseBody getGroupMembers(@PathVariable("groupId") int groupId,
                                              @RequestParam(value = "offset", required = false) int offset,
                                              @RequestParam(value = "limit", required = false) int limit,
                                              @RequestParam(value = "sort", required = false) String sort) {
        List<User> users = groupService.getGroupMembers(groupId, offset, limit, sort);

        StudioResponseBody responseBody = new StudioResponseBody();
        ResultList result = new ResultList();
        result.setResponse(ApiResponse.CODE_0);
        responseBody.setResult(result);
        List<Entity> entities = new ArrayList<Entity>();
        entities.addAll(users);
        result.setEntities(entities);
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
    public StudioResponseBody addGroupMembers(@PathVariable("groupId") int groupId,
                                              @RequestBody AddGroupMembers addGroupMembers) {
        groupService.addGroupMembers(groupId, addGroupMembers.getUserIds(), addGroupMembers.getUsernames());

        StudioResponseBody responseBody = new StudioResponseBody();
        ResultOne result = new ResultOne();
        result.setResponse(ApiResponse.CODE_0);
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
    public StudioResponseBody removeGroupMembers(@PathVariable("groupId") int groupId,
                                   @RequestParam(value = "userId", required = false) List<Long> userIds,
                                   @RequestParam(value = "username", required = false) List<String> usernames) {
        groupService.removeGroupMembers(groupId, userIds, usernames);

        StudioResponseBody responseBody = new StudioResponseBody();
        ResultOne result = new ResultOne();
        result.setResponse(ApiResponse.CODE_0);
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
