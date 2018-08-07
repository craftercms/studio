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

import org.craftercms.studio.api.v2.dal.SecurityMapper;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v1.service.security.UserDetailsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDetailsManagerImpl implements UserDetailsManager {

    //@Autowired
    protected SecurityMapper securityMapper;

    @Override
    public UserDetails loadUserByUsername(String userName) {
        User user = securityMapper.getUser(userName);
        return user;
    }


    public SecurityMapper getSecurityMapper() {
        return securityMapper;
    }

    public void setSecurityMapper(SecurityMapper securityMapper) {
        this.securityMapper = securityMapper;
    }
}
