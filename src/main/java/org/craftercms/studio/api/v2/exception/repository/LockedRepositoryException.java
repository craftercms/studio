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

package org.craftercms.studio.api.v2.exception.repository;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;

/**
 * Exception thrown when an operation request a exclusive lock on a
 * already locked repository. e.g.: trying to publish while performing a pull from remote
 */
public class LockedRepositoryException extends ServiceLayerException {
    public LockedRepositoryException(final String message) {
        super(message);
    }
}
