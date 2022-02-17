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
package org.craftercms.studio.impl.v2.security.userdetails;

import com.google.common.cache.Cache;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.model.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.beans.ConstructorProperties;

/**
 * Implementation of {@link DbAuthenticationUserDetailsService} that uses Studio's {@link UserDAO}
 *
 * @author joseross
 * @since 4.0
 */
public class DbAuthenticationUserDetailsService<T extends Authentication> extends AbstractCachedUserDetailsService
        implements AuthenticationUserDetailsService<T> {

    @ConstructorProperties({"userDao", "cache"})
    public DbAuthenticationUserDetailsService(UserDAO userDao, Cache<String, User> cache) {
        super(userDao, cache);
    }

    @Override
    public UserDetails loadUserDetails(T auth) throws UsernameNotFoundException {
        var user = getUser(auth.getName());

        // This is only needed to avoid cast exceptions from all other services
        return new AuthenticatedUser(user);
    }

}
