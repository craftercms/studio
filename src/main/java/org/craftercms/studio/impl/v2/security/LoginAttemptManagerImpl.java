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

package org.craftercms.studio.impl.v2.security;

import org.apache.commons.collections.map.LRUMap;
import org.craftercms.studio.api.v2.security.LoginAttemptManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static java.time.LocalDateTime.now;

/**
 * {@link LoginAttemptManager} implementation that keeps track of login failures in memory. <br/>
 * <ul>
 *     <li>On login success, username is removed from the loginFailures map</li>
 *     <li>On login failure:
 *     <ol>
 *         <li>If user is already locked, no action is performed</li>
 *         <li>If user is not locked, the login attempt count is incremented (or initialized to 1, if missing)</li>
 *         <li>Then the user is locked for the period calculated as <code>min( baseTimeSeconds ^ (failedAttempts - 1), maxTimeSeconds)</code></li>
 *     </ol>
 *     </li>
 * </ul>
 * <code>isUserLocked()</code> is implemented by checking if the user is present in the loginFailures map and if the lock time has not expired.<br/>
 * This implementation relies on {@link LRUMap} to limit the number of tracked usernames. When adding new items to a full {@link LRUMap}(<code>maxTrackedUsernames</code> is reached),
 * it will delete the least recently used items (based on calls to <code>put</code> and <code>get</code> methods). <br/>
 * In order to prevent leaking information about user existence (Enumeration attacks), this implementation is agnostic to whether the user exists or not.
 *
 * @since 4.1.2
 */
public class LoginAttemptManagerImpl implements LoginAttemptManager {
    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptManagerImpl.class);
    private final Map<String, LoginFailure> loginFailures;
    private final int baseTimeSeconds;
    private final long maxTimeSeconds;
    private final boolean enabled;

    /**
     * Creates a new instance of {@link LoginAttemptManagerImpl}.
     *
     * @param maxTrackedUsernames The maximum number of usernames to track. When this number is reached, the least recently used usernames will be removed from the map.
     * @param baseTimeSeconds     The base time in seconds to use for exponential backoff.
     * @param maxTimeSeconds      The maximum time in seconds to use for exponential backoff.
     * @param enabled             Whether this manager is enabled or not.
     */
    public LoginAttemptManagerImpl(final int maxTrackedUsernames, final int baseTimeSeconds, final long maxTimeSeconds, final boolean enabled) {
        this.baseTimeSeconds = baseTimeSeconds;
        this.maxTimeSeconds = maxTimeSeconds;
        this.enabled = enabled;
        this.loginFailures = enabled ? Collections.synchronizedMap(new LRUMap(maxTrackedUsernames)) : null;
    }

    @Override
    public boolean isUserLocked(final String username) {
        if (!enabled) {
            return false;
        }
        LoginFailure loginFailure = loginFailures.get(username);
        return loginFailure != null && now().isBefore(loginFailure.lockedUntil);
    }

    @Override
    public void loginSucceeded(final String username) {
        if (enabled) {
            logger.debug("User '{}' logged in successfully, will remove from login failures map", username);
            loginFailures.remove(username);
        }
    }

    @Override
    public void loginFailed(final String username) {
        if (!enabled) {
            return;
        }
        LoginFailure failure = loginFailures.computeIfAbsent(username, k -> new LoginFailure());
        // Exponential backoff
        long lockTimeSeconds = (long) Math.min(Math.pow(baseTimeSeconds, failure.attempts), maxTimeSeconds);
        failure.lockedUntil = now().plusSeconds(lockTimeSeconds);
        logger.warn("User '{}' login has failed. Locked for {} seconds until '{}'", username, lockTimeSeconds, failure.lockedUntil);
        failure.attempts++;
    }

    /**
     * Keep track of login attempts and lock expiration time.
     */
    private static class LoginFailure {
        @NonNull
        private LocalDateTime lockedUntil = now();
        private int attempts = 0;
    }
}
