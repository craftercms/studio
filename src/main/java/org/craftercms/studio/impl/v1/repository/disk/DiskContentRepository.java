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

import org.apache.commons.io.FileUtils;
import org.springframework.web.context.ServletContextAware;

import java.io.*;
import java.lang.String;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import javax.servlet.ServletContext;

import org.craftercms.commons.http.*;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.impl.v1.repository.AbstractContentRepository;

import reactor.core.Reactor;

import java.nio.file.*;
import java.nio.file.attribute.*;
import static java.nio.file.StandardCopyOption.*;
import java.io.IOException;
/**
 * Disk repository implementation. 
 * @author russdanner
 *
 */
public class DiskContentRepository extends AbstractContentRepository implements ServletContextAware{

    private static final Logger logger = LoggerFactory.getLogger(DiskContentRepository.class);


    @Override
    public InputStream getContent(String path) throws ContentNotFoundException {
        InputStream retStream = null;

        try {
            File file = constructRepoPath(path).toFile();
            retStream = new BufferedInputStream(FileUtils.openInputStream(file));
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
            try {
                Files.createDirectories(constructRepoPath(path.substring(0, path.lastIndexOf("/") ) ) );
            }
            catch(Exception err) {
            }

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
            Files.createDirectories(constructRepoPath(path, name));
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
            File file = constructRepoPath(path).toFile();
            FileUtils.deleteQuietly(file);

        }
        catch(Exception err) {
            // log this error
            logger.error("error while deleting content", err);
            success = false;
        }

        return success;
    }

    @Override
    public boolean copyContent(String fromPath, String toPath) {
        
        boolean success = true;

        try {
            Path source = constructRepoPath(fromPath);
            Path target = constructRepoPath(toPath);
            TreeCopier tc = new TreeCopier(source, target, false, false);
            EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
            Files.walkFileTree(source, opts, Integer.MAX_VALUE, tc);
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
            FileUtils.moveFileToDirectory(constructRepoPath(fromPath).toFile(), constructRepoPath(toPath).toFile(), true);
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
            EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
            final String finalPath = path;
            Files.walkFileTree(constructRepoPath(finalPath), opts, 1, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path visitPath, BasicFileAttributes attrs)
                        throws IOException {

                    RepositoryItem item = new RepositoryItem();
                    item.name = visitPath.toFile().getName();


                    String visitFolderPath = visitPath.toString();//.replace("/index.xml", "");
                    //Path visitFolder = constructRepoPath(visitFolderPath);
                    item.isFolder = visitPath.toFile().isDirectory();
                    int lastIdx = visitFolderPath.lastIndexOf("/"+item.name);
                    if (lastIdx > 0) {
                        item.path = visitFolderPath.substring(0, lastIdx);
                    }
                    //item.path = visitFolderPath.replace("/" + item.name, "");
                    item.path = item.path.replace(getRootPath(), "");
                    item.path = item.path.replace("/.xml", "");

                    if (!".DS_Store".equals(item.name)) {
                        logger.debug("ITEM NAME: {0}", item.name);
                        logger.debug("ITEM PATH: {0}", item.path);
                        logger.debug("ITEM FOLDER: ({0}): {1}", visitFolderPath, item.isFolder);
                        retItems.add(item);
                    }

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
        final List<VersionTO> versionList = new ArrayList<VersionTO>();

        try {
            final String pathToContent = path.substring(0, path.lastIndexOf("/"));
            final String filename = path.substring(path.lastIndexOf("/")+1);
 
            Path versionPath = constructVersionRepoPath(pathToContent);

            EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);

            Files.walkFileTree(versionPath, opts, 1, new SimpleFileVisitor<Path>() { 
                @Override
                public FileVisitResult visitFile(Path visitPath, BasicFileAttributes attrs)
                throws IOException {
                    String versionFilename = visitPath.toString();

                    if(versionFilename.contains(filename)) {
                        VersionTO version = new VersionTO();
                        String label = versionFilename.substring(versionFilename.lastIndexOf("--")+2);

                        BasicFileAttributes attr = Files.readAttributes(visitPath, BasicFileAttributes.class);

                        version.setVersionNumber(label);
                        version.setLastModifier("ADMIN");
                        version.setLastModifiedDate(new Date(attr.lastModifiedTime().toMillis()));  
                        version.setComment("");

                        versionList.add(version);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch(Exception err) {
            logger.error("error while getting history for content item " + path, err);
        }

        VersionTO[] versions = new VersionTO[versionList.size()];
        versions = versionList.toArray(versions);
        return versions;
    }

    /**
     * create a version
     * @param path location of content
     * @param majorVersion true if major
     * @return the created version ID or null on failure
     */
    public String createVersion(String path, boolean majorVersion) {
        String versionId = null;

        synchronized(path) {
            versionId = determineNextVersionLabel(path, majorVersion);
            InputStream content = null;

            try {
                content = getContent(path);
                String versionPath = path+"--"+versionId;

                CopyOption options[] = { StandardCopyOption.REPLACE_EXISTING };

                String pathToContent = versionPath.substring(0, versionPath.lastIndexOf("/"));
                Files.createDirectories(constructVersionRepoPath(pathToContent));

                Files.copy(content, constructVersionRepoPath(versionPath), options);
            }
            catch(Exception err) {
                logger.error("error versionign file: "+path, err);
                versionId = null;
            }
            finally {
                closeInputStreamQuietly(content);
            }
        }

        return versionId;
    }

    /** 
     * revert a version (create a new version based on an old version)
     * @param path - the path of the item to "revert"
     * @param version - old version ID to base to version on
     */
    public boolean revertContent(String path, String label, boolean major, String comment) {
        boolean success = false;

        synchronized(path) {
            String versionId = determineNextVersionLabel(path, major);
            InputStream versionContent = null;
            InputStream wipContent = null;
            
            try {
                versionContent = getVersionedContent(path, label);
                String versionPath = path+"--"+versionId;

                CopyOption options[] = { StandardCopyOption.REPLACE_EXISTING };
                String pathToContent = versionPath.substring(0, versionPath.lastIndexOf("/"));
                Files.createDirectories(constructVersionRepoPath(pathToContent));
                Files.copy(versionContent, constructVersionRepoPath(versionPath), options);

                // write the repo content
                wipContent = getVersionedContent(path, label);
                Files.copy(wipContent, constructRepoPath(path), options);
            }
            catch(Exception err) {
                logger.error("error versionign file: "+path, err);
                versionId = null;
            }
            finally {
                closeInputStreamQuietly(versionContent);
                closeInputStreamQuietly(wipContent);
            }

        }

        return success;
    }

    public void lockItem(String site, String path) {
    }

    public void unLockItem(String site, String path) {
    }


    protected InputStream getVersionedContent(String path, String label) 
    throws ContentNotFoundException {
        InputStream retStream = null;

        try {
            OpenOption options[] = { StandardOpenOption.READ };
            retStream = Files.newInputStream(constructVersionRepoPath(path+"--"+label));
        }

        catch(Exception err) {
            throw new ContentNotFoundException("error while opening file", err);
        }

        return retStream;
    }

    /**
     * determine the next version label based on what's in the current version store
     * @param path path to item
     * @param majorVersion true if version is major
     * @return next label
     */
    protected String determineNextVersionLabel(String path, boolean majorVersion) {
        String versionId = null;

        VersionTO[] versions = getContentVersionHistory(path);

        if(versions.length != 0) {
            VersionTO latestVersion = versions[versions.length-1];

            String label = latestVersion.getVersionNumber();
            String[] labelParts = label.split("\\.");
            int major = Integer.parseInt(labelParts[0]);
            int minor = Integer.parseInt(labelParts[1]);

            if(majorVersion) {
                versionId = (major+1) + ".0";
            }
            else {
                versionId = major + "." + (minor+1);
            }                
        }
        else {
            if(majorVersion) {
                versionId = "1.0";
            }
            else {
                versionId = "0.1";
            }
        }

        return versionId;
    }

    /**
     * bootstrap the repository
     */
    public void bootstrap() throws Exception {
        Path cstudioFolder = constructRepoPath("cstudio");
        boolean bootstrapCheck = Files.exists(cstudioFolder);

        if(bootstrapCheck == false) {
            try{
                logger.error("Bootstrapping repository for Crafter CMS");
                Files.createDirectories(constructRepoPath());
            }
            catch(Exception alreadyExistsErr){
                // do nothing.
            }

            RequestContext context = RequestContext.getCurrent();
            //ServletContext servletContext = context.getServletContext();

            String bootstrapFolderPath = this.ctx.getRealPath("/repo-bootstrap/bootstrap.xml");
            bootstrapFolderPath = bootstrapFolderPath.replace("/bootstrap.xml", "");

            logger.info("Bootstrapping with baseline @ " + bootstrapFolderPath);
            Path source = java.nio.file.FileSystems.getDefault().getPath(bootstrapFolderPath);
            Path target = constructRepoPath();

            TreeCopier tc = new TreeCopier(source, target, false, false);
            EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
            Files.walkFileTree(source, opts, Integer.MAX_VALUE, tc);
        }
    }

    private ServletContext ctx;

    public void setServletContext(ServletContext ctx) {
    logger.debug("ServletContext: {0} ", ctx);
    this.ctx = ctx;
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

    protected boolean closeInputStreamQuietly(InputStream is) {
        boolean success = true;

        if(is != null) { 
            try { 
                is.close(); 
            } 
            catch(Exception ioErr) { 
                success = false;

                /* eat error */
                if(Logger.LEVEL_DEBUG.equals(logger.getLevel())) {
                    logger.error("Error while closing InputStream quietly", ioErr);
                } 
            } 
        } 

        return success;     
    }

    public Reactor getRepositoryReactor() { return repositoryReactor; }
    public void setRepositoryReactor(Reactor repositoryReactor) { this.repositoryReactor = repositoryReactor; }

    String rootPath;
    public String getRootPath() { return rootPath; }
    public void setRootPath(String path) { rootPath = path; }

    protected Reactor repositoryReactor;

    /**
     * A {@code FileVisitor} that copies a file-tree ("cp -r")
     */
    static class TreeCopier implements FileVisitor<Path> {
        private final Path source;
        private final Path target;
        private final boolean prompt;
        private final boolean preserve;

        static void copyFile(Path source, Path target, boolean prompt, boolean preserve) {
            CopyOption[] options = (preserve) ?
                new CopyOption[] { COPY_ATTRIBUTES, REPLACE_EXISTING } :
                new CopyOption[] { REPLACE_EXISTING };
            
                try {
                    Files.copy(source, target, options);
                } catch (IOException x) {
                    logger.error("Unable to copy: %s: %s%n", source, x);
                }
            
        }
     
        TreeCopier(Path source, Path target, boolean prompt, boolean preserve) {
            this.source = source;
            this.target = target;
            this.prompt = prompt;
            this.preserve = preserve;
        }
 
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            // before visiting entries in a directory we copy the directory
            // (okay if directory already exists).
            CopyOption[] options = (preserve) ?
                new CopyOption[] { COPY_ATTRIBUTES } : new CopyOption[0];
 
            Path newdir = target.resolve(source.relativize(dir));
            try {
                Files.copy(dir, newdir, options);
            } catch (FileAlreadyExistsException x) {
                // ignore
            } catch (IOException x) {
                logger.error("Unable to create: %s: %s%n", newdir, x);
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }
 
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            TreeCopier.copyFile(file, target.resolve(source.relativize(file)),
                     prompt, preserve);
            return FileVisitResult.CONTINUE;
        }
 
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            // fix up modification time of directory when done
            if (exc == null && preserve) {
                Path newdir = target.resolve(source.relativize(dir));
                try {
                    FileTime time = Files.getLastModifiedTime(dir);
                    Files.setLastModifiedTime(newdir, time);
                } catch (IOException x) {
                    logger.error("Unable to copy all attributes to: %s: %s%n", newdir, x);
                }
            }
            return FileVisitResult.CONTINUE;
        }
 
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            if (exc instanceof FileSystemLoopException) {
                logger.error("cycle detected: " + file);
            } else {
                logger.error("Unable to copy: %s: %s%n", file, exc);
            }
            return FileVisitResult.CONTINUE;
        }
    }
}