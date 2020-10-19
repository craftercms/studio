/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v1.web.security.access;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_PUBLIC_URLS;

public class StudioLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private static final String PARAM_REDIRECT = "redirect";
    private static final Logger logger = LoggerFactory.getLogger(StudioLoginUrlAuthenticationEntryPoint.class);

    private StudioConfiguration studioConfiguration;

    public StudioLoginUrlAuthenticationEntryPoint(String loginFormUrl, StudioConfiguration studioConfiguration) {
        super(loginFormUrl);
        this.studioConfiguration = studioConfiguration;
    }

    /**
     * @param loginFormUrl URL where the login page can be found. Should either be
     *                     relative to the web-app context path (include a leading {@code /}) or an absolute
     *                     URL.
     */
    public StudioLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response,
                                                     AuthenticationException exception) {

        String redirectParamValue = request.getContextPath() + UrlUtils.buildRequestUrl(request);
        redirectParamValue = UriUtils.encode(redirectParamValue, StandardCharsets.UTF_8.toString());

        String redirect = super.determineUrlToUseForThisRequest(request, response, exception);
        return UriComponentsBuilder.fromPath(redirect).queryParam(PARAM_REDIRECT, redirectParamValue).toUriString();
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {
        String requestUrl = UrlUtils.buildRequestUrl(request);
        if (StringUtils.startsWith(requestUrl, "/api/") && !StringUtils.equalsAny(requestUrl, getPublicUrls())) {
            // This is invoked when user tries to access a secured REST resource without supplying any credentials
            // We should just send a 401 Unauthorized response because there is no 'login page' to redirect to
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        } else {
            super.commence(request, response, authException);
        }
    }

    private String[] getPublicUrls() {
        StringTokenizer st = new StringTokenizer(studioConfiguration.getProperty(SECURITY_PUBLIC_URLS), ",");
        String[] publicUrls = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            publicUrls[i++] = st.nextToken().trim();
        }
        return publicUrls;
    }
}
