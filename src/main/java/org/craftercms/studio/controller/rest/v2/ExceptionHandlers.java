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

import org.craftercms.commons.http.HttpUtils;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller advice that handles exceptions thrown by API 2 REST controllers.
 *
 * @author avasquez
 */
@RestControllerAdvice("org.craftercms.studio.controller.rest.v2")
public class ExceptionHandlers {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlers.class);

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseBody handleAuthenticationException(HttpServletRequest request, ServiceException e) {
        return handleExceptionInternal(request, e, ApiResponse.UNAUTHENTICATED);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseBody handleUserAlreadyExistsException(HttpServletRequest request, ServiceException e) {
        ApiResponse response = new ApiResponse(ApiResponse.USER_ALREADY_EXISTS);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(GroupAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseBody handleGroupAlreadyExistsException(HttpServletRequest request, ServiceException e) {
        ApiResponse response = new ApiResponse(ApiResponse.GROUP_ALREADY_EXISTS);
        return handleExceptionInternal(request, e, response);
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseBody handleServiceException(HttpServletRequest request, ServiceException e) {
        ApiResponse response = new ApiResponse(ApiResponse.INTERNAL_SYSTEM_FAILURE);
        response.setMessage(response.getMessage() + ": "+ e.getMessage());

        return handleExceptionInternal(request, e, response);
    }

    protected ResponseBody handleExceptionInternal(HttpServletRequest request, Exception e, ApiResponse response) {
        logger.error("API endpoint " + HttpUtils.getFullRequestUri(request, true) + " failed with response: " +
                     response, e);

        Result result = new Result();
        result.setResponse(response);

        ResponseBody responseBody = new ResponseBody();
        responseBody.setResult(result);

        return responseBody;
    }

}
