/*
 * Crafter Studio
 *
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

package org.craftercms.studio.impl.v1.repository.git;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletContext;

import com.google.gdata.util.common.base.StringUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.LockFile;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.FS;
import org.springframework.web.context.ServletContextAware;

import static org.craftercms.studio.api.v1.constant.StudioConstants.BOOTSTRAP_REPO_GLOBAL_PATH;
import static org.craftercms.studio.api.v1.constant.StudioConstants.BOOTSTRAP_REPO_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.BOOTSTRAP_REPO;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.INITIAL_COMMIT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.*;

public class GitContentRepository implements ContentRepository, ServletContextAware {

    private static final Logger logger = LoggerFactory.getLogger(GitContentRepository.class);
    private GitContentRepositoryHelper helper = null;

    @Override
    public boolean contentExists(String site, String path) {
        Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories
            .SANDBOX);
        try {
            RevTree tree = helper.getTree(repo);
            TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree);
            if (tw != null && tw.getObjectId(0) != null) {
                return true;
            }
        } catch (IOException e) {
            logger.info("Content not found for site: " + site + " path: " + path, e);
        }
        return false;
    }

    @Override
    public InputStream getContent(String site, String path) throws ContentNotFoundException {
        logger.debug("getContent invoked with site: " + site + " path: " + path);
        InputStream toRet = null;
        try {
            Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories
                    .GLOBAL:GitRepositories.SANDBOX);

            RevTree tree = helper.getTree(repo);
            TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree);

            ObjectId id = tw.getObjectId(0);
            ObjectLoader objectLoader = repo.open(id);
            toRet = objectLoader.openStream();

        } catch (IOException e) {
            logger.error("Error while getting content for file at site: " + site + " path: " + path, e);
        }
        return toRet;
    }

    @Override
    public String writeContent(String site, String path, InputStream content) {
        // Write content to git and commit it
        String result = null;

        Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories
            .SANDBOX);

        if (repo != null) {
            // TODO: SJ: TODAY: handle errors and exceptions
            if (helper.writeFile(repo, site, path, content))
                result = helper.commitFile(repo, site, path);
        }

        return result;
    }

    @Override
    public boolean createFolder(String site, String path, String name) {
        boolean success = true;

        try {
            Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories
                .SANDBOX);
            RevTree tree = helper.getTree(repo);
            TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree);

            FS fs = FS.detect();
            File repoRoot = repo.getWorkTree();
            Path folderPath = Paths.get(fs.normalize(repoRoot.getPath()), tw.getPathString(), name);
            Path keepPath = Paths.get(folderPath.toString(), ".keep");
            Files.createDirectories(folderPath);
            File keep = Files.createFile(keepPath).toFile();
            String gitPath = Paths.get(tw.getPathString(), name, ".keep").toString();
            Git git = new Git(repo);
            git.add()
                    .addFilepattern(gitPath)
                    .call();

            PersonIdent currentUserIdent = helper.getCurrentUserIdent();

            RevCommit commit = git.commit()
                    .setOnly(helper.getGitPath(gitPath))
                    .setMessage(StringUtils.EMPTY)
                    .setCommitter(currentUserIdent)
                    .call();
        } catch (IOException | GitAPIException e) {
            logger.error("Error creating folder " + name + " for site " + site + " at path " + path, e);
            success = false;
        }

        return success;
    }

    @Override
    public boolean deleteContent(String site, String path) {
        try {
            Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories
                    .GLOBAL:GitRepositories.SANDBOX);
            Git git = new Git(repo);
            git.rm()
                    .addFilepattern(helper.getGitPath(path))
                    .setCached(false)
                    .call();

            RevCommit commit = git.commit()
                    .setOnly(helper.getGitPath(path))
                    .setMessage(StringUtils.EMPTY)
                    .call();
            return true;
        } catch (GitAPIException e) {
            logger.error("Error while deleting content for site: " + site + " path: " + path, e);
        }
        return false;
    }

    @Override
    public boolean moveContent(String site, String fromPath, String toPath) {
        return moveContent(site, fromPath, toPath, null);
    }

    @Override
    public boolean moveContent(String site, String fromPath, String toPath, String newName) {
        boolean success = true;

        try {
            Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories
                    .GLOBAL:GitRepositories.SANDBOX);
            String gitFromPath = helper.getGitPath(fromPath);
            RevTree fromTree = helper.getTree(repo);
            TreeWalk fromTw = TreeWalk.forPath(repo, gitFromPath, fromTree);

            String gitToPath = helper.getGitPath(toPath);
            RevTree toTree = helper.getTree(repo);
            TreeWalk toTw = TreeWalk.forPath(repo, gitToPath, toTree);

            // TODO: SJ: Look into the need (if any) for FS.detect()
            FS fs = FS.detect();
            File repoRoot = repo.getWorkTree();
            Path sourcePath = null;
            if (fromTw == null) {
                sourcePath = Paths.get(fs.normalize(repoRoot.getPath()), gitFromPath);
            } else {
                sourcePath = Paths.get(fs.normalize(repoRoot.getPath()), fromTw.getPathString());
            }
            Path targetPath = null;
            if (toTw == null) {
                targetPath = Paths.get(fs.normalize(repoRoot.getPath()), gitToPath);
            } else {
                targetPath = Paths.get(fs.normalize(repoRoot.getPath()), toTw.getPathString());
            }

            File source = sourcePath.toFile();
            File destDir = targetPath.toFile();
            File dest = destDir;
            if (StringUtils.isNotEmpty(newName)) {
                dest = new File(destDir, newName);
            }
            if (source.isDirectory()) {
                File[] dirList = source.listFiles();
                for (File file : dirList) {
                    if (file.isDirectory()) {
                        FileUtils.moveDirectoryToDirectory(file, dest, true);
                    } else {
                        FileUtils.moveFileToDirectory(file, dest, true);
                    }
                }
                source.delete();
            } else {
                if (dest.isDirectory()) {
                    FileUtils.moveFileToDirectory(source, dest, true);
                } else {
                    source.renameTo(dest);
                }
            }
            Git git = new Git(repo);
            git.add()
                    .addFilepattern(gitToPath)
                    .call();
            git.rm()
                    .addFilepattern(gitFromPath)
                    .call();
            RevCommit commit = git.commit()
                    .setOnly(gitFromPath)
                    .setOnly(gitToPath)
                    .setMessage(StringUtils.EMPTY)
                    .call();
        } catch (IOException | GitAPIException err) {
            // log this error
            logger.error("Error while moving content from {0} to {1}", err, fromPath, toPath);
            success = false;
        }

        return success;
    }

    @Override
    public boolean copyContent(String site, String fromPath, String toPath) {
        boolean success = true;

        try {
            Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories
                    .GLOBAL:GitRepositories.SANDBOX);
            String gitFromPath = helper.getGitPath(fromPath);
            RevTree fromTree = helper.getTree(repo);
            TreeWalk fromTw = TreeWalk.forPath(repo, gitFromPath, fromTree);

            String gitToPath = helper.getGitPath(toPath);
            RevTree toTree = helper.getTree(repo);
            TreeWalk toTw = TreeWalk.forPath(repo, gitToPath, toTree);

            FS fs = FS.detect();
            File repoRoot = repo.getWorkTree();
            Path sourcePath = null;
            if (fromTw == null) {
                sourcePath = Paths.get(fs.normalize(repoRoot.getPath()), gitFromPath);
            } else {
                sourcePath = Paths.get(fs.normalize(repoRoot.getPath()), fromTw.getPathString());
            }
            Path targetPath = null;
            if (toTw == null) {
                targetPath = Paths.get(fs.normalize(repoRoot.getPath()), gitToPath);
            } else {
                targetPath = Paths.get(fs.normalize(repoRoot.getPath()), toTw.getPathString());
            }
            File sourceFile = sourcePath.toFile();
            if (sourceFile.isDirectory()) {
                FileUtils.copyDirectory(sourceFile, targetPath.toFile());
            } else {
                FileUtils.copyFileToDirectory(sourceFile, targetPath.toFile());
            }
            Git git = new Git(repo);
            git.add()
                    .addFilepattern(gitToPath)
                    .call();
        } catch (IOException | GitAPIException err) {
            // log this error
            logger.error("Error while copping content from {0} to {1}", err, fromPath, toPath);
            success = false;
        }

        return success;
    }

    @Override
    public RepositoryItem[] getContentChildren(String site, String path) {
        final List<RepositoryItem> retItems = new ArrayList<RepositoryItem>();
        try {
            Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories
                    .GLOBAL:GitRepositories.SANDBOX);
            RevTree tree = helper.getTree(repo);
            TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree);
            if (tw != null) {
                ObjectLoader loader = repo.open(tw.getObjectId(0));
                if (loader.getType() == Constants.OBJ_TREE) {
                    int depth = tw.getDepth();
                    tw.enterSubtree();
                    while (tw.next()) {
                        if (tw.getDepth() == depth + 1) {

                            RepositoryItem item = new RepositoryItem();
                            item.name = tw.getNameString();

                            String visitFolderPath = "/" + tw.getPathString();
                            loader = repo.open(tw.getObjectId(0));
                            item.isFolder = loader.getType() == Constants.OBJ_TREE;
                            int lastIdx = visitFolderPath.lastIndexOf(File.separator + item.name);
                            if (lastIdx > 0) {
                                item.path = visitFolderPath.substring(0, lastIdx);
                            }

                            if (!ArrayUtils.contains(IGNORE_FILES, item.name)) {
                                retItems.add(item);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error while getting children for site: " + site + " path: " + path, e);
        }

        RepositoryItem[] items = new RepositoryItem[retItems.size()];
        items = retItems.toArray(items);
        return items;
    }

    @Override
    public RepositoryItem[] getContentChildren(String site, String path, boolean ignoreCache) {
        return getContentChildren(site, path);
    }

    @Override
    public VersionTO[] getContentVersionHistory(String site, String path) {
        List<VersionTO> versionHistory = new ArrayList<VersionTO>();
        try {
            Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories
                    .GLOBAL:GitRepositories.SANDBOX);
            ObjectId head = repo.resolve(Constants.HEAD);
            String gitPath = helper.getGitPath(path);
            Git git = new Git(repo);
            Iterable<RevCommit> commits = git.log()
                    .add(head)
                    .addPath(gitPath)
                    .call();
            Iterator<RevCommit> iterator = commits.iterator();
            while (iterator.hasNext()) {
                RevCommit revCommit = iterator.next();
                VersionTO versionTO = new VersionTO();
                versionTO.setVersionNumber(revCommit.getId().toString());
                versionTO.setLastModifier(revCommit.getAuthorIdent().getName());
                versionTO.setLastModifiedDate(new Date(revCommit.getCommitTime()*1000));
                versionTO.setComment(revCommit.getShortMessage());
                versionHistory.add(versionTO);
            }
        } catch (IOException | GitAPIException err) {
            logger.error("error while getting history for content item " + path);
        }
        VersionTO[] toRet = new VersionTO[versionHistory.size()];
        return versionHistory.toArray(toRet);
    }

    @Override
    public String createVersion(String site, String path, boolean majorVersion) {
        return createVersion(site, path, StringUtils.EMPTY, majorVersion);
    }

    @Override
    public String createVersion(String site, String path, String comment, boolean majorVersion) {
        // SJ: Will ignore minor revisions since git handles that via write/commit
        // SJ: Major revisions become git tags
        String toRet = StringUtils.EMPTY;

        if (majorVersion) {
            try {
                // Tag the repository with a date-time based version label
                Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories
                    .GLOBAL:GitRepositories.PUBLISHED);

                String gitPath = helper.getGitPath(path);
                Git git = new Git(repo);

                PersonIdent currentUserIdent = helper.getCurrentUserIdent();
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                String versionLabel = dateFormat.format(cal);

                TagCommand tagCommand = git.tag().setName(versionLabel).setMessage(comment).setTagger(currentUserIdent);
                tagCommand.call();

                toRet = versionLabel;
            } catch (GitAPIException err) {
                logger.error("error creating new version for site:  " + site + " path: " + path, err);
            }
        } else {
            logger.error("request to create minor revision ignored for site: " + site + " path: " + path);
        }
        return toRet;

    }

    @Override
    public boolean revertContent(String site, String path, String version, boolean major, String comment) {
        boolean success = false;
        try {
            InputStream versionContent = getContentVersion(site, path, version);
            writeContent(site, path, versionContent);
            createVersion(site, path, major);
            success = true;
        } catch (ContentNotFoundException err) {
            logger.error("error reverting content for site:  " + site + " path: " + path, err);
        }
        return success;
    }

    @Override
    public InputStream getContentVersion(String site, String path, String version) throws ContentNotFoundException {
        InputStream toRet = null;
        try {
            Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories
                    .GLOBAL:GitRepositories.SANDBOX);
            RevTree tree = helper.getTreeForCommit(repo, version);
            TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree);

            ObjectId id = tw.getObjectId(0);
            ObjectLoader objectLoader = repo.open(id);
            toRet = objectLoader.openStream();

        } catch (IOException err) {
            logger.error("Error while getting content for file at site: " + site + " path: " + path, err);
        }
        return toRet;
    }

    @Override
    public Date getModifiedDate(String site, String path)
    {
        throw new RuntimeException("Method not implemented.");
    }

    @Override
    public void lockItem(String site, String path) {
        Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories
            .SANDBOX);

        try (TreeWalk tw = new TreeWalk(repo)) {
            RevTree tree = helper.getTree(repo);
            tw.addTree(tree); // tree ‘0’
            tw.setRecursive(false);
            tw.setFilter(PathFilter.create(path));

            if (!tw.next()) {
                return;
            }

            File repoRoot = repo.getWorkTree();
            Paths.get(repoRoot.getPath(), tw.getPathString());
            File file = new File(tw.getPathString());
            LockFile lock = new LockFile(file);
            lock.lock();

            tw.close();

        } catch (IOException e) {
            logger.error("Error while locking file for site: " + site + " path: " + path, e);
        }
    }

    @Override
    public void unLockItem(String site, String path) {
        Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories
            .SANDBOX);

        try (TreeWalk tw = new TreeWalk(repo)) {
            RevTree tree = helper.getTree(repo);
            tw.addTree(tree); // tree ‘0’
            tw.setRecursive(false);
            tw.setFilter(PathFilter.create(path));

            if (!tw.next()) {
                return;
            }

            File repoRoot = repo.getWorkTree();
            Paths.get(repoRoot.getPath(), tw.getPathString());
            File file = new File(tw.getPathString());
            LockFile lock = new LockFile(file);
            lock.unlock();

            tw.close();

        } catch (IOException e) {
            logger.error("Error while unlocking file for site: " + site + " path: " + path, e);
        }
    }

    /**
     * bootstrap the repository
     */
    public void bootstrap() throws Exception {
        // Initialize the helper
        helper = new GitContentRepositoryHelper(studioConfiguration, securityProvider);

        if (Boolean.parseBoolean(studioConfiguration.getProperty(BOOTSTRAP_REPO))) {
            if (helper.createGlobalRepo()) {
                // Copy the global config defaults to the global site
                // Build a path to the bootstrap repo (the repo that ships with Studio)
                String bootstrapFolderPath = this.ctx.getRealPath(File.separator + BOOTSTRAP_REPO_PATH + File
                        .separator + BOOTSTRAP_REPO_GLOBAL_PATH);
                Path source = java.nio.file.FileSystems.getDefault().getPath(bootstrapFolderPath);

                logger.info("Bootstrapping with baseline @ " + source.toFile().toString());

                // Copy the bootstrap repo to the global repo
                Path globalConfigPath = helper.buildRepoPath(GitRepositories.GLOBAL);
                TreeCopier tc = new TreeCopier(source,
                    globalConfigPath);
                EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
                Files.walkFileTree(source, opts, Integer.MAX_VALUE, tc);

                try {
                    Repository globalConfigRepo = helper.getRepository(StringUtil.EMPTY_STRING, GitRepositories.GLOBAL);

                    Git git = new Git(globalConfigRepo);

                    Status status = git.status().call();

                    if (status.hasUncommittedChanges() || !status.isClean()) {
                        // Commit everything
                        // TODO: Consider what to do with the commitId in the future
                        git.add().addFilepattern(GIT_COMMIT_ALL_ITEMS).call();
                        git.commit().setMessage(INITIAL_COMMIT).call();
                    }
                } catch (GitAPIException err) {
                    logger.error("error creating initial commit for global configuration", err);
                }
            }
        }

        // Create global repository object
        if (!helper.buildGlobalRepo()) {
            logger.error("Failed to create global repository!");
        }
    }

    @Override
    public boolean createSiteFromBlueprint(String blueprintName, String siteId) {
        boolean toReturn;

        // create git repository for site content
        toReturn = helper.createSiteGitRepo(siteId);

        if (toReturn) {
            // copy files from blueprint
            toReturn = helper.copyContentFromBlueprint(blueprintName, siteId);
        }

        if (toReturn) {
            // commit everything so it is visible
            toReturn = helper.performInitialCommit(siteId, INITIAL_COMMIT);
        }

        return toReturn;
    }

    public void setServletContext(ServletContext ctx) { this.ctx = ctx; }

    public SecurityProvider getSecurityProvider() {
        return securityProvider;
    }

    public void setSecurityProvider(final SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }
    public void setStudioConfiguration(final StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    ServletContext ctx;
    SecurityProvider securityProvider;
    StudioConfiguration studioConfiguration;
}
