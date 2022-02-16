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
package org.craftercms.studio.api.v1.asset;

import java.nio.file.Path;

/**
 * Represents an asset that can be processed (image, pdf, Word document, etc.)
 *
 * @author avasquez
 */
public class Asset {

    private String repoPath;
    private Path filePath;

    public Asset(String repoPath, Path filePath) {
        this.repoPath = repoPath;
        this.filePath = filePath;
    }

    /**
     * Returns the repo path for the asset.
     */
    public String getRepoPath() {
        return repoPath;
    }

    /**
     * Sets the file path where the asset is temporarily being stored.
     */
    public Path getFilePath() {
        return filePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Asset asset = (Asset)o;

        return repoPath != null? repoPath.equals(asset.repoPath): asset.repoPath == null;
    }

    @Override
    public int hashCode() {
        return repoPath != null? repoPath.hashCode(): 0;
    }

    @Override
    public String toString() {
        return "Asset{" + "repoPath='" + repoPath + '\'' + '}';
    }

}
