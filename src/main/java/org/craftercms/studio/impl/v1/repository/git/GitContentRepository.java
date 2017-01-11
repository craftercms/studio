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
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.ServletContext;

import com.google.gdata.util.common.base.StringUtil;
import freemarker.template.utility.DateUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.constant.RepoOperation;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.objectstate.TransitionEvent;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.api.v1.to.RepoOperationTO;
import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.internal.storage.file.LockFile;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.springframework.web.context.ServletContextAware;

import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.BOOTSTRAP_REPO_GLOBAL_PATH;
import static org.craftercms.studio.api.v1.constant.StudioConstants.BOOTSTRAP_REPO_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.BOOTSTRAP_REPO;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.EMPTY_FILE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_COMMIT_ALL_ITEMS;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.IGNORE_FILES;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.INITIAL_COMMIT;

public class GitContentRepository implements ContentRepository, ServletContextAware {

    private static final Logger logger = LoggerFactory.getLogger(GitContentRepository.class);
    private GitContentRepositoryHelper helper = null;

    private final static Map<String, ReentrantLock> repositoryLocks = new HashMap<String, ReentrantLock>();

    @Override
    public boolean contentExists(String site, String path) {
        boolean toReturn = false;
        Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories
            .SANDBOX);

        try {
            RevTree tree = helper.getTreeForLastCommit(repo);
            try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree)) {
                // Check if the array of items is not null, and since we have an absolute path to the item,
                // pick the first item in the list
                if (tw != null && tw.getObjectId(0) != null) {
                    toReturn = true;
                    tw.close();
                }
            } catch (IOException e) {
                logger.info("Content not found for site: " + site + " path: " + path, e);
            }
        } catch (IOException e) {
            logger.error("Failed to create RevTree for site: " + site + " path: " + path, e);
        }

        return toReturn;
    }

    @Override
    public InputStream getContent(String site, String path) throws ContentNotFoundException {
        InputStream toReturn = null;
        Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories
            .GLOBAL:GitRepositories.SANDBOX);

        try {
            RevTree tree = helper.getTreeForLastCommit(repo);
            try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree)) {
                // Check if the array of items is not null, and since we have an absolute path to the item,
                // pick the first item in the list
                if (tw != null && tw.getObjectId(0) != null) {
                    ObjectId id = tw.getObjectId(0);
                    ObjectLoader objectLoader = repo.open(id);
                    toReturn = objectLoader.openStream();
                    tw.close();
                }
            } catch (IOException e) {
                logger.error("Error while getting content for file at site: " + site + " path: " + path, e);
            }
        } catch (IOException e) {
            logger.error("Failed to create RevTree for site: " + site + " path: " + path, e);
        }

        return toReturn;
    }

    @Override
    public String writeContent(String site, String path, InputStream content) {
        // Write content to git and commit it
        String commitId = null;

        Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories
            .SANDBOX);

        if (repo != null) {
            if (helper.writeFile(repo, site, path, content))
                commitId = helper.commitFile(repo, site, path, "Wrote content " + path, helper.getCurrentUserIdent());
            else
                logger.error("Failed to write content site: " + site + " path: " + path);
        } else {
            logger.error("Missing repository during write for site: " + site + " path: " + path);
        }

        return commitId;
    }

    @Override
    public String createFolder(String site, String path, String name) {
        // TODO: SJ: Git doesn't care about empty folders, so we will create the folders and put a 0 byte file in them
        String commitId = null;
        boolean result;
        Path emptyFilePath = Paths.get(path, name, EMPTY_FILE);
        Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories.GLOBAL:GitRepositories
            .SANDBOX);

        try {
            // Create basic file
            File file = new File(repo.getDirectory().getParent(), emptyFilePath.toString());

            // Create parent folders
            File folder = file.getParentFile();
            if (folder != null) {
                if (!folder.exists()) {
                    folder.mkdirs();
                }
            }

            // Create the file
            if (!file.createNewFile()) {
                logger.error("error writing file: site: " + site + " path: " + emptyFilePath);
                result = false;
            } else {
                // Add the file to git
                try (Git git = new Git(repo)) {
                    git.add().addFilepattern(helper.getGitPath(emptyFilePath.toString())).call();

                    git.close();
                    result = true;
                } catch (GitAPIException e) {
                    logger.error("error adding file to git: site: " + site + " path: " + emptyFilePath, e);
                    result = false;
                }
            }
        } catch (IOException e) {
            logger.error("error writing file: site: " + site + " path: " + emptyFilePath, e);
            result = false;
        }

        if (result) {
            commitId = helper.commitFile(repo, site, emptyFilePath.toString(), "Created folder site: " + site + " "
                + "path: " +
                path, helper.getCurrentUserIdent());
        }

        return commitId;
    }

    @Override
    public String deleteContent(String site, String path) {
        String commitId = null;
        Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories
            .GLOBAL:GitRepositories.SANDBOX);

        try (Git git = new Git(repo)) {
            git.rm().addFilepattern(helper.getGitPath(path))
                    .setCached(false)
                    .call();

            // TODO: SJ: we need to define messages in a string table of sorts
            commitId = helper.commitFile(repo, site, path, "Delete file " + path,
                helper.getCurrentUserIdent());

            git.close();
        } catch (GitAPIException e) {
            logger.error("Error while deleting content for site: " + site + " path: " + path, e);
        }

        return commitId;
    }

    @Override
    public String moveContent(String site, String fromPath, String toPath) {
        return moveContent(site, fromPath, toPath, null);
    }

    @Override
    public String moveContent(String site, String fromPath, String toPath, String newName) {
        String commitId = null;
        Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories
            .GLOBAL:GitRepositories.SANDBOX);
        String gitFromPath = helper.getGitPath(fromPath);
        String gitToPath = helper.getGitPath(toPath + newName);

        try (Git git = new Git(repo)) {
            // Check if destination is a file, then this is a rename operation
            // Perform rename and exit
            Path sourcePath = Paths.get(repo.getDirectory().getParent(), fromPath);
            File sourceFile = sourcePath.toFile();
            Path targetPath = Paths.get(repo.getDirectory().getParent(), toPath);
            File targetFile = targetPath.toFile();
            if (targetFile.isFile()) {
                if (sourceFile.isFile()) {
                    sourceFile.renameTo(targetFile);
                } else {
                    // This is not a valid operation
                    logger.error("Invalid move operation: Trying to rename a directory to a file for site: " + site + " fromPath: " + fromPath + " toPath: " + toPath + " newName: " + newName);
                }
            } else if (sourceFile.isDirectory()) {
                // Check if we're moving a single file or whole subtree
                FileUtils.moveToDirectory(sourceFile, targetFile, true);
            }

            // The operation is done on disk, now it's time to commit
            git.add().addFilepattern(gitToPath).call();
            // git.rm().addFilepattern(gitFromPath).call();     // TODO: SJ: Delete this line after testing
            RevCommit commit = git.commit().setOnly(gitFromPath).setOnly(gitToPath).setAuthor(helper.getCurrentUserIdent()).setCommitter(helper.getCurrentUserIdent()).setMessage("Moving " + fromPath + " to " + toPath + newName).call();
            commitId = commit.getName();

            git.close();
        } catch (IOException | GitAPIException e) {
            logger.error("Error while moving content for site: " + site + " fromPath: " + fromPath + " toPath: " + toPath + " newName: " + newName);
        }

        return commitId;
    }

    @Override
    public String copyContent(String site, String fromPath, String toPath) {
        String commitId = null;
        Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories
            .GLOBAL:GitRepositories.SANDBOX);
        String gitFromPath = helper.getGitPath(fromPath);
        String gitToPath = helper.getGitPath(toPath);

        try (Git git = new Git(repo)) {
            Path sourcePath = Paths.get(repo.getDirectory().getParent(), fromPath);
            File sourceFile = sourcePath.toFile();
            Path targetPath = Paths.get(repo.getDirectory().getParent(), toPath);
            File targetFile = targetPath.toFile();

            // Check if we're copying a single file or whole subtree
            // TODO: SJ: This might not work, test and remove this comment after final code
            FileUtils.copyDirectory(sourceFile, targetFile);

            // The operation is done on disk, now it's time to commit
            git.add().addFilepattern(gitToPath).call();
            RevCommit commit = git.commit()
                .setOnly(gitFromPath)
                .setOnly(gitToPath)
                .setAuthor(helper.getCurrentUserIdent())
                .setCommitter(helper.getCurrentUserIdent())
                .setMessage("Copying " + fromPath + " to " + toPath)
                .call();
            commitId = commit.getName();

            git.close();
        } catch (IOException | GitAPIException e){
            logger.error("Error while copying content for site: " +
                site + " fromPath: " + fromPath + " toPath: " + toPath + " newName: ");
        }

        return commitId;
    }

    @Override
    public RepositoryItem[] getContentChildren(String site, String path) {
        // TODO: SJ: Rethink this API call for 2.7.x
        final List<RepositoryItem> retItems = new ArrayList<RepositoryItem>();
        Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories
            .GLOBAL:GitRepositories.SANDBOX);

        try {
            RevTree tree = helper.getTreeForLastCommit(repo);
            try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree)) {
                if (tw != null) {
                    // Loop for all children and gather path of item excluding the item, file/folder name, and
                    // whether or not it's a folder
                    ObjectLoader loader = repo.open(tw.getObjectId(0));
                    if (loader.getType() == Constants.OBJ_TREE) {
                        int depth = tw.getDepth();
                        tw.enterSubtree();
                        while (tw.next()) {
                            if (tw.getDepth() == depth + 1) {

                                RepositoryItem item = new RepositoryItem();
                                item.name = tw.getNameString();

                                String visitFolderPath = File.separator + tw.getPathString();
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
                        tw.close();
                    } else {
                        logger.error("Error getChildren invoked for a file for site: " + site + " path: " + path);
                    }
                }
            } catch (IOException e) {
                logger.error("Error while getting children for site: " + site + " path: " + path, e);
            }
        } catch (IOException e) {
            logger.error("Failed to create RevTree for site: " + site + " path: " + path, e);
        }

        RepositoryItem[] items = new RepositoryItem[retItems.size()];
        items = retItems.toArray(items);
        return items;
    }

    @Override
    public VersionTO[] getContentVersionHistory(String site, String path) {
        List<VersionTO> versionHistory = new ArrayList<VersionTO>();
        Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories
            .GLOBAL:GitRepositories.SANDBOX);

        try {
            ObjectId head = repo.resolve(Constants.HEAD);
            String gitPath = helper.getGitPath(path);
            try (Git git = new Git(repo)) {
                Iterable<RevCommit> commits = git.log().add(head).addPath(gitPath).call();
                Iterator<RevCommit> iterator = commits.iterator();
                while (iterator.hasNext()) {
                    RevCommit revCommit = iterator.next();
                    VersionTO versionTO = new VersionTO();
                    versionTO.setVersionNumber(revCommit.getName());
                    versionTO.setLastModifier(revCommit.getAuthorIdent().getName());
                    versionTO.setLastModifiedDate(new Date(revCommit.getCommitTime() * 1000));
                    versionTO.setComment(revCommit.getFullMessage());
                    versionHistory.add(versionTO);
                }

                git.close();
            } catch (IOException e) {
                logger.error("error while getting history for content item " + path);
            }
        } catch (IOException | GitAPIException e) {
            logger.error("Failed to create Git repo for site: " + site + " path: " + path, e);
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
        // TODO: SJ: Redesign/refactor the whole approach in 2.7.x
        String toReturn = StringUtils.EMPTY;

        if (majorVersion) {
            Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories
                .GLOBAL:GitRepositories.PUBLISHED);
                // Tag the repository with a date-time based version label
                String gitPath = helper.getGitPath(path);

            try (Git git = new Git(repo)) {
                PersonIdent currentUserIdent = helper.getCurrentUserIdent();
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                String versionLabel = dateFormat.format(cal);

                TagCommand tagCommand = git.tag()
                    .setName(versionLabel)
                    .setMessage(comment)
                    .setTagger(currentUserIdent);
                tagCommand.call();

                toReturn = versionLabel;

                git.close();
            } catch (GitAPIException err) {
                logger.error("error creating new version for site:  " + site + " path: " + path, err);
            }
        } else {
            logger.info("request to create minor revision ignored for site: " + site + " path: " + path);
        }

        return toReturn;
    }

    @Override
    public String revertContent(String site, String path, String version, boolean major, String comment) {
        // TODO: SJ: refactor to remove the notion of a major/minor for 2.7.x
        String commitId = null;

        try {
            InputStream versionContent = getContentVersion(site, path, version);
            commitId = writeContent(site, path, versionContent);
            createVersion(site, path, major);
        } catch (ContentNotFoundException err) {
            logger.error("error reverting content for site:  " + site + " path: " + path, err);
        }

        return commitId;
    }

    @Override
    public InputStream getContentVersion(String site, String path, String version) throws ContentNotFoundException {
        InputStream toReturn = null;

        Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories
            .GLOBAL: SANDBOX);

        try {
            RevTree tree = helper.getTreeForCommit(repo, version);
            try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree)) {
                if (tw != null) {
                    ObjectId id = tw.getObjectId(0);
                    ObjectLoader objectLoader = repo.open(id);
                    toReturn = objectLoader.openStream();
                    tw.close();
                }
            } catch (IOException e) {
                logger.error("Error while getting content for file at site: " + site + " path: " + path + " version:"
                    + " " + version, e);
            }
        } catch (IOException e) {
            logger.error("Failed to create RevTree for site: " + site + " path: " + path + " version: " + version, e);
        }

        return toReturn;
    }

    @Override
    public Date getModifiedDate(String site, String path)
    {
        throw new RuntimeException("Method not implemented.");
    }

    @Override
    public void lockItem(String site, String path) {
        Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories.GLOBAL:
                SANDBOX);

        try (TreeWalk tw = new TreeWalk(repo)) {
            RevTree tree = helper.getTreeForLastCommit(repo);
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
        Repository repo = helper.getRepository(site, StringUtil.isEmpty(site)? GitRepositories.GLOBAL:
                SANDBOX);

        try (TreeWalk tw = new TreeWalk(repo)) {
            RevTree tree = helper.getTreeForLastCommit(repo);
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

                Repository globalConfigRepo = helper.getRepository(StringUtil.EMPTY_STRING, GitRepositories.GLOBAL);
                try (Git git = new Git(globalConfigRepo)) {

                    Status status = git.status().call();

                    if (status.hasUncommittedChanges() || !status.isClean()) {
                        // Commit everything
                        // TODO: Consider what to do with the commitId in the future
                        git.add().addFilepattern(GIT_COMMIT_ALL_ITEMS).call();
                        git.commit().setMessage(INITIAL_COMMIT).call();
                    }

                    git.close();
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
            // update site name variable inside config files
            toReturn = helper.updateSitenameConfigVar(siteId);
        }

        if (toReturn) {
            // commit everything so it is visible
            toReturn = helper.performInitialCommit(siteId, INITIAL_COMMIT);
        }

        return toReturn;
    }

    @Override
    public boolean deleteSite(String siteId) {
        boolean toReturn;

        toReturn = helper.deleteSiteGitRepo(siteId);

        return toReturn;
    }

    @Override
    public void publish(String site, List<String> commitIds, String environment, String author, String comment) {
        Repository repo = helper.getRepository(site, GitRepositories.PUBLISHED);
        lockTheWorld(site);
        try (Git git = new Git(repo)) {
            // fetch "origin/master"
            FetchResult fetchResult = git.fetch().setRemote(Constants.DEFAULT_REMOTE_NAME).call();

            // checkout environment branch
            Ref checkoutResult = git.checkout().setCreateBranch(true).setName(environment).call();

            // cherry pick all commit ids
            CherryPickCommand cherryPickCommand = git.cherryPick().setStrategy(MergeStrategy.THEIRS);
            for (String commitId : commitIds) {
                ObjectId objectId = ObjectId.fromString(commitId);
                cherryPickCommand.include(objectId);
            }
            CherryPickResult cherryPickResult = cherryPickCommand.call();

            // tag
            PersonIdent authorIdent = helper.getAuthorIdent(author);
            Ref tagResult = git.tag().setTagger(authorIdent).setMessage(comment).call();
        } catch (GitAPIException e) {
            logger.error("Error when publishing site " + site + " to environment " + environment, e);
        } finally {
            unlockTheWorld(site);
        }

    }

    private void lockTheWorld(String site) {
        ReentrantLock repositoryLock = null;
        synchronized (repositoryLocks) {
            if (repositoryLocks.containsKey(site)) {
                repositoryLock = repositoryLocks.get(site);
            } else {
                repositoryLock = new ReentrantLock();
                repositoryLocks.put(site, repositoryLock);
            }
            repositoryLock.lock();
        }
    }

    private void unlockTheWorld(String site) {
        ReentrantLock repositoryLock = null;
        synchronized (repositoryLocks) {
            repositoryLock = repositoryLocks.get(site);
            if (repositoryLock != null) {
                repositoryLock.unlock();
            }
        }
    }

    @Override
    public List<RepoOperationTO> getOperations(String site, String commitIdFrom, String commitIdTo) {
        List<RepoOperationTO> operations = new ArrayList<>();

        try {
            // Get the sandbox repo, and then get a reference to the commitId we received and another for head
            Repository repo = helper.getRepository(site, SANDBOX);
            ObjectId objCommitIdFrom = repo.resolve(commitIdFrom);
            ObjectId objCommitIdTo = repo.resolve(commitIdTo);

            // If the commitIdFrom is the same as commitIdTo, there is nothing to calculate, otherwise, let's do it
            if  (!objCommitIdFrom.equals(objCommitIdTo)) {
                // Compare HEAD with commitId we're given
                // Get list of commits between commitId and HEAD in chronological order
                try (Git git = new Git(repo)) {
                    // Get the log of all the commits between commitId and head
                    Iterable<RevCommit> commits = git.log()
                            .addRange(objCommitIdFrom, objCommitIdTo)
                            .call();

                    // Loop through through the commits and diff one from the next util head
                    ObjectId prevCommitId = objCommitIdFrom;
                    ObjectId nextCommitId = objCommitIdFrom;

                    Iterator<RevCommit> iterator = commits.iterator();
                    while (iterator.hasNext()) {
                        RevCommit commit = iterator.next();
                        nextCommitId = commit.getId();

                        RevTree prevTree = helper.getTreeForCommit(repo, prevCommitId.getName());
                        RevTree nextTree = helper.getTreeForCommit(repo, nextCommitId.getName());

                        try (ObjectReader reader = repo.newObjectReader()) {
                            CanonicalTreeParser prevCommitTreeParser = new CanonicalTreeParser();
                            CanonicalTreeParser nextCommitTreeParser = new CanonicalTreeParser();
                            prevCommitTreeParser.reset(reader, prevTree.getId());
                            nextCommitTreeParser.reset(reader, nextTree.getId());

                            // Diff the two commit Ids
                            List<DiffEntry> diffEntries = git.diff().setOldTree(prevCommitTreeParser).setNewTree
                                (nextCommitTreeParser).call();

                            // Now that we have a diff, let's itemize the file changes, pack them into a TO
                            // and add them to the list of RepoOperations to return to the caller
                            // also include date/time of commit by taking number of seconds and multiply by 1000 and
                            // convert to java date before sending over
                            operations.addAll(processDiffEntry(diffEntries, new Date(commit
                                .getCommitTime() * 1000)));
                            prevCommitId = nextCommitId;
                        }
                    }
                } catch (GitAPIException e) {
                    logger.error("Error getting operations for site " + site + " from commit ID: " + commitIdFrom +
                            " to commit ID: " + commitIdTo, e);
                }
            }
        } catch (IOException e) {
            logger.error("Error getting operations for site " + site + " from commit ID: " + commitIdFrom +
                " to commit ID: " + commitIdTo, e);
        }

        return operations;
    }

    @Override
    public String getRepoLastCommitId(final String site) {
        String toReturn = StringUtil.EMPTY_STRING;

        Repository repo = helper.getRepository(site, SANDBOX);
        try {
            ObjectId commitId = repo.resolve(Constants.HEAD);
            toReturn = commitId.getName();
        } catch (IOException e) {
            logger.error("Error getting last commit ID for site " + site, e);
        }

        return toReturn;
    }

    List<RepoOperationTO> processDiffEntry(List<DiffEntry> diffEntries, Date commitTime) {
        List<RepoOperationTO> toReturn = new ArrayList<RepoOperationTO>();

        for (DiffEntry diffEntry : diffEntries) {
            // TODO: SJ: remove these logs
            logger.error("-------------------------");
            logger.error("Diff new path: " + diffEntry.getNewPath());
            logger.error("Diff old path: " + diffEntry.getOldPath());
            logger.error("Diff change type: " + diffEntry.getChangeType());
            logger.error("-------------------------");

            // Update the paths to have a preceding separator
            String pathNew = File.separator +  diffEntry.getNewPath();
            String pathOld = File.separator +  diffEntry.getOldPath();

            RepoOperationTO repoOperation = null;
            switch (diffEntry.getChangeType()) {
                case ADD:
                    repoOperation = new RepoOperationTO(RepoOperation.CREATE, pathNew,
                        commitTime, null);
                    break;
                case MODIFY:
                    repoOperation = new RepoOperationTO(RepoOperation.UPDATE, pathNew,
                        commitTime, null);
                    break;
                case DELETE:
                    repoOperation = new RepoOperationTO(RepoOperation.DELETE, pathOld,
                        commitTime, null);
                    break;
                case RENAME:
                    repoOperation = new RepoOperationTO(RepoOperation.MOVE, pathOld,
                        commitTime, pathNew);
                    break;
                case COPY:
                    repoOperation = new RepoOperationTO(RepoOperation.COPY, pathNew,
                        commitTime, null);
                    break;
                default:
                    logger.error("Error: Unknown git operation " + diffEntry.getChangeType());
                    break;
            }

            toReturn.add(repoOperation);
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

    public ObjectStateService getObjectStateService() { return objectStateService; }
    public void setObjectStateService(ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

    public ObjectMetadataManager getObjectMetadataManager() { return objectMetadataManager; }
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) { this.objectMetadataManager = objectMetadataManager; }

    public DmDependencyService getDmDependencyService() { return dmDependencyService; }
    public void setDmDependencyService(DmDependencyService dmDependencyService) { this.dmDependencyService = dmDependencyService; }

    public ServicesConfig getServicesConfig() { return servicesConfig; }
    public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

    ServletContext ctx;
    SecurityProvider securityProvider;
    StudioConfiguration studioConfiguration;

    protected ObjectStateService objectStateService;
    protected ObjectMetadataManager objectMetadataManager;
    protected DmDependencyService dmDependencyService;
    protected ServicesConfig servicesConfig;
}
