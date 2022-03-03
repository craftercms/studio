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

package org.craftercms.studio.api.v2.exception.content;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;

public class ContentLockedByAnotherUserException extends ServiceLayerException {

    private String lockOwner;

    public ContentLockedByAnotherUserException() {
        super();
    }

    public ContentLockedByAnotherUserException(Throwable e) {
        super(e);
    }

    public ContentLockedByAnotherUserException(String message) {
        super(message);
    }

    public ContentLockedByAnotherUserException(String message, Exception e) {
        super(message, e);
    }

    public String getLockOwner() {
        return lockOwner;
    }

    public void setLockOwner(String lockOwner) {
        this.lockOwner = lockOwner;
    }
}
