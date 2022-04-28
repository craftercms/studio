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

package org.craftercms.studio.impl.v2.repository;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.iterators.ReverseListIterator;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Item;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.dal.PublishRequest;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;
import org.craftercms.studio.api.v2.core.ContextManager;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.dal.GitLog;
import org.craftercms.studio.api.v2.dal.GitLogDAO;
import org.craftercms.studio.api.v2.dal.PublishRequestDAO;
import org.craftercms.studio.api.v2.dal.PublishingHistoryItem;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.dal.RemoteRepositoryDAO;
import org.craftercms.studio.api.v2.dal.RepoOperation;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.publish.internal.PublishingProgressServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.craftercms.studio.impl.v2.utils.RingBuffer;
import org.craftercms.studio.impl.v2.utils.StudioUtils;
import org.craftercms.studio.model.rest.content.DetailedItem;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.DeleteBranchCommand;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.internal.storage.file.LockFile;
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
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.springframework.core.io.Resource;
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
import java.util.Optional;
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
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_INITIAL_PUBLISH_COMMIT_MESSAGE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_PUBLISHED_COMMIT_MESSAGE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_SANDBOX_BRANCH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_SYNC_DB_COMMIT_MESSAGE_NO_PROCESSING;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.EMPTY_FILE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_REPO_USER_USERNAME;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;
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

public class GitContentRepository implements ContentRepository {

    private static final Logger logger = LoggerFactory.getLogger(GitContentRepository.class);

    private GitRepositoryHelper helper;
    private StudioConfiguration studioConfiguration;
    private GitLogDAO gitLogDao;
    private SiteFeedMapper siteFeedMapper;
    private UserServiceInternal userServiceInternal;
    private SecurityService securityService;
    private RemoteRepositoryDAO remoteRepositoryDAO;
    private TextEncryptor encryptor;
    private ContextManager contextManager;
    private ContentStoreService contentStoreService;
    private ClusterDAO clusterDao;
    private GeneralLockService generalLockService;
    private SiteService siteService;
    private PublishRequestDAO publishRequestDao;
    private ItemServiceInternal itemServiceInternal;
    private StudioUtils studioUtils;
    private RetryingRepositoryOperationFacade retryingRepositoryOperationFacade;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;
    private PublishingProgressServiceInternal publishingProgressServiceInternal;
    private ServicesConfig servicesConfig;

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
                        logger.debug("Object is not tree for site '{}' path '{}' " +
                                "- it does not have children", site, path);
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

                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error while getting children for site '{}' path '{}'", site, path, e);
        }
        return retItems;
    }

    @Override
    public List<RepoOperation> getOperations(String site, String commitIdFrom, String commitIdTo) {
        List<RepoOperation> operations = new ArrayList<>();
        Repository repository = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
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
                                DiffCommand diffCommand = git.diff()
                                        .setOldTree(firstCommitTreeParser)
                                        .setNewTree(null);
                                List<DiffEntry> diffEntries = retryingRepositoryOperationFacade.call(diffCommand);
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Git diff from '{}' to null finished in '{}' seconds",
                                            objFirstCommitId.getName(),
                                            ((System.currentTimeMillis() - startDiffMark1) / 1000));
                                    logger.debug("Number of diff entries is '{}'", diffEntries.size());
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
                        LogCommand logCommand = git.log().addRange(objCommitIdFrom, objCommitIdTo);
                        Iterable<RevCommit> commits = retryingRepositoryOperationFacade.call(logCommand);

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
                                logger.debug("Skipping commitId '{}' for site '{}' because it's marked " +
                                        "not to be processed.", prevCommitId.getName(),
                                        site);
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
                                        DiffCommand diffCommand = git.diff()
                                                .setOldTree(prevCommitTreeParser)
                                                .setNewTree(nextCommitTreeParser);
                                        List<DiffEntry> diffEntries = retryingRepositoryOperationFacade.call(diffCommand);
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
                                        operations.addAll(processDiffEntry(git, diffEntries, nextCommitId));
                                        prevCommitId = nextCommitId;
                                    }
                                }
                            }
                        }

                    }
                }
            } catch (IOException | GitAPIException e) {
                logger.error("Error getting operations for site '{}' from commit ID '{}' to commit ID '{}'",
                        site, commitIdFrom, commitIdTo, e);
            }
        }

        return operations;
    }

    @Override
    public List<RepoOperation> getOperationsFromDelta(String site, String commitIdFrom, String commitIdTo) {
        List<RepoOperation> operations = new ArrayList<>();
        Repository repository = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
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

                                    if (CollectionUtils.isEmpty(diffEntries)) {
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
                logger.error("Error getting operations for site '{}' from commit ID '{}' to commit ID '{}'",
                        site, commitIdFrom, commitIdTo, e);
            }
        }

        return operations;
    }

    @Override
    public String getRepoFirstCommitId(final String site) {
        String toReturn = EMPTY;
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
                            logger.debug("getRepoFirstCommitId for site: '{}', first commit ID is '{}'",
                                    site, toReturn);
                        }
                    } catch (IOException e) {
                        logger.error("Error getting first commit ID for site '{}'", site, e);
                    }
                }
            }
        }

        return toReturn;
    }

    private List<RepoOperation> processDiffEntry(Git git, List<DiffEntry> diffEntries, ObjectId commitId)
            throws GitAPIException, IOException {
        int size = diffEntries.size();
        logger.debug("Processing '{}' diff entries", size);
        long startMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0;
        List<RepoOperation> toReturn = new ArrayList<RepoOperation>();

        int idx = 0;
        for (DiffEntry diffEntry : diffEntries) {
            ++idx;
            long startProcessEntryMark = logger.isDebugEnabled() ? System.currentTimeMillis() : 0;
            // Update the paths to have a preceding separator
            String pathNew = FILE_SEPARATOR + diffEntry.getNewPath();
            String pathOld = FILE_SEPARATOR + diffEntry.getOldPath();

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
                    logger.error("Unknown git operation '{}'", diffEntry.getChangeType());
                    break;
            }
            if (repoOperation != null) {
                repoOperation.setAuthor(StringUtils.isEmpty(author) ? "N/A" : author);
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
        retryingDatabaseOperationFacade.markGitLogProcessed(params);
    }

    @Override
    public void markGitLogVerifiedProcessedBulk(String siteId, List<String> commitIds) {
        if (CollectionUtils.isNotEmpty(commitIds)) {
            int batchSize = studioUtils.getBulkOperationsBatchSize();
            List<List<String>> partitions = new ArrayList<List<String>>();

            for (int i = 0; i < commitIds.size(); i = i + (batchSize)) {
                partitions.add(commitIds.subList(i, Math.min(i + batchSize, commitIds.size())));
            }

            for (List<String> part : partitions) {
                retryingDatabaseOperationFacade.markGitLogProcessedBulk(siteId, part);
            }
        }
    }

    @Override
    public void insertGitLog(String siteId, String commitId, int processed) {
        String lockKey = "GitLogLock:" + siteId;
        generalLockService.lock(lockKey);
        try {
            insertGitLog(siteId, commitId, processed, 0);
        } finally {
            generalLockService.unlock(lockKey);
        }
    }

    @Override
    public void insertGitLog(String siteId, String commitId, int processed, int audited) {
        String lockKey = "GitLogLock";
        generalLockService.lock(lockKey);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", siteId);
        params.put("commitId", commitId);
        params.put("processed", processed);
        params.put("audited", audited);
        try {
            retryingDatabaseOperationFacade.insertGitLog(params);
        } catch (DuplicateKeyException e) {
            logger.debug("Failed to insert commit id '{}' for site '{}' into" +
                    " the gitlog table, because it's a duplicate entry. Marking it as unprocessed so it can be" +
                    " processed by the sync database task.", commitId, siteId);
            params = new HashMap<String, Object>();
            params.put("siteId", siteId);
            params.put("commitId", commitId);
            params.put("processed", 0);
            retryingDatabaseOperationFacade.markGitLogProcessed(params);
        } finally {
            generalLockService.unlock(lockKey);
        }
    }

    private void updateLastVerifiedGitlogCommitId(String site, String commitId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", site);
        params.put("commitId", commitId);
        retryingDatabaseOperationFacade.updateSiteLastVerifiedGitlogCommitId(params);
    }

    @Override
    public List<PublishingHistoryItem> getPublishingHistory(String siteId, String environment, String pathRegex,
                                                            String publisher, ZonedDateTime fromDate,
                                                            ZonedDateTime toDate, int limit) {
        List<PublishingHistoryItem> toRet = new ArrayList<PublishingHistoryItem>();
        Repository publishedRepo = helper.getRepository(siteId, PUBLISHED);
        if (publishedRepo != null) {
            int counter = 0;
            try (Git git = new Git(publishedRepo)) {
                // List all environments
                ListBranchCommand listBranchCommand = git.branchList();
                List<Ref> environments = retryingRepositoryOperationFacade.call(listBranchCommand);
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
                            filters.add(CommitTimeRevFilter.before(Instant.now().toEpochMilli()));
                        }
                        filters.add(NotRevFilter.create(MessageRevFilter.create("Initial commit.")));
                        if (StringUtils.isNotEmpty(publisher)) {
                            User user = userServiceInternal.getUserByIdOrUsername(-1, publisher);
                            filters.add(AuthorRevFilter.create(helper.getAuthorIdent(user).getName()));
                        }
                        LogCommand logCommand = git.log()
                                .add(env.getObjectId())
                                .setRevFilter(AndRevFilter.create(filters));
                        Iterable<RevCommit> branchLog = retryingRepositoryOperationFacade.call(logCommand);

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
            } catch (IOException | GitAPIException | UserNotFoundException | ServiceLayerException e) {
                logger.error("Error while getting deployment history for site '{}'", siteId, e);
            }
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
                        sandboxBranch);
            }
        } finally {
            generalLockService.unlock(gitLockKey);
        }

        return toReturn;
    }

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
                    logger.debug("Fetch from sandbox for site '{}'", site);
                    FetchCommand fetchCommand = git.fetch();
                    retryingRepositoryOperationFacade.call(fetchCommand);

                    // checkout master and pull from sandbox
                    logger.debug("Checkout published/master branch for site '{}'", site);
                    try {
                        // First delete it in case it already exists (ignored if does not exist)
                        String currentBranch = repo.getBranch();
                        if (currentBranch.endsWith(IN_PROGRESS_BRANCH_NAME_SUFFIX)) {
                            logger.info("Published repo for site '{}' has a failed in-progress branch " +
                                    "(the temporary branch used during the publishing process), resetting", site);
                            ResetCommand resetCommand = git.reset().setMode(HARD);
                            retryingRepositoryOperationFacade.call(resetCommand);
                        }

                        Ref ref = repo.exactRef(R_HEADS + sandboxBranchName);
                        boolean createBranch = (ref == null);

                        CheckoutCommand checkoutCommand = git.checkout()
                                .setName(sandboxBranchName)
                                .setCreateBranch(createBranch);
                        retryingRepositoryOperationFacade.call(checkoutCommand);

                        // TODO: Clean up this code, much of this work is done even if not needed
                        logger.debug("Delete 'in-progress' branch, in case it was not cleaned up for site '{}'", site);
                        DeleteBranchCommand deleteBranchCommand =
                                git.branchDelete().setBranchNames(inProgressBranchName).setForce(true);
                        retryingRepositoryOperationFacade.call(deleteBranchCommand);

                        PullCommand pullCommand = git.pull().
                                setRemote(DEFAULT_REMOTE_NAME)
                                .setRemoteBranchName(sandboxBranchName)
                                .setStrategy(THEIRS);
                        retryingRepositoryOperationFacade.call(pullCommand);
                    } catch (RefNotFoundException e) {
                        logger.error("Failed to checkout published/master and to pull content from sandbox " +
                                "for site '{}'", site, e);
                        throw new DeploymentException("Failed to checkout published master and to pull content from " +
                                "sandbox for site " + site);
                    }

                    // checkout environment branch
                    logger.debug("Checkout environment branch '{}' for site '{}'", environment, site);
                    try {
                        CheckoutCommand checkoutCommand = git.checkout()
                                .setName(environment);
                        retryingRepositoryOperationFacade.call(checkoutCommand);
                    } catch (RefNotFoundException e) {
                        logger.info("Unable to find the branch '{}' for site '{}'. Creating a new branch.",
                                environment, site);
                        // create new publishing target (environment) branch
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
                        logger.debug("Create in-progress branch for site '{}'", site);
                        CheckoutCommand checkoutCommand = git.checkout()
                                .setCreateBranch(true)
                                .setForceRefUpdate(true)
                                .setStartPoint(environment)
                                .setUpstreamMode(TRACK)
                                .setName(inProgressBranchName);
                        retryingRepositoryOperationFacade.call(checkoutCommand);
                    } catch (GitAPIException e) {
                        // TODO: DB: Error ?
                        logger.error("Failed to create in-progress published branch for site '{}'", site);
                    }

                    Set<String> deployedCommits = new HashSet<String>();
                    Set<String> deployedPackages = new HashSet<String>();
                    logger.debug("Checkout deployed files started for site '{}'", site);
                    AddCommand addCommand = git.add();
                    String currentPackageId = deploymentItems.get(0).getPackageId();
                    // TODO: SJ: Review the following code and refactor for better performance
                    // TODO: The logic should be something like:
                    // TODO: If item doesn't exist in git, the repo was reset and the commit ID doesn't mean anything
                    // TODO:   skip this file
                    // TODO: If the commit ID is null, use HEAD
                    // TODO: Publish the file
                    for (DeploymentItemTO deploymentItem : deploymentItems) {
                        commitId = deploymentItem.getCommitId();
                        path = helper.getGitPath(deploymentItem.getPath());
                        // If the commit ID is null OR the commit ID doesn't exist in the published repo
                        if (Objects.isNull(commitId) || !commitIdExists(site, PUBLISHED, commitId)) {
                            // If the content exists in the sandbox repository
                            // TODO: The contentExists call is expensive, review
                            // TODO: Why check the sandbox and not published/master from which we would be publishing?
                            if (contentExists(site, path)) {
                                // If the commit ID is null, then warning 1, else warning 2
                                if (Objects.isNull(commitId)) {
                                    logger.warn("Commit ID is null for path '{}' site '{}'. This git repository " +
                                            "may have been reset externally at some point.", path, site);
                                } else {
                                    logger.warn("Commit ID '{}' in the database doesn't exist for " +
                                            "path '{}' site '{}' in the git repository. " +
                                            "This git repository may have been reset at some point.",
                                            commitId, path, site);
                                }
                                // Log that we're publishing from HEAD
                                logger.info("The commit ID for path '{}' site '{}' is null, using HEAD instead",
                                        path, site);
                                // Set the commit ID to head
                                commitId = getRepoLastCommitId(site);
                            } else {
                                // The content doesn't exist in the sandbox, skip publishing it
                                logger.warn("Path '{}' in site '{}' doesn't exist in git, skipping " +
                                        "the publishing of this item.", path, site);
                                continue;
                            }
                        }
                        // The commit ID is not null and the content exists in the published repository OR
                        // The commit ID was null and it was set to HEAD to avoid the null issue
                        logger.debug("Publishing to the temporary branch path '{}' site '{}' commit ID '{}'",
                                path, site, commitId);

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
                        itemServiceInternal.updateLastPublishedOn(site, deploymentItem.getPath(),
                                DateUtils.getCurrentTime());

                        if (!StringUtils.equals(currentPackageId, deploymentItem.getPackageId())) {
                            currentPackageId = deploymentItem.getPackageId();
                            publishingProgressServiceInternal.updateObserver(site, currentPackageId);
                        } else {
                            publishingProgressServiceInternal.updateObserver(site);
                        }
                    } // end of for loop

                    // All deployable files are now checked out in the temporary in-progress publishing branch
                    logger.debug("Checkout deployed files completed for site '{}'", site);

                    // commit all deployed files
                    String commitMessage = studioConfiguration.getProperty(REPO_PUBLISHED_COMMIT_MESSAGE);

                    User user = userServiceInternal.getUserByIdOrUsername(-1, author);
                    PersonIdent authorIdent = helper.getAuthorIdent(user);

                    logger.debug("Git add all published items started for site '{}'", site);
                    retryingRepositoryOperationFacade.call(addCommand);
                    logger.debug("Git add all published items completed for site '{}'", site);

                    commitMessage = commitMessage.replace("{username}", author);
                    commitMessage =
                            commitMessage.replace("{datetime}",
                                    DateUtils.getCurrentTime().format(
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

                    logger.debug("Git commit all published items for site '{}' started", site);
                    CommitCommand commitCommand =
                            git.commit().setMessage(sbCommitMessage.toString()).setAuthor(authorIdent);
                    RevCommit revCommit = retryingRepositoryOperationFacade.call(commitCommand);
                    logger.debug("Git commit all published items for site '{}' completed", site);

                    int commitTime = revCommit.getCommitTime();

                    // tag
                    ZonedDateTime tagDate2 = Instant.ofEpochSecond(commitTime).atZone(UTC);
                    String publishDate = DateUtils.formatCurrentTime("yyyy-MM-dd'T'HHmmssSSSX");
                    String tagName2 = DateUtils.formatDate(tagDate2, "yyyy-MM-dd'T'HHmmssSSSX") +
                            "_published_on_" + publishDate;
                    PersonIdent authorIdent2 = helper.getAuthorIdent(user);

                    logger.debug("Git tag started for site '{}'", site);
                    TagCommand tagCommand =
                            git.tag().setTagger(authorIdent2).setName(tagName2).setMessage(commitMessage);
                    retryingRepositoryOperationFacade.call(tagCommand);
                    logger.debug("Git tag completed for site '{}'", site);

                    // Checkout the publishing target branch
                    logger.debug("Checkout publishing target branch '{}' for site '{}'", environment, site);
                    CheckoutCommand checkoutCommand = git.checkout()
                            .setName(environment);
                    retryingRepositoryOperationFacade.call(checkoutCommand);

                    Ref branchRef = repo.findRef(inProgressBranchName);

                    // merge in-progress branch
                    logger.debug("Merge the in-progress branch into the target branch '{}' for site '{}'",
                            environment, site);
                    MergeCommand mergeCommand = git.merge().setCommit(true).include(branchRef);
                    retryingRepositoryOperationFacade.call(mergeCommand);

                    // clean up
                    logger.debug("Delete the in-progress branch (clean up) for site '{}'", site);
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
            logger.error("Error publishing site '{}' to publishing target '{}' commit ID is '{}'",
                    site, environment, commitId, e);
            throw new DeploymentException(String.format("Error publishing site '%s' to publishing target " +
                            "'%s' commit ID is '%s'", site, environment, commitId));
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
                                         String remotePrivateKey, Map<String, String> params, boolean createAsOrphan)
            throws InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
            RemoteRepositoryNotFoundException, ServiceLayerException {
        boolean toReturn = false;

        // Clone the remote git repository
        logger.debug("Creating site '{}' as a clone of remote repository '{} ({})'", siteId, remoteName, remoteUrl);
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        generalLockService.lock(gitLockKey);
        try {
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
                            helper.getCommitMessage(REPO_INITIAL_COMMIT_COMMIT_MESSAGE), sandboxBranch);
                }
            } else {
                logger.error("Error while creating site '{}' by cloning remote repository '{} ({})'",
                        siteId, remoteName, remoteUrl);
            }
        } finally {
            generalLockService.unlock(gitLockKey);
        }
        return toReturn;
    }

    @Override
    public boolean removeRemote(String siteId, String remoteName) {
        logger.debug("Remove remote '{}' from the sandbox repo for the site '{}'", remoteName, siteId);
        Repository repo = helper.getRepository(siteId, SANDBOX);
        try (Git git = new Git(repo)) {
            RemoteRemoveCommand remoteRemoveCommand = git.remoteRemove();
            remoteRemoveCommand.setRemoteName(remoteName);
            retryingRepositoryOperationFacade.call(remoteRemoveCommand);

            ListBranchCommand listBranchCommand = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.REMOTE);
            List<Ref> resultRemoteBranches = retryingRepositoryOperationFacade.call(listBranchCommand);

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
            logger.error("Failed to remove remote '{}' for site '{}'", remoteName, siteId, e);
            return false;
        }

        logger.debug("Remove remote record from database where remote '{}' and site '{}'", remoteName, siteId);
        Map<String, String> params = new HashMap<String, String>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteName);
        retryingDatabaseOperationFacade.deleteRemoteRepository(params);

        return true;
    }

    private void insertRemoteToDb(String siteId, String remoteName, String remoteUrl,
                                  String authenticationType, String remoteUsername, String remotePassword,
                                  String remoteToken, String remotePrivateKey) throws CryptoException {
        logger.debug("Inserting git remote '{}' for site '{}' into the database", remoteName, siteId);
        Map<String, String> params = new HashMap<String, String>();
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
        retryingDatabaseOperationFacade.insertRemoteRepository(params);

        params = new HashMap<String, String>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteName);
        RemoteRepository remoteRepository = remoteRepositoryDAO.getRemoteRepository(params);
        if (remoteRepository != null) {
            insertClusterRemoteRepository(remoteRepository);
        }
    }

    public void insertClusterRemoteRepository(RemoteRepository remoteRepository) {
        HierarchicalConfiguration<ImmutableNode> registrationData =
                studioConfiguration.getSubConfig(CLUSTERING_NODE_REGISTRATION);
        if (registrationData != null && !registrationData.isEmpty()) {
            String localAddress = registrationData.getString(CLUSTER_MEMBER_LOCAL_ADDRESS);
            ClusterMember member = clusterDao.getMemberByLocalAddress(localAddress);
            if (member != null) {
                retryingDatabaseOperationFacade.addClusterRemoteRepository(member.getId(), remoteRepository.getId());
            }
        }
    }

    @Override
    public boolean contentExists(String site, String path) {
        boolean toReturn = false;
        try {
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
        try {
            Repository repository = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
            if (repository != null) {
                // TODO: SJ: Consider using the generalized locking service instead of the synchronized block below
                synchronized (repository) {
                    Repository repo = helper.getRepository(site, SANDBOX);
                    ObjectId commitId = repo.resolve(HEAD);
                    if (commitId != null) {
                        toReturn = commitId.getName();
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error getting the last commit ID for site '{}'", site, e);
        }
        return toReturn;
    }

    @Override
    public Item getItem(String siteId, String path, boolean flatten) {
        var context = contextManager.getContext(siteId);
        return contentStoreService.getItem(context, null, path, null, flatten);
    }

    @Override
    public long getContentSize(final String site, final String path) {
        // TODO: SJ: Reconsider this implementation for blob store backed repos
        try {
            Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
            RevTree tree = helper.getTreeForLastCommit(repo);
            try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree)) {
                if (tw != null && tw.getObjectId(0) != null) {
                    ObjectId id = tw.getObjectId(0);
                    ObjectLoader objectLoader = repo.open(id);
                    return objectLoader.getSize();
                }
            }
        } catch (IOException e) {
            logger.error("Error getting content size path '{}' in site '{}'", path, site, e);
        }
        return -1L;
    }

    @Override
    public String getLastEditCommitId(String siteId, String path) {
        String toReturn = EMPTY;
        try {
            Repository repository = helper.getRepository(siteId, StringUtils.isEmpty(siteId) ? GLOBAL : SANDBOX);
            if (repository != null) {
                // TODO: SJ: Reconsider the locking mechanism here and see if using the general locking service
                // TODO: SJ: is better since that locks everything else out for this repo
                synchronized (repository) {
                    ObjectId head = repository.resolve(HEAD);
                    String gitPath = helper.getGitPath(path);
                    try (Git git = new Git(repository)) {
                        LogCommand logCommand =  git.log().add(head).addPath(gitPath);
                        Iterable<RevCommit> commits = retryingRepositoryOperationFacade.call(logCommand);
                        Iterator<RevCommit> iterator = commits.iterator();
                        if (iterator.hasNext()) {
                            RevCommit revCommit = iterator.next();
                            toReturn = revCommit.getName();
                        }
                    } catch (IOException | GitAPIException e) {
                        logger.error("Error getting history for content item site '{}' path '{}'", siteId, path);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error getting last commit ID for site '{}' path '{}'", siteId, path, e);
        }
        return toReturn;
    }

    @Override
    public Map<String, String> getChangeSetPathsFromDelta(String site, String commitIdFrom, String commitIdTo) {
        Map<String, String> changeSet = new TreeMap<>();
        Repository repository = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
        if (repository != null) {
            // TODO: SJ: Reconsider the locking mechanism here and see if using the general locking service
            // TODO: SJ: is better since that locks everything else out for this repo
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
                    } catch (GitAPIException e) {
                        logger.error("Error getting git operations for site '{}' from commit ID '{}' to " +
                                "commit ID '{}'", site, commitIdFrom, commitIdTo, e);
                    }
                } catch (IOException e) {
                    logger.error("Error getting git operations for site '{}' from commit ID '{}' to " +
                            "commit ID '{}'", site, commitIdFrom, commitIdTo, e);
                }
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

    @Override
    public void markGitLogAudited(String siteId, String commitId) {
        // TODO: SJ: Refactor to not use string literals
        String lockKey = "GitLogLock:" + siteId;
        generalLockService.lock(lockKey);
        try {
            retryingDatabaseOperationFacade.markGitLogAudited(siteId, commitId, 1);
        } finally {
            generalLockService.unlock(lockKey);
        }
    }

    @Override
    public void updateGitlog(String siteId, String lastProcessedCommitId, int batchSize) throws SiteNotFoundException {
        RingBuffer<RevCommit> commitIds = new RingBuffer<RevCommit>(batchSize);
        SiteFeed siteFeed = siteService.getSite(siteId);
        Repository repository = helper.getRepository(siteId, StringUtils.isEmpty(siteId) ? GLOBAL : SANDBOX);
        if (repository != null) {
            // TODO: SJ: Refactor to not use string literals
            String lockKey = "GitLogLock" + siteId;
            generalLockService.lock(lockKey);
            try {
                ObjectId objCommitIdFrom = repository.resolve(lastProcessedCommitId);
                ObjectId objCommitIdTo = repository.resolve(HEAD);

                logger.debug("Update git log for site '{}' from commit ID '{}' to commit ID '{}'",
                            siteId, objCommitIdFrom.getName(), objCommitIdTo.getName());
                try (Git git = new Git(repository)) {
                    // If the commitIdFrom is the same as commitIdTo, there is nothing to calculate, otherwise,
                    // let's do it
                    if (!objCommitIdFrom.equals(objCommitIdTo)) {
                        // Get the log of all the commits between commitId and head
                        LogCommand logCommand = git.log();
                        Iterable<RevCommit> commits = retryingRepositoryOperationFacade.call(logCommand);
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
                        String commitMessage = studioConfiguration.getProperty(REPO_SYNC_DB_COMMIT_MESSAGE_NO_PROCESSING);
                        while (current != null) {
                            nextCommitId = current.getId();
                            commitId = nextCommitId.getName();
                            if (StringUtils.contains(current.getFullMessage(), commitMessage)) {
                                logger.debug("Skipping the processing of commit ID '{}' in site '{}' because it's " +
                                        "marked not to be processed", commitId, siteId);
                            } else {
                                batch.add(0, commitId);
                            }
                            current = commitIds.read();
                        }

                        if (batch.size() > 0) {
                            retryingDatabaseOperationFacade.insertIgnoreGitLogList(siteId, batch);
                            siteService.updateLastSyncedGitlogCommitId(siteId, batch.get(batch.size() - 1));
                            logger.debug("Inserted '{}' git commits into the gitlog table for site '{}'",
                                    batch.size(), siteId);
                        } else {
                            siteService.updateLastSyncedGitlogCommitId(siteId, objCommitIdTo.getName());
                        }
                    }
                } catch (GitAPIException e) {
                    logger.error("Error getting commit IDs for site '{}' from commit ID '{}' to HEAD",
                            siteId, lastProcessedCommitId, e);
                }
            } catch (IOException e) {
                logger.error("Error getting commit IDs for site '{}' from commit ID '{}' to HEAD",
                        siteId, lastProcessedCommitId, e);
            } finally {
                generalLockService.unlock(lockKey);
            }
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
    public DetailedItem.Environment getItemEnvironmentProperties(String siteId, GitRepositories repoType,
                                                                 String environment, String path) {
        DetailedItem.Environment environmentData = new DetailedItem.Environment();
        try (Repository repo = helper.getRepository(siteId, repoType)) {
            if (Objects.nonNull(repo)) {
                RevTree tree = getTree(repo, environment);
                if (Objects.nonNull(tree)) {
                    populateProperties(siteId, repo, environmentData, path, environment);
                }
            }
        } catch (IOException e) {
            logger.error("Error getting environment properties for site '{}' path '{}' environment '{}'",
                    siteId, path, environment);
        }
        return environmentData;
    }

    private RevTree getTree(Repository repository, String branch) throws IOException {
        ObjectId lastCommitId = repository.resolve(R_HEADS + branch );

        if (Objects.nonNull(lastCommitId)) {
            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(lastCommitId);

                RevTree tree = commit.getTree();
                return tree;
            }
        } else {
            return null;
        }
    }

    private void populateProperties(String siteId, Repository repository, DetailedItem.Environment environment,
                                    String path, String branch) throws IOException {
        // TODO: SJ: This seems to fail silently if repository is null, fix
        if (repository != null) {
            ObjectId head = repository.resolve(R_HEADS + branch);
            String gitPath = helper.getGitPath(path);
            try (Git git = new Git(repository)) {
                LogCommand logCommand = git.log().add(head).addPath(gitPath);
                Iterable<RevCommit> commits = retryingRepositoryOperationFacade.call(logCommand);
                Iterator<RevCommit> iterator = commits.iterator();
                if (iterator.hasNext()) {
                    RevCommit revCommit = iterator.next();
                    environment.setDatePublished(Instant.ofEpochSecond(revCommit.getCommitTime()).atZone(UTC));
                    String publisherGit = revCommit.getAuthorIdent().getName();
                    User publisher = null;
                    try {
                        publisher = userServiceInternal.getUserByGitName(publisherGit);
                    } catch (ServiceLayerException | UserNotFoundException e) {
                        logger.debug("Publisher user not found for site '{}' path '{}'. Using git repo user instead.",
                                siteId, path);
                        publisher = userServiceInternal.getUserByIdOrUsername(-1, GIT_REPO_USER_USERNAME);
                    }
                    environment.setPublisher(publisher.getUsername());
                    environment.setCommitId(revCommit.getName());
                }
            } catch (IOException | GitAPIException | UserNotFoundException | ServiceLayerException e) {
                logger.error("Error while getting repository properties for content path '{}' site '{}'",
                        path, siteId);
            }
            environment.setDateScheduled(publishRequestDao.getScheduledDateForEnvironment(siteId, path, branch,
                    PublishRequest.State.READY_FOR_LIVE, DateUtils.getCurrentTime()));
        }
    }

    @Override
    public int countUnprocessedCommits(String siteId, long marker) {
        return gitLogDao.countUnprocessedCommitsSinceMarker(siteId, marker);
    }

    @Override
    public void markGitLogProcessedBeforeMarker(String siteId, long marker, int processed) {
        retryingDatabaseOperationFacade.markGitLogProcessedBeforeMarker(siteId, marker, processed, 0);
    }

    @Override
    public String getPreviousCommitId(String siteId, String commitId) {
        String toReturn = EMPTY;
        try {
            Repository repository = helper.getRepository(siteId, StringUtils.isEmpty(siteId) ? GLOBAL : SANDBOX);
            if (repository != null) {
                ObjectId head = repository.resolve(HEAD);
                try (Git git = new Git(repository)) {
                    LogCommand logCommand = git.log().add(head);
                    Iterable<RevCommit> commits = retryingRepositoryOperationFacade.call(logCommand);
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
                }
            }
        } catch (IOException | GitAPIException e) {
            logger.error("Error getting the previous commit ID for site '{}' commit ID '{}'",
                    siteId, commitId, e);
        }

        return toReturn;
    }

    @Override
    public void lockItem(String site, String path) {
        Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);

        synchronized (helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX)) {
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
                logger.error("Error locking file in site '{}' path '{}'", site, path, e);
            }
        }
    }

    @Override
    public void itemUnlock(String site, String path) {
        Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);

        synchronized (helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX)) {
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
                logger.error("Error unlocking file in site '{}' path '{}'", site, path, e);
            }
        }
    }

    @Override
    public void upsertGitLogList(String siteId, List<String> commitIds, boolean processed, boolean audited) {
        retryingDatabaseOperationFacade.upsertGitLogList(siteId, commitIds, processed ? 1 : 0, audited ? 1 : 0);
    }

    @Override
    public Optional<Resource> getContentByCommitId(String site, String path, String commitId)
            throws ContentNotFoundException {
        try {
            Repository repo = helper.getRepository(site, StringUtils.isEmpty(site) ? GLOBAL : SANDBOX);
            RevTree tree = helper.getTreeForCommit(repo, commitId);
            if (tree != null) {
                try (TreeWalk tw = TreeWalk.forPath(repo, helper.getGitPath(path), tree)) {
                    if (tw != null) {
                        ObjectId id = tw.getObjectId(0);
                        ObjectLoader objectLoader = repo.open(id);
                        return Optional.of(new GitResource(objectLoader));
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error getting content for file in site '{}' path '{}' commit ID '{}'",
                    site, path, commitId, e);
        }
        return Optional.empty();
    }

    @Override
    public boolean publishedRepositoryExists(String siteId) {
        return Objects.nonNull(helper.getRepository(siteId, PUBLISHED));
    }

    @Override
    public void initialPublish(String siteId) throws SiteNotFoundException {
        var siteFeed = siteService.getSite(siteId);
        // Create published repo
        var created = helper.createPublishedRepository(siteId, siteFeed.getSandboxBranch());
        if (created) {
            // Create staging branch
            if (servicesConfig.isStagingEnvironmentEnabled(siteId)) {
                createEnvironmentBranch(siteId, siteFeed.getSandboxBranch(),
                        servicesConfig.getStagingEnvironment(siteId));
            }
            // Create live branch
            createEnvironmentBranch(siteId, siteFeed.getSandboxBranch(), servicesConfig.getLiveEnvironment(siteId));
        }
    }

    private boolean createEnvironmentBranch(String siteId, String sandboxBranchName, String environment) {
        Repository repository = helper.getRepository(siteId, PUBLISHED);
        try (Git git = new Git(repository)) {
            CheckoutCommand checkoutCommand = git.checkout()
                    .setOrphan(true)
                    .setForceRefUpdate(true)
                    .setStartPoint(sandboxBranchName)
                    .setUpstreamMode(TRACK)
                    .setName(environment);
            retryingRepositoryOperationFacade.call(checkoutCommand);

            CommitCommand commitCommand = git.commit()
                    .setMessage(helper.getCommitMessage(REPO_INITIAL_PUBLISH_COMMIT_MESSAGE))
                    .setAllowEmpty(true);
            retryingRepositoryOperationFacade.call(commitCommand);

            return true;
        } catch (GitAPIException e) {
            logger.error("Failed to create publishing target branch (environment) '{}' in the published repo for " +
                    "site '{}'", environment, siteId, e);
            return false;
        }
    }

    public void setHelper(GitRepositoryHelper helper) {
        this.helper = helper;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setGitLogDao(GitLogDAO gitLogDao) {
        this.gitLogDao = gitLogDao;
    }

    public void setSiteFeedMapper(SiteFeedMapper siteFeedMapper) {
        this.siteFeedMapper = siteFeedMapper;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
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

    public void setClusterDao(ClusterDAO clusterDao) {
        this.clusterDao = clusterDao;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setPublishRequestDao(PublishRequestDAO publishRequestDao) {
        this.publishRequestDao = publishRequestDao;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public void setStudioUtils(StudioUtils studioUtils) {
        this.studioUtils = studioUtils;
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
}
