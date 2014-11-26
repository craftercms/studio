/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
 */
package org.craftercms.cstudio.alfresco.exception;

/**
 * Thrown to indicate that a specified scope is not valid, that is, it is not registered in the cache.
 *
 * @author Sumer Jabri
 * @author Alfonso Vásquez
 */
public class InvalidScopeException extends CacheException {

    private static final long serialVersionUID = -7661987386320420066L;

    public InvalidScopeException() {
    }

    public InvalidScopeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidScopeException(String message) {
        super(message);
    }

    public InvalidScopeException(Throwable cause) {
        super(cause);
    }
}
