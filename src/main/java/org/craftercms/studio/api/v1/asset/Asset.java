package org.craftercms.studio.api.v1.asset;

import java.nio.file.Path;

public class Asset {

    private String repoPath;
    private Path file;

    public Asset(String repoPath, Path file) {
        this.repoPath = repoPath;
        this.file = file;
    }

    public String getRepoPath() {
        return repoPath;
    }

    public Path getFile() {
        return file;
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
