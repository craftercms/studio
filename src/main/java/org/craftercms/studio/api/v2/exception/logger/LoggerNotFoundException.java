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

package org.craftercms.studio.api.v2.exception.logger;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;

/**
 * Exception thrown then trying to retrieve or perform an operation on
 * a Logger that does not exist.
 *
 * @author jmendeza
 * @since 4.0.2
 */
public class LoggerNotFoundException extends ServiceLayerException {

    public LoggerNotFoundException() {
    }

    public LoggerNotFoundException(String name) {
        super(name);
    }

    public LoggerNotFoundException(String message, Exception e) {
        super(message, e);
    }
}
