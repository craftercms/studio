/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
 *
 */

package org.craftercms.studio.impl.v1.repository.alfresco;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.RepositoryItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class AlfrescoExtContentRepository extends AlfrescoContentRepository {

    private final static Logger logger = LoggerFactory.getLogger(AlfrescoExtContentRepository.class);

    @Override
    public RepositoryItem[] getContentChildren(String site, String path) {
        if (path.startsWith("/cstudio")) {
            return super.getContentChildren(site, path);
        } else {
            final List<RepositoryItem> retItems = new ArrayList<RepositoryItem>();

            try {
                EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
                final String finalPath = path;
                Path pathObj = constructRepoPath(path);
                if (!Files.exists(pathObj)) {
                    return super.getContentChildren(site, path);
                }
                Files.walkFileTree(constructRepoPath(finalPath), opts, 1, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path visitPath, BasicFileAttributes attrs)
                            throws IOException {

                        if (!visitPath.equals(constructRepoPath(finalPath))) {
                            RepositoryItem item = new RepositoryItem();
                            item.name = visitPath.toFile().getName();


                            String visitFolderPath = visitPath.toString();//.replace("/index.xml", "");
                            //Path visitFolder = constructRepoPath(visitFolderPath);
                            item.isFolder = visitPath.toFile().isDirectory();
                            int lastIdx = visitFolderPath.lastIndexOf(File.separator + item.name);
                            if (lastIdx > 0) {
                                item.path = visitFolderPath.substring(0, lastIdx);
                            }
                            //item.path = visitFolderPath.replace("/" + item.name, "");
                            item.path = item.path.replace(previewRepoRootPath.replace("/", File.separator), "");
                            item.path = item.path.replace(File.separator + ".xml", "");
                            item.path = item.path.replace(File.separator, "/");

                            if (!".DS_Store".equals(item.name)) {
                                logger.debug("ITEM NAME: {0}", item.name);
                                logger.debug("ITEM PATH: {0}", item.path);
                                logger.debug("ITEM FOLDER: ({0}): {1}", visitFolderPath, item.isFolder);
                                retItems.add(item);
                            }
                        }

                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (Exception err) {
                // log this error
            }

            RepositoryItem[] items = new RepositoryItem[retItems.size()];
            items = retItems.toArray(items);
            return items;
        }
    }

    @Override
    public RepositoryItem[] getContentChildren(String site, String path, boolean ignoreCache) {
        return super.getContentChildren(site, path);
    }

    /**
     * build a repo path from the relative path
     */
    protected Path constructRepoPath(String ... args) {

        return java.nio.file.FileSystems.getDefault().getPath(previewRepoRootPath, args);

    }

    @Override
    public boolean createFolder(String site, String path, String name) {
        boolean toRet = super.createFolder(site, path, name);
        if (toRet) {
            try {
                Files.createDirectories(constructRepoPath(path, name));
            }
            catch(Exception err) {
                // log this error
                logger.warn("Error while creating folder in preview content");
            }
        }
        return toRet;
    }

    protected String previewRepoRootPath;

    public String getPreviewRepoRootPath() { return previewRepoRootPath; }
    public void setPreviewRepoRootPath(String previewRepoRootPath) { this.previewRepoRootPath = previewRepoRootPath; }
}
