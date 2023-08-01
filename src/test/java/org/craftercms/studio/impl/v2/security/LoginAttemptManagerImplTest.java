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

import org.craftercms.studio.api.v2.security.LoginAttemptManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginAttemptManagerImplTest {

    private static final String USERNAME = "admin";
    private static final int BASE_LOCK_TIME_SECONDS = 2;
    private static final int MAX_TRACKED_USERNAMES = 3;
    private static final long MAX_LOCKED_TIME_SECONDS = 5;
    private static final int LOCK_LOGIN_ATTEMPTS = 3;
    private static final int UNLOCK_WAIT_TIME_SECONDS = ((int) Math.pow(BASE_LOCK_TIME_SECONDS, LOCK_LOGIN_ATTEMPTS)) + 1;

    public LoginAttemptManagerImpl getLoginAttemptManager(final boolean enabled) {
        return new LoginAttemptManagerImpl(MAX_TRACKED_USERNAMES, BASE_LOCK_TIME_SECONDS, MAX_LOCKED_TIME_SECONDS, enabled);
    }

    private void lockAccount(LoginAttemptManager loginAttemptManager, String username) {
        for (int i = 0; i < LOCK_LOGIN_ATTEMPTS; i++) {
            loginAttemptManager.loginFailed(username);
        }
    }

    @Test
    public void testUserLockAfterFailedAttempts() {
        LoginAttemptManagerImpl loginAttemptManager = getLoginAttemptManager(true);
        assertFalse(loginAttemptManager.isUserLocked(USERNAME));
        lockAccount(loginAttemptManager, USERNAME);
        assertTrue(loginAttemptManager.isUserLocked(USERNAME));
    }

    @Test
    public void testUserIsUnlockedAfterPeriod() throws InterruptedException {
        LoginAttemptManagerImpl loginAttemptManager = getLoginAttemptManager(true);
        assertFalse(loginAttemptManager.isUserLocked(USERNAME));
        lockAccount(loginAttemptManager, USERNAME);
        assertTrue(loginAttemptManager.isUserLocked(USERNAME));
        Thread.sleep(UNLOCK_WAIT_TIME_SECONDS * 1000);
        assertFalse(loginAttemptManager.isUserLocked(USERNAME));
    }

    @Test
    public void testMaximumTrackedUsernames() {
        LoginAttemptManagerImpl loginAttemptManager = getLoginAttemptManager(true);
        assertFalse(loginAttemptManager.isUserLocked(USERNAME));
        lockAccount(loginAttemptManager, USERNAME);
        assertTrue(loginAttemptManager.isUserLocked(USERNAME));
        fillUsernames(loginAttemptManager);
        assertFalse(loginAttemptManager.isUserLocked(USERNAME));
    }

    @Test
    public void testDisabledManager() {
        LoginAttemptManagerImpl loginAttemptManager = getLoginAttemptManager(false);
        assertFalse(loginAttemptManager.isUserLocked(USERNAME));
        lockAccount(loginAttemptManager, USERNAME);
        assertFalse(loginAttemptManager.isUserLocked(USERNAME));
    }

    private void fillUsernames(LoginAttemptManagerImpl loginAttemptManager) {
        for (int i = 0; i < MAX_TRACKED_USERNAMES; i++) {
            lockAccount(loginAttemptManager, "username" + i);
        }
    }
}
