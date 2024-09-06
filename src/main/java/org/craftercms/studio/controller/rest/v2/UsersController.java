/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.ValidationException;
import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.craftercms.commons.validation.annotations.param.SqlSort;
import org.craftercms.commons.validation.annotations.param.ValidSiteId;
import org.craftercms.commons.validation.validators.impl.EsapiValidator;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.*;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.controller.rest.ValidationUtils;
import org.craftercms.studio.impl.v2.utils.PaginationUtils;
import org.craftercms.studio.model.AuthenticatedUser;
import org.craftercms.studio.model.Site;
import org.craftercms.studio.model.rest.*;
import org.craftercms.studio.model.users.HasPermissionsRequest;
import org.craftercms.studio.model.users.UpdateUserPropertiesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.SEARCH_KEYWORDS;
import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.USERNAME;
import static org.craftercms.studio.api.v1.constant.StudioConstants.DEFAULT_ORGANIZATION_ID;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_SET_PASSWORD_DELAY;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.*;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.*;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.*;
import static org.craftercms.studio.model.rest.ApiResponse.*;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@RestController
@RequestMapping(API_2 + USERS)
public class UsersController {

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);

    private final UserService userService;
    private final StudioConfiguration studioConfiguration;

    @ConstructorProperties({"userService", "studioConfiguration"})
    public UsersController(UserService userService, StudioConfiguration studioConfiguration) {
        this.userService = userService;
        this.studioConfiguration = studioConfiguration;
    }

    /**
     * Get all users API
     *
     * @param siteId Site identifier
     * @param offset Result set offset
     * @param limit  Result set limit
     * @param sort   Sort order
     * @return Response containing list of users
     */
    @GetMapping
    public PaginatedResultList<UserResponse> getAllUsers(
            @ValidSiteId @RequestParam(value = REQUEST_PARAM_SITE_ID, required = false) String siteId,
            @EsapiValidatedParam(type = SEARCH_KEYWORDS) @RequestParam(value = REQUEST_PARAM_KEYWORD, required = false) String keyword,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit,
            @SqlSort(columns = USER_SORT_COLUMNS) @RequestParam(value = REQUEST_PARAM_SORT, required = false,
                    defaultValue = "id asc") String sort)
            throws ServiceLayerException {
        List<UserResponse> users;
        int total;
        if (isEmpty(siteId)) {
            total = userService.getAllUsersTotal(keyword);
            users = userService.getAllUsers(keyword, offset, limit, sort);
        } else {
            total = userService.getAllUsersForSiteTotal(DEFAULT_ORGANIZATION_ID, siteId, keyword);
            users = userService.getAllUsersForSite(DEFAULT_ORGANIZATION_ID, siteId, keyword, offset, limit, sort);
        }

        PaginatedResultList<UserResponse> result = new PaginatedResultList<>();
        result.setTotal(total);
        result.setOffset(offset);
        result.setLimit(CollectionUtils.isEmpty(users) ? 0 : users.size());
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_USERS, users);
        return result;
    }

    /**
     * Create user API
     *
     * @param user User to create
     * @return Response object
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResultOne<UserResponse> createUser(@Valid @RequestBody CreateUserRequest user)
            throws UserAlreadyExistsException, ServiceLayerException, AuthenticationException {
        UserResponse newUser = userService.createUser(buildUser(user));
        ResultOne<UserResponse> result = new ResultOne<>();
        result.setResponse(CREATED);
        result.setEntity(RESULT_KEY_USER, newUser);
        return result;
    }

    private User buildUser(final CreateUserRequest userRequest) {
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setPassword(userRequest.getPassword());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setExternallyManaged(userRequest.isExternallyManaged());
        user.setEmail(userRequest.getEmail());
        user.setEnabled(userRequest.isEnabled());

        return user;
    }

    private User buildUser(final UpdateUserRequest userRequest) {
        User user = new User();
        user.setId(userRequest.getId());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setEnabled(userRequest.isEnabled());

        return user;
    }

    /**
     * Update user API
     *
     * @param user User to update
     * @return Response object
     */
    @PatchMapping(consumes = APPLICATION_JSON_VALUE)
    public ResultOne<UserResponse> updateUser(@Valid @RequestBody UpdateUserRequest user)
            throws ServiceLayerException, UserNotFoundException, AuthenticationException, UserExternallyManagedException {
        User userRequest = buildUser(user);
        userService.updateUser(userRequest);

        ResultOne<UserResponse> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_USER, new UserResponse(userRequest));
        return result;
    }

    /**
     * Delete users API
     *
     * @param userIds   List of user identifiers
     * @param usernames List of usernames
     * @return Response object
     */
    @DeleteMapping
    public Result deleteUsers(
            @RequestParam(value = REQUEST_PARAM_ID, required = false) List<@NotNull Long> userIds,
            @RequestParam(value = REQUEST_PARAM_USERNAME, required = false)
            List<@NotBlank @EsapiValidatedParam(type = USERNAME) String> usernames)
            throws ServiceLayerException, AuthenticationException, UserNotFoundException, UserExternallyManagedException {
        ValidationUtils.validateAnyListNonEmpty(userIds, usernames);

        userService.deleteUsers(requireNonNullElse(userIds, emptyList()),
                requireNonNullElse(usernames, emptyList()));

        Result result = new Result();
        result.setResponse(DELETED);
        return result;
    }

    /**
     * Get user API
     *
     * @param userId User identifier
     * @return Response containing user
     */
    @GetMapping(value = PATH_PARAM_ID, consumes = ALL_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResultOne<UserResponse> getUser(@PathVariable(REQUEST_PARAM_ID) String userId)
            throws ServiceLayerException, UserNotFoundException, ValidationException {
        int uId = -1;
        String username = StringUtils.EMPTY;
        if (isNumeric(userId)) {
            uId = Integer.parseInt(userId);
        } else {
            ValidationUtils.validateValue(new EsapiValidator(USERNAME), userId, REQUEST_PARAM_ID);
            username = userId;
        }
        User user = userService.getUserByIdOrUsername(uId, username);

        ResultOne<UserResponse> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_USER, new UserResponse(user));
        return result;
    }

    /**
     * Enable users API
     *
     * @param enableUsers Enable users request body (json representation)
     * @return Response object
     */
    @PatchMapping(value = ENABLE, consumes = APPLICATION_JSON_VALUE)
    public ResultList<UserResponse> enableUsers(@Valid @RequestBody EnableUsers enableUsers)
            throws ServiceLayerException, UserNotFoundException, AuthenticationException, UserExternallyManagedException {
        ValidationUtils.validateEnableUsers(enableUsers);

        List<UserResponse> users = userService.enableUsers(enableUsers.getIds(), enableUsers.getUsernames(), true);

        ResultList<UserResponse> result = new ResultList<>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_USERS, users);
        return result;
    }

    /**
     * Disable users API
     *
     * @param enableUsers Disable users request body (json representation)
     * @return Response object
     */
    @PatchMapping(value = DISABLE, consumes = APPLICATION_JSON_VALUE)
    public ResultList<UserResponse> disableUsers(@Valid @RequestBody EnableUsers enableUsers)
            throws ServiceLayerException, UserNotFoundException, AuthenticationException, UserExternallyManagedException {
        ValidationUtils.validateEnableUsers(enableUsers);

        List<UserResponse> users = userService.enableUsers(enableUsers.getIds(), enableUsers.getUsernames(), false);

        ResultList<UserResponse> result = new ResultList<>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_USERS, users);
        return result;
    }

    /**
     * Get user sites API
     *
     * @param userId User identifier
     * @return Response containing list of sites
     */
    @GetMapping(PATH_PARAM_ID + SITES)
    public PaginatedResultList<Site> getUserSites(
            @NotNull @PathVariable(REQUEST_PARAM_ID) String userId,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit)
            throws ServiceLayerException, UserNotFoundException, ValidationException {
        int uId = -1;
        String username = StringUtils.EMPTY;
        if (isNumeric(userId)) {
            uId = Integer.parseInt(userId);
        } else {
            ValidationUtils.validateValue(new EsapiValidator(USERNAME), userId, REQUEST_PARAM_ID);
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

        return result;
    }

    /**
     * Get user roles for a site API
     *
     * @param userId User identifier
     * @param site   The site ID
     * @return Response containing list of roles
     */
    @GetMapping(PATH_PARAM_ID + SITES + PATH_PARAM_SITE + ROLES)
    public ResultList<String> getUserSiteRoles(@NotNull @PathVariable(REQUEST_PARAM_ID) String userId,
                                               @NotNull @ValidSiteId @PathVariable(REQUEST_PARAM_SITE) String site)
            throws ServiceLayerException, UserNotFoundException, ValidationException {
        int uId = -1;
        String username = StringUtils.EMPTY;
        if (isNumeric(userId)) {
            uId = Integer.parseInt(userId);
        } else {
            ValidationUtils.validateValue(new EsapiValidator(USERNAME), userId, REQUEST_PARAM_ID);
            username = userId;
        }

        List<String> roles = userService.getUserSiteRoles(uId, username, site);
        ResultList<String> result = new ResultList<>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_ROLES, roles);

        return result;
    }

    /**
     * Get current authenticated user API
     *
     * @return Response containing current authenticated user
     */
    @GetMapping(ME)
    public ResultOne<AuthenticatedUser> getCurrentUser() throws AuthenticationException, ServiceLayerException {
        AuthenticatedUser user = userService.getCurrentUser();

        ResultOne<AuthenticatedUser> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_CURRENT_USER, user);

        return result;
    }

    /**
     * Get the sites of the current authenticated user API
     *
     * @return Response containing current authenticated user sites
     */
    @GetMapping(ME + SITES)
    public PaginatedResultList<Site> getCurrentUserSites(
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_OFFSET, required = false, defaultValue = "0") int offset,
            @PositiveOrZero @RequestParam(value = REQUEST_PARAM_LIMIT, required = false, defaultValue = "10") int limit)
            throws AuthenticationException, ServiceLayerException {
        List<Site> allSites = userService.getCurrentUserSites();
        List<Site> paginatedSites = PaginationUtils.paginate(allSites, offset, limit, "siteId");

        PaginatedResultList<Site> result = new PaginatedResultList<>();
        result.setResponse(OK);
        result.setTotal(allSites.size());
        result.setOffset(offset);
        result.setLimit(limit);
        result.setEntities(RESULT_KEY_SITES, paginatedSites);

        return result;
    }

    /**
     * Get the roles in a site of the current authenticated user API
     *
     * @return Response containing current authenticated user roles
     */
    @GetMapping(ME + SITES + PATH_PARAM_SITE + ROLES)
    public ResultList<String> getCurrentUserSiteRoles(@NotBlank @ValidSiteId @PathVariable(REQUEST_PARAM_SITE) String site)
            throws AuthenticationException, ServiceLayerException {
        List<String> roles = userService.getCurrentUserSiteRoles(site);

        ResultList<String> result = new ResultList<>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_ROLES, roles);

        return result;
    }

    /**
     * Get the SSO SP logout URL for the current authenticated user. The system should redirect to this logout URL
     * <strong>AFTER</strong> local logout. Response entity can be null if user is not authenticated through SSO
     * or if logout is disabled
     *
     * @return Response containing SSO logout URL for the current authenticated user
     * @deprecated since 3.2, all logout redirects are now handled by Spring Security
     */
    @GetMapping(ME + LOGOUT_SSO_URL)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result getCurrentUserSsoLogoutUrl() {
        Result result = new Result();
        result.setResponse(DEPRECATED);

        return result;
    }

    @GetMapping(FORGOT_PASSWORD)
    public ResultOne<String> forgotPassword(@NotBlank @RequestParam(value = REQUEST_PARAM_USERNAME) String username) {
        int delay = studioConfiguration.getProperty(SECURITY_SET_PASSWORD_DELAY, Integer.class);
        try {
            TimeUnit.SECONDS.sleep(delay);
            ValidationUtils.validateValue(new EsapiValidator(USERNAME), username, REQUEST_PARAM_USERNAME);
            userService.forgotPassword(username);
        } catch (ServiceLayerException e) {
            logger.error("Failed to process forgot password for user '{}'", username, e);
        } catch (ValidationException e) {
            logger.error("Validation error while processing forgot password for user '{}'", username, e);
        } catch (InterruptedException e) {
            logger.debug("Interrupted while delaying request by '{}' seconds", delay, e);
        }
        ResultOne<String> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_MESSAGE, "If the user exists, a password recovery email has been sent to them.");
        result.setResponse(OK);
        return result;
    }

    @PostMapping(ME + CHANGE_PASSWORD)
    public ResultOne<UserResponse> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest)
            throws PasswordDoesNotMatchException, ServiceLayerException, UserExternallyManagedException,
            AuthenticationException, UserNotFoundException {
        int delay = studioConfiguration.getProperty(SECURITY_SET_PASSWORD_DELAY, Integer.class);
        try {
            TimeUnit.SECONDS.sleep(delay);
        } catch (InterruptedException e) {
            logger.debug("Interrupted while delaying request by '{}' seconds", delay, e);
        }
        UserResponse user = userService.changePassword(changePasswordRequest.getUsername(),
                changePasswordRequest.getCurrent(), changePasswordRequest.getNewPassword());

        ResultOne<UserResponse> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_USER, user);
        result.setResponse(OK);
        return result;
    }

    @PostMapping(SET_PASSWORD)
    public ResultOne<UserResponse> setPassword(@Valid @RequestBody SetPasswordRequest setPasswordRequest)
            throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException {
        int delay = studioConfiguration.getProperty(SECURITY_SET_PASSWORD_DELAY, Integer.class);
        try {
            TimeUnit.SECONDS.sleep(delay);
        } catch (InterruptedException e) {
            logger.debug("Interrupted while delaying request by '{}' seconds", delay, e);
        }
        UserResponse user = userService.setPassword(setPasswordRequest.getToken(), setPasswordRequest.getNewPassword());

        ResultOne<UserResponse> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_USER, user);
        result.setResponse(OK);
        return result;
    }

    @PostMapping(PATH_PARAM_ID + RESET_PASSWORD)
    public Result resetPassword(@NotBlank @EsapiValidatedParam(type = USERNAME) @PathVariable(REQUEST_PARAM_ID) String userId,
                                @Valid @RequestBody ResetPasswordRequest resetPasswordRequest)
            throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException {
        userService.resetPassword(resetPasswordRequest.getUsername(), resetPasswordRequest.getNewPassword());

        Result result = new Result();
        result.setResponse(OK);
        return result;
    }

    @GetMapping(value = VALIDATE_TOKEN, produces = APPLICATION_JSON_VALUE)
    public Result validateToken(HttpServletResponse response,
                                @NotBlank @RequestParam(value = REQUEST_PARAM_TOKEN) String token)
            throws UserNotFoundException, UserExternallyManagedException, ServiceLayerException {
        int delay = studioConfiguration.getProperty(SECURITY_SET_PASSWORD_DELAY, Integer.class);
        try {
            TimeUnit.SECONDS.sleep(delay);
        } catch (InterruptedException e) {
            logger.debug("Interrupted while delaying request by '{}' seconds", delay, e);
        }

        boolean valid = userService.validateToken(token);
        Result result = new Result();
        if (valid) {
            result.setResponse(OK);
        } else {
            result.setResponse(UNAUTHORIZED);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
        return result;
    }

    @GetMapping(value = ME + PROPERTIES, produces = APPLICATION_JSON_VALUE)
    public ResultOne<Map<String, Map<String, String>>> getUserProperties(
            @ValidSiteId @RequestParam(required = false, defaultValue = StringUtils.EMPTY) String siteId)
            throws ServiceLayerException {
        ResultOne<Map<String, Map<String, String>>> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity("properties", userService.getUserProperties(siteId)); //TODO: Extract key
        return result;
    }

    @PostMapping(value = ME + PROPERTIES, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResultOne<Map<String, String>> updateUserProperties(@Valid @RequestBody UpdateUserPropertiesRequest request)
            throws ServiceLayerException {
        ResultOne<Map<String, String>> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity("properties", userService.updateUserProperties(request.getSiteId(), request.getProperties())); //TODO: Extract key

        return result;
    }

    @DeleteMapping(value = ME + PROPERTIES, produces = APPLICATION_JSON_VALUE)
    public ResultOne<Map<String, String>> deleteUserProperties(
            @ValidSiteId @RequestParam(required = false, defaultValue = StringUtils.EMPTY) String siteId,
            @Valid @NotEmpty @RequestParam List<@NotBlank String> properties) throws ServiceLayerException {
        ResultOne<Map<String, String>> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity("properties", userService.deleteUserProperties(siteId, properties)); //TODO: Extract key
        return result;
    }

    /**
     * Get the permissions in a site of the current authenticated user API
     *
     * @return Response containing current authenticated user permissions
     */
    @GetMapping(value = ME + SITES + PATH_PARAM_SITE + PERMISSIONS, produces = APPLICATION_JSON_VALUE)
    public ResultList<String> getCurrentUserSitePermissions(@ValidSiteId @PathVariable(REQUEST_PARAM_SITE) String site)
            throws ServiceLayerException, UserNotFoundException, ExecutionException {
        List<String> permissions = userService.getCurrentUserSitePermissions(site);
        ResultList<String> result = new ResultList<>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_PERMISSIONS, permissions);
        return result;
    }

    /**
     * Check if user has permissions in a site of the current authenticated user API
     *
     * @return Response containing current authenticated user roles
     */
    @PostMapping(value = ME + SITES + PATH_PARAM_SITE + HAS_PERMISSIONS, consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResultOne<Map<String, Boolean>> checkCurrentUserHasSitePermissions(@ValidSiteId @PathVariable(REQUEST_PARAM_SITE) String site,
                                                                              @Valid @RequestBody HasPermissionsRequest permissionsRequest)
            throws ServiceLayerException, UserNotFoundException, ExecutionException {
        Map<String, Boolean> hasPermissions =
                userService.hasCurrentUserSitePermissions(site, permissionsRequest.getPermissions());

        ResultOne<Map<String, Boolean>> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_PERMISSIONS, hasPermissions);
        return result;
    }

    /**
     * Get the global permissions of the current authenticated user API
     *
     * @return Response containing current authenticated user global permissions
     */
    @GetMapping(value = ME + GLOBAL + PERMISSIONS, produces = APPLICATION_JSON_VALUE)
    public ResultList<String> getCurrentUserGlobalPermissions()
            throws ServiceLayerException, UserNotFoundException, ExecutionException {
        List<String> permissions = userService.getCurrentUserGlobalPermissions();

        ResultList<String> result = new ResultList<>();
        result.setResponse(OK);
        result.setEntities(RESULT_KEY_PERMISSIONS, permissions);

        return result;
    }

    /**
     * Check if the current authenticated user has global permissions
     *
     * @return Response containing current authenticated user roles
     */
    @PostMapping(value = ME + GLOBAL + HAS_PERMISSIONS, consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResultOne<Map<String, Boolean>> checkCurrentUserHasGlobalPermissions(@Valid @RequestBody HasPermissionsRequest permissionsRequest)
            throws ServiceLayerException, UserNotFoundException, ExecutionException {
        Map<String, Boolean> hasPermissions =
                userService.hasCurrentUserGlobalPermissions(permissionsRequest.getPermissions());

        ResultOne<Map<String, Boolean>> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_PERMISSIONS, hasPermissions);
        return result;
    }
}
