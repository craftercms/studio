/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.studio.api.v2.security.LoginAttemptManager;
import org.craftercms.studio.model.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * Listener that handles login attempts and adds a delay to the response.<br/>
 * This listener will add a random delay to all login attempts, successful or not.<br/>
 * It will notify the {@link LoginAttemptManager} of the login attempt result.
 *
 * @since 4.1.2
 */
public class LoginAttemptListener {

    Logger logger = LoggerFactory.getLogger(LoginAttemptListener.class);
    private final LoginAttemptManager loginAttemptManager;

    public LoginAttemptListener(final LoginAttemptManager loginAttemptManager) {
        this.loginAttemptManager = loginAttemptManager;
    }

    @EventListener
    public void handleSuccessfulAttempt(AuthenticationSuccessEvent event) {
        logger.debug("Login success for user {}", event.getAuthentication().getPrincipal());
        if (!(event.getAuthentication() instanceof PreAuthenticatedAuthenticationToken)) {
            AuthenticatedUser user = (AuthenticatedUser) event.getAuthentication().getPrincipal();
            loginAttemptManager.loginSucceeded(user.getUsername());
        }
    }

    @EventListener
    public void handleFailedAttempt(AuthenticationFailureBadCredentialsEvent event) {
        logger.debug("Login failed for user {}", event.getAuthentication().getPrincipal().toString());
        String username = event.getAuthentication().getPrincipal().toString();
        loginAttemptManager.loginFailed(username);
    }

}
