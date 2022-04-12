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

import java.io.IOException;

/**
 * Thrown when the Git CLI exits with a non-zero value. The output of the command is in the message.
 *
 * @author Alfonso Vasquez
 * @since 3.1.23
 */
public class GitCliOutputException extends IOException {

    private int exitValue;

    public GitCliOutputException(int exitValue, String output) {
        super("\n" + output);

        this.exitValue = exitValue;
    }

    public int getExitValue() {
        return exitValue;
    }

}
