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
package org.craftercms.studio.api.v1.exception.repository;

import org.craftercms.studio.api.v1.exception.ServiceException;

public class RemoteAlreadyExistsException extends ServiceException {


    private static final long serialVersionUID = 6422788562443045182L;

    public RemoteAlreadyExistsException() {}

	public RemoteAlreadyExistsException(Exception e) {
		super(e);
	}

	public RemoteAlreadyExistsException(String message) {
		super(message);
	}

	public RemoteAlreadyExistsException(String message, Exception e) {
		super(message, e);
	}
}
