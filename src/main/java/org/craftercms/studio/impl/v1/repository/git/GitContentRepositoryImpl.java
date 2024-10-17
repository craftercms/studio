/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

import jakarta.servlet.ServletContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.repository.GitContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.to.RemoteRepositoryInfoTO;
import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.api.v2.annotation.LogExecutionTime;
import org.craftercms.studio.api.v2.core.ContextManager;
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v2.service.security.SecurityService;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.internal.storage.file.LockFile;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.ServletContextAware;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.String.format;
import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static java.time.ZoneOffset.UTC;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.craftercms.studio.api.v1.constant.GitRepositories.*;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.*;
import static org.craftercms.studio.api.v2.utils.StudioUtils.getStudioTemporaryFilesRoot;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.*;
import static org.eclipse.jgit.api.ListBranchCommand.ListMode.REMOTE;
import static org.eclipse.jgit.lib.Constants.*;
import static org.eclipse.jgit.revwalk.RevSort.REVERSE;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.*;

public class GitContentRepositoryImpl implements GitContentRepository, ServletContextAware {

    private static final Logger logger = LoggerFactory.getLogger(GitContentRepositoryImpl.class);

    private static final String STUDIO_MANIFEST_LOCATION = "/META-INF/MANIFEST.MF";

    protected TextEncryptor encryptor;
    protected ServletContext ctx;
    protected StudioConfiguration studioConfiguration;
    protected ServicesConfig servicesConfig;
    protected RemoteRepositoryDAO remoteRepositoryDAO;
    protected SecurityService securityService;
    protected SiteDAO siteDao;
    protected ContextManager contextManager;
    protected GeneralLockService generalLockService;
    protected GitRepositoryHelper helper;
    protected ProcessedCommitsDAO processedCommitsDao;
    protected RetryingRepositoryOperationFacade retryingRepositoryOperationFacade;
    protected RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    @Override
    public boolean contentExists(String site, String path) {
        boolean toReturn = false;
        try {
            Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
            if (repo != null) {
                RevTree tree = helper.getTreeForLastCommit(repo);
                try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree)) {
                    // Check if the array of items is not null, and since we have an absolute path to the item,
                    // pick the first item in the list
                    if (tw != null && tw.getObjectId(0) != null) {
                        toReturn = true;
                        tw.close();
                    } else if (tw == null) {
                        String gitPath = helper.getGitPath(path);
                        if (StringUtils.isEmpty(gitPath) || gitPath.equals(".")) {
                            toReturn = true;
                        }
                    }
                } catch (IOException e) {
                    logger.debug("Content not found at site '{}' path '{}'", site, path, e);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to check for content existence at site '{}' path '{}'", site, path, e);
        }

        return toReturn;
    }

    @Override
    public boolean shallowContentExists(String site, String path) {
        return Files.exists(helper.buildRepoPath(SANDBOX, site).resolve(helper.getGitPath(path)));
    }

    protected InputStream shallowGetContent(String site, String path) throws ContentNotFoundException {
        Path filePath = helper.buildRepoPath(SANDBOX, site).resolve(helper.getGitPath(path));
        try {
            return new FileInputStream(filePath.toFile());
        } catch (FileNotFoundException e) {
            throw new ContentNotFoundException(format("Content not found at site '%s' path '%s'", site, path), e);
        }
    }

    @Override
    @LogExecutionTime
    public InputStream getContent(String site, String path, boolean shallow) throws ContentNotFoundException {
        if (shallow) {
            return shallowGetContent(site, path);
        }
        return getContentFromGit(site, path);
    }

    private InputStream getContentFromGit(String site, String path) throws ContentNotFoundException {
        InputStream toReturn = null;
        try {
            Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
            if (repo == null) {
                throw new ContentNotFoundException("Repository not found for site " + site);
            }
            RevTree tree = helper.getTreeForLastCommit(repo);
            try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree)) {
                // Check if the array of items is not null, and since we have an absolute path to the item,
                // pick the first item in the list
                if (tw != null && tw.getObjectId(0) != null) {
                    ObjectId id = tw.getObjectId(0);
                    ObjectLoader objectLoader = repo.open(id);
                    if (OBJ_BLOB == objectLoader.getType()) {
                        toReturn = objectLoader.openStream();
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to get content from site '{}' path '{}'", site, path, e);
        }

        return toReturn;
    }

    @Override
    public String writeContent(String siteId, String path, InputStream content) {
        // Write content to git and commit it
        String commitId = null;
        String gitLockKey = helper.getSandboxRepoLockKey(siteId, true);
        generalLockService.lock(gitLockKey);
        try {
            Repository repo = helper.getRepository(siteId, StringUtils.isEmpty(siteId)? GLOBAL: SANDBOX);
            if (repo != null) {
                if (helper.writeFile(repo, siteId, path, content)) {
                    PersonIdent user = helper.getCurrentUserIdent();
                    String username = securityService.getCurrentUser();
                    String comment = helper.getCommitMessage(REPO_SANDBOX_WRITE_COMMIT_MESSAGE)
                            .replace(REPO_COMMIT_MESSAGE_USERNAME_VAR, username)
                            .replace(REPO_COMMIT_MESSAGE_PATH_VAR, path);
                    commitId = helper.commitFiles(repo, siteId, comment, user, path);
                    if (commitId != null) {
                        insertProcessedCommitId(siteId, commitId);
                    }
                } else {
                    logger.error("Failed to write content to site '{}' path '{}'", siteId, path);
                }
            } else {
                logger.error("Missing repository during write for site '{}' path '{}'", siteId, path);
            }
        }  catch (ServiceLayerException | UserNotFoundException e) {
            logger.error("Failed to write content to site '{}' path '{}'", siteId, path, e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }

        return commitId;
    }

    @Override
    public String createFolder(String siteId, String path, String name) {
        // SJ: Git doesn't care about empty folders, so we will create the folders and put a 0 byte file in them
        String commitId = null;
        boolean result;
        String gitLockKey = helper.getSandboxRepoLockKey(siteId, true);
        generalLockService.lock(gitLockKey);
        try {
            Path emptyFilePath = Paths.get(path, name, EMPTY_FILE);
            Repository repo = helper.getRepository(siteId, StringUtils.isEmpty(siteId) ? GLOBAL : SANDBOX);

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
                    logger.error("Failed to write file to site '{}' path '{}'", siteId, emptyFilePath);
                    result = false;
                } else {
                    // Add the file to git
                    result = helper.addFiles(repo, siteId, emptyFilePath.toString());
                }
            } catch (Exception e) {
                logger.error("Failed to add file to git in site '{}' path '{}'", siteId, emptyFilePath, e);
                result = false;
            }

            if (result) {
                try {
                    commitId = helper.commitFiles(repo, siteId,
                                                    helper.getCommitMessage(REPO_CREATE_FOLDER_COMMIT_MESSAGE)
                                                        .replaceAll(PATTERN_SITE, siteId)
                                                        .replaceAll(PATTERN_PATH, path + FILE_SEPARATOR + name),
                                                    helper.getCurrentUserIdent(),
                                                    emptyFilePath.toString());
                    insertProcessedCommitId(siteId, commitId);
                } catch (ServiceLayerException | UserNotFoundException e) {
                    logger.error("Failed to commit file in site '{}' path '{}'", siteId, emptyFilePath, e);
                }
            }
        } finally {
            generalLockService.unlock(gitLockKey);
        }
        return commitId;
    }

    private String deleteParentFolder(Git git, Path parentFolder, boolean wasPage)
            throws GitAPIException, IOException {
        String parent = parentFolder.toString();
        String toRet = parent;
        String folderToDelete = helper.getGitPath(parent);
        Path toDelete = Paths.get(git.getRepository().getDirectory().getParent(), parent);
        if (toDelete.toFile().exists()) {
            List<String> dirs = Files.walk(toDelete).filter(x -> !x.equals(toDelete)).filter(Files::isDirectory)
                    .map(y -> y.getFileName().toString()).collect(Collectors.toList());
            List<String> files = Files.walk(toDelete, 1).filter(x -> !x.equals(toDelete)).filter(Files::isRegularFile)
                    .map(y -> y.getFileName().toString()).collect(Collectors.toList());
            if (wasPage ||
                    (CollectionUtils.isEmpty(dirs) &&
                            (CollectionUtils.isEmpty(files) || files.size() < 2 && files.get(0).equals(EMPTY_FILE)))) {
                if (CollectionUtils.isNotEmpty(dirs)) {
                    for (String child : dirs) {
                        Path childToDelete = Paths.get(folderToDelete, child);
                        deleteParentFolder(git, childToDelete, false);
                        RmCommand rmCommand = git.rm()
                                .addFilepattern(folderToDelete + FILE_SEPARATOR + child + FILE_SEPARATOR + "*")
                                .setCached(false);
                        retryingRepositoryOperationFacade.call(rmCommand);
                    }
                }

                if (CollectionUtils.isNotEmpty(files)) {
                    for (String child : files) {
                        RmCommand rmCommand = git.rm()
                                .addFilepattern(folderToDelete + FILE_SEPARATOR + child)
                                .setCached(false);
                        retryingRepositoryOperationFacade.call(rmCommand);
                    }
                }
            }
        }
        return toRet;
    }

    @Override
    public String moveContent(String siteId, String fromPath, String toPath, String newName) {
        String commitId = null;
        String gitLockKey = helper.getSandboxRepoLockKey(siteId, true);
        generalLockService.lock(gitLockKey);
        try {
            Repository repo = helper.getRepository(siteId, StringUtils.isEmpty(siteId) ? GLOBAL : SANDBOX);

            String gitFromPath = helper.getGitPath(fromPath);
            String gitToPath;
            if (StringUtils.isEmpty(newName)) {
                gitToPath = helper.getGitPath(toPath);
            } else {
                gitToPath = helper.getGitPath(toPath + FILE_SEPARATOR + newName);
            }

            try (Git git = new Git(repo)) {
                // Check if destination is a file, then this is a rename operation
                // Perform rename and exit
                Path sourcePath = Paths.get(repo.getDirectory().getParent(), gitFromPath);
                File sourceFile = sourcePath.toFile();
                Path targetPath = Paths.get(repo.getDirectory().getParent(), gitToPath);
                File targetFile = targetPath.toFile();

                if (sourceFile.getCanonicalFile().equals(targetFile.getCanonicalFile())) {
                    sourceFile.renameTo(targetFile);
                } else {
                    if (targetFile.isFile()) {
                        if (sourceFile.isFile()) {
                            sourceFile.renameTo(targetFile);
                        } else {
                            // This is not a valid operation
                            logger.error("Failed to move. Trying to rename a directory to a file " +
                                            "in site '{}' from path '{}' to path '{}' with name '{}'",
                                    siteId, fromPath, toPath, newName);
                        }
                    } else if (sourceFile.isDirectory()) {
                        // Check if we're moving a single file or whole subtree
                        File[] dirList = sourceFile.listFiles();
                        for (File child : dirList) {
                            if (!child.equals(sourceFile)) {
                                FileUtils.moveToDirectory(child, targetFile, true);
                            }
                        }
                        FileUtils.deleteDirectory(sourceFile);
                    } else {
                        if (sourceFile.isFile()) {
                            FileUtils.moveFile(sourceFile, targetFile);
                        } else {
                            FileUtils.moveToDirectory(sourceFile, targetFile, true);
                        }
                    }
                }

                // The operation is done on disk, now it's time to commit
                boolean result = helper.addFiles(repo, siteId, gitToPath);
                if (result) {
                    StatusCommand statusCommand = git.status().addPath(gitToPath);
                    Status gitStatus = retryingRepositoryOperationFacade.call(statusCommand);
                    List<String> changeSet = new ArrayList<>(gitStatus.getAdded().size() * 2);
                    PersonIdent user = helper.getCurrentUserIdent();
                    String commitMsg = helper.getCommitMessage(REPO_MOVE_CONTENT_COMMIT_MESSAGE)
                                                .replaceAll(PATTERN_FROM_PATH, fromPath)
                                                .replaceAll(PATTERN_TO_PATH,
                                                            toPath + (StringUtils.isNotEmpty(newName) ? newName : EMPTY));
                    for (String pathToCommit : gitStatus.getAdded()) {
                        String pathRemoved = pathToCommit.replace(gitToPath, gitFromPath);
                        changeSet.add(pathToCommit);
                        changeSet.add(pathRemoved);
                    }
                    commitId = helper.commitFiles(repo, siteId, commitMsg, user, changeSet.toArray(new String[0]));
                    insertProcessedCommitId(siteId, commitId);
                    return commitId;
                } else {
                    logger.error("Failed to move item in site '{}' from path '{}' to path '{}' with name '{}'",
                            siteId, fromPath, toPath, newName);
                }
            } catch (Exception e) {
                logger.error("Failed to move item in site '{}' from path '{}' to path '{}' with name '{}'",
                        siteId, fromPath, toPath, newName, e);
            }
        } finally {
            generalLockService.unlock(gitLockKey);
        }
        return commitId;
    }

    /**
     * Insert commit id into processed_commits table if the site exists
     *
     * @param siteId   site id
     * @param commitId commit id
     */
    private void insertProcessedCommitId(final String siteId, final String commitId) {
        Site site = siteDao.getSite(siteId);
        if (site != null) {
            retryingDatabaseOperationFacade.retry(() -> processedCommitsDao.insertCommit(site.getId(), commitId));
        }
    }

    @Override
    public RepositoryItem[] getContentChildren(String site, String path) {
        // TODO: SJ: Rethink this API call for 3.1+
        final List<RepositoryItem> retItems = new ArrayList<>();
        try {
            Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
            RevTree tree = helper.getTreeForLastCommit(repo);
            try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree)) {

                if (tw != null) {
                    // Loop for all children and gather path of item excluding the item, file/folder name, and
                    // whether or not it's a folder
                    ObjectLoader loader = repo.open(tw.getObjectId(0));
                    if (loader.getType() == OBJ_TREE) {
                        int depth = tw.getDepth();
                        tw.enterSubtree();
                        while (tw.next()) {
                            if (tw.getDepth() == depth + 1) {

                                RepositoryItem item = new RepositoryItem();
                                item.name = tw.getNameString();

                                String visitFolderPath = FILE_SEPARATOR + tw.getPathString();
                                loader = repo.open(tw.getObjectId(0));
                                item.isFolder = loader.getType() == OBJ_TREE;
                                int lastIdx = visitFolderPath.lastIndexOf(FILE_SEPARATOR + item.name);
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
                        logger.debug("Item at site '{}' path '{}' doesn't have any children",
                                site, path);
                    }
                } else {
                    String gitPath = helper.getGitPath(path);
                    if (StringUtils.isEmpty(gitPath) || gitPath.equals(".")) {
                        try (TreeWalk treeWalk = new TreeWalk(repo)) {
                            treeWalk.addTree(tree);

                            while (treeWalk.next()) {
                                RepositoryItem item = new RepositoryItem();
                                item.name = treeWalk.getNameString();

                                String visitFolderPath = FILE_SEPARATOR + treeWalk.getPathString();
                                ObjectLoader loader = repo.open(treeWalk.getObjectId(0));
                                item.isFolder = loader.getType() == OBJ_TREE;
                                int lastIdx = visitFolderPath.lastIndexOf(FILE_SEPARATOR + item.name);
                                if (lastIdx > 0) {
                                    item.path = visitFolderPath.substring(0, lastIdx);
                                } else {
                                    item.path = EMPTY;
                                }

                                if (!ArrayUtils.contains(IGNORE_FILES, item.name)) {
                                    retItems.add(item);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to get children at site '{}' path '{}'", site, path, e);
        }

        RepositoryItem[] items = new RepositoryItem[retItems.size()];
        items = retItems.toArray(items);
        return items;
    }

    @Override
    public VersionTO[] getContentVersionHistory(String site, String path) {
        List<VersionTO> versionHistory = new ArrayList<>();
        String gitLockKey = helper.getSandboxRepoLockKey(site, true);
        generalLockService.lock(gitLockKey);
        try {
            Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
            ObjectId head = repo.resolve(HEAD);
            String gitPath = helper.getGitPath(path);
            try (Git git = new Git(repo)) {
                LogCommand logCommand = git.log().add(head).addPath(gitPath);
                Iterable<RevCommit> commits = retryingRepositoryOperationFacade.call(logCommand);
                Iterator<RevCommit> iterator = commits.iterator();
                while (iterator.hasNext()) {
                    RevCommit revCommit = iterator.next();
                    VersionTO versionTO = new VersionTO();
                    versionTO.setVersionNumber(revCommit.getName());
                    versionTO.setLastModifier(revCommit.getAuthorIdent().getName());
                    versionTO.setLastModifiedDate(
                            Instant.ofEpochSecond(revCommit.getCommitTime()).atZone(UTC));
                    versionTO.setComment(revCommit.getFullMessage());
                    versionHistory.add(versionTO);
                }
            }
        } catch (IOException | GitAPIException e) {
            logger.error("Failed to get the history for item at site '{}' path '{}'", site, path, e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }

        VersionTO[] toRet = new VersionTO[versionHistory.size()];
        return versionHistory.toArray(toRet);
    }

    @Override
    public String createVersion(String site, String path, boolean majorVersion) {
        return createVersion(site, path, EMPTY, majorVersion);
    }

    @Override
    public String createVersion(String site, String path, String comment, boolean majorVersion) {
        // SJ: Will ignore minor revisions since git handles that via write/commit
        // SJ: Major revisions become git tags
        String toReturn = EMPTY;
        String gitLockKey = StringUtils.isEmpty(site) ?
                                GLOBAL_REPOSITORY_GIT_LOCK :
                                helper.getPublishedRepoLockKey(site);
        generalLockService.lock(gitLockKey);
        try {
            if (majorVersion) {
                Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : PUBLISHED);
                // Tag the repository with a date-time based version label

                try (Git git = new Git(repo)) {
                    PersonIdent currentUserIdent = helper.getCurrentUserIdent();
                    String versionLabel = DateUtils.formatCurrentTime("yyyy-MM-dd'T'HHmmssX");

                    TagCommand tagCommand = git.tag()
                            .setName(versionLabel)
                            .setMessage(comment)
                            .setTagger(currentUserIdent);

                    retryingRepositoryOperationFacade.call(tagCommand);

                    toReturn = versionLabel;

                } catch (GitAPIException | ServiceLayerException | UserNotFoundException e) {
                    logger.error("Failed to create a new version for site '{}' path '{}'", site, path, e);
                }
            } else {
                logger.info("Ignore the request to create a minor version for site '{}' path '{}' since " +
                        "we no longer support that mechanism of versioning", site, path);
            }
        } finally {
            generalLockService.unlock(gitLockKey);
        }

        return toReturn;
    }

    @Override
    public String revertContent(String site, String path, String version, boolean major, String comment) {
        // TODO: SJ: refactor to remove the notion of a major/minor for 3.1+
        String commitId = null;
        String gitLockKey = helper.getSandboxRepoLockKey(site);
        generalLockService.lock(gitLockKey);
        try {
            InputStream versionContent = getContentVersion(site, path, version);
            commitId = writeContent(site, path, versionContent);
            createVersion(site, path, major);
        } finally {
            generalLockService.unlock(gitLockKey);
        }

        return commitId;
    }

    private InputStream getContentVersion(String site, String path, String version) {
        InputStream toReturn = null;

        try {
            Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);

            RevTree tree = helper.getTreeForCommit(repo, version);
            if (tree != null) {
                try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree)) {
                    if (tw != null) {
                        ObjectId id = tw.getObjectId(0);
                        ObjectLoader objectLoader = repo.open(id);
                        toReturn = objectLoader.openStream();
                        tw.close();
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to get the content item at site '{}' path '{}' version '{}'",
                    site, path, version, e);
        }

        return toReturn;
    }

    @Override
    public void lockItemForPublishing(String site, String path) {
        String gitLockKey = helper.getPublishedRepoLockKey(site);
        Repository repo = helper.getRepository(site, PUBLISHED);
        generalLockService.lock(gitLockKey);
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

        } catch (IOException e) {
            logger.error("Failed to lock the item at site '{}' path '{}'", site, path, e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
    }

    @Override
    public void unLockItem(String site, String path) {
        String gitLockKey = helper.getSandboxRepoLockKey(site, true);
        Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
        generalLockService.lock(gitLockKey);
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

        } catch (IOException e) {
            logger.error("Failed to unlock the item at site '{}' path '{}'", site, path, e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
    }

    @Override
    public void unLockItemForPublishing(String site, String path) {
        String gitLockKey = helper.getPublishedRepoLockKey(site);
        Repository repo = helper.getRepository(site, PUBLISHED);
        generalLockService.lock(gitLockKey);
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

        } catch (IOException e) {
            logger.error("Failed to unlock the item at site '{}' path '{}'", site, path, e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
    }

    /**
     * bootstrap the global repository
     */
    @Order(1)
    @EventListener(ContextRefreshedEvent.class)
    public void bootstrap() throws Exception {
        logger.debug("Bootstrap the Global repository");

        boolean bootstrapRepo = Boolean.parseBoolean(studioConfiguration.getProperty(BOOTSTRAP_REPO));
        boolean isCreated = false;

        if (bootstrapRepo && !isCreated && helper.createGlobalRepo()) {
            // Copy the global config defaults to the global site
            // Build a path to the bootstrap repo (the repo that ships with Studio)
            String bootstrapFolderPath = this.ctx.getRealPath(FILE_SEPARATOR + BOOTSTRAP_REPO_PATH +
                    FILE_SEPARATOR + BOOTSTRAP_REPO_GLOBAL_PATH);
            Path source = java.nio.file.FileSystems.getDefault().getPath(bootstrapFolderPath);

            logger.info("Bootstrap with baseline @'{}'", source.toFile());

            // Copy the bootstrap repo to the global repo
            Path globalConfigPath = helper.buildRepoPath(GLOBAL);
            TreeCopier tc = new TreeCopier(source,
                    globalConfigPath);
            EnumSet<FileVisitOption> opts = EnumSet.of(FOLLOW_LINKS);
            Files.walkFileTree(source, opts, MAX_VALUE, tc);

            Path studioManifestLocation = Paths.get(this.ctx.getRealPath(STUDIO_MANIFEST_LOCATION));
            // TODO: SJ: Clean up string literals
            if (Files.exists(studioManifestLocation)) {
                FileUtils.copyFile(studioManifestLocation.toFile(),
                        Paths.get(globalConfigPath.toAbsolutePath().toString(),
                                studioConfiguration.getProperty(BLUE_PRINTS_PATH), "BLUEPRINTS.MF").toFile());
            }
            Repository globalConfigRepo = helper.getRepository(EMPTY, GLOBAL);
            try (Git git = new Git(globalConfigRepo)) {

                StatusCommand statusCommand = git.status();
                Status status = retryingRepositoryOperationFacade.call(statusCommand);

                if (status.hasUncommittedChanges() || !status.isClean()) {
                    // Commit everything
                    // TODO: Consider what to do with the commitId in the future
                    AddCommand addCommand = git.add().addFilepattern(GIT_COMMIT_ALL_ITEMS);
                    retryingRepositoryOperationFacade.call(addCommand);
                    CommitCommand commitCommand =
                            git.commit().setMessage(helper.getCommitMessage(REPO_INITIAL_COMMIT_COMMIT_MESSAGE));
                    retryingRepositoryOperationFacade.call(commitCommand);
                }
            } catch (GitAPIException e) {
                logger.error("Failed to create the initial commit for the global repository", e);
            }
        }

        // Create global repository object
        if (!helper.buildGlobalRepo()) {
            logger.error("Failed to create the global repository");
        }
    }

    @Override
    public boolean deleteSite(String site) {
        boolean toReturn;
        String gitLockKey = helper.getSandboxRepoLockKey(site, true);
        generalLockService.lock(gitLockKey);
        try {
            contextManager.destroyContext(site);
            Repository repository = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
            if (repository != null) {
                Repository publishedRepository = helper.getRepository(site, PUBLISHED);
                if (publishedRepository != null) {
                    String publishSiteGitLockKey = helper.getPublishedRepoLockKey(site);
                    generalLockService.tryLock(publishSiteGitLockKey);
                    try {
                        toReturn = helper.deleteSiteGitRepo(site);
                    } finally {
                        generalLockService.unlock(publishSiteGitLockKey);
                    }
                } else {
                    toReturn = helper.deleteSiteGitRepo(site);
                }
            } else {
                Path sitePath = Paths.get(studioConfiguration.getProperty(REPO_BASE_PATH),
                        studioConfiguration.getProperty(SITES_REPOS_PATH), site);
                try {
                    FileUtils.deleteDirectory(sitePath.toFile());
                    toReturn = true;
                } catch (IOException e) {
                    logger.error("Failed to delete the site '{}'", site, e);
                    toReturn = false;
                }
            }
        } finally {
            generalLockService.unlock(gitLockKey);
        }
        return toReturn;
    }

    @Override
    public String getRepoLastCommitId(final String site) {
        String toReturn = EMPTY;
        String gitLockKey = helper.getSandboxRepoLockKey(site);
        Repository repository = helper.getRepository(site, SANDBOX);
        if (repository != null) {
            generalLockService.lock(gitLockKey);
            try {
                ObjectId commitId = repository.resolve(HEAD);
                if (commitId != null) {
                    toReturn = commitId.getName();
                }
            } catch (IOException e) {
                logger.error("Failed to get the last commit ID from site '{}'", site, e);
            } finally {
                generalLockService.unlock(gitLockKey);
            }
        }

        return toReturn;
    }

    @Override
    public String getRepoFirstCommitId(final String site) {
        String toReturn = EMPTY;
        String gitLockKey = helper.getSandboxRepoLockKey(site, true);
        Repository repository = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
        if (repository != null) {
            generalLockService.lock(gitLockKey);
            try (RevWalk rw = new RevWalk(repository)) {
                ObjectId head = repository.resolve(HEAD);
                if (head != null) {
                    RevCommit root = rw.parseCommit(head);
                    rw.sort(REVERSE);
                    rw.markStart(root);
                    ObjectId first = rw.next();
                    toReturn = first.getName();
                    logger.debug("The first commit ID for site '{}' is '{}'", site, toReturn);
                }
            } catch (IOException e) {
                logger.error("Failed to get the first commit ID from site '{}'", site, e);
            } finally {
                generalLockService.unlock(gitLockKey);
            }
        }

        return toReturn;
    }

    @Override
    public boolean addRemote(String siteId, String remoteName, String remoteUrl,
                             String authenticationType, String remoteUsername, String remotePassword,
                             String remoteToken, String remotePrivateKey)
            throws InvalidRemoteUrlException, ServiceLayerException {
        boolean isValid = false;
        try {
            logger.debug("Add the remote '{}' url '{}' to the sandbox in site '{}'", remoteName, remoteUrl, siteId);
            Repository repo = helper.getRepository(siteId, SANDBOX);
            try (Git git = new Git(repo)) {

                Config storedConfig = repo.getConfig();
                // TODO: SJ: Clean up string literals
                Set<String> remotes = storedConfig.getSubsections("remote");

                if (remotes.contains(remoteName)) {
                    throw new RemoteAlreadyExistsException(remoteName);
                }

                RemoteAddCommand remoteAddCommand = git.remoteAdd();
                remoteAddCommand.setName(remoteName);
                remoteAddCommand.setUri(new URIish(remoteUrl));
                retryingRepositoryOperationFacade.call(remoteAddCommand);

                try {
                    isValid = isRemoteValid(git, remoteName, authenticationType, remoteUsername, remotePassword,
                            remoteToken, remotePrivateKey);
                } finally {
                    if (!isValid) {
                        RemoteRemoveCommand remoteRemoveCommand = git.remoteRemove();
                        remoteRemoveCommand.setRemoteName(remoteName);
                        retryingRepositoryOperationFacade.call(remoteRemoveCommand);

                        ListBranchCommand listBranchCommand = git.branchList()
                                .setListMode(REMOTE);
                        List<Ref> resultRemoteBranches = retryingRepositoryOperationFacade.call(listBranchCommand);

                        List<String> branchesToDelete = new ArrayList<>();
                        for (Ref remoteBranchRef : resultRemoteBranches) {
                            if (remoteBranchRef.getName().startsWith(Constants.R_REMOTES + remoteName)) {
                                branchesToDelete.add(remoteBranchRef.getName());
                            }
                        }
                        if (CollectionUtils.isNotEmpty(branchesToDelete)) {
                            DeleteBranchCommand delBranch = git.branchDelete();
                            String[] array = new String[branchesToDelete.size()];
                            delBranch.setBranchNames(branchesToDelete.toArray(array));
                            delBranch.setForce(true);
                            retryingRepositoryOperationFacade.call(delBranch);
                        }
                    }
                }
            } catch (URISyntaxException | ClassCastException e) {
                logger.error("Failed to add the remote '{}' to the site '{}'. The remote URL '{}' is invalid.",
                        remoteName, siteId, remoteUrl, e);
                throw new InvalidRemoteUrlException(format("Failed to add the remote '%s' to the site '%s'. " +
                                "The remote URL '%s' is invalid.", remoteName, siteId, remoteUrl), e);
            } catch (GitAPIException | IOException e) {
                logger.error("Failed to add the remote '{}' url '{}' to the sandbox in site '{}'",
                        remoteName, remoteUrl, siteId, e);
                throw new ServiceLayerException(format("Failed to add the remote '%s' url '%s' to site '%s'",
                        remoteName, remoteUrl, siteId), e);
            }

            if (isValid) {
                insertRemoteToDb(siteId, remoteName, remoteUrl, authenticationType, remoteUsername, remotePassword,
                        remoteToken, remotePrivateKey);
            }
        } catch (CryptoException e) {
            throw new ServiceLayerException(e);
        }
        return isValid;
    }

    private boolean isRemoteValid(Git git, String remote, String authenticationType,
                                  String remoteUsername, String remotePassword, String remoteToken,
                                  String remotePrivateKey)
            throws CryptoException, IOException, ServiceLayerException, GitAPIException {
        LsRemoteCommand lsRemoteCommand = git.lsRemote();
        lsRemoteCommand.setRemote(remote);
        Path tempKey = Files.createTempFile(getStudioTemporaryFilesRoot(), UUID.randomUUID().toString(), TMP_FILE_SUFFIX);
        try {
            helper.setAuthenticationForCommand(lsRemoteCommand, authenticationType, remoteUsername, remotePassword,
                    remoteToken, remotePrivateKey, tempKey, false);
            retryingRepositoryOperationFacade.call(lsRemoteCommand);
            return true;
        } finally {
            Files.deleteIfExists(tempKey);
        }

    }

    private void insertRemoteToDb(String siteId, String remoteName, String remoteUrl,
                                  String authenticationType, String remoteUsername, String remotePassword,
                                  String remoteToken, String remotePrivateKey) throws CryptoException {
        // TODO: SJ: Refactor to shorten and reduce duplication
        // TODO: SJ: Clean up string literals
        logger.debug("Adding remote '{}' to the database for site '{}'", remoteName, siteId);
        Map<String, String> params = new HashMap<>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteName);
        params.put("remoteUrl", remoteUrl);
        params.put("authenticationType", authenticationType);
        params.put("remoteUsername", remoteUsername);

        if (StringUtils.isNotEmpty(remotePassword)) {
            // Encrypt password before inserting to database
            String hashedPassword = encryptor.encrypt(remotePassword);
            params.put("remotePassword", hashedPassword);
        } else {
            params.put("remotePassword", remotePassword);
        }
        if (StringUtils.isNotEmpty(remoteToken)) {
            // Encrypt token before inserting to database
            String hashedToken = encryptor.encrypt(remoteToken);
            params.put("remoteToken", hashedToken);
        } else {
            params.put("remoteToken", remoteToken);
        }
        if (StringUtils.isNotEmpty(remotePrivateKey)) {
            // Encrypt private key before inserting to database
            String hashedPrivateKey = encryptor.encrypt(remotePrivateKey);
            params.put("remotePrivateKey", hashedPrivateKey);
        } else {
            params.put("remotePrivateKey", remotePrivateKey);
        }

        // Insert site remote record into database
        retryingDatabaseOperationFacade.retry(() -> remoteRepositoryDAO.insertRemoteRepository(params));
    }

    @Override
    public void removeRemoteRepositoriesForSite(String siteId) {
        Map<String, Object> params = new HashMap<>();
        // TODO: SJ: Clean up string literals
        params.put("siteId", siteId);
        retryingDatabaseOperationFacade.retry(() -> remoteRepositoryDAO.deleteRemoteRepositoriesForSite(params));
    }

    @Override
    public List<RemoteRepositoryInfoTO> listRemote(String siteId, String sandboxBranch)
            throws ServiceLayerException {
        List<RemoteRepositoryInfoTO> res = new ArrayList<>();
        try (Repository repo = helper.getRepository(siteId, SANDBOX)) {

            try (Git git = new Git(repo)) {
                RemoteListCommand remoteListCommand = git.remoteList();
                List<RemoteConfig> resultRemotes = retryingRepositoryOperationFacade.call(remoteListCommand);
                if (CollectionUtils.isNotEmpty(resultRemotes)) {
                    for (RemoteConfig conf : resultRemotes) {
                        Map<String, String> params = new HashMap<>();
                        // TODO: SJ: Clean up string literals
                        params.put("siteId", siteId);
                        params.put("remoteName", conf.getName());
                        RemoteRepository remoteRepository = remoteRepositoryDAO.getRemoteRepository(params);
                        FetchCommand fetchCommand = git.fetch().setRemote(conf.getName());
                        if (remoteRepository != null) {
                            Path tempKey = Files.createTempFile(getStudioTemporaryFilesRoot(), UUID.randomUUID().toString(), TMP_FILE_SUFFIX);
                            try {
                                helper.setAuthenticationForCommand(fetchCommand,
                                        remoteRepository.getAuthenticationType(),
                                        remoteRepository.getRemoteUsername(), remoteRepository.getRemotePassword(),
                                        remoteRepository.getRemoteToken(), remoteRepository.getRemotePrivateKey(),
                                        tempKey, true);
                                retryingRepositoryOperationFacade.call(fetchCommand);
                            } catch (CryptoException e) {
                                logger.error("Failed to list the remote repositories in site '{}'.", siteId, e);
                                throw new ServiceLayerException(format("Failed to list the remote repositories " +
                                        "in site '%s'.", siteId), e);
                            } finally {
                                Files.deleteIfExists(tempKey);
                            }
                        }
                    }
                    ListBranchCommand listBranchCommand = git.branchList().setListMode(REMOTE);
                    List<Ref> resultRemoteBranches = retryingRepositoryOperationFacade.call(listBranchCommand);
                    Map<String, List<String>> remoteBranches = new HashMap<>();
                    for (Ref remoteBranchRef : resultRemoteBranches) {
                        String branchFullName = remoteBranchRef.getName().replace(R_REMOTES, "");
                        String remotePart = EMPTY;
                        String branchNamePart = EMPTY;
                        // TODO: SJ: Clean up string literals
                        // TODO: SJ: Refactor to reduce code duplication
                        int slashIndex = branchFullName.indexOf("/");
                        if (slashIndex > 0) {
                            remotePart = branchFullName.substring(0, slashIndex);
                            branchNamePart = branchFullName.substring(slashIndex + 1);
                        }

                        if (!remoteBranches.containsKey(remotePart)) {
                            remoteBranches.put(remotePart, new ArrayList<>());
                        }
                        remoteBranches.get(remotePart).add(branchNamePart);
                    }
                    String sandboxBranchName = sandboxBranch;
                    if (StringUtils.isEmpty(sandboxBranchName)) {
                        sandboxBranchName = studioConfiguration.getProperty(REPO_SANDBOX_BRANCH);
                    }
                    for (RemoteConfig conf : resultRemotes) {
                        RemoteRepositoryInfoTO rri = new RemoteRepositoryInfoTO();
                        rri.setName(conf.getName());
                        List<String> branches = remoteBranches.get(rri.getName());
                        if (CollectionUtils.isEmpty(branches)) {
                            branches = new ArrayList<>();
                            branches.add(sandboxBranchName);
                        }
                        rri.setBranches(branches);

                        StringBuilder sbUrl = new StringBuilder();
                        if (CollectionUtils.isNotEmpty(conf.getURIs())) {
                            for (int i = 0; i < conf.getURIs().size(); i++) {
                                sbUrl.append(conf.getURIs().get(i).toString());
                                if (i < conf.getURIs().size() - 1) {
                                    sbUrl.append(":");
                                }
                            }
                        }
                        rri.setUrl(sbUrl.toString());

                        StringBuilder sbFetch = new StringBuilder();
                        if (CollectionUtils.isNotEmpty(conf.getFetchRefSpecs())) {
                            for (int i = 0; i < conf.getFetchRefSpecs().size(); i++) {
                                sbFetch.append(conf.getFetchRefSpecs().get(i).toString());
                                if (i < conf.getFetchRefSpecs().size() - 1) {
                                    sbFetch.append(":");
                                }
                            }
                        }
                        rri.setFetch(sbFetch.toString());

                        StringBuilder sbPushUrl = new StringBuilder();
                        if (CollectionUtils.isNotEmpty(conf.getPushURIs())) {
                            for (int i = 0; i < conf.getPushURIs().size(); i++) {
                                sbPushUrl.append(conf.getPushURIs().get(i).toString());
                                if (i < conf.getPushURIs().size() - 1) {
                                    sbPushUrl.append(":");
                                }
                            }
                        } else {
                            sbPushUrl.append(rri.getUrl());
                        }
                        rri.setPush_url(sbPushUrl.toString());
                        res.add(rri);
                    }
                }
            } catch (GitAPIException | IOException e) {
                logger.error("Failed to get the remote repositories for site '{}'", siteId, e);
            }
        }
        return res;
    }

    @Override
    public boolean pushToRemote(String siteId, String remoteName, String remoteBranch) throws ServiceLayerException,
            InvalidRemoteUrlException {
        logger.debug("Push from site '{}' to remote '{}' branch '{}'", siteId, remoteName, remoteBranch);
        Map<String, String> params = new HashMap<>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteName);
        RemoteRepository remoteRepository = remoteRepositoryDAO.getRemoteRepository(params);

        // Prepare push command
        Repository repo = helper.getRepository(siteId, SANDBOX);
        try (Git git = new Git(repo)) {
            Iterable<PushResult> pushResultIterable = null;
            PushCommand pushCommand = git.push();
            // Set remote to remoteName
            pushCommand.setRemote(remoteRepository.getRemoteName());
            // Set branch to remoteBranch
            RefSpec r = new RefSpec();
            r = r.setSourceDestination(Constants.R_HEADS + repo.getBranch(),
                    Constants.R_HEADS +  remoteBranch);
            pushCommand.setRefSpecs(r);
            Path tempKey = Files.createTempFile(getStudioTemporaryFilesRoot(), UUID.randomUUID().toString(),TMP_FILE_SUFFIX);
            try {
                helper.setAuthenticationForCommand(pushCommand, remoteRepository.getAuthenticationType(),
                        remoteRepository.getRemoteUsername(), remoteRepository.getRemotePassword(),
                        remoteRepository.getRemoteToken(), remoteRepository.getRemotePrivateKey(), tempKey, true);
                pushResultIterable = retryingRepositoryOperationFacade.call(pushCommand);
            } finally {
                Files.deleteIfExists(tempKey);
            }
            boolean toRet = true;
            List<RemoteRefUpdate.Status> failure = Arrays.asList(REJECTED_NODELETE, REJECTED_NONFASTFORWARD,
                    REJECTED_REMOTE_CHANGED, REJECTED_OTHER_REASON);
            for (PushResult pushResult : pushResultIterable) {
                Collection<RemoteRefUpdate> updates = pushResult.getRemoteUpdates();
                for (RemoteRefUpdate remoteRefUpdate : updates) {
                    toRet = toRet && !failure.contains(remoteRefUpdate.getStatus());
                    if (!toRet) break;
                }
                if (!toRet) break;
            }
            return toRet;
        } catch (InvalidRemoteException e) {
            logger.error("Failed to push from site '{}' to remote '{}' branch '{}'. Remote '{}' is invalid.",
                    siteId, remoteName, remoteBranch, remoteName, e);
            throw new InvalidRemoteUrlException();
        } catch (IOException | JGitInternalException | GitAPIException | CryptoException e) {
            logger.error("Failed to push from site '{}' to remote '{}' branch '{}'",
                    siteId, remoteName, remoteBranch, e);
            throw new ServiceLayerException(format("Failed to push from site '%s' to remote '%s' branch '%s'",
                    siteId, remoteName, remoteBranch), e);
        }
    }

    @Override
    public boolean pullFromRemote(String siteId, String remoteName, String remoteBranch) throws ServiceLayerException,
            InvalidRemoteUrlException {
        logger.debug("Pull from remote '{}' branch '{}' in site '{}'", remoteName, remoteBranch, siteId);
        Map<String, String> params = new HashMap<>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteName);
        RemoteRepository remoteRepository = remoteRepositoryDAO.getRemoteRepository(params);

        // Prepare pull command
        Repository repo = helper.getRepository(siteId, SANDBOX);
        String gitLockKey = helper.getSandboxRepoLockKey(siteId);
        generalLockService.lock(gitLockKey);
        try (Git git = new Git(repo)) {
            PullResult pullResult = null;
            PullCommand pullCommand = git.pull();
            // Set remote remoteName
            pullCommand.setRemote(remoteRepository.getRemoteName());
            // Set branch remoteBranch
            pullCommand.setRemoteBranchName(remoteBranch);
            Path tempKey = Files.createTempFile(getStudioTemporaryFilesRoot(), UUID.randomUUID().toString(),TMP_FILE_SUFFIX);
            try {
                helper.setAuthenticationForCommand(pullCommand, remoteRepository.getAuthenticationType(),
                        remoteRepository.getRemoteUsername(), remoteRepository.getRemotePassword(),
                        remoteRepository.getRemoteToken(), remoteRepository.getRemotePrivateKey(), tempKey, true);
                pullResult = retryingRepositoryOperationFacade.call(pullCommand);
            } catch (CryptoException e) {
                logger.error("Failed to pull from the remote repository '{}' branch '{}' in site '{}'",
                        remoteName, remoteBranch, siteId, e);
                throw new ServiceLayerException(format("Failed to pull from the remote repository '%s' branch '%s' " +
                                "in site '%s'", remoteName, remoteBranch, siteId), e);
            } finally {
                Files.deleteIfExists(tempKey);
            }
            return pullResult != null && pullResult.isSuccessful();
        } catch (InvalidRemoteException e) {
            logger.error("The remote '{}' branch '{}' in site '{}' is invalid ", remoteName, remoteBranch, siteId, e);
            throw new InvalidRemoteUrlException(format("The remote '%s' branch '%s' in site '%s' is invalid ",
                    remoteName, remoteBranch, siteId), e);
        } catch (GitAPIException e) {
            logger.error("Failed to pull from remote '{}' branch '{}' in site '{}'",
                    remoteName, remoteBranch, siteId, e);
            throw new ServiceLayerException(format("Failed to pull from remote '%s' branch '%s' in site '%s'",
                    remoteName, remoteBranch, siteId), e);
        } catch (IOException e) {
            throw new ServiceLayerException(e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
    }

    @Override
    public boolean isFolder(String siteId, String path) {
        Path p = Paths.get(helper.buildRepoPath(StringUtils.isEmpty(siteId) ? GLOBAL : SANDBOX, siteId)
                .toAbsolutePath().toString(), path);
        File file = p.toFile();
        return file.isDirectory();
    }

    @Override
    public void resetStagingRepository(String siteId) throws ServiceLayerException {
        // TODO: SJ: Refactor if still in use
        Repository repo = helper.getRepository(siteId, PUBLISHED);
        String stagingName = servicesConfig.getStagingEnvironment(siteId);
        String liveName = servicesConfig.getLiveEnvironment(siteId);
        String gitLockKey = helper.getPublishedRepoLockKey(siteId);
        generalLockService.lock(gitLockKey);
        try (Git git = new Git(repo)) {
            logger.trace("Checkout the live branch in site '{}' first since we cannot delete a checked out branch",
                    siteId);
            CheckoutCommand checkoutCommand = git.checkout().setName(liveName);
            retryingRepositoryOperationFacade.call(checkoutCommand);
            logger.debug("Delete the staging branch in order to reset it in site '{}'", siteId);
            DeleteBranchCommand deleteBranchCommand =
                    git.branchDelete().setBranchNames(stagingName).setForce(true);
            retryingRepositoryOperationFacade.call(deleteBranchCommand);

            logger.debug("Create a new branch for staging with live/HEAD as starting point in site '{}'",
                    siteId);
            CreateBranchCommand createBranchCommand = git.branchCreate()
                    .setName(stagingName)
                    .setStartPoint(liveName);
            retryingRepositoryOperationFacade.call(createBranchCommand);
        } catch (GitAPIException e) {
            logger.error("Failed to reset the staging environment in site '{}'", siteId, e);
            throw new ServiceLayerException(e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
    }

    protected void cleanup(String siteId, GitRepositories repository) {
        // TODO: SJ: Rename this to indicate what it actually does, garbage collect git
        Repository sandbox = helper.getRepository(siteId, repository);
        try (Git git = new Git(sandbox)) {
            retryingRepositoryOperationFacade.call(git.gc());
        }  catch (Exception e) {
            logger.warn("Failed to garbage collect the git repository in site '{}'", siteId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanupRepositories(final String siteId) {
        // TODO: SJ: Rename to indicate what this actually does, garbage collect the git repos
        if(StringUtils.isEmpty(siteId)) {
            logger.info("Garbage collect the global repository");
            String gitLockKey = GLOBAL_REPOSITORY_GIT_LOCK;
            generalLockService.lock(gitLockKey);
            try {
                cleanup(siteId, GLOBAL);
            } finally {
                generalLockService.unlock(gitLockKey);
            }
        } else {
            logger.info("Garbage collect the git repositories in site '{}'", siteId);
            String gitLockKeySandbox = helper.getSandboxRepoLockKey(siteId);
            String gitLockKeyPublished = helper.getPublishedRepoLockKey(siteId);
            generalLockService.lock(gitLockKeySandbox);
            // TODO: SJ: Redo the exception handling as part of refactoring this method
            try {
                cleanup(siteId, SANDBOX);
            } finally {
                generalLockService.unlock(gitLockKeySandbox);
            }
            generalLockService.lock(gitLockKeyPublished);
            try {
                cleanup(siteId, PUBLISHED);
            } finally {
                generalLockService.unlock(gitLockKeyPublished);
            }
        }
    }

    public void setServletContext(ServletContext ctx) {
        this.ctx = ctx;
    }

    public void setStudioConfiguration(final StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setRemoteRepositoryDAO(RemoteRepositoryDAO remoteRepositoryDAO) {
        this.remoteRepositoryDAO = remoteRepositoryDAO;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setSiteDao(SiteDAO siteDao) {
        this.siteDao = siteDao;
    }

    public void setEncryptor(TextEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    public void setContextManager(ContextManager contextManager) {
        this.contextManager = contextManager;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public void setHelper(GitRepositoryHelper helper) {
        this.helper = helper;
    }

    public void setProcessedCommitsDao(ProcessedCommitsDAO processedCommitsDao) {
        this.processedCommitsDao = processedCommitsDao;
    }

    public void setRetryingRepositoryOperationFacade(RetryingRepositoryOperationFacade retryingRepositoryOperationFacade) {
        this.retryingRepositoryOperationFacade = retryingRepositoryOperationFacade;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }
}
