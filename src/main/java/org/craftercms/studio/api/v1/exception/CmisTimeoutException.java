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

package org.craftercms.studio.api.v1.exception;

public class CmisTimeoutException extends Exception {

    private static final long serialVersionUID = -5443425520248208346L;

    public CmisTimeoutException() {
    }

    public CmisTimeoutException(String message) {
        super(message);
    }

    public CmisTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public CmisTimeoutException(Throwable cause) {
        super(cause);
    }

    public CmisTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
