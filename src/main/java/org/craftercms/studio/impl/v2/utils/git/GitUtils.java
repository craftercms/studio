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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for Git operations.
 *
 * @author Phil Nguyen
 */

public class GitUtils {
    private static final Logger logger = LoggerFactory.getLogger(GitUtils.class);

    public static final String GIT_FOLDER_NAME = ".git";
    public static final String GIT_INDEX_NAME = "index";
    public static final String GIT_LOCK_NAME = GIT_INDEX_NAME + ".lock";

    public static boolean isRepositoryLocked(String repoPath) {
        Path path = Paths.get(repoPath, GIT_FOLDER_NAME, GIT_LOCK_NAME);
        return Files.exists(path);
    }

    public static void unlock(String repoPath) throws IOException {
        deleteFile(Paths.get(repoPath, GIT_FOLDER_NAME, GIT_LOCK_NAME));
    }

    public static void deleteGitIndex(String repoPath) throws IOException {
        deleteFile(Paths.get(repoPath, GIT_FOLDER_NAME, GIT_INDEX_NAME));
    }

    protected static void deleteFile(Path file) throws IOException {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            logger.debug("Error deleting file {}, forcing delete", file, e);
            FileUtils.forceDelete(file.toFile());
        }
    }
}