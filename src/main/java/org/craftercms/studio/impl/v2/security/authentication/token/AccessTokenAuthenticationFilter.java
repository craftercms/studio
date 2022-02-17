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
package org.craftercms.studio.impl.v2.security.authentication.token;

import org.craftercms.studio.api.v2.service.security.AccessTokenService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import javax.servlet.http.HttpServletRequest;

import java.beans.ConstructorProperties;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.removeStartIgnoreCase;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

/**
 * Implementation of {@link AbstractPreAuthenticatedProcessingFilter} to support token authentication
 *
 * @author joseross
 * @since 4.0
 */
public class AccessTokenAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

    public static final String TOKEN_PREFIX = "Bearer ";

    protected AccessTokenService accessTokenService;

    @ConstructorProperties({"accessTokenService"})
    public AccessTokenAuthenticationFilter(AccessTokenService accessTokenService) {
        this.accessTokenService = accessTokenService;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        // Check if the Authentication header is present
        var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (isEmpty(authHeader)) {
            return null;
        }
        // Check if the header has the right prefix
        if (!startsWithIgnoreCase(authHeader, TOKEN_PREFIX)) {
            return null;
        }
        var token = removeStartIgnoreCase(authHeader, TOKEN_PREFIX);
        // Check if the token is valid
        return accessTokenService.getUsername(token);
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

}
