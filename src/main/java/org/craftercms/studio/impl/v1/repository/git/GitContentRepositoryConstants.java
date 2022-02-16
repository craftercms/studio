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

package org.craftercms.studio.impl.v1.repository.git;

/**
 * Created by Sumer Jabri
 */
public final class  GitContentRepositoryConstants {
    public static final String GIT_ROOT = ".git";
    public static final String GIT_COMMIT_ALL_ITEMS = ".";
    public static final String EMPTY_FILE = ".keep";
    public static final String IGNORE_FILE = ".gitignore";
    public static final String SOURCES_FOLDER = "sources";
    public static final String[] IGNORE_FILES = new String[] { ".keep", ".DS_Store" };

    /** Configuration */
    public static final String CONFIG_SECTION_CORE = "core";
    public static final String CONFIG_SECTION_REMOTE = "remote";
    public static final String CONFIG_PARAMETER_COMPRESSION = "compression";
    public static final int CONFIG_PARAMETER_COMPRESSION_DEFAULT = 0;
    public static final String CONFIG_PARAMETER_BIG_FILE_THRESHOLD = "bigFileThreshold";
    public static final String CONFIG_PARAMETER_BIG_FILE_THRESHOLD_DEFAULT = "20m";
    public static final String CONFIG_PARAMETER_FILE_MODE = "fileMode";
    public static final boolean CONFIG_PARAMETER_FILE_MODE_DEFAULT = false;
    public static final String CONFIG_PARAMETER_URL = "url";
    public static final String CONFIG_PARAMETER_FETCH = "fetch";
    public static final String CONFIG_PARAMETER_FETCH_DEFAULT = "+refs/heads/*:refs/remotes/origin/*";

    public static final String PREVIOUS_COMMIT_SUFFIX = "~1";

    public static final String CLUSTER_NODE_REMOTE_NAME_PREFIX = "cluster_node_";

    public static final String GIT_REPO_USER_USERNAME = "git_repo_user";
    public static final String LOCK_FILE = "index.lock";

    private GitContentRepositoryConstants() {
    }
}
