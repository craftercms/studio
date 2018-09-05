/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */

package org.craftercms.studio.impl.v1.service.security;

import org.craftercms.studio.api.v1.service.security.UserDetailsManager;
import org.craftercms.studio.api.v2.dal.UserTO;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.Map;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAME;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USER_ID;

public class UserDetailsManagerImpl implements UserDetailsManager {

    protected UserDAO userDAO;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USER_ID, -1);
        params.put(USERNAME, username);
        UserTO user = userDAO.getUserByIdOrUsername(params);
        return user;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
}
