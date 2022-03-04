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
package org.craftercms.studio.impl.v2.security.listener;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.studio.api.v2.service.security.AccessTokenService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.beans.ConstructorProperties;

/**
 * Listener for Spring's {@link AuthenticationSuccessEvent} that generates an access token for the user
 *
 * @author joseross
 * @since 4.0
 */
public class AccessTokenAuthenticationListener {

    protected AccessTokenService accessTokenService;

    @ConstructorProperties({"accessTokenService"})
    public AccessTokenAuthenticationListener(AccessTokenService accessTokenService) {
        this.accessTokenService = accessTokenService;
    }

    @EventListener
    public void generateTokens(AuthenticationSuccessEvent event) {
        // Don't change any tokens for pre-authenticated events
        if (event.getAuthentication() instanceof PreAuthenticatedAuthenticationToken) {
            return;
        }
        accessTokenService.updateRefreshToken(event.getAuthentication(), RequestContext.getCurrent().getResponse());
    }

    @EventListener
    public void deleteTokens(LogoutSuccessEvent event) {
        // Don't change any tokens for pre-authenticated events
        if (event.getAuthentication() instanceof PreAuthenticatedAuthenticationToken) {
            return;
        }
        accessTokenService.deleteRefreshToken(event.getAuthentication());
    }

}
