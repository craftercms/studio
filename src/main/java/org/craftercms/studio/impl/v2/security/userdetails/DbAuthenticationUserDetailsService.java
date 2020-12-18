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
package org.craftercms.studio.impl.v2.security.userdetails;

import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.model.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Map;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_ID;

/**
 * Implementation of {@link DbAuthenticationUserDetailsService} that uses Studio's {@link UserDAO}
 *
 * @author joseross
 * @since 3.2
 */
public class DbAuthenticationUserDetailsService<T extends Authentication>
        implements AuthenticationUserDetailsService<T> {

    protected UserDAO userDao;

    public DbAuthenticationUserDetailsService(UserDAO userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserDetails(T token) throws UsernameNotFoundException {
        var user = userDao.getUserByIdOrUsername(Map.of(USERNAME, token.getName(), USER_ID, -1));
        if (user == null) {
            throw new UsernameNotFoundException("User not found for " + token.getName());
        }

        // This is only needed to avoid cast exceptions from all other services
        return new AuthenticatedUser(user);
    }

}
