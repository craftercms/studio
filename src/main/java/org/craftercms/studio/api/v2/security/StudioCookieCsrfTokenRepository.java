/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.util.StringUtils;

import java.beans.ConstructorProperties;
import java.util.function.Consumer;

/**
 * TODO: Review this implementation if Spring Security resolves the issue.
 * <br>
 * Overrides {@link CookieCsrfTokenRepository} to only set the `secure` and `httpOnly` flags
 * if their values are explicitly `true`. This is necessary due to a change introduced
 * in Tomcat 11 (<a href="https://tomcat.apache.org/tomcat-11.0-doc/changelog.html">changelog</a>):
 * <p>
 * "Refactor the internal representation of the HttpOnly and Secure attributes to use
 * the empty string as the value for consistency with recent changes to Set-Cookie
 * header generation."
 * </p>
 */
public class StudioCookieCsrfTokenRepository implements CsrfTokenRepository {
    /**
     * private properties from {@link CookieCsrfTokenRepository}
     */
    static final String DEFAULT_CSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final String CSRF_TOKEN_REMOVED_ATTRIBUTE_NAME = CookieCsrfTokenRepository.class.getName()
            .concat(".REMOVED");
    private String cookieName = DEFAULT_CSRF_COOKIE_NAME;
    private final boolean cookieHttpOnly;
    private String cookiePath;
    private String cookieDomain;
    private Boolean secure;
    private int cookieMaxAge = -1;
    private Consumer<ResponseCookie.ResponseCookieBuilder> cookieCustomizer = (builder) -> {
    };

    /**
     * Delegate instance of {@link CookieCsrfTokenRepository} to minimize the amount of overridden functionality.
     */
    private final CookieCsrfTokenRepository delegate = new CookieCsrfTokenRepository();

    @ConstructorProperties({"cookieHttpOnly"})
    public StudioCookieCsrfTokenRepository(final boolean cookieHttpOnly) {
        this.cookieHttpOnly = cookieHttpOnly;
        delegate.setCookieHttpOnly(cookieHttpOnly);
    }

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        return delegate.generateToken(request);
    }

    /**
     * Override from {@link CookieCsrfTokenRepository} how to add to response with a customized `mapToCookie` function
     */
    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        String tokenValue = (token != null) ? token.getToken() : "";
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(this.cookieName, tokenValue)
                .secure((this.secure != null) ? this.secure : request.isSecure())
                .path(StringUtils.hasLength(this.cookiePath) ? this.cookiePath : this.getRequestContext(request))
                .maxAge((token != null) ? this.cookieMaxAge : 0)
                .httpOnly(this.cookieHttpOnly)
                .domain(this.cookieDomain);

        this.cookieCustomizer.accept(cookieBuilder);

        Cookie cookie = mapToCookie(cookieBuilder.build());
        response.addCookie(cookie);

        // Set request attribute to signal that response has blank cookie value,
        // which allows loadToken to return null when token has been removed
        if (!StringUtils.hasLength(tokenValue)) {
            request.setAttribute(CSRF_TOKEN_REMOVED_ATTRIBUTE_NAME, Boolean.TRUE);
        } else {
            request.removeAttribute(CSRF_TOKEN_REMOVED_ATTRIBUTE_NAME);
        }
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        return delegate.loadToken(request);
    }

    private String getRequestContext(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return (!contextPath.isEmpty()) ? contextPath : "/";
    }

    /**
     * Override from {@link CookieCsrfTokenRepository} to only set secure; httpOnly flag if the values are `true`
     * @param responseCookie response cookie
     * @return cookie
     */
    private Cookie mapToCookie(ResponseCookie responseCookie) {
        Cookie cookie = new Cookie(responseCookie.getName(), responseCookie.getValue());
        if (responseCookie.isSecure()) {
            cookie.setSecure(true);
        }
        cookie.setPath(responseCookie.getPath());
        cookie.setMaxAge((int) responseCookie.getMaxAge().getSeconds());
        if (responseCookie.isHttpOnly()) {
            cookie.setHttpOnly(true);
        }
        if (StringUtils.hasLength(responseCookie.getDomain())) {
            cookie.setDomain(responseCookie.getDomain());
        }
        if (StringUtils.hasText(responseCookie.getSameSite())) {
            cookie.setAttribute("SameSite", responseCookie.getSameSite());
        }
        return cookie;
    }

    public String getCookiePath() {
        return this.cookiePath;
    }

    public void setCookiePath(String path) {
        this.delegate.setCookiePath(path);
        this.cookiePath = path;
    }

    public void setCookieDomain(String cookieDomain) {
        this.delegate.setCookieDomain(cookieDomain);
        this.cookieDomain = cookieDomain;
    }

    public void setSecure(Boolean secure) {
        this.delegate.setSecure(secure);
        this.secure = secure;
    }

    public void setCookieMaxAge(int cookieMaxAge) {
        this.delegate.setCookieMaxAge(cookieMaxAge);
        this.cookieMaxAge = cookieMaxAge;
    }

    public void setCookieCustomizer(Consumer<ResponseCookie.ResponseCookieBuilder> cookieCustomizer) {
        this.delegate.setCookieCustomizer(cookieCustomizer);
        this.cookieCustomizer = cookieCustomizer;
    }

    public void setHeaderName(String headerName) {
        this.delegate.setHeaderName(headerName);
    }

    public void setCookieName(String cookieName) {
        this.delegate.setCookieName(cookieName);
        this.cookieName = cookieName;
    }

    public void setParameterName(String parameterName) {
        this.delegate.setParameterName(parameterName);
    }
}
