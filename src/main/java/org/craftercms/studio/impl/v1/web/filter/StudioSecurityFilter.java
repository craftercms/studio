/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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

package org.craftercms.studio.impl.v1.web.filter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.craftercms.commons.http.HttpUtils;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.impl.v1.util.SessionTokenUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class StudioSecurityFilter extends GenericFilterBean {

    private final static String STUDIO_SESSION_TOKEN_ATRIBUTE = "studioSessionToken";

    public StudioSecurityFilter() {
        pathMatcher = new AntPathMatcher();
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpReponse = (HttpServletResponse) response;

        if (ArrayUtils.contains(exceptionUrls, HttpUtils.getRequestUriWithoutContextPath(httpRequest))) {
            chain.doFilter(request, response);
        } else {
            if (!checkSessionTimeout(httpRequest, httpReponse)) {
                if ((includeRequest(httpRequest) || !excludeRequest(httpRequest))) {
                    doFilterInternal(httpRequest, httpReponse, chain);
                } else {
                    chain.doFilter(request, response);
                }
            } else {
                securityProvider.logout();
                httpReponse.sendRedirect(httpRequest.getContextPath() + "/");
            }
        }

    }

    @Override
    public void destroy() {
        // do nothing
    }

    protected boolean checkSessionTimeout(HttpServletRequest request, HttpServletResponse response) {
        if (request.getRequestURI().contains("/validate-token.json")) return false;
        HttpSession httpSession = request.getSession();
        String sessionToken = (String)httpSession.getAttribute(STUDIO_SESSION_TOKEN_ATRIBUTE);
        String user = securityProvider.getCurrentUser();
        if (StringUtils.isEmpty(sessionToken) || StringUtils.isEmpty(user)) {
            return false;
        }
        if (StringUtils.isNotEmpty(sessionToken) && StringUtils.isNotEmpty(user)) {
            if (SessionTokenUtils.validateToken(sessionToken, user)) {
                sessionToken = SessionTokenUtils.createToken(user, sessionTimeout);
                httpSession.setAttribute(STUDIO_SESSION_TOKEN_ATRIBUTE, sessionToken);
                return false;
            }
        }
        return true;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        try {
            String token = securityProvider.getCurrentToken();
            String user = securityProvider.getCurrentUser();
            if (StringUtils.isEmpty(user) || StringUtils.isEmpty(token) || StringUtils.equals(token, "NOTICKET")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } else {
                chain.doFilter(request, response);
            }
        } catch (IOException | ServletException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    /**
     * Returns true if the request should be excluded from processing.
     */
    protected boolean excludeRequest(HttpServletRequest request) {
        if (ArrayUtils.isNotEmpty(urlsToExclude)) {
            for (String pathPattern : urlsToExclude) {
                if (pathMatcher.match(pathPattern, HttpUtils.getRequestUriWithoutContextPath(request))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns true if the request should be included for processing.
     */
    protected boolean includeRequest(HttpServletRequest request) {
        if (ArrayUtils.isNotEmpty(urlsToInclude)) {
            for (String pathPattern : urlsToInclude) {
                if (pathMatcher.match(pathPattern, HttpUtils.getRequestUriWithoutContextPath(request))) {
                    return true;
                }
            }
        }

        return false;
    }

    public String[] getUrlsToInclude() { return urlsToInclude; }
    public void setUrlsToInclude(String[] urlsToInclude) { this.urlsToInclude = urlsToInclude; }

    public String[] getUrlsToExclude() { return urlsToExclude; }
    public void setUrlsToExclude(String[] urlsToExclude) { this.urlsToExclude = urlsToExclude; }

    public String[] getExceptionUrls() { return exceptionUrls; }
    public void setExceptionUrls(String[] exceptionUrls) { this.exceptionUrls = exceptionUrls; }

    public SecurityProvider getSecurityProvider() { return securityProvider; }
    public void setSecurityProvider(SecurityProvider securityProvider) { this.securityProvider = securityProvider; }

    public int getSessionTimeout() { return sessionTimeout; }
    public void setSessionTimeout(int sessionTimeout) { this.sessionTimeout = sessionTimeout; }

    public SecurityService getSecurityService() { return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    protected String[] urlsToInclude;
    protected String[] urlsToExclude;
    protected String[] exceptionUrls;
    protected SecurityProvider securityProvider;
    protected SecurityService securityService;
    protected int sessionTimeout;

    protected PathMatcher pathMatcher;
}
