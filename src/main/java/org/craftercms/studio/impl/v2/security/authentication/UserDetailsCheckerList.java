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

package org.craftercms.studio.impl.v2.security.authentication;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

import java.util.List;

/**
 * A {@link UserDetailsChecker} that delegates to a list of other {@link UserDetailsChecker} instances.
 *
 * @since 4.1.2
 */
public class UserDetailsCheckerList implements UserDetailsChecker {

    private final List<UserDetailsChecker> userDetailsCheckers;

    public UserDetailsCheckerList(final List<UserDetailsChecker> userDetailsCheckers) {
        this.userDetailsCheckers = userDetailsCheckers;
    }

    @Override
    public void check(UserDetails user) {
        for (UserDetailsChecker userDetailsChecker : userDetailsCheckers) {
            userDetailsChecker.check(user);
        }
    }
}

