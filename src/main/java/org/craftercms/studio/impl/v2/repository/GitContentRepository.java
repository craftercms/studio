/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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
import org.apache.commons.collections4.iterators.ReverseListIterator;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.dal.DeploymentSyncHistory;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;
import org.craftercms.studio.api.v1.util.filter.DmFilterWrapper;
import org.craftercms.studio.api.v2.annotation.RetryingOperation;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.dal.GitLog;
import org.craftercms.studio.api.v2.dal.GitLogDAO;
import org.craftercms.studio.api.v2.dal.PublishingHistoryItem;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.dal.RemoteRepositoryDAO;
import org.craftercms.studio.api.v2.dal.RepoOperation;
import org.craftercms.studio.api.v2.dal.RetryingOperationFacade;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v2.service.deployment.DeploymentHistoryProvider;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.utils.RingBuffer;
import org.craftercms.studio.impl.v2.utils.StudioUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.DeleteBranchCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.AndRevFilter;
import org.eclipse.jgit.revwalk.filter.AuthorRevFilter;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.revwalk.filter.MessageRevFilter;
import org.eclipse.jgit.revwalk.filter.NotRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.dao.DuplicateKeyException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.craftercms.studio.api.v1.constant.GitRepositories.GLOBAL;
import static org.craftercms.studio.api.v1.constant.GitRepositories.PUBLISHED;
import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CLUSTER_MEMBER_LOCAL_ADDRESS;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.INDEX_FILE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.IN_PROGRESS_BRANCH_NAME_SUFFIX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_PUBLISHED_REPOSITORY_GIT_LOCK;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_SANDBOX_REPOSITORY_GIT_LOCK;
import static org.craftercms.studio.api.v2.dal.RepoOperation.Action.COPY;
import static org.craftercms.studio.api.v2.dal.RepoOperation.Action.CREATE;
import static org.craftercms.studio.api.v2.dal.RepoOperation.Action.DELETE;
import static org.craftercms.studio.api.v2.dal.RepoOperation.Action.MOVE;
import static org.craftercms.studio.api.v2.dal.RepoOperation.Action.UPDATE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CLUSTERING_NODE_REGISTRATION;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_COMMIT_MESSAGE_POSTSCRIPT;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_COMMIT_MESSAGE_PROLOGUE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_INITIAL_COMMIT_COMMIT_MESSAGE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_PUBLISHED_COMMIT_MESSAGE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_SANDBOX_BRANCH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_SYNC_DB_COMMIT_MESSAGE_NO_PROCESSING;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.EMPTY_FILE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.IGNORE_FILES;
import static org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.TRACK;
import static org.eclipse.jgit.api.ResetCommand.ResetType.HARD;
import static org.eclipse.jgit.lib.Constants.DEFAULT_REMOTE_NAME;
import static org.eclipse.jgit.lib.Constants.DOT_GIT_IGNORE;
import static org.eclipse.jgit.lib.Constants.HEAD;
import static org.eclipse.jgit.lib.Constants.MASTER;
import static org.eclipse.jgit.lib.Constants.OBJ_TREE;
import static org.eclipse.jgit.lib.Constants.R_HEADS;
import static org.eclipse.jgit.merge.MergeStrategy.THEIRS;
import static org.eclipse.jgit.revwalk.RevSort.REVERSE;

public class GitContentRepository implements ContentRepository, DeploymentHistoryProvider {

    private static final Logger logger = LoggerFactory.getLogger(GitContentRepository.class);

    private StudioConfiguration studioConfiguration;
    private GitLogDAO gitLogDao;
    private SiteFeedMapper siteFeedMapper;
    private UserServiceInternal userServiceInternal;
    private SecurityService securityService;
    private RemoteRepositoryDAO remoteRepositoryDAO;
    private TextEncryptor encryptor;
    private ClusterDAO clusterDao;
    private GeneralLockService generalLockService;
    private SiteService siteService;
    private ObjectMetadataManager objectMetadataManager;
    private StudioUtils studioUtils;
    private RetryingOperationFacade retryingOperationFacade;
    private RetryingRepositoryOperationFacade retryingRepositoryOperationFacade;

    @Override
    public List<String> getSubtreeItems(String site, String path) {
        final List<String> retItems = new ArrayList<String>();
        String rootPath;
        if (path.endsWith(FILE_SEPARATOR + INDEX_FILE)) {
            int lastIdx = path.lastIndexOf(FILE_SEPARATOR + INDEX_FILE);
            rootPath = path.substring(0, lastIdx);
        } else {
            rootPath = path;
        }
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);

            RevTree tree = helper.getTreeForLastCommit(repo);
            try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(rootPath), tree)) {

                if (tw != null) {
                    // Loop for all children and gather path of item excluding the item, file/folder name, and
                    // whether or not it's a folder
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
                        logger.debug("Object is not tree for site: " + site + " path: " + path +
                                " - it does not have children");
                    }
                } else {
                    String gitPath = helper.getGitPath(rootPath);
                    if (StringUtils.isEmpty(gitPath) || gitPath.equals(".")) {
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

                        } catch (IOException e) {
                            logger.error("Error while getting children for site: " + site + " path: " + path, e);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Error while getting children for site: " + site + " path: " + path, e);
            }
        } catch (IOException | CryptoException e) {
            logger.error("Failed to create RevTree for site: " + site + " path: " + path, e);
        }
        return retItems;
    }

    @Override
    public List<RepoOperation> getOperations(String site, String commitIdFrom, String commitIdTo) {
        List<RepoOperation> operations = new ArrayList<>();
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repository =
                    helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
            if (repository != null) {
                    try {
                        // Get the sandbox repo, and then get a reference to the commitId we received and another for head
                        boolean fromEmptyRepo = StringUtils.isEmpty(commitIdFrom);
                        String firstCommitId = getRepoFirstCommitId(site);
                        if (fromEmptyRepo) {
                            commitIdFrom = firstCommitId;
                        }
                        Repository repo = helper.getRepository(site, SANDBOX);
                        ObjectId objCommitIdFrom = repo.resolve(commitIdFrom);
                        ObjectId objCommitIdTo = repo.resolve(commitIdTo);

                        ObjectId objFirstCommitId = repo.resolve(firstCommitId);

                        try (Git git = new Git(repo)) {

                            if (fromEmptyRepo) {
                                try (RevWalk walk = new RevWalk(repo)) {
                                    RevCommit firstCommit = walk.parseCommit(objFirstCommitId);
                                    try (ObjectReader reader = repo.newObjectReader()) {
                                        CanonicalTreeParser firstCommitTreeParser = new CanonicalTreeParser();
                                        firstCommitTreeParser.reset();//reset(reader, firstCommitTree.getId());
                                        // Diff the two commit Ids
                                        long startDiffMark1 = logger.isDebugEnabled() ? System.currentTimeMillis() : 0;
                                        List<DiffEntry> diffEntries = git.diff()
                                                .setOldTree(firstCommitTreeParser)
                                                .setNewTree(null)
                                                .call();
                                        if (logger.isDebugEnabled()) {
                                            logger.debug("Diff from " + objFirstCommitId.getName() + " to null " +
                                                    " finished in " +
                                                    ((System.currentTimeMillis() - startDiffMark1) / 1000) + " seconds");
                                            logger.debug("Number of diff entries " + diffEntries.size());
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

                                // Get the log of all the commits between commitId and head
                                Iterable<RevCommit> commits = git.log().addRange(objCommitIdFrom, objCommitIdTo).call();

                                // Loop through through the commits and diff one from the next util head
                                ObjectId prevCommitId = objCommitIdFrom;
                                ObjectId nextCommitId = objCommitIdFrom;
                                String author = EMPTY;

                                // Reverse orders of commits
                                // TODO: DB: try to find better algorithm
                                Iterator<RevCommit> iterator = commits.iterator();
                                List<RevCommit> revCommits = new ArrayList<RevCommit>();
                                while (iterator.hasNext()) {

                                    RevCommit commit = iterator.next();
                                    revCommits.add(commit);
                                }

                                ReverseListIterator<RevCommit> reverseIterator = new ReverseListIterator<RevCommit>(revCommits);
                                while (reverseIterator.hasNext()) {

                                    RevCommit commit = reverseIterator.next();

                                    if (StringUtils.contains(commit.getFullMessage(),
                                            studioConfiguration.getProperty(REPO_SYNC_DB_COMMIT_MESSAGE_NO_PROCESSING))) {
                                        prevCommitId = commit.getId();
                                        logger.debug("Skipping commitId: " + prevCommitId.getName() + " for site "
                                                + site + " because it is marked not to be processed.");
                                        GitLog gitLog = getGitLog(site, prevCommitId.getName());
                                        if (gitLog != null) {
                                            markGitLogVerifiedProcessed(site, prevCommitId.getName());
                                        } else {
                                            insertGitLog(site, prevCommitId.getName(), 1);
                                        }
                                        updateLastVerifiedGitlogCommitId(site, prevCommitId.getName());
                                    } else {
                                        nextCommitId = commit.getId();
                                        if (commit.getAuthorIdent() != null) {
                                            author = commit.getAuthorIdent().getName();
                                        }
                                        if (StringUtils.isEmpty(author)) {
                                            author = commit.getCommitterIdent().getName();
                                        }

                                        RevTree prevTree = helper.getTreeForCommit(repo, prevCommitId.getName());
                                        RevTree nextTree = helper.getTreeForCommit(repo, nextCommitId.getName());
                                        if (prevTree != null && nextTree != null) {
                                            try (ObjectReader reader = repo.newObjectReader()) {
                                                CanonicalTreeParser prevCommitTreeParser = new CanonicalTreeParser();
                                                CanonicalTreeParser nextCommitTreeParser = new CanonicalTreeParser();
                                                prevCommitTreeParser.reset(reader, prevTree.getId());
                                                nextCommitTreeParser.reset(reader, nextTree.getId());

                                                // Diff the two commit Ids
                                                long startDiffMark2 = logger.isDebugEnabled() ?
                                                        System.currentTimeMillis() : 0;
                                                List<DiffEntry> diffEntries = git.diff()
                                                        .setOldTree(prevCommitTreeParser)
                                                        .setNewTree(nextCommitTreeParser)
                                                        .call();
                                                if (logger.isDebugEnabled()) {
                                                    logger.debug("Diff from " + objCommitIdFrom.getName() + " to " +
                                                            objCommitIdTo.getName() + " finished in " +
                                                            ((System.currentTimeMillis() - startDiffMark2) / 1000) + " seconds");
                                                    logger.debug("Number of diff entries " + diffEntries.size());
                                                }
                                                // Now that we have a diff, let's itemize the file changes, pack them into a TO
                                                // and add them to the list of RepoOperations to return to the caller
                                                // also include date/time of commit by taking number of seconds and multiply by 1000 and
                                                // convert to java date before sending over
                                                operations.addAll(processDiffEntry(git, diffEntries, nextCommitId));
                                                prevCommitId = nextCommitId;
                                            }
                                        }
                                    }
                                }

                            }
                        } catch (GitAPIException e) {
                            logger.error("Error getting operations for site " + site + " from commit ID: " + commitIdFrom
                                    + " to commit ID: " + commitIdTo, e);
                        }
                    } catch (IOException e) {
                        logger.error("Error getting operations for site " + site + " from commit ID: " + commitIdFrom +
                                " to commit ID: " + commitIdTo, e);
                    }
            }
        } catch (CryptoException e) {
            logger.error("Error getting operations for site " + site + " from commit ID: " + commitIdFrom +
                    " to commit ID: " + commitIdTo, e);
        }

        return operations;
    }

    @Override
    public List<RepoOperation> getOperationsFromDelta(String site, String commitIdFrom, String commitIdTo) {
        List<RepoOperation> operations = new ArrayList<>();
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repository =
                    helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
            if (repository != null) {
                    try {
                        // Get the sandbox repo, and then get a reference to the commitId we received and another for head
                        boolean fromEmptyRepo = StringUtils.isEmpty(commitIdFrom);
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
                                            List<DiffEntry> diffEntries = git.diff()
                                                    .setOldTree(firstCommitTreeParser)
                                                    .setNewTree(null)
                                                    .call();
                                            if (logger.isDebugEnabled()) {
                                                logger.debug("Diff from " + objFirstCommitId.getName() + " to null " +
                                                        "finished in " + ((System.currentTimeMillis() - startDiffMark1) / 1000)
                                                        + " seconds");
                                                logger.debug("Number of diff entries " + diffEntries.size());
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
                                            List<DiffEntry> diffEntries = git.diff()
                                                    .setOldTree(fromCommitTreeParser)
                                                    .setNewTree(toCommitTreeParser)
                                                    .call();
                                            if (logger.isDebugEnabled()) {
                                                logger.debug("Diff from " + objCommitIdFrom.getName() + " to " +
                                                        objCommitIdTo.getName() + " finished in " +
                                                        ((System.currentTimeMillis() - startDiffMark2) / 1000) + " seconds");
                                                logger.debug("Number of diff entries " + diffEntries.size());
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
                            } catch (GitAPIException e) {
                                logger.error("Error getting operations for site " + site + " from commit ID: "
                                        + commitIdFrom + " to commit ID: " + commitIdTo, e);
                            }
                        }
                    } catch (IOException e) {
                        logger.error("Error getting operations for site " + site + " from commit ID: "
                                + commitIdFrom + " to commit ID: " + commitIdTo, e);
                    }
            }
        } catch (CryptoException e) {
            logger.error("Error getting operations for site " + site + " from commit ID: " + commitIdFrom +
                    " to commit ID: " + commitIdTo, e);
        }

        return operations;
    }

    @Override
    public String getRepoFirstCommitId(final String site) {
        String toReturn = EMPTY;
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repository = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
            if (repository != null) {
                synchronized (repository) {
                    Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
                    if (repo != null) {
                        try (RevWalk rw = new RevWalk(repo)) {
                            ObjectId head = repo.resolve(HEAD);
                            if (head != null) {
                                RevCommit root = rw.parseCommit(head);
                                rw.sort(REVERSE);
                                rw.markStart(root);
                                ObjectId first = rw.next();
                                toReturn = first.getName();
                                logger.debug("getRepoFirstCommitId for site: " + site + " First commit ID: " + toReturn);
                            }
                        } catch (IOException e) {
                            logger.error("Error getting first commit ID for site " + site, e);
                        }
                    }
                }
            }
        } catch (CryptoException e) {
            logger.error("Error getting first commit ID for site " + site, e);
        }
        return toReturn;
    }

    private List<RepoOperation> processDiffEntry(Git git, List<DiffEntry> diffEntries, ObjectId commitId)
            throws GitAPIException, IOException {
        int size = diffEntries.size();
        logger.debug("Processing " + size + " diff entries");
        long startMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0;
        List<RepoOperation> toReturn = new ArrayList<RepoOperation>();

        int idx = 0;
        for (DiffEntry diffEntry : diffEntries) {
            logger.debug("Processing " + ++idx + " of " + size + " diff entries");
            long startProcessEntryMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0;
            // Update the paths to have a preceding separator
            String pathNew = FILE_SEPARATOR + diffEntry.getNewPath();
            String pathOld = FILE_SEPARATOR + diffEntry.getOldPath();

            if (ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(pathNew)) ||
                    ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(pathOld))) {
                continue;
            }

            RepoOperation repoOperation = null;
            Iterable<RevCommit> iterable = null;
            RevCommit revCommit = null;
            ZonedDateTime commitTime = null;
            String author = null;

            try (Repository repo = git.getRepository()) {
                try (RevWalk revWalk = new RevWalk(repo)) {
                    revCommit = revWalk.parseCommit(commitId);
                }
            }
            if (revCommit == null) {
                iterable = git.log().setMaxCount(1).call();
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
                    repoOperation = new RepoOperation(DELETE, pathOld, commitTime, null,
                            revCommit.getId().getName());
                    break;
                case RENAME:
                    repoOperation = new RepoOperation(MOVE, pathOld, commitTime, pathNew, commitId.getName());
                    break;
                case COPY:
                    repoOperation = new RepoOperation(COPY, pathNew, commitTime, null, commitId.getName());
                    break;
                default:
                    logger.error("Error: Unknown git operation " + diffEntry.getChangeType());
                    break;
            }
            if ((repoOperation != null) && (!repoOperation.getPath().endsWith(".keep"))) {
                repoOperation.setAuthor(StringUtils.isEmpty(author) ? "N/A" : author);
                toReturn.add(repoOperation);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Finished processing " + idx + " of " + size + " entries in " +
                        ((System.currentTimeMillis() - startProcessEntryMark) / 1000) + " seconds");
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Finished processing " + size + " diff entries in " +
                    ((System.currentTimeMillis() - startMark) / 1000) + " seconds");
        }
        return toReturn;
    }

    @Override
    public GitLog getGitLog(String siteId, String commitId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", siteId);
        params.put("commitId", commitId);
        return gitLogDao.getGitLog(params);
    }

    @Override
    public void markGitLogVerifiedProcessed(String siteId, String commitId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", siteId);
        params.put("commitId", commitId);
        params.put("processed", 1);
        retryingOperationFacade.markGitLogProcessed(params);
    }

    @RetryingOperation
    @Override
    public void markGitLogVerifiedProcessedBulk(String siteId, List<String> commitIds) {
        if (CollectionUtils.isNotEmpty(commitIds)) {
            int batchSize = studioUtils.getBulkOperationsBatchSize();
            List<List<String>> partitions = new ArrayList<List<String>>();
            for (int i = 0; i < commitIds.size(); i = i + (batchSize)) {
                partitions.add(commitIds.subList(i, Math.min(i + batchSize, commitIds.size())));
            }
            for (List<String> part : partitions) {
                gitLogDao.markGitLogProcessedBulk(siteId, part);
            }
        }
    }

    @RetryingOperation
    @Override
    public void insertGitLog(String siteId, String commitId, int processed) {
        insertGitLog(siteId, commitId, processed, 0);
    }

    @RetryingOperation
    @Override
    public void insertGitLog(String siteId, String commitId, int processed, int audited) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", siteId);
        params.put("commitId", commitId);
        params.put("processed", processed);
        params.put("audited", audited);
        try {
            gitLogDao.insertGitLog(params);
        } catch (DuplicateKeyException e) {
            logger.debug("Failed to insert commit id: " + commitId + " for site: " + siteId + " into" +
                    " gitlog table, because it is duplicate entry. Marking it as not processed so it can be" +
                    " processed by sync database task.");
            params = new HashMap<String, Object>();
            params.put("siteId", siteId);
            params.put("commitId", commitId);
            params.put("processed", 0);
            gitLogDao.markGitLogProcessed(params);
        }
    }

    private void updateLastVerifiedGitlogCommitId(String site, String commitId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", site);
        params.put("commitId", commitId);
        siteFeedMapper.updateLastVerifiedGitlogCommitId(params);
    }

    @Override
    public List<PublishingHistoryItem> getPublishingHistory(String siteId, String environment, String pathRegex,
                                                            String publisher, ZonedDateTime fromDate,
                                                            ZonedDateTime toDate, int limit) {
        List<PublishingHistoryItem> toRet = new ArrayList<PublishingHistoryItem>();
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository publishedRepo = helper.getRepository(siteId, PUBLISHED);
            if (publishedRepo != null) {
                int counter = 0;
                try (Git git = new Git(publishedRepo)) {
                    // List all environments
                    List<Ref> environments = git.branchList().call();
                    for (int i = 0; i < environments.size() && counter < limit; i++) {
                        Ref env = environments.get(i);
                        String environmentGit = env.getName();
                        environmentGit = environmentGit.replace(R_HEADS, "");
                        if ((StringUtils.isBlank(environment) && !StringUtils.equals(MASTER, environmentGit))
                                || StringUtils.equals(environment, environmentGit)) {
                            List<RevFilter> filters = new ArrayList<RevFilter>();
                            if (fromDate != null) {
                                filters.add(CommitTimeRevFilter.after(fromDate.toInstant().toEpochMilli()));
                            }
                            if (toDate != null) {
                                filters.add(CommitTimeRevFilter.before(toDate.toInstant().toEpochMilli()));
                            } else {
                                filters.add(CommitTimeRevFilter.before(ZonedDateTime.now().toInstant().toEpochMilli()));
                            }
                            filters.add(NotRevFilter.create(MessageRevFilter.create("Initial commit.")));
                            if (StringUtils.isNotEmpty(publisher)) {
                                User user = userServiceInternal.getUserByIdOrUsername(-1, publisher);
                                filters.add(AuthorRevFilter.create(helper.getAuthorIdent(user).getName()));
                            }
                            Iterable<RevCommit> branchLog = git.log()
                                    .add(env.getObjectId())
                                    .setRevFilter(AndRevFilter.create(filters))
                                    .call();

                            Iterator<RevCommit> iterator = branchLog.iterator();
                            while (iterator.hasNext() && counter < limit) {
                                RevCommit revCommit = iterator.next();
                                List<String> files = helper.getFilesInCommit(publishedRepo, revCommit);
                                for (int j = 0; j < files.size() && counter < limit; j++) {
                                    String file = files.get(j);
                                    Path path = Paths.get(file);
                                    String fileName = path.getFileName().toString();
                                    if (!ArrayUtils.contains(IGNORE_FILES, fileName)) {
                                        boolean addFile = false;
                                        if (StringUtils.isNotEmpty(pathRegex)) {
                                            Pattern pattern = Pattern.compile(pathRegex);
                                            Matcher matcher = pattern.matcher(file);
                                            addFile = matcher.matches();
                                        } else {
                                            addFile = true;
                                        }
                                        if (addFile) {
                                            PublishingHistoryItem phi = new PublishingHistoryItem();
                                            phi.setSiteId(siteId);
                                            phi.setPath(file);
                                            phi.setPublishedDate(
                                                    Instant.ofEpochSecond(revCommit.getCommitTime()).atZone(UTC));
                                            phi.setPublisher(revCommit.getAuthorIdent().getName());
                                            phi.setEnvironment(environmentGit.replace(R_HEADS, ""));
                                            toRet.add(phi);
                                            counter++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    git.close();
                    toRet.sort((o1, o2) -> o2.getPublishedDate().compareTo(o1.getPublishedDate()));
                } catch (IOException | GitAPIException | UserNotFoundException | ServiceLayerException e1) {
                    logger.error("Error while getting deployment history for site " + siteId, e1);
                }
            }
        } catch (CryptoException e) {
            e.printStackTrace();
        }
        return toRet;
    }

    @Override
    public boolean createSiteFromBlueprint(String blueprintLocation, String site, String sandboxBranch,
                                           Map<String, String> params) {
        boolean toReturn;
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
        generalLockService.lock(gitLockKey);
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);

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
                toReturn = helper.addGitIgnoreFile(site);
            }

            if (toReturn) {
                // commit everything so it is visible
                toReturn = helper.performInitialCommit(site, helper.getCommitMessage(REPO_INITIAL_COMMIT_COMMIT_MESSAGE),
                        sandboxBranch);
            }
        } catch (CryptoException e) {
            logger.error("Error creating site from blueprint", e);
            toReturn = false;
        } finally {
            generalLockService.unlock(gitLockKey);
        }

        return toReturn;
    }

    @Override
    public List<DeploymentSyncHistory> getDeploymentHistory(String site, List<String> environmentNames,
                                                            ZonedDateTime fromDate, ZonedDateTime toDate,
                                                            DmFilterWrapper dmFilterWrapper,
                                                            String filterType, int numberOfItems) {
        List<DeploymentSyncHistory> toRet = new ArrayList<DeploymentSyncHistory>();
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository publishedRepo = helper.getRepository(site, PUBLISHED);
            if (Objects.nonNull(publishedRepo)) {
                int counter = 0;
                try (Git git = new Git(publishedRepo)) {
                    // List all environments
                    List<Ref> environments = git.branchList().call();
                    for (int i = 0; i < environments.size() && counter < numberOfItems; i++) {
                        Ref env = environments.get(i);
                        String environment = env.getName();
                        environment = environment.replace(R_HEADS, "");
                        if (environmentNames.contains(environment)) {
                            List<RevFilter> filters = new ArrayList<RevFilter>();
                            filters.add(CommitTimeRevFilter.after(fromDate.toInstant().toEpochMilli()));
                            filters.add(CommitTimeRevFilter.before(toDate.toInstant().toEpochMilli()));
                            filters.add(NotRevFilter.create(MessageRevFilter.create("Initial commit.")));

                            Iterable<RevCommit> branchLog = git.log()
                                    .add(env.getObjectId())
                                    .setRevFilter(AndRevFilter.create(filters))
                                    .call();

                            Iterator<RevCommit> iterator = branchLog.iterator();
                            while (iterator.hasNext() && counter < numberOfItems) {
                                RevCommit revCommit = iterator.next();
                                List<String> files = helper.getFilesInCommit(publishedRepo, revCommit);
                                for (int j = 0; j < files.size() && counter < numberOfItems; j++) {
                                    String file = files.get(j);
                                    Path path = Paths.get(file);
                                    String fileName = path.getFileName().toString();
                                    if (!ArrayUtils.contains(IGNORE_FILES, fileName)) {
                                        if (dmFilterWrapper.accept(site, file, filterType)) {
                                            DeploymentSyncHistory dsh = new DeploymentSyncHistory();
                                            dsh.setSite(site);
                                            dsh.setPath(file);
                                            dsh.setSyncDate(
                                                    Instant.ofEpochSecond(revCommit.getCommitTime()).atZone(UTC));
                                            dsh.setUser(revCommit.getAuthorIdent().getName());
                                            dsh.setEnvironment(environment.replace(R_HEADS, ""));
                                            toRet.add(dsh);
                                            counter++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    git.close();
                    toRet.sort((o1, o2) -> o2.getSyncDate().compareTo(o1.getSyncDate()));
                }
            }
        } catch (CryptoException | IOException | GitAPIException e) {
            logger.error("Error while getting deployment history for site " + site, e);
        }
        return toRet;
    }

    @RetryingOperation
    @Override
    public void publish(String site, String sandboxBranch, List<DeploymentItemTO> deploymentItems, String environment,
                        String author, String comment) throws DeploymentException {
        if (CollectionUtils.isEmpty(deploymentItems)) {
            return;
        }
        String commitId = EMPTY;
        String gitLockKey = SITE_PUBLISHED_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
        generalLockService.lock(gitLockKey);
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repo = helper.getRepository(site, PUBLISHED);
            boolean repoCreated = false;
            if (Objects.isNull(repo)) {
                helper.createPublishedRepository(site, sandboxBranch);
                repo = helper.getRepository(site, PUBLISHED);
                repoCreated = Objects.nonNull(repo);
            }
            String path = EMPTY;
            String sandboxBranchName = sandboxBranch;
            if (StringUtils.isEmpty(sandboxBranchName)) {
                sandboxBranchName = studioConfiguration.getProperty(REPO_SANDBOX_BRANCH);
            }
            synchronized (repo) {
                try (Git git = new Git(repo)) {
                    String inProgressBranchName = environment + IN_PROGRESS_BRANCH_NAME_SUFFIX;

                    // fetch "origin/master"
                    logger.debug("Fetch from sandbox for site " + site);
                    FetchCommand fetchCommand = git.fetch();
                    retryingRepositoryOperationFacade.call(fetchCommand);

                    // checkout master and pull from sandbox
                    logger.debug("Checkout published/master branch for site " + site);
                    try {
                        // First delete it in case it already exists (ignored if does not exist)
                        String currentBranch = repo.getBranch();
                        if (currentBranch.endsWith(IN_PROGRESS_BRANCH_NAME_SUFFIX)) {
                            ResetCommand resetCommand = git.reset().setMode(HARD);
                            retryingRepositoryOperationFacade.call(resetCommand);
                        }

                        Ref ref = repo.exactRef(R_HEADS + sandboxBranchName);
                        boolean createBranch = (ref == null);

                        CheckoutCommand checkoutCommand = git.checkout()
                                .setName(sandboxBranchName)
                                .setCreateBranch(createBranch);
                        retryingRepositoryOperationFacade.call(checkoutCommand);

                        logger.debug("Delete in-progress branch, in case it was not cleaned up for site " + site);
                        DeleteBranchCommand deleteBranchCommand =
                                git.branchDelete().setBranchNames(inProgressBranchName).setForce(true);
                        retryingRepositoryOperationFacade.call(deleteBranchCommand);

                        PullCommand pullCommand = git.pull().
                                setRemote(DEFAULT_REMOTE_NAME)
                                .setRemoteBranchName(sandboxBranchName)
                                .setStrategy(THEIRS);
                        retryingRepositoryOperationFacade.call(pullCommand);
                    } catch (RefNotFoundException e) {
                        logger.error("Failed to checkout published master and to pull content from sandbox for site "
                                + site, e);
                        throw new DeploymentException("Failed to checkout published master and to pull content from " +
                                "sandbox for site " + site);
                    }

                    // checkout environment branch
                    logger.debug("Checkout environment branch " + environment + " for site " + site);
                    try {
                        CheckoutCommand checkoutCommand = git.checkout().setName(environment);
                        retryingRepositoryOperationFacade.call(checkoutCommand);
                    } catch (RefNotFoundException e) {
                        logger.info("Not able to find branch " + environment + " for site " + site +
                                ". Creating new branch");
                        // create new environment branch
                        // it will start as empty orphan branch
                        CheckoutCommand checkoutCommand = git.checkout()
                                .setOrphan(true)
                                .setForceRefUpdate(true)
                                .setStartPoint(sandboxBranchName)
                                .setUpstreamMode(TRACK)
                                .setName(environment);
                        retryingRepositoryOperationFacade.call(checkoutCommand);

                        // remove any content to create empty branch
                        RmCommand rmcmd = git.rm();
                        File[] toDelete = repo.getWorkTree().listFiles();
                        for (File toDel : toDelete) {
                            if (!repo.getDirectory().equals(toDel) &&
                                    !StringUtils.equals(toDel.getName(), DOT_GIT_IGNORE)) {
                                rmcmd.addFilepattern(toDel.getName());
                            }
                        }
                        retryingRepositoryOperationFacade.call(rmcmd);
                        CommitCommand commitCommand = git.commit()
                                .setMessage(helper.getCommitMessage(REPO_INITIAL_COMMIT_COMMIT_MESSAGE))
                                .setAllowEmpty(true);
                        retryingRepositoryOperationFacade.call(commitCommand);
                    }

                    // Create in progress branch
                    try {

                        // Create in progress branch
                        logger.debug("Create in-progress branch for site " + site);
                        CheckoutCommand checkoutCommand = git.checkout()
                                .setCreateBranch(true)
                                .setForceRefUpdate(true)
                                .setStartPoint(environment)
                                .setUpstreamMode(TRACK)
                                .setName(inProgressBranchName);
                        retryingRepositoryOperationFacade.call(checkoutCommand);
                    } catch (GitAPIException e) {
                        // TODO: DB: Error ?
                        logger.error("Failed to create in-progress published branch for site " + site);
                    }

                    Set<String> deployedCommits = new HashSet<String>();
                    Set<String> deployedPackages = new HashSet<String>();
                    logger.debug("Checkout deployed files started.");
                    AddCommand addCommand = git.add();
                    for (DeploymentItemTO deploymentItem : deploymentItems) {
                        commitId = deploymentItem.getCommitId();
                        path = helper.getGitPath(deploymentItem.getPath());
                        if (Objects.isNull(commitId) || !commitIdExists(site, PUBLISHED, commitId)) {
                            if (contentExists(site, path)) {
                                if (Objects.isNull(commitId)) {
                                    logger.warn("Commit ID is NULL for content " + path +
                                            ". Was the git repo reset at some point?" );
                                } else {
                                    logger.warn("Commit ID " + commitId + " does not exist for content " + path +
                                            ". Was the git repo reset at some point?" );
                                }
                                logger.info("Publishing content from HEAD for " + path);
                                commitId = getRepoLastCommitId(site);
                            } else {
                                logger.warn("Skipping file " + path + " because commit id is null");
                                continue;
                            }
                        }
                        logger.debug("Checking out file " + path + " from commit id " + commitId +
                                " for site " + site);

                        CheckoutCommand checkout = git.checkout();
                        checkout.setStartPoint(commitId).addPath(path);
                        retryingRepositoryOperationFacade.call(checkout);

                        if (deploymentItem.isMove()) {
                            if (!StringUtils.equals(deploymentItem.getPath(), deploymentItem.getOldPath())) {
                                String oldPath = helper.getGitPath(deploymentItem.getOldPath());
                                RmCommand rmCommand = git.rm().addFilepattern(oldPath).setCached(false);
                                retryingRepositoryOperationFacade.call(rmCommand);
                                cleanUpMoveFolders(git, oldPath);
                            }
                        }

                        if (deploymentItem.isDelete()) {
                            String deletePath = helper.getGitPath(deploymentItem.getPath());
                            boolean isPage = deletePath.endsWith(FILE_SEPARATOR + INDEX_FILE);
                            RmCommand rmCommand = git.rm().addFilepattern(deletePath).setCached(false);
                            retryingRepositoryOperationFacade.call(rmCommand);
                            Path parentToDelete = Paths.get(path).getParent();
                            deleteParentFolder(git, parentToDelete, isPage);
                        }
                        deployedCommits.add(commitId);
                        String packageId = deploymentItem.getPackageId();
                        if (StringUtils.isNotEmpty(packageId)) {
                            deployedPackages.add(deploymentItem.getPackageId());
                        }

                        addCommand.addFilepattern(path);
                        objectMetadataManager.updateLastPublishedDate(site, deploymentItem.getPath(),
                                ZonedDateTime.now(UTC));
                    }
                    logger.debug("Checkout deployed files completed.");

                    // commit all deployed files
                    String commitMessage = studioConfiguration.getProperty(REPO_PUBLISHED_COMMIT_MESSAGE);

                    logger.debug("Get Author Ident started.");
                    User user = userServiceInternal.getUserByIdOrUsername(-1, author);
                    PersonIdent authorIdent = helper.getAuthorIdent(user);
                    logger.debug("Get Author Ident completed.");

                    logger.debug("Git add all published items started.");
                    retryingRepositoryOperationFacade.call(addCommand);
                    logger.debug("Git add all published items completed.");

                    commitMessage = commitMessage.replace("{username}", author);
                    commitMessage =
                            commitMessage.replace("{datetime}",
                                    ZonedDateTime.now(UTC).format(
                                            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmssSSSX")));
                    commitMessage = commitMessage.replace("{source}", "UI");
                    commitMessage = commitMessage.replace("{message}", comment);
                    StringBuilder sb = new StringBuilder();
                    for (String c : deployedCommits) {
                        sb.append(c).append(" ");
                    }
                    StringBuilder sbPackage = new StringBuilder();
                    for (String p : deployedPackages) {
                        sbPackage.append(p).append(" ");
                    }
                    commitMessage = commitMessage.replace("{commit_id}", sb.toString().trim());
                    commitMessage = commitMessage.replace("{package_id}", sbPackage.toString().trim());
                    logger.debug("Git commit all published items started.");
                    String prologue = studioConfiguration.getProperty(REPO_COMMIT_MESSAGE_PROLOGUE);
                    String postscript = studioConfiguration.getProperty(REPO_COMMIT_MESSAGE_POSTSCRIPT);
                    StringBuilder sbCommitMessage = new StringBuilder();
                    if (StringUtils.isNotEmpty(prologue)) {
                        sbCommitMessage.append(prologue).append("\n\n");
                    }
                    sbCommitMessage.append(commitMessage);
                    if (StringUtils.isNotEmpty(postscript)) {
                        sbCommitMessage.append("\n\n").append(postscript);
                    }
                    CommitCommand commitCommand =
                            git.commit().setMessage(sbCommitMessage.toString()).setAuthor(authorIdent);
                    RevCommit revCommit = retryingRepositoryOperationFacade.call(commitCommand);
                    logger.debug("Git commit all published items completed.");
                    int commitTime = revCommit.getCommitTime();

                    // tag
                    ZonedDateTime tagDate2 = Instant.ofEpochSecond(commitTime).atZone(UTC);
                    ZonedDateTime publishDate = ZonedDateTime.now(UTC);
                    String tagName2 = tagDate2.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmssSSSX")) +
                            "_published_on_" + publishDate.format(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmssSSSX"));
                    logger.debug("Get Author Ident started.");
                    PersonIdent authorIdent2 = helper.getAuthorIdent(user);
                    logger.debug("Get Author Ident completed.");

                    logger.debug("Git tag started.");
                    TagCommand tagCommand =
                            git.tag().setTagger(authorIdent2).setName(tagName2).setMessage(commitMessage);
                    retryingRepositoryOperationFacade.call(tagCommand);
                    logger.debug("Git tag completed.");

                    // checkout environment
                    logger.debug("Checkout environment " + environment + " branch for site " + site);
                    CheckoutCommand checkoutCommand = git.checkout().setName(environment);
                    retryingRepositoryOperationFacade.call(checkoutCommand);

                    Ref branchRef = repo.findRef(inProgressBranchName);

                    // merge in-progress branch
                    logger.debug("Merge in-progress branch into environment " + environment + " for site " +
                            site);
                    MergeCommand mergeCommand = git.merge().setCommit(true).include(branchRef);
                    retryingRepositoryOperationFacade.call(mergeCommand);

                    // clean up
                    logger.debug("Delete in-progress branch (clean up) for site " + site);
                    DeleteBranchCommand deleteBranchCommand =
                            git.branchDelete().setBranchNames(inProgressBranchName).setForce(true);
                    retryingRepositoryOperationFacade.call(deleteBranchCommand);
                    git.close();
                    if (repoCreated) {
                        siteService.setPublishedRepoCreated(site);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error when publishing site " + site + " to environment " + environment, e);
            throw new DeploymentException("Error when publishing site " + site + " to environment " +
                    environment + " [commit ID = " + commitId + "]");
        } finally {
            generalLockService.unlock(gitLockKey);
        }
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

    private String deleteParentFolder(Git git, Path parentFolder, boolean wasPage) throws GitAPIException, IOException {
        String parent = parentFolder.toString();
        String toRet = parent;
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            String folderToDelete = helper.getGitPath(parent);
            Path toDelete = Paths.get(git.getRepository().getDirectory().getParent(), parent);
            if (Files.exists(toDelete)) {
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
                            RmCommand rmCommand= git.rm()
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
        } catch (CryptoException e) {
            logger.error("Error deleting parent folder " + parentFolder.toString(), e);
        }
        return toRet;
    }

    @Override
    public boolean repositoryExists(String site) {
        return commitIdExists(site, HEAD);
    }

    @Override
    public boolean commitIdExists(String site, String commitId) {
        return commitIdExists(site, SANDBOX, commitId);
    }

    @Override
    public boolean commitIdExists(String site, GitRepositories repoType, String commitId) {
        boolean toRet = false;
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            try (Repository repo = helper.getRepository(site, repoType)) {
                if (repo != null) {
                    ObjectId objCommitId = repo.resolve(commitId);
                    if (objCommitId != null) {
                        RevCommit revCommit = repo.parseCommit(objCommitId);
                        if (revCommit != null) {
                            toRet = true;
                        }
                    }
                }
            }
        } catch (CryptoException | IOException e) {
            logger.info("Commit ID " + commitId + " does not exist in " + repoType + " for site " + site);
        }
        return toRet;
    }

    @Override
    public boolean createSiteCloneRemote(String siteId, String sandboxBranch, String remoteName, String remoteUrl,
                                         String remoteBranch, boolean singleBranch, String authenticationType,
                                         String remoteUsername, String remotePassword, String remoteToken,
                                         String remotePrivateKey, Map<String, String> params, boolean createAsOrphan)
            throws InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
            RemoteRepositoryNotFoundException, ServiceLayerException {
        boolean toReturn = false;

        // clone remote git repository for site content
        logger.debug("Creating site " + siteId + " as a clone of remote repository " + remoteName +
                " (" + remoteUrl + ").");
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        generalLockService.lock(gitLockKey);
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            toReturn = helper.createSiteCloneRemoteGitRepo(siteId, sandboxBranch, remoteName, remoteUrl, remoteBranch,
                    singleBranch, authenticationType, remoteUsername, remotePassword, remoteToken, remotePrivateKey,
                    createAsOrphan);

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
                // update site name variable inside config files
                logger.debug("Update site name configuration variables for site " + siteId);
                toReturn = helper.updateSiteNameConfigVar(siteId);

                if (toReturn) {
                    toReturn = helper.replaceParameters(siteId, params);
                }

                if (toReturn) {
                    // commit everything so it is visible
                    logger.debug("Perform initial commit for site " + siteId);
                    toReturn = helper.performInitialCommit(siteId,
                            helper.getCommitMessage(REPO_INITIAL_COMMIT_COMMIT_MESSAGE), sandboxBranch);
                }
            } else {
                logger.error("Error while creating site " + siteId + " by cloning remote repository " + remoteName +
                        " (" + remoteUrl + ").");
            }
        } catch (CryptoException e) {
            logger.error("Error while creating site " + siteId + " by cloning remote repository " + remoteName +
                    " (" + remoteUrl + ").", e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
        return toReturn;
    }

    @RetryingOperation
    @Override
    public boolean removeRemote(String siteId, String remoteName) {
        logger.debug("Remove remote " + remoteName + " from the sandbox repo for the site " + siteId);
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repo = helper.getRepository(siteId, SANDBOX);
            try (Git git = new Git(repo)) {
                RemoteRemoveCommand remoteRemoveCommand = git.remoteRemove();
                remoteRemoveCommand.setRemoteName(remoteName);
                retryingRepositoryOperationFacade.call(remoteRemoveCommand);

                List<Ref> resultRemoteBranches = git.branchList()
                        .setListMode(ListBranchCommand.ListMode.REMOTE)
                        .call();

                List<String> branchesToDelete = new ArrayList<String>();
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

            } catch (GitAPIException e) {
                logger.error("Failed to remove remote " + remoteName + " for site " + siteId, e);
                return false;
            }

            logger.debug("Remove remote record from database for remote " + remoteName + " and site " + siteId);
            Map<String, String> params = new HashMap<String, String>();
            params.put("siteId", siteId);
            params.put("remoteName", remoteName);
            remoteRepositoryDAO.deleteRemoteRepository(params);

            return true;
        } catch (CryptoException e) {
            logger.error("Failed to remove remote " + remoteName + " for site " + siteId, e);
            return false;
        }
    }

    private void insertRemoteToDb(String siteId, String remoteName, String remoteUrl,
                                  String authenticationType, String remoteUsername, String remotePassword,
                                  String remoteToken, String remotePrivateKey) throws CryptoException {
        logger.debug("Inserting remote " + remoteName + " for site " + siteId + " into database.");
        Map<String, String> params = new HashMap<String, String>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteName);
        params.put("remoteUrl", remoteUrl);
        params.put("authenticationType", authenticationType);
        params.put("remoteUsername", remoteUsername);

        if (StringUtils.isNotEmpty(remotePassword)) {
            logger.debug("Encrypt password before inserting to database");
            String hashedPassword = encryptor.encrypt(remotePassword);
            params.put("remotePassword", hashedPassword);
        } else {
            params.put("remotePassword", remotePassword);
        }
        if (StringUtils.isNotEmpty(remoteToken)) {
            logger.debug("Encrypt token before inserting to database");
            String hashedToken = encryptor.encrypt(remoteToken);
            params.put("remoteToken", hashedToken);
        } else {
            params.put("remoteToken", remoteToken);
        }
        if (StringUtils.isNotEmpty(remotePrivateKey)) {
            logger.debug("Encrypt private key before inserting to database");
            String hashedPrivateKey = encryptor.encrypt(remotePrivateKey);
            params.put("remotePrivateKey", hashedPrivateKey);
        } else {
            params.put("remotePrivateKey", remotePrivateKey);
        }

        logger.debug("Insert site remote record into database");
        remoteRepositoryDAO.insertRemoteRepository(params);

        params = new HashMap<String, String>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteName);
        RemoteRepository remoteRepository = remoteRepositoryDAO.getRemoteRepository(params);
        if (remoteRepository != null) {
            insertClusterRemoteRepository(remoteRepository);
        }
    }

    @RetryingOperation
    public void insertClusterRemoteRepository(RemoteRepository remoteRepository) {
        HierarchicalConfiguration<ImmutableNode> registrationData =
                studioConfiguration.getSubConfig(CLUSTERING_NODE_REGISTRATION);
        if (registrationData != null && !registrationData.isEmpty()) {
            String localAddress = registrationData.getString(CLUSTER_MEMBER_LOCAL_ADDRESS);
            ClusterMember member = clusterDao.getMemberByLocalAddress(localAddress);
            if (member != null) {
                clusterDao.addClusterRemoteRepository(member.getId(), remoteRepository.getId());
            }
        }
    }
    @Override
    public boolean contentExists(String site, String path) {
        boolean toReturn = false;
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
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
                        if (StringUtils.isEmpty(gitPath) || gitPath.equals(".")) {
                            toReturn = true;
                        }
                    }
                } catch (IOException e) {
                    logger.info("Content not found for site: " + site + " path: " + path, e);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to create RevTree for site: " + site + " path: " + path, e);
        }
        return toReturn;
    }

    @Override
    public String getRepoLastCommitId(final String site) {
        String toReturn = EMPTY;
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repository =
                    helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
            if (repository != null) {
                synchronized (repository) {
                    Repository repo = helper.getRepository(site, SANDBOX);
                    ObjectId commitId = repo.resolve(HEAD);
                    if (commitId != null) {
                        toReturn = commitId.getName();
                    }
                }
            }
        } catch (IOException | CryptoException e) {
            logger.error("Error getting last commit ID for site " + site, e);
        }
        return toReturn;
    }

    @Override
    public String getLastEditCommitId(String siteId, String path) {
        String toReturn = EMPTY;
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repository =
                    helper.getRepository(siteId, StringUtils.isEmpty(siteId) ? GLOBAL : SANDBOX);
            if (repository != null) {
                synchronized (repository) {
                    ObjectId head = repository.resolve(HEAD);
                    String gitPath = helper.getGitPath(path);
                    try (Git git = new Git(repository)) {
                        Iterable<RevCommit> commits = git.log().add(head).addPath(gitPath).call();
                        Iterator<RevCommit> iterator = commits.iterator();
                        if (iterator.hasNext()) {
                            RevCommit revCommit = iterator.next();
                            toReturn = revCommit.getName();
                        }
                    } catch (IOException | GitAPIException e) {
                        logger.error("error while getting history for content item " + path);
                    }
                }
            }
        } catch (IOException | CryptoException e) {
            logger.error("Error getting last commit ID for site " + siteId + " path " + path, e);
        }
        return toReturn;
    }

    @Override
    public Map<String, String> getChangeSetPathsFromDelta(String site, String commitIdFrom, String commitIdTo) {
        Map<String, String> changeSet = new TreeMap<>();
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repository =
                    helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
            if (repository != null) {
                synchronized (repository) {
                    try {
                        // Get the sandbox repo, and then get a reference to the commitId we received and another for head
                        boolean fromEmptyRepo = StringUtils.isEmpty(commitIdFrom);
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
                                List<DiffEntry> diffEntries = git.diff()
                                        .setOldTree(firstCommitTreeParser)
                                        .setNewTree(null)
                                        .call();


                                // Now that we have a diff, let's itemize the file changes, pack them into a TO
                                // and add them to the list of RepoOperations to return to the caller
                                // also include date/time of commit by taking number of seconds and multiply by 1000 and
                                // convert to java date before sending over
                                changeSet = getChangeSetFromDiff(diffEntries);
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
                                        List<DiffEntry> diffEntries = git.diff()
                                                .setOldTree(fromCommitTreeParser)
                                                .setNewTree(toCommitTreeParser)
                                                .call();


                                        // Now that we have a diff, let's itemize the file changes, pack them into a TO
                                        // and add them to the list of RepoOperations to return to the caller
                                        // also include date/time of commit by taking number of seconds and multiply by 1000 and
                                        // convert to java date before sending over
                                        changeSet = getChangeSetFromDiff(diffEntries);
                                    }
                                }



                            }
                        } catch (GitAPIException e) {
                            logger.error("Error getting operations for site " + site + " from commit ID: " + commitIdFrom
                                    + " to commit ID: " + commitIdTo, e);
                        }
                    } catch (IOException e) {
                        logger.error("Error getting operations for site " + site + " from commit ID: " + commitIdFrom +
                                " to commit ID: " + commitIdTo, e);
                    }
                }
            }
        } catch (CryptoException e) {
            logger.error("Error getting operations for site " + site + " from commit ID: " + commitIdFrom +
                    " to commit ID: " + commitIdTo, e);
        }

        return changeSet;
    }

    private Map<String, String> getChangeSetFromDiff(List<DiffEntry> diffEntries) {
        Map<String, String> toReturn = new TreeMap<>();

        for (DiffEntry diffEntry : diffEntries) {

            // Update the paths to have a preceding separator
            String pathNew = FILE_SEPARATOR + diffEntry.getNewPath();
            String pathOld = FILE_SEPARATOR + diffEntry.getOldPath();

            if (ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(pathNew)) ||
                    ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(pathOld))) {
                continue;
            }

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
                    logger.error("Error: Unknown git operation " + diffEntry.getChangeType());
                    break;
            }
        }
        return toReturn;
    }

    @Override
    public void markGitLogAudited(String siteId, String commitId) {
        retryingOperationFacade.markGitLogAudited(siteId, commitId, 1);
    }

    @Override
    public void updateGitlog(String siteId, String lastProcessedCommitId, int batchSize) throws SiteNotFoundException {
        RingBuffer<RevCommit> commitIds = new RingBuffer<RevCommit>(batchSize);
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repository =
                    helper.getRepository(siteId, StringUtils.isEmpty(siteId) ? GLOBAL : SANDBOX);
            if (repository != null) {
                try {
                    ObjectId objCommitIdFrom = repository.resolve(lastProcessedCommitId);
                    ObjectId objCommitIdTo = repository.resolve(HEAD);

                    logger.debug("Update git log from " + objCommitIdFrom.getName() + " to " + objCommitIdTo.getName());
                    try (Git git = new Git(repository)) {

                        // If the commitIdFrom is the same as commitIdTo, there is nothing to calculate, otherwise,
                        // let's do it
                        if (!objCommitIdFrom.equals(objCommitIdTo)) {

                            // Get the log of all the commits between commitId and head
                            Iterable<RevCommit> commits = git.log().call();
                            ObjectId nextCommitId;
                            String commitId = EMPTY;

                            Iterator<RevCommit> iterator = commits.iterator();
                            while (iterator.hasNext()) {
                                RevCommit commit = iterator.next();
                                if (StringUtils.equals(commit.getId().getName(), lastProcessedCommitId)) {
                                    break;
                                }
                                commitIds.write(commit);
                            }

                            List<String> batch = new ArrayList<String>();
                            RevCommit current = commitIds.read();
                            while (current != null) {
                                nextCommitId = current.getId();
                                commitId = nextCommitId.getName();
                                if (StringUtils.contains(current.getFullMessage(),
                                        studioConfiguration.getProperty(REPO_SYNC_DB_COMMIT_MESSAGE_NO_PROCESSING))) {
                                    logger.debug("Skipping commitId: " + commitId + " for site "
                                            + siteId + " because it is marked not to be processed.");

                                } else {
                                    batch.add(0, commitId);
                                }
                                current = commitIds.read();
                            }
                            if (batch.size() > 0) {
                                gitLogDao.insertIgnoreGitLogList(siteId, batch);
                                siteService.updateLastSyncedGitlogCommitId(siteId, batch.get(batch.size() - 1));
                                logger.debug("Inserted " + batch.size() + " git log commits for site " + siteId);
                            } else {
                                siteService.updateLastSyncedGitlogCommitId(siteId, objCommitIdTo.getName());
                            }
                        }
                    } catch (GitAPIException e) {
                        logger.error("Error getting commit ids for site " + siteId + " from commit ID: " +
                                lastProcessedCommitId + " to HEAD", e);
                    }
                } catch (IOException e) {
                    logger.error("Error getting commit ids for site " + siteId + " from commit ID: " +
                            lastProcessedCommitId + " to HEAD", e);
                }

            }
        } catch (CryptoException e) {
            logger.error("Error getting commit ids for site " + siteId + " from commit ID: " +
                    lastProcessedCommitId + " to HEAD", e);
        }
    }

    @Override
    public List<GitLog> getUnauditedCommits(String siteId, int batchSize) {
        return gitLogDao.getUnauditedCommits(siteId, batchSize);
    }

    @Override
    public List<GitLog> getUnprocessedCommits(String siteId, long marker) {
        return gitLogDao.getUnprocessedCommitsSinceMarker(siteId, marker);
    }

    @Override
    public int countUnprocessedCommits(String siteId, long marker) {
        return gitLogDao.countUnprocessedCommitsSinceMarker(siteId, marker);
    }

    @Override
    public void markGitLogProcessedBeforeMarker(String siteId, long marker, int processed) {
        retryingOperationFacade.markGitLogProcessedBeforeMarker(siteId, marker, processed, 0);
    }

    @Override
    public String getPreviousCommitId(String siteId, String commitId) {
        String toReturn = EMPTY;
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                    userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
            Repository repository =
                    helper.getRepository(siteId, StringUtils.isEmpty(siteId) ? GLOBAL : SANDBOX);
            if (repository != null) {
                ObjectId head = repository.resolve(HEAD);
                try (Git git = new Git(repository)) {
                    Iterable<RevCommit> commits = git.log().add(head).call();
                    Iterator<RevCommit> iterator = commits.iterator();
                    boolean found = false;
                    while (!found || iterator.hasNext()) {
                        RevCommit revCommit = iterator.next();
                        if (StringUtils.equals(commitId, revCommit.getName())) {
                            found = true;
                            if (iterator.hasNext()) {
                                revCommit = iterator.next();
                                toReturn = revCommit.getName();
                            }
                        }
                    }
                } catch (IOException | GitAPIException e) {
                    logger.error("Error while getting previous commit ID for " + commitId);
                }
            }
        } catch (IOException | CryptoException e) {
            logger.error("Error while getting previous commit ID for site " + siteId + " commit ID " + commitId, e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Previous commit id for site " + siteId + " and commit id " + commitId + " is " + toReturn);
        }
        return toReturn;
    }

    @Override
    public void upsertGitLogList(String siteId, List<String> commitIds, boolean processed, boolean audited) {
        gitLogDao.upsertGitLogList(siteId, commitIds, processed ? 1 : 0, audited ? 1 : 0);
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public GitLogDAO getGitLogDao() {
        return gitLogDao;
    }

    public void setGitLogDao(GitLogDAO gitLogDao) {
        this.gitLogDao = gitLogDao;
    }

    public SiteFeedMapper getSiteFeedMapper() {
        return siteFeedMapper;
    }

    public void setSiteFeedMapper(SiteFeedMapper siteFeedMapper) {
        this.siteFeedMapper = siteFeedMapper;
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public RemoteRepositoryDAO getRemoteRepositoryDAO() {
        return remoteRepositoryDAO;
    }

    public void setRemoteRepositoryDAO(RemoteRepositoryDAO remoteRepositoryDAO) {
        this.remoteRepositoryDAO = remoteRepositoryDAO;
    }

    public void setEncryptor(TextEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    public ClusterDAO getClusterDao() {
        return clusterDao;
    }

    public void setClusterDao(ClusterDAO clusterDao) {
        this.clusterDao = clusterDao;
    }

    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public ObjectMetadataManager getObjectMetadataManager() {
        return objectMetadataManager;
    }

    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) {
        this.objectMetadataManager = objectMetadataManager;
    }

    public StudioUtils getStudioUtils() {
        return studioUtils;
    }

    public void setStudioUtils(StudioUtils studioUtils) {
        this.studioUtils = studioUtils;
    }

    public RetryingOperationFacade getRetryingOperationFacade() {
        return retryingOperationFacade;
    }

    public void setRetryingOperationFacade(RetryingOperationFacade retryingOperationFacade) {
        this.retryingOperationFacade = retryingOperationFacade;
    }

    public RetryingRepositoryOperationFacade getRetryingRepositoryOperationFacade() {
        return retryingRepositoryOperationFacade;
    }

    public void setRetryingRepositoryOperationFacade(RetryingRepositoryOperationFacade retryingRepositoryOperationFacade) {
        this.retryingRepositoryOperationFacade = retryingRepositoryOperationFacade;
    }
}
