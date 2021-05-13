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
package org.craftercms.studio.impl.v2.security.authentication.headers;

import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.beans.ConstructorProperties;
import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_ENABLED;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE_HEADERS;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_USERNAME_HEADER;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_AUTHENTICATION_CHAIN_CONFIG;

/**
 * Extension of {@link RequestHeaderAuthenticationFilter} that supports Studio's authentication chain configuration
 *
 * @author joseross
 * @since 4.0
 */
public class HeadersAuthenticationFilter extends RequestHeaderAuthenticationFilter {

    protected boolean enabled = false;

    @ConstructorProperties({"studioConfiguration"})
    public HeadersAuthenticationFilter(StudioConfiguration studioConfiguration) {
        var chainConfig = studioConfiguration.getSubConfigs(CONFIGURATION_AUTHENTICATION_CHAIN_CONFIG);
        chainConfig.stream()
                // if there is a headers provider in the chain
                .filter(config -> equalsIgnoreCase(config.getString(AUTHENTICATION_CHAIN_PROVIDER_TYPE),
                        AUTHENTICATION_CHAIN_PROVIDER_TYPE_HEADERS))
                // and it is enabled
                .filter(config -> config.getBoolean(AUTHENTICATION_CHAIN_PROVIDER_ENABLED))
                .findFirst()
                // enable this filter and check for the header name given in the config
                .ifPresent(config -> {
                    enabled = true;
                    setPrincipalRequestHeader(config.getString(AUTHENTICATION_CHAIN_PROVIDER_USERNAME_HEADER));
                });
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // If the headers auth is enabled
        if (enabled) {
            try {
                // try to authenticate the request
                super.doFilter(request, response, chain);
            } catch (PreAuthenticatedCredentialsNotFoundException e) {
                // if not credentials are found let it pass, it could be handled by another provider in the chain
                chain.doFilter(request, response);
            }
        } else {
            // continue normal execution
            chain.doFilter(request, response);
        }
    }

}
