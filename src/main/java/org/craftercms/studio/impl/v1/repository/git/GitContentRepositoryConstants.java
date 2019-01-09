/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
public interface GitContentRepositoryConstants {
    String GIT_ROOT = ".git";
    String GIT_COMMIT_ALL_ITEMS = ".";
    String EMPTY_FILE = ".keep";
    String[] IGNORE_FILES = new String[] { ".keep", ".DS_Store" };

    /** Configuration */
    String CONFIG_SECTION_CORE = "core";
    String CONFIG_SECTION_REMOTE = "remote";
    String CONFIG_PARAMETER_COMPRESSION = "compression";
    int CONFIG_PARAMETER_COMPRESSION_DEFAULT = 0;
    String CONFIG_PARAMETER_BIG_FILE_THRESHOLD = "bigFileThreshold";
    String CONFIG_PARAMETER_BIG_FILE_THRESHOLD_DEFAULT = "20m";
    String CONFIG_PARAMETER_FILE_MODE = "fileMode";
    boolean CONFIG_PARAMETER_FILE_MODE_DEFAULT = false;
    String CONFIG_PARAMETER_URL = "url";
    String CONFIG_PARAMETER_FETCH = "fetch";
    String CONFIG_PARAMETER_FETCH_DEFAULT = "+refs/heads/*:refs/remotes/origin/*";

    String PREVIOUS_COMMIT_SUFFIX = "~1";
}
