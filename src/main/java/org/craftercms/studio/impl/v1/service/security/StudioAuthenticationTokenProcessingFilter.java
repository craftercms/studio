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

package org.craftercms.studio.impl.v1.service.security;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.security.UserDetailsManager;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.GenericFilterBean;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.craftercms.studio.api.v1.service.security.SecurityService.STUDIO_SESSION_TOKEN_ATRIBUTE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.*;

public class StudioAuthenticationTokenProcessingFilter extends GenericFilterBean {

    private final static Logger crafterLogger = LoggerFactory.getLogger(StudioAuthenticationTokenProcessingFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = this.getAsHttpRequest(servletRequest);
        HttpSession httpSession = httpRequest.getSession();
        synchronized (httpSession) {
            String userName = securityService.getCurrentUser();
            String authToken = securityService.getCurrentToken();

            if (userName != null) {

                UserDetails userDetails = this.userDetailsManager.loadUserByUsername(userName);

                if (TokenUtils.validateToken(authToken, userDetails)) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    if (httpRequest.getRequestURI().startsWith(httpRequest.getContextPath() + "/api/1") && !httpRequest.getRequestURI().contains("/validate-session.json")) {
                        int timeout = Integer.parseInt(studioConfiguration.getProperty(SECURITY_SESSION_TIMEOUT));
                        long ttl = 1000L * 60 * timeout;
                        String newToken = TokenUtils.createToken(userDetails, ttl);
                        httpSession.setAttribute(STUDIO_SESSION_TOKEN_ATRIBUTE, newToken);
                    }
                }
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private HttpServletRequest getAsHttpRequest(ServletRequest request)
    {
        if (!(request instanceof HttpServletRequest)) {
            throw new RuntimeException("Expecting an HTTP request");
        }

        return (HttpServletRequest) request;
    }



    private UserDetailsManager userDetailsManager;
    private SecurityService securityService;
    private StudioConfiguration studioConfiguration;

    public UserDetailsManager getUserDetailsManager() { return userDetailsManager; }
    public void setUserDetailsManager(UserDetailsManager userDetailsManager) { this.userDetailsManager = userDetailsManager; }

    public SecurityService getSecurityService() { return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }
}
