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
package org.craftercms.studio.impl.v2.security.authentication;

import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.security.authentication.DeletedException;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Extension of {@link AccountStatusUserDetailsChecker} that checks for deleted users too.
 *
 * @author joseross
 * @since 4.0
 */
public class DeletedUserDetailsChecker extends AccountStatusUserDetailsChecker {

    @Override
    public void check(UserDetails user) {
        super.check(user);
        if (((User) user).isDeleted()) {
            throw new DeletedException("User has been deleted");
        }
    }

}
