/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
 */

package org.craftercms.studio.controller.rest.v2;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.PasswordDoesNotMatchException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserExternallyManagedException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.service.security.GroupService;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.impl.v2.utils.PaginationUtils;
import org.craftercms.studio.model.AuthenticatedUser;
import org.craftercms.studio.model.rest.ResetPasswordRequest;
import org.craftercms.studio.model.rest.SetPasswordRequest;
import org.craftercms.studio.model.Site;
import org.craftercms.studio.model.rest.ChangePasswordRequest;
import org.craftercms.studio.model.rest.EnableUsers;
import org.craftercms.studio.model.rest.PaginatedResultList;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.ResultOne;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
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
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_ID;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_LIMIT;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_OFFSET;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_SITE;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_SITE_ID;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_SORT;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_USERNAME;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_CURRENT_USER;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_LOGOUT_URL;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ROLES;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_SITES;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_USER;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_USERS;
import static org.craftercms.studio.model.rest.ApiResponse.CREATED;
import static org.craftercms.studio.model.rest.ApiResponse.DELETED;
import static org.craftercms.studio.model.rest.ApiResponse.OK;

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
    public ResponseBody getAllUsers(
            @RequestParam(value = REQUEST_PARAM_SITE_ID, required = false) String siteId,
            @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit,
            @RequestParam(value = REQUEST_PARAM_SORT, required = false, defaultValue = StringUtils.EMPTY) String sort)
            throws ServiceLayerException {
        List<User> users = null;
        int total = 0;
        if (StringUtils.isEmpty(siteId)) {
            total = userService.getAllUsersTotal();
            users = userService.getAllUsers(offset, limit, sort);
        } else {
            total = userService.getAllUsersForSiteTotal(DEFAULT_ORGANIZATION_ID, siteId);
            users = userService.getAllUsersForSite(DEFAULT_ORGANIZATION_ID, siteId, offset, limit, sort);
        }

        ResponseBody responseBody = new ResponseBody();
        PaginatedResultList<User> result = new PaginatedResultList<>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isEmpty(users) ? 0 : users.size());
        result.setResponse(OK);
        responseBody.setResult(result);
        result.setEntities(RESULT_KEY_USERS, users);
        return responseBody;
    }

    /**
     * Create user API
     *
     * @param user User to create
     * @return Response object
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/api/2/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseBody createUser(@RequestBody User user)
            throws UserAlreadyExistsException, ServiceLayerException, AuthenticationException {
        User newUser = userService.createUser(user);

        ResponseBody responseBody = new ResponseBody();
        ResultOne<User> result = new ResultOne<>();
        result.setResponse(CREATED);
        result.setEntity(RESULT_KEY_USER, newUser);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Update user API
     *
     * @param user User to update
     * @return Response object
     */
    @PatchMapping(value = "/api/2/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseBody updateUser(@RequestBody User user)
            throws ServiceLayerException, UserNotFoundException, AuthenticationException {
        userService.updateUser(user);

        ResponseBody responseBody = new ResponseBody();
        ResultOne<User> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_USER, user);
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
    public ResponseBody deleteUser(
            @RequestParam(value = REQUEST_PARAM_ID, required = false) List<Long> userIds,
            @RequestParam(value = REQUEST_PARAM_USERNAME, required = false) List<String> usernames)
            throws ServiceLayerException, AuthenticationException, UserNotFoundException {
        ValidationUtils.validateAnyListNonEmpty(userIds, usernames);

        userService.deleteUsers(userIds != null? userIds : Collections.emptyList(),
                                usernames != null? usernames : Collections.emptyList());

        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(DELETED);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Get user API
     *
     * @param userId User identifier
     * @return Response containing user
     */
    @GetMapping("/api/2/users/{id}")
    public ResponseBody getUser(@PathVariable(REQUEST_PARAM_ID) String userId)
            throws ServiceLayerException, UserNotFoundException {
        int uId = -1;
        String username = StringUtils.EMPTY;
        if (StringUtils.isNumeric(userId)) {
            uId = Integer.parseInt(userId);
        } else {
            username = userId;
        }
        User user = userService.getUserByIdOrUsername(uId, username);

        ResponseBody responseBody = new ResponseBody();
        ResultOne<User> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_USER, user);
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
    public ResponseBody enableUsers(@RequestBody EnableUsers enableUsers)
            throws ServiceLayerException, UserNotFoundException, AuthenticationException {
        ValidationUtils.validateEnableUsers(enableUsers);

        List<User> users = userService.enableUsers(enableUsers.getIds(), enableUsers.getUsernames(), true);

        ResponseBody responseBody = new ResponseBody();
        ResultList<User> result = new ResultList<>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_USERS, users);
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
    public ResponseBody disableUsers(@RequestBody EnableUsers enableUsers)
            throws ServiceLayerException, UserNotFoundException, AuthenticationException {
        ValidationUtils.validateEnableUsers(enableUsers);

        List<User> users = userService.enableUsers(enableUsers.getIds(), enableUsers.getUsernames(), false);

        ResponseBody responseBody = new ResponseBody();
        ResultList<User> result = new ResultList<>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_USERS, users);
        responseBody.setResult(result);
        return responseBody;
    }

    /**
     * Get user sites API
     *
     * @param userId User identifier
     * @return Response containing list of sites
     */
    @GetMapping("/api/2/users/{id}/sites")
    public ResponseBody getUserSites(
            @PathVariable(REQUEST_PARAM_ID) String userId,
            @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit)
            throws ServiceLayerException, UserNotFoundException {
        int uId = -1;
        String username = StringUtils.EMPTY ;
        if (StringUtils.isNumeric(userId)) {
            uId = Integer.parseInt(userId);
        } else {
            username = userId;
        }


        List<Site> allSites = userService.getUserSites(uId, username);
        List<Site> paginatedSites = PaginationUtils.paginate(allSites, offset, limit, "siteId");

        PaginatedResultList<Site> result = new PaginatedResultList<>();
        result.setResponse(OK);
        result.setTotal(allSites.size());
        result.setOffset(offset);
        result.setLimit(limit);
        result.setEntities(RESULT_KEY_SITES, paginatedSites);

        ResponseBody responseBody = new ResponseBody();
        responseBody.setResult(result);

        return responseBody;
    }

    /**
     * Get user roles for a site API
     *
     * @param userId User identifier
     * @param site The site ID
     * @return Response containing list of roles
     */
    @GetMapping("/api/2/users/{id}/sites/{site}/roles")
    public ResponseBody getUserSiteRoles(@PathVariable(REQUEST_PARAM_ID) String userId,
                                         @PathVariable(REQUEST_PARAM_SITE) String site)
            throws ServiceLayerException, UserNotFoundException {
        int uId = -1;
        String username = StringUtils.EMPTY ;
        if (StringUtils.isNumeric(userId)) {
            uId = Integer.parseInt(userId);
        } else {
            username = userId;
        }

        List<String> roles = userService.getUserSiteRoles(uId, username, site);

        ResultList<String> result = new ResultList<>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_ROLES, roles);

        ResponseBody responseBody = new ResponseBody();
        responseBody.setResult(result);

        return responseBody;
    }

    /**
     * Get current authenticated user API
     *
     * @return Response containing current authenticated user
     */
    @GetMapping("/api/2/users/me")
    public ResponseBody getCurrentUser() throws AuthenticationException, ServiceLayerException {
        AuthenticatedUser user = userService.getCurrentUser();

        ResultOne<AuthenticatedUser> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_CURRENT_USER, user);

        ResponseBody responseBody = new ResponseBody();
        responseBody.setResult(result);

        return responseBody;
    }

    /**
     * Get the sites of the current authenticated user API
     *
     * @return Response containing current authenticated user sites
     */
    @GetMapping("/api/2/users/me/sites")
    public ResponseBody getCurrentUserSites(
            @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit)
            throws AuthenticationException, ServiceLayerException {
        List<Site> allSites = userService.getCurrentUserSites();
        List<Site> paginatedSites = PaginationUtils.paginate(allSites, offset, limit, "siteId");

        PaginatedResultList<Site> result = new PaginatedResultList<>();
        result.setResponse(OK);
        result.setTotal(allSites.size());
        result.setOffset(offset);
        result.setLimit(limit);
        result.setEntities(RESULT_KEY_SITES, paginatedSites);

        ResponseBody responseBody = new ResponseBody();
        responseBody.setResult(result);

        return responseBody;
    }

    /**
     * Get the roles in a site of the current authenticated user API
     *
     * @return Response containing current authenticated user roles
     */
    @GetMapping("/api/2/users/me/sites/{site}/roles")
    public ResponseBody getCurrentUserSiteRoles(@PathVariable(REQUEST_PARAM_SITE) String site)
            throws AuthenticationException, ServiceLayerException {
        List<String> roles = userService.getCurrentUserSiteRoles(site);

        ResultList<String> result = new ResultList<>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_ROLES, roles);

        ResponseBody responseBody = new ResponseBody();
        responseBody.setResult(result);

        return responseBody;
    }

    /**
     * Get the SSO SP logout URL for the current authenticated user. The system should redirect to this logout URL
     * <strong>AFTER</strong> local logout. Response entity can be null if user is not authenticated through SSO
     * or if logout is disabled
     *
     * @return Response containing SSO logout URL for the current authenticated user
     */
    @GetMapping("/api/2/users/me/logout/sso/url")
    public ResponseBody getCurrentUserSsoLogoutUrl() throws ServiceLayerException, AuthenticationException {
        String logoutUrl = userService.getCurrentUserSsoLogoutUrl();

        ResultOne<String> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_LOGOUT_URL, logoutUrl);

        ResponseBody responseBody = new ResponseBody();
        responseBody.setResult(result);

        return responseBody;
    }

    @GetMapping("/api/2/users/forgot_password")
    public ResponseBody forgotPassword(@RequestParam(value = REQUEST_PARAM_USERNAME, required = true) String username)
            throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException {
        userService.forgotPassword(username);

        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping("/api/2/users/me/change_password")
    public ResponseBody changePassword(@RequestBody ChangePasswordRequest changePasswordRequest)
            throws PasswordDoesNotMatchException, ServiceLayerException, UserExternallyManagedException,
            AuthenticationException, UserNotFoundException {
        User result = userService.changePassword(changePasswordRequest.getUsername(),
                changePasswordRequest.getCurrent(), changePasswordRequest.getNewPassword());

        ResponseBody responseBody = new ResponseBody();
        ResultOne<User> resultOne = new ResultOne<User>();
        resultOne.setEntity(RESULT_KEY_USER, result);
        resultOne.setResponse(OK);
        responseBody.setResult(resultOne);
        return responseBody;
    }

    @PostMapping("/api/2/users/me/set_password")
    public ResponseBody setPassword(@RequestBody SetPasswordRequest setPasswordRequest)
            throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException {
        User user = userService.setPassword(setPasswordRequest.getToken(), setPasswordRequest.getNewPassword());

        ResponseBody responseBody = new ResponseBody();
        ResultOne<User> result = new ResultOne<User>();
        result.setEntity(RESULT_KEY_USER, user);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping("/api/2/users/{id}/reset_password")
    public ResponseBody resetPassword(@PathVariable(REQUEST_PARAM_ID) String userId,
                                      @RequestBody ResetPasswordRequest resetPasswordRequest)
            throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException {
        userService.resetPassword(resetPasswordRequest.getUsername(), resetPasswordRequest.getNewPassword());

        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(OK);
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
