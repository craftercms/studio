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

import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Extension of {@link LogoutFilter} that integrates the {@link SecurityService}.
 * This class is required because the session is no longer available when the logout handlers are executed.
 *
 * @author joseross
 * @since 3.1.11
 */
public class StudioLogoutFilter extends LogoutFilter {

    protected SecurityService securityService;

    public StudioLogoutFilter(LogoutSuccessHandler logoutSuccessHandler, SecurityService securityService,
                              LogoutHandler... handlers) {
        super(logoutSuccessHandler, handlers);
        this.securityService = securityService;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        if (requiresLogout((HttpServletRequest) req, (HttpServletResponse) res)) {
            try {
                securityService.logout();
            } catch (SiteNotFoundException e) {
                throw new ServletException("Error performing logout", e);
            }
            super.doFilter(req, res, chain);
        } else {
            chain.doFilter(req, res);
        }

    }

}
