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
import org.craftercms.studio.api.v2.utils.git.cli.GitCliOutputExceptionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for {@link GitCliOutputExceptionResolver}s that want to determine the error produced by a Git command
 * by finding a special regex pattern in the output.
 *
 * @author Alfonso Vasquez
 * @since 3.1.23
 */
public abstract class PatternFindingGitCliExceptionResolver implements GitCliOutputExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger(PatternFindingGitCliExceptionResolver.class);

    @Override
    public GitCliOutputException resolveException(int exitValue, String output) {
        Matcher matcher = getErrorMessagePattern().matcher(output);
        if (matcher.find()) {
            GitCliOutputException ex = createException(exitValue, output);

            logger.debug("Found pattern of {} in Git output", ex.getClass().getSimpleName());

            return ex;
        } else {
            return null;
        }
    }

    protected abstract Pattern getErrorMessagePattern();

    protected abstract GitCliOutputException createException(int exitValue, String output);

}
