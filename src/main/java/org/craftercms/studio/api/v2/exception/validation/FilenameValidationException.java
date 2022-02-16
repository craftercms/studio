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
package org.craftercms.studio.api.v2.exception.validation;

import static java.lang.String.format;

/**
 * Base exception for all filename related validations
 *
 * @author joseross
 * @since 4.0.0
 */
public abstract class FilenameValidationException extends ValidationException {

    protected String filename;

    public FilenameValidationException(String filename, String message, Exception cause) {
        super(format(message, filename), cause);
        this.filename = filename;
    }

    public FilenameValidationException(String filename, String message) {
        super(format(message, filename));
        this.filename = filename;
    }

}
