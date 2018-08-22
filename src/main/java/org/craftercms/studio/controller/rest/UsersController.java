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

package org.craftercms.studio.controller.rest;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.service.security.GroupService;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.model.ApiResponse;
import org.craftercms.studio.model.EnableUsers;
import org.craftercms.studio.model.Entity;
import org.craftercms.studio.model.Group;
import org.craftercms.studio.model.ResultList;
import org.craftercms.studio.model.ResultOne;
import org.craftercms.studio.model.Site;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class UsersController {

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);

    private UserService userService;
    private GroupService groupService;
    private SiteService siteService;

    /**
     * Get all users API
     *
     * @param siteId Site identifier
     * @param offset Result set offset
     * @param limit Result set limit
     * @param sort Sort order
     * @return Response containing list of users
     */
    @GetMapping("/api/2/users")
    public StudioResponseBody getAllUsers(@RequestParam("siteId") String siteId, @RequestParam("offset") int offset,
                                          @RequestParam("limit") int limit, @RequestParam("sort") String sort) {
        List<User> users = userService.getAllUsersForSite(1, siteId, offset, limit, sort);

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
     * Create user API
     *
     * @param user User to create
     * @return Response object
     */
    @PostMapping(value = "/api/2/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    public StudioResponseBody createUser(@RequestBody User user) {
        try {
            userService.createUser(user);

            StudioResponseBody responseBody = new StudioResponseBody();
            ResultOne result = new ResultOne();
            result.setResponse(ApiResponse.CODE_0);
            responseBody.setResult(result);
            return responseBody;
        } catch (UserAlreadyExistsException e) {
            StudioResponseBody responseBody = new StudioResponseBody();
            ResultOne result = new ResultOne();
            result.setResponse(ApiResponse.CODE_0);
            responseBody.setResult(result);
            return responseBody;
        }
    }

    /**
     * Update user API
     *
     * @param user User to update
     * @return Response object
     */
    @PatchMapping(value = "/api/2/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    public StudioResponseBody updateUser(@RequestBody User user) {
        userService.updateUser(user);

        StudioResponseBody responseBody = new StudioResponseBody();
        ResultOne result = new ResultOne();
        result.setResponse(ApiResponse.CODE_0);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Delete users API
     *
     * @param userIds List of user identifiers
     * @param usernames List of usernames
     * @return Response object
     */
    @DeleteMapping("/api/2/users")
    public StudioResponseBody deleteUser(@RequestParam("userId") List<Long> userIds,
                           @RequestParam("username") List<String> usernames) {
        userService.deleteUsers(userIds, usernames);

        StudioResponseBody responseBody = new StudioResponseBody();
        ResultOne result = new ResultOne();
        result.setResponse(ApiResponse.CODE_0);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Get user API
     *
     * @param userId User identifier
     * @return Response containing user
     */
    @GetMapping("/api/2/users/{userId}")
    public StudioResponseBody getUser(@PathVariable String userId) {
        int uId = -1;
        String username = StringUtils.EMPTY;
        if (StringUtils.isNumeric(userId)) {
            uId = Integer.parseInt(userId);
        } else {
            username = userId;
        }
        User user = userService.getUserByIdOrUsername(uId, username);

        StudioResponseBody responseBody = new StudioResponseBody();
        ResultOne result = new ResultOne();
        result.setResponse(ApiResponse.CODE_0);
        result.setEntity(user);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Enable users API
     *
     * @param enableUsers Enable users request body (json representation)
     * @return Response object
     */
    @PatchMapping(value = "/api/2/users/enable", consumes = MediaType.APPLICATION_JSON_VALUE)
    public StudioResponseBody enableUsers(@RequestBody EnableUsers enableUsers) {
        userService.enableUsers(enableUsers.getUserIds(), enableUsers.getUsernames(), true);

        StudioResponseBody responseBody = new StudioResponseBody();
        ResultOne result = new ResultOne();
        result.setResponse(ApiResponse.CODE_0);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Disable users API
     *
     * @param enableUsers Disable users request body (json representation)
     * @return Response object
     */
    @PatchMapping(value = "/api/2/users/disable", consumes = MediaType.APPLICATION_JSON_VALUE)
    public StudioResponseBody disableUsers(@RequestBody EnableUsers enableUsers) {
        userService.enableUsers(enableUsers.getUserIds(), enableUsers.getUsernames(), false);

        StudioResponseBody responseBody = new StudioResponseBody();
        ResultOne result = new ResultOne();
        result.setResponse(ApiResponse.CODE_0);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Get user sites API
     *
     * @param userId User identifier
     * @param offset Result set offset
     * @param limit Result set limit
     * @param sort Sort order
     * @return Response containing list of sites
     */
    @GetMapping("/api/2/users/{userId}/sites")
    public StudioResponseBody getUserSites(@PathVariable String userId, @RequestParam int offset,
                                           @RequestParam int limit, @RequestParam String sort) {
        List<Site> sites = new ArrayList<Site>();
        Set<String> allSites = siteService.getAllAvailableSites();
        Map<String, List<String>> siteGroupsMap = new HashMap<String, List<String>>();
        allSites.forEach(s -> {
            siteGroupsMap.put(s, groupService.getSiteGroups(s));
        });
        int uId = -1;
        String username = StringUtils.EMPTY ;
        if ( StringUtils.isNumeric(userId)) {
            uId = Integer.parseInt(userId);
        } else {
            username = userId;
        }
        List<Group> userGroups = userService.getUserGroups(uId, username);
        userGroups.forEach(ug -> {
            for (Map.Entry<String, List<String>> entry : siteGroupsMap.entrySet()) {
                if (entry.getValue().contains(ug.getName())) {
                    try {
                        SiteFeed siteFeed = siteService.getSite(entry.getKey());
                        Site site = new Site();
                        site.setId(siteFeed.getId());
                        site.setDesc(siteFeed.getDescription());
                        sites.add(site);
                    } catch (SiteNotFoundException e) {
                        logger.error("Site not found " + entry.getKey(), e);
                    }
                    break;
                }
            }
        });

        StudioResponseBody responseBody = new StudioResponseBody();
        ResultList result = new ResultList();
        result.setResponse(ApiResponse.CODE_0);
        responseBody.setResult(result);
        List<Entity> entities = new ArrayList<Entity>();
        entities.addAll(sites);
        result.setEntities(entities);
        return responseBody;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public GroupService getGroupService() {
        return groupService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
