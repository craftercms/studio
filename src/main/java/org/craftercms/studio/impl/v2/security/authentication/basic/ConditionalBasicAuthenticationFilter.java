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
package org.craftercms.studio.impl.v2.security.authentication.basic;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.ConstructorProperties;
import java.io.IOException;

/**
 *  Extension of {@link BasicAuthenticationFilter} that can be disabled
 *
 * @author joseross
 * @since 4.0
 */
public class ConditionalBasicAuthenticationFilter extends BasicAuthenticationFilter {

    protected boolean enabled;

    @ConstructorProperties({"authenticationManager", "enabled"})
    public ConditionalBasicAuthenticationFilter(AuthenticationManager authenticationManager, boolean enabled) {
        super(authenticationManager);
        this.enabled = enabled;
    }

    @ConstructorProperties({"authenticationManager", "authenticationEntryPoint", "enabled"})
    public ConditionalBasicAuthenticationFilter(AuthenticationManager authenticationManager,
                                                AuthenticationEntryPoint authenticationEntryPoint, boolean enabled) {
        super(authenticationManager, authenticationEntryPoint);
        this.enabled = enabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (enabled) {
            super.doFilterInternal(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

}
