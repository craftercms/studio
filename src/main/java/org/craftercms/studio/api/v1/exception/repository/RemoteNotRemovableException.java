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

package org.craftercms.studio.api.v1.exception.repository;

public class RemoteNotRemovableException extends Exception {

    private static final long serialVersionUID = 6683185301012812522L;

    public RemoteNotRemovableException() {
    }

    public RemoteNotRemovableException(String s) {
        super(s);
    }

    public RemoteNotRemovableException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public RemoteNotRemovableException(Throwable throwable) {
        super(throwable);
    }

    public RemoteNotRemovableException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
