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
package org.craftercms.studio.impl.v2.utils.git.cli;

import org.craftercms.studio.api.v2.exception.git.cli.GitCliOutputException;
import org.craftercms.studio.api.v2.exception.git.cli.GitRepositoryLockedException;

import java.util.regex.Pattern;

/**
 * {@link org.craftercms.studio.api.v2.utils.git.cli.GitCliOutputExceptionResolver} that tries to find a pattern
 * in the Git output to see if it corresponds to a {@link RepositoryLockedExceptionResolver}.
 *
 * @author Alfonso Vasquez
 * @since 3.1.23
 */
public class RepositoryLockedExceptionResolver extends PatternFindingGitCliExceptionResolver {

    public static final RepositoryLockedExceptionResolver INSTANCE = new RepositoryLockedExceptionResolver();

    @Override
    protected Pattern getErrorMessagePattern() {
        return Pattern.compile("unable to create '[^']+/\\.git/index\\.lock': file exists", Pattern.CASE_INSENSITIVE);
    }

    @Override
    protected GitCliOutputException createException(int exitValue, String output) {
        return new GitRepositoryLockedException(exitValue, output);
    }

}
