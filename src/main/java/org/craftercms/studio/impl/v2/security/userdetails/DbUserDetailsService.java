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
package org.craftercms.studio.impl.v2.security.userdetails;

import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.security.LoginAttemptManager;
import org.craftercms.studio.api.v2.security.authentication.LockedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_ID;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_REPO_USER_USERNAME;

/**
 * Implementation of {@link UserDetailsService} that uses Studio's {@link UserDAO}
 *
 * @author joseross
 * @since 4.0
 */
public class DbUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(DbUserDetailsService.class);

    private static final List<String> BLOCKED_USERS = List.of(GIT_REPO_USER_USERNAME);

    private final UserDAO userDao;
    private final LoginAttemptManager loginAttemptManager;

    @ConstructorProperties({"userDao", "loginAttemptManager"})
    public DbUserDetailsService(final UserDAO userDao, final LoginAttemptManager loginAttemptManager) {
        this.userDao = userDao;
        this.loginAttemptManager = loginAttemptManager;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String userNotFoundMessage = format("User not found for '%s'", username);
        if (isBlockedUser(username)) {
            logger.info("Access denied: User '{}' is blocked from logging in", username);
            throw new UsernameNotFoundException(userNotFoundMessage);
        }

        if (loginAttemptManager.isUserLocked(username)) {
            throw new LockedException(username, format("User '%s' is temporarily locked out", username),
                    loginAttemptManager.getUserLockTimeLeftSeconds(username));
        }

        UserDetails user = userDao.getUserByIdOrUsername(Map.of(USER_ID, -1, USERNAME, username));
        if (user != null) {
            return user;
        }

        throw new UsernameNotFoundException(userNotFoundMessage);
    }

    /**
     * Check if a certain user is blocked
     * @param username the username to check
     * @return true if user is blocked, false otherwise
     */
    private boolean isBlockedUser(String username) {
        return BLOCKED_USERS.contains(username.toLowerCase());
    }

}
