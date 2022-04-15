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
package org.craftercms.studio.api.v2.exception.git.cli;

/**
 * Exception thrown when a Git commit is attempted but there's nothing to commit.
 *
 * @author Alfonso Vasquez
 * @since 3.1.23
 */
public class NoChangesToCommitException extends GitCliOutputException {

    public NoChangesToCommitException(int exitValue, String msg) {
        super(exitValue, msg);
    }

}
