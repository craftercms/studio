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

package org.craftercms.studio.api.v2.exception;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.Collections;

/**
 * Exception that wraps multiple exceptions
 */
public class CompositeException extends ServiceLayerException {

    private final Collection<Exception> exceptions;

    public CompositeException(final String message, @NonNull final Collection<Exception> exceptions) {
        super(message);
        this.exceptions = Collections.unmodifiableCollection(exceptions);
    }

    public Collection<Exception> getExceptions() {
        return exceptions;
    }
}
