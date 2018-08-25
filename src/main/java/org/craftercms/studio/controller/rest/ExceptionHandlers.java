package org.craftercms.studio.controller.rest;

import org.craftercms.commons.http.HttpUtils;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.model.ApiResponse;
import org.craftercms.studio.model.rest.ApiResponseBody;
import org.craftercms.studio.model.rest.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ExceptionHandlers {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlers.class);

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponseBody handleAuthenticationException(HttpServletRequest request, ServiceException e) {
        return handleExceptionInternal(request, e, ApiResponse.UNAUTHENTICATED);
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponseBody handleServiceException(HttpServletRequest request, ServiceException e) {
        ApiResponse response = new ApiResponse(ApiResponse.INTERNAL_SYSTEM_FAILURE);
        response.setMessage(response.getMessage() + ": "+ e.getMessage());

        return handleExceptionInternal(request, e, response);
    }

    protected ApiResponseBody handleExceptionInternal(HttpServletRequest request, Exception e, ApiResponse response) {
        logger.error("API endpoint " + HttpUtils.getFullRequestUri(request, true) + " failed with response: " +
                     response, e);

        Result result = new Result();
        result.setResponse(response);

        ApiResponseBody responseBody = new ApiResponseBody();
        responseBody.setResult(result);

        return responseBody;
    }

}
