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

import java.util.Arrays;
import java.util.Collection;

/**
 * {@link org.craftercms.studio.api.v2.utils.git.cli.GitCliOutputExceptionResolver} that uses a list of other resolvers
 * to determine which exception to throw. The first non-null exception that is returned from the list is the one used.
 *
 * @author Alfonso Vasquez
 * @since 3.1.23
 */
public class CompositeGitCliExceptionResolver implements GitCliOutputExceptionResolver {

    private Collection<GitCliOutputExceptionResolver> resolvers;

    public CompositeGitCliExceptionResolver(Collection<GitCliOutputExceptionResolver> resolvers) {
        this.resolvers = resolvers;
    }

    public CompositeGitCliExceptionResolver(GitCliOutputExceptionResolver... resolvers) {
        this(Arrays.asList(resolvers));
    }

    @Override
    public GitCliOutputException resolveException(int exitValue, String output) {
        for (GitCliOutputExceptionResolver resolver : resolvers) {
            GitCliOutputException ex = resolver.resolveException(exitValue, output);
            if (ex != null) {
                return ex;
            }
        }

        return null;
    }

}
