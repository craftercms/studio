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
package org.craftercms.studio.impl.v2.security.web;

import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.AuthenticatedUser;
import org.craftercms.studio.model.AuthenticationType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_ENABLED;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_LOGOUT_ENABLED;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_LOGOUT_URL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE_HEADERS;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_AUTHENTICATION_CHAIN_CONFIG;

/**
 * Extension of {@link SimpleUrlLogoutSuccessHandler} that handles redirection to external SSO logout when needed
 *
 * @author joseross
 * @since 3.2
 */
public class ChainLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    protected String ssoLogoutUrl = "/";

    public ChainLogoutSuccessHandler(StudioConfiguration studioConfiguration) {
        var chainConfig = studioConfiguration.getSubConfigs(CONFIGURATION_AUTHENTICATION_CHAIN_CONFIG);
        chainConfig.stream()
                .filter(config -> config.getBoolean(AUTHENTICATION_CHAIN_PROVIDER_ENABLED))
                .filter(config -> equalsIgnoreCase(config.getString(AUTHENTICATION_CHAIN_PROVIDER_TYPE),
                        AUTHENTICATION_CHAIN_PROVIDER_TYPE_HEADERS))
                .filter(config -> config.getBoolean(AUTHENTICATION_CHAIN_PROVIDER_LOGOUT_ENABLED, false))
                .findFirst()
                .ifPresent(config ->
                        ssoLogoutUrl = config.getString(AUTHENTICATION_CHAIN_PROVIDER_LOGOUT_URL, ssoLogoutUrl));
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        var user = (AuthenticatedUser) authentication.getPrincipal();

        if (user.getAuthenticationType() == AuthenticationType.AUTH_HEADERS) {
            return ssoLogoutUrl;
        }

        return super.determineTargetUrl(request, response, authentication);
    }

}
