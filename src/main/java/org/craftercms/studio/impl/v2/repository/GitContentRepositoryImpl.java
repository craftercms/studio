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

package org.craftercms.studio.impl.v2.repository;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Item;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v2.annotation.LogExecutionTime;
import org.craftercms.studio.api.v2.core.ContextManager;
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.dal.publish.PublishItem.Action;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.exception.PublishedRepositoryNotFoundException;
import org.craftercms.studio.api.v2.exception.git.NoChangesForPathException;
import org.craftercms.studio.api.v2.exception.publish.PublishException;
import org.craftercms.studio.api.v2.repository.GitContentRepository;
import org.craftercms.studio.api.v2.repository.PublishItemTO;
import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v2.service.publish.internal.PublishingProgressServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.craftercms.studio.model.history.ItemVersion;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffConfig;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.internal.storage.file.LockFile;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.subtract;
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.prependIfMissing;
import static org.craftercms.studio.api.v1.constant.GitRepositories.*;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v2.dal.RepoOperation.Action.*;
import static org.craftercms.studio.api.v2.dal.publish.PublishItem.Action.ADD;
import static org.craftercms.studio.api.v2.dal.publish.PublishItem.Action.DELETE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.*;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.*;
import static org.eclipse.jgit.api.ResetCommand.ResetType.HARD;
import static org.eclipse.jgit.lib.Constants.*;
import static org.eclipse.jgit.revwalk.RevSort.REVERSE;
import static org.eclipse.jgit.revwalk.RevSort.TOPO_KEEP_BRANCH_TOGETHER;

/**
 * Implementation of the GitContentRepositoryImpl interface.
 */
public class GitContentRepositoryImpl implements GitContentRepository {

    private static final Logger logger = LoggerFactory.getLogger(GitContentRepositoryImpl.class);
    private static final String REFS_HEADS_FORMAT = "refs/heads/%s";

    private GitRepositoryHelper helper;
    private StudioConfiguration studioConfiguration;
    private UserServiceInternal userServiceInternal;
    private RemoteRepositoryDAO remoteRepositoryDAO;
    private TextEncryptor encryptor;
    private ContextManager contextManager;
    private ContentStoreService contentStoreService;
    private GeneralLockService generalLockService;
    private SitesService siteService;
    private RetryingRepositoryOperationFacade retryingRepositoryOperationFacade;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;
    private PublishingProgressServiceInternal publishingProgressServiceInternal;

    private ServicesConfig servicesConfig;

    protected StudioDBScriptRunnerFactory scriptRunnerFactory;

    @Override
    public List<String> getSubtreeItems(String site, String path, GitRepositories repoType, String branch) {
        final List<String> retItems = new ArrayList<>();
        String rootPath;
        if (path.endsWith(FILE_SEPARATOR + INDEX_FILE)) {
            int lastIdx = path.lastIndexOf(FILE_SEPARATOR + INDEX_FILE);
            rootPath = path.substring(0, lastIdx);
        } else {
            rootPath = path;
        }
        try {
            Repository repo = helper.getRepository(site, isEmpty(site) ? GLOBAL : repoType);

            RevTree tree = helper.getTreeForCommit(repo, branch);
            try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(rootPath), tree)) {

                if (tw != null) {
                    // Loop for all children and gather path of item excluding the item, file/folder name, and
                    // whether it's a folder
                    ObjectLoader loader = repo.open(tw.getObjectId(0));
                    if (loader.getType() == OBJ_TREE) {
                        tw.enterSubtree();
                        tw.setRecursive(true);
                        while (tw.next()) {
                            String name = tw.getNameString();
                            String childPath = FILE_SEPARATOR +  tw.getPathString();

                            if (!ArrayUtils.contains(IGNORE_FILES, name) && !childPath.equals(path)) {
                                retItems.add(childPath);
                            }

                        }
                        tw.close();
                    } else {
                        logger.debug("Item at site '{}' path '{}' does not have children", site, path);
                    }
                } else {
                    String gitPath = helper.getGitPath(rootPath);
                    if (isEmpty(gitPath) || gitPath.equals(".")) {
                        try (TreeWalk treeWalk = new TreeWalk(repo)) {
                            treeWalk.addTree(tree);
                            treeWalk.setRecursive(true);
                            while (treeWalk.next()) {
                                String name = treeWalk.getNameString();
                                String childPath = FILE_SEPARATOR + treeWalk.getPathString();

                                if (!ArrayUtils.contains(IGNORE_FILES, name) && !childPath.equals(path)) {
                                    retItems.add(childPath);
                                }
                            }

                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to get children at site '{}' path '{}'", site, path, e);
        }
        return retItems;
    }

    @Override
    public List<RepoOperation> getOperationsFromDelta(String site, String commitIdFrom, String commitIdTo) {
        List<RepoOperation> operations = new ArrayList<>();
        Repository repository = helper.getRepository(site, isEmpty(site) ? GLOBAL : SANDBOX);
        if (repository != null) {
            try {
                // Get the sandbox repo, and then get a reference to the commitId we received and another for head
                boolean fromEmptyRepo = isEmpty(commitIdFrom);
                String firstCommitId = getRepoFirstCommitId(site);
                if (fromEmptyRepo) {
                    commitIdFrom = firstCommitId;
                }
                Repository repo = helper.getRepository(site, SANDBOX);
                ObjectId objCommitIdFrom = repo.resolve(commitIdFrom);
                ObjectId objCommitIdTo = repo.resolve(commitIdTo);

                if (Objects.nonNull(objCommitIdFrom) && Objects.nonNull(objCommitIdTo)) {
                    ObjectId objFirstCommitId = repo.resolve(firstCommitId);

                    try (Git git = new Git(repo)) {

                        if (fromEmptyRepo) {
                            try (RevWalk walk = new RevWalk(repo)) {
                                RevCommit firstCommit = walk.parseCommit(objFirstCommitId);
                                try (ObjectReader reader = repo.newObjectReader()) {
                                    CanonicalTreeParser firstCommitTreeParser = new CanonicalTreeParser();
                                    firstCommitTreeParser.reset();//reset(reader, firstCommitTree.getId());
                                    // Diff the two commit Ids
                                    long startDiffMark1 = logger.isDebugEnabled() ?
                                            System.currentTimeMillis() : 0;
                                    DiffCommand diffCommand = git.diff()
                                            .setOldTree(firstCommitTreeParser)
                                            .setNewTree(null);
                                    List<DiffEntry> diffEntries = retryingRepositoryOperationFacade.call(diffCommand);

                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Git diff from '{}' to null finished in '{}' seconds",
                                                objFirstCommitId.getName(),
                                                ((System.currentTimeMillis() - startDiffMark1) / 1000));
                                        logger.debug("Number of diff entries '{}'", diffEntries.size());
                                    }

                                    // Now that we have a diff, let's itemize the file changes, pack them into a TO
                                    // and add them to the list of RepoOperations to return to the caller
                                    // also include date/time of commit by taking number of seconds and multiply by 1000 and
                                    // convert to java date before sending over
                                    operations.addAll(processDiffEntry(git, diffEntries, firstCommit.getId()));
                                }
                            }
                        }

                        // If the commitIdFrom is the same as commitIdTo, there is nothing to calculate, otherwise,
                        // let's do it
                        if (!objCommitIdFrom.equals(objCommitIdTo)) {
                            // Compare HEAD with commitId we're given
                            // Get list of commits between commitId and HEAD in chronological order

                            RevTree fromTree = helper.getTreeForCommit(repo, objCommitIdFrom.getName());
                            RevTree toTree = helper.getTreeForCommit(repo, objCommitIdTo.getName());
                            if (fromTree != null && toTree != null) {
                                try (ObjectReader reader = repo.newObjectReader()) {
                                    CanonicalTreeParser fromCommitTreeParser = new CanonicalTreeParser();
                                    CanonicalTreeParser toCommitTreeParser = new CanonicalTreeParser();
                                    fromCommitTreeParser.reset(reader, fromTree.getId());
                                    toCommitTreeParser.reset(reader, toTree.getId());

                                    // Diff the two commit Ids
                                    long startDiffMark2 = logger.isDebugEnabled() ?
                                            System.currentTimeMillis() : 0;
                                    DiffCommand diffCommand = git.diff()
                                            .setOldTree(fromCommitTreeParser)
                                            .setNewTree(toCommitTreeParser);
                                    List<DiffEntry> diffEntries = retryingRepositoryOperationFacade.call(diffCommand);

                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Git diff from '{}' to '{}' finished in '{}' seconds",
                                                objCommitIdFrom.getName(),
                                                objCommitIdTo.getName(),
                                                ((System.currentTimeMillis() - startDiffMark2) / 1000));
                                        logger.debug("Number of diff entries '{}'", diffEntries.size());
                                    }

                                    if (isEmpty(diffEntries)) {
                                        ObjectId objCommitIdPrevious = repo.resolve(commitIdTo + "~");
                                        if (Objects.nonNull(objCommitIdPrevious)) {
                                            RevTree previousTree = helper.getTreeForCommit(repo,
                                                    objCommitIdPrevious.getName());
                                            CanonicalTreeParser previousCommitTreeParser = new CanonicalTreeParser();
                                            previousCommitTreeParser.reset(reader, previousTree.getId());
                                            toCommitTreeParser.reset(reader, toTree.getId());
                                            diffCommand = git.diff()
                                                    .setOldTree(previousCommitTreeParser)
                                                    .setNewTree(toCommitTreeParser);
                                            diffEntries = retryingRepositoryOperationFacade.call(diffCommand);
                                            if (logger.isDebugEnabled()) {
                                                logger.debug("Git diff from '{}' to '{}' finished in '{}' seconds",
                                                        objCommitIdPrevious.getName(),
                                                        objCommitIdTo.getName(),
                                                        ((System.currentTimeMillis() - startDiffMark2) / 1000));
                                                logger.debug("Number of diff entries '{}'", diffEntries.size());
                                            }
                                        }
                                    }

                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Git diff from '{}' to '{}' finished in '{}' seconds",
                                                objCommitIdFrom.getName(),
                                                objCommitIdTo.getName(),
                                                ((System.currentTimeMillis() - startDiffMark2) / 1000));
                                        logger.debug("Number of diff entries '{}'", diffEntries.size());
                                    }

                                    // Now that we have a diff, let's itemize the file changes, pack them into a TO
                                    // and add them to the list of RepoOperations to return to the caller
                                    // also include date/time of commit by taking number of seconds and multiply by 1000 and
                                    // convert to java date before sending over
                                    operations.addAll(
                                            processDiffEntry(git, diffEntries, objCommitIdTo));
                                }
                            }


                        }
                    }
                }
            } catch (IOException | GitAPIException e) {
                logger.error("Failed to get operations in site '{}' from commit ID '{}' to commit ID '{}'",
                        site, commitIdFrom, commitIdTo, e);
            }
        }

        return operations;
    }

    @Override
    public String getRepoFirstCommitId(final String site) {
        String toReturn = EMPTY;
        String gitLockKey = helper.getSandboxRepoLockKey(site, true);
        Repository repository = helper.getRepository(site, isEmpty(site) ? GLOBAL : SANDBOX);
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
                    logger.debug("getRepoFirstCommitId in site '{}', the first commit ID is '{}'",
                            site, toReturn);
                }
            } catch (IOException e) {
                logger.error("Failed to get the first commit ID in site '{}'", site, e);
            } finally {
                generalLockService.unlock(gitLockKey);
            }
        }

        return toReturn;
    }

    private List<RepoOperation> processDiffEntry(Git git, List<DiffEntry> diffEntries, ObjectId commitId)
            throws GitAPIException, IOException {
        int size = diffEntries.size();
        logger.debug("Process '{}' diff entries", size);
        long startMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0;
        List<RepoOperation> toReturn = new ArrayList<>();

        for (DiffEntry diffEntry : diffEntries) {
            // Update the paths to have a preceding separator
            String pathNew = FILE_SEPARATOR + diffEntry.getNewPath();
            String pathOld = FILE_SEPARATOR + diffEntry.getOldPath();

            RepoOperation repoOperation = null;
            Iterable<RevCommit> iterable;
            RevCommit revCommit;
            ZonedDateTime commitTime;
            String author;

            try (Repository repo = git.getRepository()) {
                try (RevWalk revWalk = new RevWalk(repo)) {
                    revCommit = revWalk.parseCommit(commitId);
                }
            }
            if (revCommit == null) {
                LogCommand logCommand = git.log().setMaxCount(1);
                iterable = retryingRepositoryOperationFacade.call(logCommand);
                revCommit = iterable.iterator().next();
            }
            commitTime = Instant.ofEpochSecond(revCommit.getCommitTime()).atZone(UTC);
            author = revCommit.getAuthorIdent().getName();

            switch (diffEntry.getChangeType()) {
                case ADD:
                    repoOperation = new RepoOperation(CREATE, pathNew, commitTime, null,
                            revCommit.getId().getName());
                    break;
                case MODIFY:
                    repoOperation = new RepoOperation(UPDATE, pathNew, commitTime, null,
                            revCommit.getId().getName());
                    break;
                case DELETE:
                    repoOperation = new RepoOperation(RepoOperation.Action.DELETE, pathOld, commitTime, null,
                            revCommit.getId().getName());
                    break;
                case RENAME:
                    repoOperation = new RepoOperation(MOVE, pathOld, commitTime, pathNew, commitId.getName());
                    break;
                case COPY:
                    repoOperation = new RepoOperation(COPY, pathNew, commitTime, null, commitId.getName());
                    break;
                default:
                    logger.error("Unknown git operation '{}'", diffEntry.getChangeType());
                    break;
            }
            if (repoOperation != null) {
                repoOperation.setAuthor(isEmpty(author) ? "N/A" : author);
                toReturn.add(repoOperation);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Finished processing '{}' diff entries in '{}' seconds",
                    size, ((System.currentTimeMillis() - startMark) / 1000));
        }
        return toReturn;
    }

    @Override
    public boolean createSiteFromBlueprint(String blueprintLocation, String site, String sandboxBranch,
                                           Map<String, String> params, String creator) {
        boolean toReturn;
        String gitLockKey = helper.getSandboxRepoLockKey(site);
        generalLockService.lock(gitLockKey);
        try {
            // create git repository for site content
            toReturn = helper.createSandboxRepository(site, sandboxBranch);

            if (toReturn) {
                // copy files from blueprint
                toReturn = helper.copyContentFromBlueprint(blueprintLocation, site);
            }

            if (toReturn) {
                // update site name variable inside config files
                toReturn = helper.updateSiteNameConfigVar(site);
            }

            if (toReturn) {
                toReturn = helper.replaceParameters(site, params);
            }

            if (toReturn) {
                toReturn = helper.addGitIgnoreFiles(site);
            }

            if (toReturn) {
                // commit everything so it is visible
                toReturn = helper.performInitialCommit(site, helper.getCommitMessage(REPO_INITIAL_COMMIT_COMMIT_MESSAGE),
                        sandboxBranch, creator);
            }
        } finally {
            generalLockService.unlock(gitLockKey);
        }

        return toReturn;
    }

    /**
     * Creates environment branch if it does not exist.
     * This method will create a branch in the given repository.
     * The starting point of the new branch will be chosen from the following rules:
     * <ul>
     *     <li>If the environment is live, the sandbox branch will be used</li>
     *     <li>If the environment is not live (staging), the live branch will be used if it exists. Otherwise it will use the sandbox branch</li>
     * </ul>
     *
     * @param site              the site id
     * @param environment       the publishing target
     * @param repo              git repo
     * @param sandboxBranchName sandbox repository branch name
     * @throws IOException if an I/O error occurs while verifying branch existence
     */
    private void ensureEnvironmentBranch(String site, String environment, Repository repo, String sandboxBranchName) throws IOException {
        if (branchExists(repo, environment)) {
            return;
        }
        String liveEnvironment = servicesConfig.getLiveEnvironment(site);
        boolean liveExists = branchExists(repo, liveEnvironment);
        String baseBranch = liveExists ? liveEnvironment : sandboxBranchName;
        createEnvironmentBranch(site, baseBranch, environment);
    }

    protected void resetIfNeeded(Repository repo, Git git) throws IOException, GitAPIException {
        String currentBranch = repo.getBranch();
        if (currentBranch.endsWith(IN_PROGRESS_BRANCH_NAME_SUFFIX)) {
            ResetCommand resetCommand = git.reset().setMode(HARD);
            retryingRepositoryOperationFacade.call(resetCommand);
        }
    }

    protected void checkoutBranch(Git git, String name, boolean create) throws GitAPIException {
        CheckoutCommand checkoutCommand = git.checkout()
                                             .setName(name)
                                             .setCreateBranch(create);
        retryingRepositoryOperationFacade.call(checkoutCommand);
    }

    protected void checkoutBranch(Git git, String name) throws GitAPIException {
        checkoutBranch(git, name, false);
    }

    protected void deleteBranches(Git git, String... names) throws GitAPIException {
        DeleteBranchCommand deleteCommand = git.branchDelete()
                                               .setForce(true)
                                               .setBranchNames(names);

        retryingRepositoryOperationFacade.call(deleteCommand);
    }

    protected boolean branchExists(Repository repo, String branch) throws IOException {
        return repo.resolve(branch) != null;
    }

    private void cleanUpMoveFolders(Git git, String path) throws GitAPIException, IOException {
        Path parentToDelete = Paths.get(path).getParent();
        boolean isPage = path.endsWith(FILE_SEPARATOR + INDEX_FILE);
        deleteParentFolder(git, parentToDelete, isPage);
        Path testDelete = Paths.get(git.getRepository().getDirectory().getParent(), parentToDelete.toString());
        File testDeleteFile = testDelete.toFile();
        if (!testDeleteFile.exists()) {
            cleanUpMoveFolders(git, parentToDelete.toString());
        }
    }

    private void deleteParentFolder(Git git, Path parentFolder, boolean wasPage) throws GitAPIException, IOException {
        String parent = parentFolder.toString();
        String folderToDelete = helper.getGitPath(parent);
        Path toDelete = Paths.get(git.getRepository().getDirectory().getParent(), parent);
        if (Files.exists(toDelete)) {
            try (Stream<Path> dirStream = Files.walk(toDelete);
                 Stream<Path> fileStream = Files.walk(toDelete, 1)) {
                List<String> dirs = dirStream.filter(x -> !x.equals(toDelete))
                                             .filter(Files::isDirectory)
                                             .map(y -> y.getFileName().toString())
                                             .collect(toList());
                List<String> files = fileStream.filter(x -> !x.equals(toDelete))
                                               .filter(Files::isRegularFile)
                                               .map(y -> y.getFileName().toString())
                                               .collect(toList());
                if (wasPage ||
                        (isEmpty(dirs) &&
                                (isEmpty(files) || files.size() < 2 && files.get(0).equals(EMPTY_FILE)))) {
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
        }
    }

    @Override
    public boolean repositoryExists(String siteId) {
        boolean exists = false;
        Path siteSandboxRepoPath = helper.buildRepoPath(GitRepositories.SANDBOX, siteId).resolve(GIT_ROOT);
        if (Files.exists(siteSandboxRepoPath)) {
            exists = commitIdExists(siteId, HEAD);
        }
        return exists;
    }

    @Override
    public boolean commitIdExists(String site, String commitId) {
        return commitIdExists(site, SANDBOX, commitId);
    }

    @Override
    public boolean commitIdExists(String site, GitRepositories repoType, String commitId) {
        boolean toRet = false;
        try {
            Repository repo = helper.getRepository(site, repoType);
            if (repo != null) {
                ObjectId objCommitId = repo.resolve(commitId);
                if (objCommitId != null) {
                    RevCommit revCommit = repo.parseCommit(objCommitId);
                    if (revCommit != null) {
                        toRet = true;
                    }
                }
            }
        } catch (IOException e) {
            logger.info("Commit ID '{}' doesn't exist in repo '{}' for site '{}'", commitId, repoType, site);
        }
        return toRet;
    }

    @Override
    public boolean createSiteCloneRemote(String siteId, String sandboxBranch, String remoteName, String remoteUrl,
                                         String remoteBranch, boolean singleBranch, String authenticationType,
                                         String remoteUsername, String remotePassword, String remoteToken,
                                         String remotePrivateKey, Map<String, String> params, boolean createAsOrphan,
                                         String creator)
            throws InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
            RemoteRepositoryNotFoundException, ServiceLayerException {
        boolean toReturn;

        // Clone the remote git repository
        logger.debug("Creating site '{}' as a clone of remote repository '{} ({})'", siteId, remoteName, remoteUrl);
        String gitLockKey = helper.getSandboxRepoLockKey(siteId);
        generalLockService.lock(gitLockKey);
        try {
            toReturn = helper.createSiteCloneRemoteGitRepo(siteId, sandboxBranch, remoteName, remoteUrl, remoteBranch,
                    singleBranch, authenticationType, remoteUsername, remotePassword, remoteToken, remotePrivateKey,
                    createAsOrphan, creator);

            if (toReturn) {
                try {
                    if (createAsOrphan) {
                        removeRemote(siteId, remoteName);
                    } else {
                        insertRemoteToDb(siteId, remoteName, remoteUrl, authenticationType, remoteUsername, remotePassword,
                                remoteToken, remotePrivateKey);
                    }
                } catch (CryptoException e) {
                    throw new ServiceLayerException(e);
                }

                // Update the siteName variable inside the config files
                logger.debug("Update siteName configuration variables for site '{}'", siteId);
                toReturn = helper.updateSiteNameConfigVar(siteId);

                if (toReturn) {
                    toReturn = helper.replaceParameters(siteId, params);
                }

                if (toReturn) {
                    // Commit everything so it is visible
                    logger.debug("Perform initial commit for site '{}'", siteId);
                    toReturn = helper.performInitialCommit(siteId,
                            helper.getCommitMessage(REPO_INITIAL_COMMIT_COMMIT_MESSAGE), sandboxBranch, creator);
                }
            } else {
                logger.error("Failed to create site '{}' by cloning remote repository '{} ({})'",
                        siteId, remoteName, remoteUrl);
            }
        } finally {
            generalLockService.unlock(gitLockKey);
        }
        return toReturn;
    }

    @Override
    public boolean removeRemote(String siteId, String remoteName) {
        logger.debug("Remove remote '{}' from the sandbox repo in the site '{}'", remoteName, siteId);
        Repository repo = helper.getRepository(siteId, SANDBOX);
        try (Git git = new Git(repo)) {
            RemoteRemoveCommand remoteRemoveCommand = git.remoteRemove();
            remoteRemoveCommand.setRemoteName(remoteName);
            retryingRepositoryOperationFacade.call(remoteRemoveCommand);

            ListBranchCommand listBranchCommand = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.REMOTE);
            List<Ref> resultRemoteBranches = retryingRepositoryOperationFacade.call(listBranchCommand);

            List<String> branchesToDelete = new ArrayList<>();
            for (Ref remoteBranchRef : resultRemoteBranches) {
                if (remoteBranchRef.getName().startsWith(Constants.R_REMOTES + remoteName)) {
                    branchesToDelete.add(remoteBranchRef.getName());
                }
            }
            if (CollectionUtils.isNotEmpty(branchesToDelete)) {
                deleteBranches(git, branchesToDelete.toArray(new String[]{}));
            }

        } catch (GitAPIException e) {
            logger.error("Failed to remove remote '{}' in site '{}'", remoteName, siteId, e);
            return false;
        }

        logger.debug("Remove remote record from the database where the remote is '{}' in site '{}'",
                remoteName, siteId);
        Map<String, String> params = new HashMap<>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteName);
        retryingDatabaseOperationFacade.retry(() -> remoteRepositoryDAO.deleteRemoteRepository(params));

        return true;
    }

    private void insertRemoteToDb(String siteId, String remoteName, String remoteUrl,
                                  String authenticationType, String remoteUsername, String remotePassword,
                                  String remoteToken, String remotePrivateKey) throws CryptoException {
        logger.debug("Insert git remote '{}' in site '{}' into the database", remoteName, siteId);
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
    public void checkContentExists(String site, String path) throws ServiceLayerException {
        if (!contentExists(site, path)) {
            throw new ContentNotFoundException(path, site, format("Content does not exist at '%s' for site '%s'", path, site));
        }
    }

    @Override
    public boolean deleteSite(String siteId) {
        // Destroy site context
        contextManager.destroyContext(siteId);
        // Delete git repositories (sandbox and published)
        // The helper will take care of locking the repos
        return helper.deleteSiteGitRepo(siteId);
    }

    @Override
    public boolean shallowContentExists(String site, String path) {
        return Files.exists(helper.buildRepoPath(SANDBOX, site).resolve(helper.getGitPath(path)));
    }

    @Override
    public List<String> validatePublishCommits(final String siteId, final Collection<String> commitIds) throws IOException, ServiceLayerException {
        String repoLockKey = helper.getSandboxRepoLockKey(siteId);
        Repository repo = helper.getRepository(siteId, SANDBOX);
        generalLockService.lock(repoLockKey);
        String repoLastCommitId = getRepoLastCommitId(siteId);

        List<String> resultCommits = new LinkedList<>();

        try (Git git = Git.wrap(repo)) {
            // git log --first-parent --reverse commitFrom..commitTo
            RevWalk revWalk = new RevWalk(git.getRepository());
            revWalk.setFirstParent(true);
            revWalk.markStart(revWalk.parseCommit(repo.resolve(repoLastCommitId)));
            revWalk.setRevFilter(new RevFilter() {
                private final List<String> targetCommits = new ArrayList<>(commitIds);

                @Override
                public boolean include(RevWalk walker, RevCommit commit) throws StopWalkException {
                    if (targetCommits.isEmpty()) {
                        // Stop early if we found them all
                        throw StopWalkException.INSTANCE;
                    }
                    if (targetCommits.contains(commit.getName())) {
                        targetCommits.remove(commit.getName());
                        return true;
                    }

                    return false;
                }

                @Override
                public RevFilter clone() {
                    return this;
                }
            });
            revWalk.sort(TOPO_KEEP_BRANCH_TOGETHER);
            revWalk.sort(REVERSE, true);

            for (RevCommit revCommit : revWalk) {
                resultCommits.add(revCommit.getName());
            }
            Collection<String> notFoundCommits = subtract(commitIds, resultCommits);
            if (!notFoundCommits.isEmpty()) {
                throw new InvalidParametersException(format("Failed to publish items: Invalid commit ids %s", notFoundCommits));
            }
            return resultCommits;
        } finally {
            generalLockService.unlock(repoLockKey);
        }
    }

    @Override
    public boolean isFolder(final String siteId, final String path) {
        Path p = Paths.get(helper.buildRepoPath(isEmpty(siteId) ? GLOBAL : SANDBOX, siteId)
                .toAbsolutePath().toString(), path);
        File file = p.toFile();
        return file.isDirectory();
    }

    @Override
    public boolean contentExists(String site, String path) {
        boolean toReturn = false;
        try {
            Repository repo = helper.getRepository(site, isEmpty(site) ? GLOBAL : SANDBOX);
            if (repo != null ) {

                RevTree tree = helper.getTreeForLastCommit(repo);
                try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree)) {
                    // Check if the array of items is not null, and since we have an absolute path to the item,
                    // pick the first item in the list
                    if (tw != null && tw.getObjectId(0) != null) {
                        toReturn = true;
                        tw.close();
                    } else if (tw == null) {
                        String gitPath = helper.getGitPath(path);
                        if (isEmpty(gitPath) || gitPath.equals(".")) {
                            toReturn = true;
                        }
                    }
                } catch (IOException e) {
                    logger.debug("Content not found for site '{}' path '{}'", site, path, e);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to create RevTree for site '{}' path '{}'", site, path, e);
        }
        return toReturn;
    }

    @Override
    public String getRepoLastCommitId(final String site) {
        String toReturn = EMPTY;
        String gitLockKey = helper.getSandboxRepoLockKey(site, true);
        generalLockService.lock(gitLockKey);
        try {
            Repository repository = helper.getRepository(site, isEmpty(site) ? GLOBAL : SANDBOX);
            if (repository != null) {
                ObjectId commitId = repository.resolve(HEAD);
                if (commitId != null) {
                    toReturn = commitId.getName();
                }
            }
        } catch (IOException e) {
            logger.error("Failed to get the last commit ID in site '{}'", site, e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }

        return toReturn;
    }

    @Override
    public Item getItem(String siteId, String path, boolean flatten) {
        var context = contextManager.getContext(siteId);
        return contentStoreService.getItem(context, null, path, null, flatten);
    }

    @Override
    public boolean isTargetPublished(String siteId, String target) throws IOException {
        Repository repo = helper.getRepository(siteId, PUBLISHED);
        return branchExists(repo, target);
    }

    @Override
    public String deleteContent(String site, Collection<String> paths,
                                String approver) throws ServiceLayerException {
        String gitLockKey = helper.getSandboxRepoLockKey(site, true);
        generalLockService.lock(gitLockKey);
        try {
            Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
            try (Git git = new Git(repo)) {
                List<String> pathsToCommit = new ArrayList<>(paths.size());
                for (String path : paths) {
                    String pathToDelete = helper.getGitPath(path);
                    RmCommand rmCommand = git.rm().addFilepattern(pathToDelete).setCached(false);
                    retryingRepositoryOperationFacade.call(rmCommand);

                    String pathToCommit = pathToDelete;
                    boolean isPage = path.endsWith(FILE_SEPARATOR + INDEX_FILE);
                    if (isPage) {
                        Path parentToDelete = Paths.get(pathToDelete).getParent();
                        pathToCommit = parentToDelete.toString();
                        deleteParentFolder(git, parentToDelete, true);
                    }
                    pathsToCommit.add(pathToCommit);
                }
                String commitMsg = helper.getCommitMessage(REPO_DELETE_CONTENT_COMMIT_MESSAGE)
                        .replaceAll(PATTERN_PATH, StringUtils.join(paths));
                PersonIdent user = StringUtils.isEmpty(approver) ? helper.getCurrentUserIdent() :
                        helper.getAuthorIdent(approver);

                // TODO: SJ: we need to define messages in a string table of sorts
                return helper.commitFiles(repo, site, commitMsg, user, pathsToCommit.toArray(new String[0]));
            } catch (ServiceLayerException e) {
                logger.error("Failed to delete content at site '{}' paths '{}'", site, paths, e);
                throw e;
            } catch (GitAPIException | UserNotFoundException | IOException e) {
                logger.error("Failed to delete content at site '{}' paths '{}'", site, paths, e);
                throw new ServiceLayerException(format("Failed to delete content at site '%s' paths '%s'", site, StringUtils.join(paths)), e);
            }
        } finally {
            generalLockService.unlock(gitLockKey);
        }
    }

    @Override
    public long getContentSize(final String site, final String path) {
        // TODO: SJ: Reconsider this implementation for blob store backed repos
        try {
            Repository repo = helper.getRepository(site, isEmpty(site) ? GLOBAL : SANDBOX);
            RevTree tree = helper.getTreeForLastCommit(repo);
            try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree)) {
                if (tw != null && tw.getObjectId(0) != null) {
                    ObjectId id = tw.getObjectId(0);
                    ObjectLoader objectLoader = repo.open(id);
                    return objectLoader.getSize();
                }
            }
        } catch (IOException e) {
            logger.error("Failed to get content size for path '{}' in site '{}'", path, site, e);
        }
        return -1L;
    }

    @Override
    @LogExecutionTime
    public Map<String, String> getChangeSetPathsFromDelta(String site, String commitIdFrom, String commitIdTo) {
        Map<String, String> changeSet = new TreeMap<>();
        String gitLockKey = helper.getSandboxRepoLockKey(site, true);
        Repository repository = helper.getRepository(site, isEmpty(site) ? GLOBAL : SANDBOX);
        if (repository != null) {
            generalLockService.lock(gitLockKey);
            try {
                // Get the sandbox repo, and then get a reference to the commitId we received and another for head
                boolean fromEmptyRepo = isEmpty(commitIdFrom);
                String firstCommitId = getRepoFirstCommitId(site);
                if (fromEmptyRepo) {
                    commitIdFrom = firstCommitId;
                }
                Repository repo = helper.getRepository(site, SANDBOX);
                ObjectId objCommitIdFrom = repo.resolve(commitIdFrom);
                ObjectId objCommitIdTo = repo.resolve(commitIdTo);

                try (Git git = new Git(repo)) {

                    if (fromEmptyRepo) {
                        CanonicalTreeParser firstCommitTreeParser = new CanonicalTreeParser();
                        firstCommitTreeParser.reset();//reset(reader, firstCommitTree.getId());
                        // Diff the two commit Ids
                        DiffCommand diffCommand = git.diff()
                                .setOldTree(firstCommitTreeParser)
                                .setNewTree(null);
                        List<DiffEntry> diffEntries = retryingRepositoryOperationFacade.call(diffCommand);

                        // Now that we have a diff, let's itemize the file changes, pack them into a TO
                        // and add them to the list of RepoOperations to return to the caller
                        // also include date/time of commit by taking number of seconds and multiply by 1000 and
                        // convert to java date before sending over
                        changeSet.putAll(getChangeSetFromDiff(diffEntries));
                    }

                    // If the commitIdFrom is the same as commitIdTo, there is nothing to calculate, otherwise,
                    // let's do it
                    if (!objCommitIdFrom.equals(objCommitIdTo)) {
                        // Compare HEAD with commitId we're given
                        // Get list of commits between commitId and HEAD in chronological order

                        RevTree fromTree = helper.getTreeForCommit(repo, objCommitIdFrom.getName());
                        RevTree toTree = helper.getTreeForCommit(repo, objCommitIdTo.getName());
                        if (fromTree != null && toTree != null) {
                            try (ObjectReader reader = repo.newObjectReader()) {
                                CanonicalTreeParser fromCommitTreeParser = new CanonicalTreeParser();
                                CanonicalTreeParser toCommitTreeParser = new CanonicalTreeParser();
                                fromCommitTreeParser.reset(reader, fromTree.getId());
                                toCommitTreeParser.reset(reader, toTree.getId());

                                // Diff the two commit Ids
                                DiffCommand diffCommand = git.diff()
                                        .setOldTree(fromCommitTreeParser)
                                        .setNewTree(toCommitTreeParser);
                                List<DiffEntry> diffEntries = retryingRepositoryOperationFacade.call(diffCommand);

                                // Now that we have a diff, let's itemize the file changes, pack them into a TO
                                // and add them to the list of RepoOperations to return to the caller
                                // also include date/time of commit by taking number of seconds and multiply by 1000 and
                                // convert to java date before sending over
                                changeSet.putAll(getChangeSetFromDiff(diffEntries));
                            }
                        }
                    }
                }
            } catch (GitAPIException | IOException e) {
                logger.error("Failed to get the git operations in site '{}' from commit ID '{}' to commit ID '{}'",
                        site, commitIdFrom, commitIdTo, e);
            } finally {
                generalLockService.unlock(gitLockKey);
            }
        }

        return changeSet;
    }

    private Map<String, String> getChangeSetFromDiff(List<DiffEntry> diffEntries) {
        Map<String, String> toReturn = new TreeMap<>();

        for (DiffEntry diffEntry : diffEntries) {

            // Update the paths to have a preceding separator
            String pathNew = FILE_SEPARATOR + diffEntry.getNewPath();
            String pathOld = FILE_SEPARATOR + diffEntry.getOldPath();

            switch (diffEntry.getChangeType()) {
                case ADD:
                case COPY:
                    toReturn.put(pathNew, "C");
                    break;
                case MODIFY:
                    toReturn.put(pathNew, "U");
                    break;
                case DELETE:
                    toReturn.put(pathOld, "D");
                    break;
                case RENAME:
                    toReturn.put(pathOld, pathNew);
                    break;
                default:
                    logger.error("Unknown git operation '{}'", diffEntry.getChangeType());
                    break;
            }
        }
        return toReturn;
    }

    private RevTree getTree(Repository repository, String branch) throws IOException {
        ObjectId lastCommitId = repository.resolve(R_HEADS + branch );

        if (Objects.nonNull(lastCommitId)) {
            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(lastCommitId);

                return commit.getTree();
            }
        } else {
            return null;
        }
    }

    @Override
    public String getPreviousCommitId(String siteId, String commitId) {
        String toReturn = EMPTY;
        try {
            Repository repository = helper.getRepository(siteId, isEmpty(siteId) ? GLOBAL : SANDBOX);
            if (repository != null) {
                ObjectId head = repository.resolve(HEAD);
                try (Git git = new Git(repository)) {
                    LogCommand logCommand = git.log().add(head);
                    Iterable<RevCommit> commits = retryingRepositoryOperationFacade.call(logCommand);
                    Iterator<RevCommit> iterator = commits.iterator();
                    boolean found = false;
                    while (!found && iterator.hasNext()) {
                        RevCommit revCommit = iterator.next();
                        if (StringUtils.equals(commitId, revCommit.getName())) {
                            found = true;
                            if (iterator.hasNext()) {
                                revCommit = iterator.next();
                                toReturn = revCommit.getName();
                            }
                        }
                    }
                }
            }
        } catch (IOException | GitAPIException e) {
            logger.error("Failed to get the previous commit ID in site '{}' commit ID '{}'",
                    siteId, commitId, e);
        }

        return toReturn;
    }

    @Override
    public void lockItem(String site, String path) {
        String gitLockKey = helper.getSandboxRepoLockKey(site, true);
        Repository repo = helper.getRepository(site, isEmpty(site) ? GLOBAL : SANDBOX);
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
            logger.error("Failed to lock file at '{}' path '{}'", site, path, e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
    }

    @Override
    public void itemUnlock(String site, String path) {
        String gitLockKey = helper.getSandboxRepoLockKey(site, true);
        Repository repo = helper.getRepository(site, isEmpty(site) ? GLOBAL : SANDBOX);
        generalLockService.lock(gitLockKey);
        try (TreeWalk tw = new TreeWalk(repo)) {
            RevTree tree = helper.getTreeForLastCommit(repo);
            tw.addTree(tree);
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
            logger.error("Failed to unlock file at site '{}' path '{}'", site, path, e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
    }

    @Override
    public Optional<Resource> getContentByCommitId(String site, String path, String commitId) {
        try {
            Repository repo = helper.getRepository(site, isEmpty(site) ? GLOBAL : SANDBOX);
            RevTree tree = helper.getTreeForCommit(repo, commitId);
            if (tree != null) {
                try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree)) {
                    if (tw != null) {
                        ObjectId id = tw.getObjectId(0);
                        ObjectLoader objectLoader = repo.open(id);
                        if (OBJ_BLOB == objectLoader.getType()) {
                            return Optional.of(new GitResource(objectLoader));
                        }
                        return Optional.empty();
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to get content from file at site '{}' path '{}' with commit ID '{}'",
                    site, path, commitId, e);
        }
        return Optional.empty();
    }

    @Override
    public boolean publishedRepositoryExists(String siteId) {
        return Objects.nonNull(helper.getRepository(siteId, PUBLISHED));
    }

    @Override
    public String initialPublish(final String siteId) throws SiteNotFoundException {
        String commitId = getRepoLastCommitId(siteId);
        // Create published repo
        var created = helper.createPublishedRepository(siteId);
        if (created) {
            // Create staging branch
            if (servicesConfig.isStagingEnvironmentEnabled(siteId)) {
                createEnvironmentBranch(siteId, commitId,
                        servicesConfig.getStagingEnvironment(siteId));
            }
            // Create live branch
            createEnvironmentBranch(siteId, commitId,
                    servicesConfig.getLiveEnvironment(siteId));
            siteService.setPublishedRepoCreated(siteId);
        }

        logger.info("Completed the initial publish of the site '{}'", siteId);
        return commitId;
    }

    private void createEnvironmentBranch(String siteId, String startPoint, String environment) {
        Repository repository = helper.getRepository(siteId, PUBLISHED);
        try (Git git = new Git(repository)) {
            CreateBranchCommand createBranchCommand = git.branchCreate().setName(environment).setStartPoint(startPoint);
            retryingRepositoryOperationFacade.call(createBranchCommand);
        } catch (GitAPIException e) {
            logger.error("Failed to create the publishing target branch '{}' in the published repo for " +
                    "site '{}'", environment, siteId, e);
        }
    }

    @Override
    public <T extends PublishItemTO> GitPublishChangeSet<T> publishAll(final PublishPackage publishPackage,
                                                                    final String publishingTarget,
                                                                    final Collection<T> publishItems)
            throws ServiceLayerException, IOException {
        String siteId = publishPackage.getSite().getSiteId();
        logger.debug("Publishing all changes for site '{}' package '{}' target '{}'",
                siteId, publishPackage.getId(), publishingTarget);
        Repository repo = helper.getRepository(siteId, PUBLISHED);
        if (repo == null) {
            throw new PublishedRepositoryNotFoundException(
                    format("Failed to publish package '%s' for site '%s': published repository not found",
                            publishPackage.getId(), siteId));
        }
        ensureEnvironmentBranch(siteId, publishingTarget, repo, publishPackage.getSite().getSandboxBranch());
        String repoLockKey = helper.getPublishedRepoLockKey(siteId);
        generalLockService.lock(repoLockKey);
        try (Git git = Git.wrap(repo)) {
            logger.debug("Fetching changes from sandbox to published repo for site '{}' package '{}' target '{}'",
                    siteId, publishPackage.getId(), publishingTarget);
            retryingRepositoryOperationFacade.call(git.fetch());
            RevTree sandboxTree = helper.getTreeForCommit(repo, publishPackage.getCommitId());
            ObjectId publishedLastCommitId = repo.resolve(publishingTarget);
            logger.debug("Creating new commit for tree '{}' in published repo for site '{}' package '{}' target '{}'",
                    sandboxTree, siteId, publishPackage.getId(), publishingTarget);

            User user = userServiceInternal.getUserByIdOrUsername(publishPackage.getSubmitterId(), "");
            String newCommitId = helper.commitTree(repo, sandboxTree.getId().getName(),
                    publishedLastCommitId, user, getPublishCommitMessage(publishPackage, user));
            logger.debug("Published all changes for site '{}' package '{}' target '{}'",
                    siteId, publishPackage.getId(), publishingTarget);
            return new GitPublishChangeSet<>(newCommitId, publishItems, emptyList());
        } catch (GitAPIException | IOException | UserNotFoundException e) {
            logger.error("Failed to publish all changes for site '{}' package '{}' target '{}'",
                    siteId, publishPackage.getId(), publishingTarget, e);
            throw new ServiceLayerException(format("Failed to publish all changes for site '%s' package '%s' target '%s'",
                    siteId, publishPackage.getId(), publishingTarget), e);
        } finally {
            generalLockService.unlock(repoLockKey);
        }
    }

    @Override
    public void updateRef(final String siteId, final long packageId,
                          final String newCommitId, final String publishingTarget) throws IOException {
        Repository repo = helper.getRepository(siteId, PUBLISHED);
        String repoLockKey = helper.getPublishedRepoLockKey(siteId);
        generalLockService.lock(repoLockKey);
        try {
            logger.debug("Updating target branch '{}' in published repo for site '{}' package '{}' with new commit ID '{}'",
                    publishingTarget, siteId, packageId, newCommitId);
            RefUpdate refUpdate = repo.updateRef(format(REFS_HEADS_FORMAT, publishingTarget));
            refUpdate.setNewObjectId(repo.resolve(newCommitId));
            refUpdate.update();
        } finally {
            generalLockService.unlock(repoLockKey);
        }
    }

    @Override
    @LogExecutionTime
    public <T extends PublishItemTO> GitPublishChangeSet<T> publish(final PublishPackage publishPackage,
                                                                 final String publishingTarget,
                                                                 final Collection<T> publishItems) throws ServiceLayerException, IOException {
        String siteId = publishPackage.getSite().getSiteId();
        logger.debug("Publishing all changes for site '{}' package '{}' target '{}'",
                siteId, publishPackage.getId(), publishingTarget);
        if (isEmpty(publishItems)) {
            logger.warn("No items to publish for site '{}' package '{}' target '{}'",
                    siteId, publishPackage.getId(), publishingTarget);
            throw new PublishException(format("No items to publish for site '%s' package '%s' target '%s'",
                    siteId, publishPackage.getId(), publishingTarget));
        }
        Repository repo = helper.getRepository(siteId, PUBLISHED);
        if (repo == null) {
            throw new PublishedRepositoryNotFoundException(
                    format("Failed to publish package '%s' for site '%s': published repository not found",
                            publishPackage.getId(), siteId));
        }
        ensureEnvironmentBranch(siteId, publishingTarget, repo, publishPackage.getSite().getSandboxBranch());
        String repoLockKey = helper.getPublishedRepoLockKey(siteId);
        generalLockService.lock(repoLockKey);
        try (Git git = Git.wrap(repo)) {
            logger.debug("Fetching changes from sandbox to published repo for site '{}' package '{}' target '{}'",
                    siteId, publishPackage.getId(), publishingTarget);
            retryingRepositoryOperationFacade.call(git.fetch());
            User user = userServiceInternal.getUserByIdOrUsername(publishPackage.getSubmitterId(), "");
            ObjectId publishedLastCommitId = repo.resolve(publishingTarget);

            // Get affected paths, translate to git paths, group by action
            Map<Action, List<String>> pathsByAction = publishItems.stream()
                    .collect(groupingBy(PublishItemTO::getAction,
                            mapping(((Function<String, String>) helper::getGitPath).compose(PublishItemTO::getPath), toList())));

            // git read-tree target_branch
            // git ls-tree commit_id list_of_paths | git update-index --index-info
            // git write-tree
            String newTreeId = helper.writeTree(repo,
                    pathsByAction.get(ADD),
                    pathsByAction.get(DELETE),
                    publishPackage.getCommitId(),
                    publishedLastCommitId);
            // git commit-tree
            String newCommitId = helper.commitTree(repo, newTreeId, publishedLastCommitId, user, getPublishCommitMessage(publishPackage, user));
            logger.debug("Published all changes for site '{}' package '{}' target '{}'",
                    siteId, publishPackage.getId(), publishingTarget);
            return new GitPublishChangeSet<>(newCommitId, publishItems, emptyList());
        } catch (GitAPIException | IOException | UserNotFoundException | InterruptedException e) {
            logger.error("Failed to publish changes for site '{}' package '{}' target '{}'",
                    siteId, publishPackage.getId(), publishingTarget, e);
            throw new ServiceLayerException(format("Failed to publish all changes for site '%s' package '%s' target '%s'",
                    siteId, publishPackage.getId(), publishingTarget), e);
        } finally {
            generalLockService.unlock(repoLockKey);
        }
    }

    private String getPublishCommitMessage(final PublishPackage publishPackage, final User user) throws UserNotFoundException, ServiceLayerException {
        String commitMessage = studioConfiguration.getProperty(REPO_PUBLISHED_COMMIT_MESSAGE);

        commitMessage = commitMessage.replace("{username}", user.getUsername());
        commitMessage =
                commitMessage.replace("{datetime}",
                        DateUtils.getCurrentTime().format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmssSSSX")));
        commitMessage = commitMessage.replace("{source}", "UI");
        commitMessage = commitMessage.replace("{message}", defaultIfEmpty(publishPackage.getSubmitterComment(), ""));
        commitMessage = commitMessage.replace("{commit_id}", publishPackage.getCommitId());
        commitMessage = commitMessage.replace("{package_id}", String.valueOf(publishPackage.getId()));

        return commitMessage;
    }

    @Override
    public List<ItemVersion> getContentItemHistory(String site, String path) throws IOException, GitAPIException {
        List<ItemVersion> versionHistory = new ArrayList<>();
        final String gitPath = helper.getGitPath(path);
        String repoLockKey = helper.getSandboxRepoLockKey(site);
        Repository repo = helper.getRepository(site, SANDBOX);
        generalLockService.lock(repoLockKey);
        try (Git git = Git.wrap(repo)) {
            DiffConfig diffConfig = repo.getConfig().get(DiffConfig.KEY);
            final RevWalk revWalk = new RevWalk(git.getRepository());
            revWalk.setTreeFilter(FollowFilter.create(gitPath, diffConfig));
            revWalk.markStart(revWalk.parseCommit(repo.resolve(HEAD)));
            revWalk.sort(RevSort.TOPO);
            String currentPath = gitPath;
            boolean revertible = true;
            for (RevCommit revCommit : revWalk) {
                ItemVersion version = new ItemVersion();
                version.setRevertible(revertible);
                version.setPath(prependIfMissing(currentPath, FILE_SEPARATOR));
                version.setVersionNumber(revCommit.getName());
                version.setCommitter(revCommit.getAuthorIdent().getName());
                version.setModifiedDate(
                        Instant.ofEpochSecond(revCommit.getCommitTime()).atZone(UTC));
                version.setComment(revCommit.getFullMessage());
                try {
                    DiffEntry diffEntry = helper.getDiffEntry(repo, revCommit, currentPath);
                    if (!StringUtils.equals(currentPath, diffEntry.getOldPath())) {
                        if (StringUtils.equals(diffEntry.getOldPath(), DiffEntry.DEV_NULL)) {
                            currentPath = null;
                        } else {
                            currentPath = diffEntry.getOldPath();
                            revertible = false;
                        }
                    }
                } catch (NoChangesForPathException e) {
                    logger.error("Failed to get diff entry for path '{}' in commit '{}'", currentPath, revCommit.getName(), e);
                }
                // Set this after the diff entry is retrieved, so that the old path is set correctly
                version.setOldPath(prependIfMissing(currentPath, FILE_SEPARATOR));
                versionHistory.add(version);
            }
        } finally {
            generalLockService.unlock(repoLockKey);
        }
        return versionHistory;
    }

    @Override
    public List<String> getCommitIdsBetween(final String site, final String commitFrom, final String commitTo) throws IOException {
        List<String> result = new ArrayList<>();
        String repoLockKey = helper.getSandboxRepoLockKey(site);
        Repository repo = helper.getRepository(site, SANDBOX);
        generalLockService.lock(repoLockKey);
        try (Git git = Git.wrap(repo)) {
            // git log --first-parent --reverse commitFrom..commitTo
            RevWalk revWalk = new RevWalk(git.getRepository());
            revWalk.setFirstParent(true);
            revWalk.markStart(revWalk.parseCommit(repo.resolve(commitTo)));
            revWalk.setRevFilter(new RevFilter() {
                @Override
                public boolean include(RevWalk walker, RevCommit commit) throws StopWalkException {
                    if (!commit.getName().equals(commitFrom)) {
                        return true;
                    }
                    throw StopWalkException.INSTANCE;
                }

                @Override
                public RevFilter clone() {
                    return this;
                }
            });
            revWalk.sort(TOPO_KEEP_BRANCH_TOGETHER);
            revWalk.sort(REVERSE, true);

            for (RevCommit revCommit : revWalk) {
                result.add(revCommit.getName());
            }
        } finally {
            generalLockService.unlock(repoLockKey);
        }
        return result;
    }

    @Override
    public List<String> getIntroducedCommits(String site, String baseCommit, String commitId) throws IOException, GitAPIException {
        List<String> result = new ArrayList<>();
        String repoLockKey = helper.getSandboxRepoLockKey(site);
        Repository repo = helper.getRepository(site, SANDBOX);
        generalLockService.lock(repoLockKey);
        try (Git git = Git.wrap(repo)) {
            RevCommit revCommitBase = repo.parseCommit(repo.resolve(baseCommit));
            RevCommit revCommit = repo.parseCommit(repo.resolve(commitId));

            git.log().addRange(revCommitBase, revCommit).call().forEach(commit -> {
                result.add(commit.getName());
            });
        } finally {
            generalLockService.unlock(repoLockKey);
        }
        return result;
    }

    @Override
    public void duplicateSite(String sourceSiteId, String siteId, String sourceSandboxBranch, String sandboxBranch) throws IOException, ServiceLayerException {
        String repoLockKey = helper.getSandboxRepoLockKey(sourceSiteId);
        generalLockService.lock(repoLockKey);

        try {
            Path sourceSandboxPath = helper.buildRepoPath(SANDBOX, sourceSiteId);
            Path destSandboxPath = helper.buildRepoPath(SANDBOX, siteId);
            if (destSandboxPath.toFile().exists()) {
                logger.warn("Deleting existing sandbox repository for site '{}'", siteId);
                FileUtils.deleteDirectory(destSandboxPath.toFile());
            }
            FileUtils.copyDirectory(sourceSandboxPath.toFile(), destSandboxPath.toFile());
            // Cache the repo and checkout the sandbox branch
            helper.getRepository(siteId, SANDBOX, sandboxBranch);

            if (!publishedRepositoryExists(sourceSiteId)) {
                return;
            }
            Path sourcePublishedPath = helper.buildRepoPath(PUBLISHED, sourceSiteId);
            Path destPublishedPath = helper.buildRepoPath(PUBLISHED, siteId);
            if (destPublishedPath.toFile().exists()) {
                logger.warn("Deleting existing published repository for site '{}'", siteId);
                FileUtils.deleteDirectory(destPublishedPath.toFile());
            }
            FileUtils.copyDirectory(sourcePublishedPath.toFile(), destPublishedPath.toFile());
            // Cache the repo
            Repository publishedRepo = helper.getRepository(siteId, PUBLISHED);
            if (StringUtils.equals(sourceSandboxBranch, sandboxBranch)) {
                return;
            }
            try {
                boolean create = !branchExists(publishedRepo, sandboxBranch);
                helper.checkoutBranch(publishedRepo, sourceSandboxBranch, sandboxBranch, create);
            } catch (GitAPIException e) {
                throw new ServiceLayerException(format("Failed to duplicate site '%s' to '%s'", sourceSiteId, siteId), e);
            }
        } finally {
            generalLockService.unlock(repoLockKey);
        }
    }

    public void setHelper(GitRepositoryHelper helper) {
        this.helper = helper;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public void setRemoteRepositoryDAO(RemoteRepositoryDAO remoteRepositoryDAO) {
        this.remoteRepositoryDAO = remoteRepositoryDAO;
    }

    public void setEncryptor(TextEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    public void setContextManager(ContextManager contextManager) {
        this.contextManager = contextManager;
    }

    public void setContentStoreService(ContentStoreService contentStoreService) {
        this.contentStoreService = contentStoreService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public void setSiteService(SitesService siteService) {
        this.siteService = siteService;
    }

    public void setRetryingRepositoryOperationFacade(RetryingRepositoryOperationFacade retryingRepositoryOperationFacade) {
        this.retryingRepositoryOperationFacade = retryingRepositoryOperationFacade;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }

    public void setPublishingProgressServiceInternal(PublishingProgressServiceInternal publishingProgressServiceInternal) {
        this.publishingProgressServiceInternal = publishingProgressServiceInternal;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setScriptRunnerFactory(StudioDBScriptRunnerFactory scriptRunnerFactory) {
        this.scriptRunnerFactory = scriptRunnerFactory;
    }
}
