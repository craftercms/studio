/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
 */

package org.craftercms.studio.api.v2.service.security;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.impl.v1.util.SessionTokenUtils;
import org.craftercms.studio.impl.v2.service.security.Authentication;

import javax.servlet.http.HttpSession;

import static org.craftercms.studio.api.v1.constant.StudioConstants.HTTP_SESSION_ATTRIBUTE_AUTHENTICATION;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_SESSION_TIMEOUT;

/**
 * Base authentication provider implementation
 */
public abstract class BaseAuthenticationProvider implements AuthenticationProvider {

    private boolean enabled;

    /**
     * Create authentication token
     *
     * @param user user to create token for
     * @param authenticationChain autientication chain
     * @return authentication token
     */
    protected String createToken(User user, AuthenticationChain authenticationChain) {
        StudioConfiguration studioConfiguration = authenticationChain.getStudioConfiguration();
        int timeout = studioConfiguration.getProperty(SECURITY_SESSION_TIMEOUT, Integer.class);
        String token = SessionTokenUtils.createToken(user.getUsername(), timeout);
        return token;
    }

    /**
     * Persist authentication within active HTTP Session
     *
     * @param authentication authentication to persist
     */
    protected void storeAuthentication(Authentication authentication) {
        RequestContext context = RequestContext.getCurrent();
        if(context != null) {
            HttpSession httpSession = context.getRequest().getSession();
            httpSession.setAttribute(HTTP_SESSION_ATTRIBUTE_AUTHENTICATION, authentication);
        }
    }

    /**
     * Check if authentication provider is enabled
     *
     * @return true if authentication provider is enabled, otherwise false
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable or disable authentication provider
     *
     * @param enabled true to enable, false to disable
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
