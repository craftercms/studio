/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.studio.impl.v1.repository.disk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.util.List;
import java.util.ArrayList;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.impl.v1.repository.AbstractContentRepository;

import reactor.core.Reactor;
import reactor.event.Event;

import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.CopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileVisitResult;

/**
 * Disk repository implementation. 
 * @author russdanner
 *
 */
public class DiskContentRepository extends AbstractContentRepository {

    private static final Logger logger = LoggerFactory.getLogger(DiskContentRepository.class);


    @Override
    public InputStream getContent(String path) throws ContentNotFoundException {
        InputStream retStream = null;

        try {
            OpenOption options[] = { StandardOpenOption.READ };
            retStream = Files.newInputStream(constructRepoPath(path));
        }

        catch(Exception err) {
            throw new ContentNotFoundException("error while opening file", err);
        }

        return retStream;
    }

    @Override
    public boolean contentExists(String path) {
        return Files.exists(constructRepoPath(path));
    }


    @Override
    public boolean writeContent(String path, InputStream content) {
        
        boolean success = true;

        try {
            logger.info("writing file: "+path);

            CopyOption options[] = { StandardCopyOption.REPLACE_EXISTING };
            Files.copy(content,constructRepoPath(path), options);
        }
        catch(Exception err) {
            logger.error("error writing file: "+path, err);
            success = false;
        }

        return success;
    }

    @Override
    public boolean createFolder(String path, String name) {
        
        boolean success = true;
        
        try {
            Files.createDirectories(constructRepoPath(path,name));
        }
        catch(Exception err) {
            // log this error
            success = false;
        }

        return success;
    }

    @Override
    public boolean deleteContent(String path) {
        
        boolean success = true;
        
        try {
            Files.delete(constructRepoPath(path));
        }
        catch(Exception err) {
            // log this error
            success = false;
        }

        return success;
    }

    @Override
    public boolean copyContent(String fromPath, String toPath) {
        
        boolean success = true;

        try {
            Files.copy(constructRepoPath(fromPath), constructRepoPath(toPath));
        }
        catch(Exception err) {
            // log this error
            success = false;
        }

        return success;
    }

    @Override
    public boolean moveContent(String fromPath, String toPath) {
        
        boolean success = true;

        try {
            Files.move(constructRepoPath(fromPath), constructRepoPath(toPath));
        }
        catch(Exception err) {
            // log this error
            success = false;
        }

        return success;
    }


    /**
     * get immediate children for path
     * @param path path to content
     */
    public RepositoryItem[] getContentChildren(String path) {
        final List<RepositoryItem> retItems = new ArrayList<RepositoryItem>();
        
        try {
            Files.walkFileTree(constructRepoPath(path), null, 2, new SimpleFileVisitor<Path>() { 
                @Override
                public FileVisitResult visitFile(Path visitPath, BasicFileAttributes attrs)
                throws IOException {

                    RepositoryItem item = new RepositoryItem();
                    item.name = visitPath.toFile().getName();
                    logger.error("MAKING ITEM: " + item.name);


                    String visitFolderPath = visitPath.toString().replace("/index.xml", "");
                    Path visitFolder = constructRepoPath(visitFolderPath);
                    item.path = visitFolderPath;

                    item.isFolder = Files.isDirectory(visitFolder);
                    logger.error("ITEM PATH: " + item.path);
                    logger.error("ITEM FOLDER: " + item.isFolder);
                    logger.error("==" + item.isFolder);

                    retItems.add(item);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch(Exception err) {
            // log this error
        }

        RepositoryItem[] items = new RepositoryItem[retItems.size()];
        items = retItems.toArray(items);
        return items;
    }

    /** 
     * get the version history for an item
     * @param path - the path of the item
     */
    public VersionTO[] getContentVersionHistory(String path) {
        return new VersionTO[0];
    }

    /**
     * create a version
     * @param path location of content
     * @param majorVersion true if major
     * @return the created version ID or null on failure
     */
    public String createVersion(String path, boolean majorVersion) {
        return null;
    }

    /** 
     * revert a version (create a new version based on an old version)
     * @param path - the path of the item to "revert"
     * @param version - old version ID to base to version on
     */
    public boolean revertContent(String path, String version, boolean major, String comment) {
        return false;
    }

    public void lockItem(String site, String path) {
    }

    public void unLockItem(String site, String path) {
    }

    /**
     * build a repo path from the relative path
     */
    protected Path constructRepoPath(String ... args) {

        return java.nio.file.FileSystems.getDefault().getPath(rootPath, args);

    }

    /**
     * build a repo path from the relative path
     */
    protected Path constructVersionRepoPath(String ... args) {

        return java.nio.file.FileSystems.getDefault().getPath(rootPath+"/versions", args);

    }

    public Reactor getRepositoryReactor() { return repositoryReactor; }
    public void setRepositoryReactor(Reactor repositoryReactor) { this.repositoryReactor = repositoryReactor; }

    String rootPath;
    public String getRootPath() { return rootPath; }
    public void setRootPath(String path) { rootPath = path; }

    protected Reactor repositoryReactor;
}