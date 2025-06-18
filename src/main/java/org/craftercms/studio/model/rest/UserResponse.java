/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.model.rest;

import org.craftercms.studio.api.v2.dal.User;

import java.util.Collection;

/**
 * Represents a {@link User} with only the necessary fields that are required to be sent in the response
 */
public class UserResponse extends User {
	public UserResponse(User user) {
		setId(user.getId());
		setUsername(user.getUsername());
		setEmail(user.getEmail());
		setFirstName(user.getFirstName());
		setLastName(user.getLastName());
		setEnabled(user.isEnabled());
		setExternallyManaged(user.isExternallyManaged());
	}

	/**
	 * Converts a collection of {@link User} objects to a collection of {@link UserResponse} objects
	 *
	 * @param users the collection of {@link User} objects to convert
	 * @return the collection of {@link UserResponse} objects
	 */
	public static Collection<UserResponse> convert(final Collection<User> users) {
		return users.stream().map(UserResponse::new).toList();
	}
}
