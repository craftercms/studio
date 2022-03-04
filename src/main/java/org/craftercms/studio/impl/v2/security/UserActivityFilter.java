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
package org.craftercms.studio.impl.v2.security;

import org.craftercms.studio.api.v2.service.security.AccessTokenService;
import org.craftercms.studio.impl.v2.utils.spring.security.OrRegexRequestMatcher;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Security filter that tracks the user activity in the API
 *
 * @author joseross
 * @since 4.0.0
 */
public class UserActivityFilter extends OncePerRequestFilter {

    /**
     * The request matcher user to exclude URLs
     */
    protected RequestMatcher excludeRequestMatcher;

    /**
     * The access token service
     */
    protected AccessTokenService accessTokenService;

    public UserActivityFilter(AccessTokenService accessTokenService, String... excludedUrls) {
        this.excludeRequestMatcher = new OrRegexRequestMatcher(excludedUrls);
        this.accessTokenService = accessTokenService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return excludeRequestMatcher.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            Authentication authentication = context.getAuthentication();
            if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
                accessTokenService.updateUserActivity(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

}
