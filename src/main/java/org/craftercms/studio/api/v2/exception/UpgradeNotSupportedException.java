/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.exception;

/**
 * Thrown when the current version of the system can't be upgraded automatically.
 * @author joseross
 */
public class UpgradeNotSupportedException extends UpgradeException {

    public UpgradeNotSupportedException() {
    }

    public UpgradeNotSupportedException(String message) {
        super(message);
    }

    public UpgradeNotSupportedException(String message, Exception cause) {
        super(message, cause);
    }

    public UpgradeNotSupportedException(Throwable cause) {
        super(cause);
    }

}
