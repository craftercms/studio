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
package org.craftercms.studio.impl.v2.utils;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.lang.RegexUtils;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ObjectId;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * Common operations related to git
 *
 * @author joseross
 * @ 4.0
 */
public abstract class GitUtils extends org.craftercms.commons.git.utils.GitUtils {

    public static List<String> getChangedFiles(Git git, ObjectId initialId, ObjectId finalId, String[] patterns)
            throws GitAPIException, IOException {
        var repo = git.getRepository();
        try (var reader = repo.newObjectReader()) {
            var diffs = doDiff(git, reader, initialId, finalId);
            return diffs.stream()
                        .map(diff -> {
                            switch (diff.getChangeType()) {
                                case MODIFY:
                                    return diff.getNewPath();
                                case DELETE:
                                    return diff.getOldPath();
                                default:
                                    return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .filter(path -> RegexUtils.matchesAny(path, patterns))
                        .collect(toList());
        }
    }

    public static List<String> getChangedFiles(Git git, String initialId, String finalId, String[] patterns)
            throws GitAPIException, IOException {
        return getChangedFiles(git, git.getRepository().resolve(initialId),
                git.getRepository().resolve(finalId), patterns);
    }

    public static void translateException(TransportException e, Logger logger, String remoteName, String remoteUrl,
                                          String remoteUsername) throws RemoteRepositoryNotFoundException,
                                                                        InvalidRemoteRepositoryCredentialsException {
        if (StringUtils.endsWithIgnoreCase(e.getMessage(), "not authorized")) {
            logger.error("Bad credentials or read only repository: " + remoteName + " (" + remoteUrl + ")",
                    e);
            throw new InvalidRemoteRepositoryCredentialsException("Bad credentials or read only repository: " +
                    remoteName + " (" + remoteUrl + ") for username " + remoteUsername, e);
        } else if (StringUtils.endsWithIgnoreCase(e.getMessage(), "key did not validate")) {
            logger.error("Invalid private key: " + remoteName + " (" + remoteUrl + ")",
                    e);
            throw new InvalidRemoteRepositoryCredentialsException("Invalid private key for repository: " +
                    remoteName + " (" + remoteUrl + ")", e);
        } else {
            logger.error("Remote repository not found: " + remoteName + " (" + remoteUrl + ")", e);
            throw new RemoteRepositoryNotFoundException("Remote repository not found: " + remoteName + " (" +
                    remoteUrl + ")");
        }
    }

}
