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
package org.craftercms.studio.impl.v2.security.userdetails;

import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.security.LoginAttemptManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.beans.ConstructorProperties;
import java.util.Map;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_ID;

/**
 * Implementation of {@link UserDetailsService} that uses Studio's {@link UserDAO}
 *
 * @author joseross
 * @since 4.0
 */
public class DbUserDetailsService implements UserDetailsService {

    private final UserDAO userDao;
    private final LoginAttemptManager loginAttemptManager;

    @ConstructorProperties({"userDao", "loginAttemptManager"})
    public DbUserDetailsService(final UserDAO userDao, final LoginAttemptManager loginAttemptManager) {
        this.userDao = userDao;
        this.loginAttemptManager = loginAttemptManager;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (loginAttemptManager.isUserLocked(username)) {
            throw new LockedException(String.format("User '%s' is temporarily locked out", username));
        }
        UserDetails user = userDao.getUserByIdOrUsername(Map.of(USER_ID, -1, USERNAME, username));
        if (user != null) {
            return user;
        }
        throw new UsernameNotFoundException("User not found for " + username);
    }

}
