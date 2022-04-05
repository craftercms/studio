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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.craftercms.commons.exceptions.InvalidManagementTokenException;
import org.craftercms.commons.http.HttpUtils;
import org.craftercms.commons.security.exception.ActionDeniedException;
import org.craftercms.commons.validation.ValidationRuntimeException;
import org.craftercms.core.exception.PathNotFoundException;
import org.craftercms.studio.api.v1.exception.CmisPathNotFoundException;
import org.craftercms.studio.api.v1.exception.CmisRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.CmisTimeoutException;
import org.craftercms.studio.api.v1.exception.CmisUnavailableException;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.StudioPathNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.repository.RemoteNotRemovableException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.GroupNotFoundException;
import org.craftercms.studio.api.v1.exception.security.PasswordDoesNotMatchException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.ClusterMemberAlreadyExistsException;
import org.craftercms.studio.api.v2.exception.ClusterMemberNotFoundException;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.exception.MissingPluginParameterException;
import org.craftercms.studio.api.v2.exception.OrganizationNotFoundException;
import org.craftercms.studio.api.v2.exception.PublishingPackageNotFoundException;
import org.craftercms.studio.api.v2.exception.PullFromRemoteConflictException;
import org.craftercms.studio.api.v2.exception.PasswordRequirementsFailedException;
import org.craftercms.studio.api.v2.exception.configuration.InvalidConfigurationException;
import org.craftercms.studio.api.v2.exception.content.ContentAlreadyUnlockedException;
import org.craftercms.studio.api.v2.exception.content.ContentLockedByAnotherUserException;
import org.craftercms.studio.api.v2.exception.marketplace.MarketplaceNotInitializedException;
import org.craftercms.studio.api.v2.exception.marketplace.MarketplaceUnreachableException;
import org.craftercms.studio.api.v2.exception.marketplace.PluginAlreadyInstalledException;
import org.craftercms.studio.api.v2.exception.marketplace.PluginInstallationException;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultOne;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;

import java.util.NoSuchElementException;

import static org.craftercms.studio.api.v1.log.Logger.LEVEL_DEBUG;
import static org.craftercms.studio.api.v1.log.Logger.LEVEL_ERROR;
import static org.craftercms.studio.api.v1.log.Logger.LEVEL_INFO;
import static org.craftercms.studio.api.v1.log.Logger.LEVEL_OFF;
import static org.craftercms.studio.api.v1.log.Logger.LEVEL_WARN;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_PERSON;

/**
 * Controller advice that handles exceptions thrown by API 2 REST controllers.
 *
 * @author avasquez
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice("org.craftercms.studio.controller.rest.v2")
public class ExceptionHandlers {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlers.class);

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseBody handleAuthenticationException(HttpServletRequest request, AuthenticationException e) {
        return handleExceptionInternal(request, e, ApiResponse.UNAUTHENTICATED);
    }

    @ExceptionHandler(ActionDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseBody handleActionDeniedException(HttpServletRequest request, ActionDeniedException e) {
        return handleExceptionInternal(request, e, ApiResponse.UNAUTHORIZED);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseBody handleUserAlreadyExistsException(HttpServletRequest request, UserAlreadyExistsException e) {
        ApiResponse response = new ApiResponse(ApiResponse.USER_ALREADY_EXISTS);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseBody handleUserNotFoundException(HttpServletRequest request, UserNotFoundException e) {
        ApiResponse response = new ApiResponse(ApiResponse.USER_NOT_FOUND);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseBody handleNoSuchElementException(HttpServletRequest request, NoSuchElementException e) {
        ApiResponse response = new ApiResponse(ApiResponse.CONTENT_NOT_FOUND);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(GroupAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseBody handleGroupAlreadyExistsException(HttpServletRequest request, GroupAlreadyExistsException e) {
        ApiResponse response = new ApiResponse(ApiResponse.GROUP_ALREADY_EXISTS);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(InvalidParametersException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseBody handleInvalidParametersException(HttpServletRequest request, InvalidParametersException e) {
        ApiResponse response = new ApiResponse(ApiResponse.INVALID_PARAMS);
        response.setMessage(response.getMessage() + " : " + e.getMessage());
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(MarketplaceNotInitializedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseBody handleMarketplaceNotInitializedException(HttpServletRequest request,
                                                                 MarketplaceNotInitializedException e) {
        ApiResponse response = new ApiResponse(ApiResponse.MARKETPLACE_NOT_INITIALIZED);
        response.setMessage(response.getMessage() + ": "+ e.getMessage());

        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(MarketplaceUnreachableException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseBody handleMarketplaceUnreachableException(HttpServletRequest request,
                                                              MarketplaceUnreachableException e) {
        ApiResponse response = new ApiResponse(ApiResponse.MARKETPLACE_UNREACHABLE);
        response.setMessage(response.getMessage() + ": "+ e.getMessage());

        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(PluginAlreadyInstalledException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseBody handlePluginAlreadyInstalledException(HttpServletRequest request,
                                                              PluginAlreadyInstalledException e) {
        ApiResponse response = new ApiResponse(ApiResponse.PLUGIN_ALREADY_INSTALLED);
        response.setMessage(response.getMessage() + ": "+ e.getMessage());

        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(MissingPluginParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseBody handleMissingPluginParameterException(HttpServletRequest request,
                                                              MissingPluginParameterException e) {
        ApiResponse response = new ApiResponse(ApiResponse.PLUGIN_INSTALLATION_ERROR);
        response.setMessage(response.getMessage() + ": "+ e.getMessage());

        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(PluginInstallationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseBody handlePluginInstallationException(HttpServletRequest request,
                                                              PluginInstallationException e) {
        ApiResponse response = new ApiResponse(ApiResponse.PLUGIN_INSTALLATION_ERROR);
        response.setMessage(response.getMessage() + ": "+ e.getMessage());

        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(ServiceLayerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseBody handleServiceException(HttpServletRequest request, ServiceLayerException e) {
        ApiResponse response = new ApiResponse(ApiResponse.INTERNAL_SYSTEM_FAILURE);
        response.setMessage(response.getMessage() + ": "+ e.getMessage());

        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(OrganizationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseBody handleOrganizationNotFoundException(HttpServletRequest request,
                                                            OrganizationNotFoundException e) {
        ApiResponse response = new ApiResponse(ApiResponse.ORG_NOT_FOUND);
        response.setMessage(response.getMessage() + ": " + e.getMessage());
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(GroupNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseBody handleGroupNotFoundException(HttpServletRequest request, GroupNotFoundException e) {
        ApiResponse response = new ApiResponse(ApiResponse.GROUP_NOT_FOUND);
        return handleExceptionInternal(request, e, response);
    }


    @ExceptionHandler(JsonProcessingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseBody handleUnrecognizedPropertyException(HttpServletRequest request, JsonProcessingException e) {
        ApiResponse response = new ApiResponse(ApiResponse.INVALID_PARAMS);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(ClusterMemberNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseBody handleClusterMemberNotFoundException(HttpServletRequest request,
                                                      ClusterMemberNotFoundException e) {
        ApiResponse response = new ApiResponse(ApiResponse.CLUSTER_MEMBER_NOT_FOUND);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(ClusterMemberAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseBody handleClusterMemberAlreadyExistsException(HttpServletRequest request,
                                                                  ClusterMemberAlreadyExistsException e) {
        ApiResponse response = new ApiResponse(ApiResponse.CLUSTER_MEMBER_ALREADY_EXISTS);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(SiteAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseBody handleSiteAlreadyExistsException(HttpServletRequest request, SiteAlreadyExistsException e) {
        ApiResponse response = new ApiResponse(ApiResponse.SITE_ALREADY_EXISTS);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(SiteNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseBody handleSiteNotFoundException(HttpServletRequest request, SiteNotFoundException e) {
        ApiResponse response = new ApiResponse(ApiResponse.SITE_NOT_FOUND);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(RemoteAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseBody handleRemoteAlreadyExistsException(HttpServletRequest request, RemoteAlreadyExistsException e) {
        ApiResponse response = new ApiResponse(ApiResponse.REMOTE_REPOSITORY_ALREADY_EXISTS);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(InvalidRemoteUrlException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseBody handleInvalidRemoteUrlException(HttpServletRequest request, InvalidRemoteUrlException e) {
        ApiResponse response = new ApiResponse(ApiResponse.INVALID_PARAMS);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(CmisRepositoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseBody handleCmisRepositoryNotFoundException(HttpServletRequest request,
                                                              CmisRepositoryNotFoundException e) {
        ApiResponse response = new ApiResponse(ApiResponse.CMIS_NOT_FOUND);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(CmisTimeoutException.class)
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    public ResponseBody handleCmisTimeoutException(HttpServletRequest request, CmisTimeoutException e) {
        ApiResponse response = new ApiResponse(ApiResponse.CMIS_TIMEOUT);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(CmisUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseBody handleCmisUnavailableException(HttpServletRequest request, CmisUnavailableException e) {
        ApiResponse response = new ApiResponse(ApiResponse.CMIS_UNREACHABLE);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(StudioPathNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseBody handleStudioPathNotFoundException(HttpServletRequest request, StudioPathNotFoundException e) {
        ApiResponse response = new ApiResponse(ApiResponse.CMIS_STUDIO_PATH_NOT_FOUND);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(CmisPathNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseBody handleCmisPathNotFoundException(HttpServletRequest request, CmisPathNotFoundException e) {
        ApiResponse response = new ApiResponse(ApiResponse.CONTENT_NOT_FOUND);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(PasswordRequirementsFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseBody handlePasswordRequirementsFailedException(HttpServletRequest request,
                                                                  PasswordRequirementsFailedException e) {
        ApiResponse response = new ApiResponse(ApiResponse.USER_PASSWORD_REQUIREMENTS_FAILED);
        return handleExceptionInternal(request, e, response, LEVEL_DEBUG);
    }

    @ExceptionHandler(PasswordDoesNotMatchException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseBody handlePasswordDoesNotMatchException(HttpServletRequest request,
                                                            PasswordDoesNotMatchException e) {
        ApiResponse response = new ApiResponse(ApiResponse.USER_PASSWORD_DOES_NOT_MATCH);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(PullFromRemoteConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseBody handlePullFromRemoteConflictException(HttpServletRequest request,
                                                              PullFromRemoteConflictException e) {
        ApiResponse response = new ApiResponse(ApiResponse.PULL_FROM_REMOTE_REPOSITORY_CONFLICT);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(ContentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseBody handleContentNotFoundException(HttpServletRequest request, ContentNotFoundException e) {
        ApiResponse response = new ApiResponse(ApiResponse.CONTENT_NOT_FOUND);
        response.setRemedialAction(
            String.format("Check that path '%s' is correct and it exists in site '%s'", e.getPath(), e.getSite()));
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(PublishingPackageNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseBody handlePublishingPackageNotFoundException(HttpServletRequest request,
                                                       PublishingPackageNotFoundException e) {
        ApiResponse response = new ApiResponse(ApiResponse.CONTENT_NOT_FOUND);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseBody handleMissingServletRequestParameterException(HttpServletRequest request,
                                                                      MissingServletRequestParameterException e) {
        ApiResponse response = new ApiResponse(ApiResponse.INVALID_PARAMS);
        response.setRemedialAction(
            String.format("Add missing parameter '%s' of type '%s'", e.getParameterName(), e.getParameterType()));
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseBody handleMissingServletRequestParameterException(HttpServletRequest request,
                                                                      MethodArgumentNotValidException e) {
        ApiResponse response = new ApiResponse(ApiResponse.INVALID_PARAMS);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseBody handleHttpMessageNotReadableException(HttpServletRequest request,
                                                              HttpMessageNotReadableException e) {
        ApiResponse response = new ApiResponse(ApiResponse.INVALID_PARAMS);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseBody handleMethodArgumentTypeMismatchException(HttpServletRequest request,
                                                                  MethodArgumentTypeMismatchException e) {
        ApiResponse response = new ApiResponse(ApiResponse.INVALID_PARAMS);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(InvalidManagementTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseBody handleInvalidManagementTokenException(HttpServletRequest request,
                                                              InvalidManagementTokenException e) {
        ApiResponse response = new ApiResponse(ApiResponse.UNAUTHORIZED);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseBody handleBeanPropertyBindingResult(HttpServletRequest request, BindException e) {
        ApiResponse response = new ApiResponse(ApiResponse.INVALID_PARAMS);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(RemoteNotRemovableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseBody handleRemoteNotRemovableException(HttpServletRequest request, RemoteNotRemovableException e) {
        ApiResponse response = new ApiResponse(ApiResponse.REMOTE_REPOSITORY_NOT_REMOVABLE);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(PathNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseBody handleException(HttpServletRequest request, PathNotFoundException e) {
        ApiResponse response = new ApiResponse(ApiResponse.CONTENT_NOT_FOUND);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(InvalidConfigurationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseBody handleInvalidConfigurationException(HttpServletRequest request,
                                                            InvalidConfigurationException e) {
        ApiResponse response = new ApiResponse(ApiResponse.INVALID_PARAMS);
        response.setMessage(response.getMessage() + ": " + e.getMessage());
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(ValidationRuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseBody handleValidationRuntimeException(HttpServletRequest request,
                                                         ValidationRuntimeException e) {
        ApiResponse response = new ApiResponse(ApiResponse.INVALID_PARAMS);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(InvalidRemoteRepositoryCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseBody handleInvalidRemoteRepositoryCredentialsException(HttpServletRequest request,
            InvalidRemoteRepositoryCredentialsException e) {
        ApiResponse response = new ApiResponse(ApiResponse.REMOTE_REPOSITORY_AUTHENTICATION_FAILED);
        response.setMessage(response.getMessage() + ": " + e.getMessage());
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(RemoteRepositoryNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseBody handleRemoteRepositoryNotFoundException(HttpServletRequest request,
            RemoteRepositoryNotFoundException e) {
        ApiResponse response = new ApiResponse(ApiResponse.REMOTE_REPOSITORY_NOT_FOUND);
        response.setMessage(response.getMessage() + ": " + e.getMessage());
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(ContentLockedByAnotherUserException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseBody handleException(HttpServletRequest request, ContentLockedByAnotherUserException e) {
        var response = new ApiResponse(ApiResponse.CONTENT_ALREADY_LOCKED);
        handleExceptionInternal(request, e, response);
        var result = new ResultOne<String>();
        result.setResponse(response);
        result.setEntity(RESULT_KEY_PERSON, e.getLockOwner());
        var responseBody = new ResponseBody();
        responseBody.setResult(result);
        return responseBody;
    }

    @ExceptionHandler(ContentAlreadyUnlockedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseBody handleException(HttpServletRequest request, ContentAlreadyUnlockedException e) {
        ApiResponse response = new ApiResponse(ApiResponse.CONTENT_ALREADY_UNLOCKED);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseBody handleException(HttpServletRequest request, Exception e) {
        ApiResponse response = new ApiResponse(ApiResponse.INTERNAL_SYSTEM_FAILURE);
        return handleExceptionInternal(request, e, response);
    }

    protected ResponseBody handleExceptionInternal(HttpServletRequest request, Exception e, ApiResponse response) {
        return handleExceptionInternal(request, e, response, LEVEL_ERROR);
    }

    protected ResponseBody handleExceptionInternal(HttpServletRequest request, Exception e, ApiResponse response,
                                                   String logLevel) {
        switch (logLevel) {
            case LEVEL_OFF:
                break;
            case LEVEL_DEBUG:
                logger.debug("API endpoint " + HttpUtils.getFullRequestUri(request, true) +
                        " failed with response: " + response);
                break;
            case LEVEL_WARN:
                logger.warn("API endpoint " + HttpUtils.getFullRequestUri(request, true) +
                        " failed with response: " + response);
                break;
            case LEVEL_INFO:
                logger.info("API endpoint " + HttpUtils.getFullRequestUri(request, true) +
                        " failed with response: " + response);
                break;
            case LEVEL_ERROR:
                logger.error("API endpoint " + HttpUtils.getFullRequestUri(request, true) +
                        " failed with response: " + response, e);
                break;
            default:
                break;
        }

        Result result = new Result();
        result.setResponse(response);

        ResponseBody responseBody = new ResponseBody();
        responseBody.setResult(result);

        return responseBody;
    }
}
