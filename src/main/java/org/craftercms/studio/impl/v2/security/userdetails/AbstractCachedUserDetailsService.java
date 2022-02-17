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
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Map;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_ID;

/**
 * Base class for all services that access user information
 *
 * @author joseross
 * @since 4.0
 */
public class AbstractCachedUserDetailsService {

    protected UserDAO userDao;

    protected Cache<String, User> cache;

    public AbstractCachedUserDetailsService(UserDAO userDao, Cache<String, User> cache) {
        this.userDao = userDao;
        this.cache = cache;
    }

    protected User getUser(String username) {
        User user = cache.getIfPresent(username);
        if (user == null) {
            user = userDao.getUserByIdOrUsername(Map.of(USER_ID, -1, USERNAME, username));
            if (user != null) {
                cache.put(username, user);
            } else {
                throw new UsernameNotFoundException("User not found for " + username);
            }
        }
        return user;
    }

}
