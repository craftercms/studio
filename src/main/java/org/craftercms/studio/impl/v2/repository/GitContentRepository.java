/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.collections4.iterators.ReverseListIterator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.dal.GitLog;
import org.craftercms.studio.api.v2.dal.GitLogDAO;
import org.craftercms.studio.api.v2.dal.PublishingHistoryItem;
import org.craftercms.studio.api.v2.dal.RepoOperation;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.ZoneOffset.UTC;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.craftercms.studio.api.v1.constant.GitRepositories.GLOBAL;
import static org.craftercms.studio.api.v1.constant.GitRepositories.PUBLISHED;
import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.INDEX_FILE;
import static org.craftercms.studio.api.v2.dal.RepoOperation.Action.COPY;
import static org.craftercms.studio.api.v2.dal.RepoOperation.Action.CREATE;
import static org.craftercms.studio.api.v2.dal.RepoOperation.Action.DELETE;
import static org.craftercms.studio.api.v2.dal.RepoOperation.Action.MOVE;
import static org.craftercms.studio.api.v2.dal.RepoOperation.Action.UPDATE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_SYNC_DB_COMMIT_MESSAGE_NO_PROCESSING;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.IGNORE_FILES;
import static org.eclipse.jgit.lib.Constants.HEAD;
import static org.eclipse.jgit.lib.Constants.MASTER;
import static org.eclipse.jgit.lib.Constants.OBJ_TREE;
import static org.eclipse.jgit.lib.Constants.R_HEADS;
import static org.eclipse.jgit.revwalk.RevSort.REVERSE;

public class GitContentRepository implements ContentRepository {

    private static final Logger logger = LoggerFactory.getLogger(GitContentRepository.class);

    private StudioConfiguration studioConfiguration;
    private GitLogDAO gitLogDao;
    private SiteFeedMapper siteFeedMapper;
    private UserServiceInternal userServiceInternal;

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
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration);
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
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration);
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

                        ObjectId objFirstCommitId = repo.resolve(firstCommitId);
                        boolean initialEqToCommit = StringUtils.equals(firstCommitId, commitIdTo);
                        boolean initialEqFromCommit = StringUtils.equals(firstCommitId, commitIdFrom);

                        try (Git git = new Git(repo)) {

                            if (fromEmptyRepo) {
                                try (RevWalk walk = new RevWalk(repo)) {
                                    RevCommit firstCommit = walk.parseCommit(objFirstCommitId);
                                    try (ObjectReader reader = repo.newObjectReader()) {
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
                                                List<DiffEntry> diffEntries = git.diff()
                                                        .setOldTree(prevCommitTreeParser)
                                                        .setNewTree(nextCommitTreeParser)
                                                        .call();


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
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration);
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

                        ObjectId objFirstCommitId = repo.resolve(firstCommitId);
                        boolean initialEqToCommit = StringUtils.equals(firstCommitId, commitIdTo);
                        boolean initialEqFromCommit = StringUtils.equals(firstCommitId, commitIdFrom);

                        try (Git git = new Git(repo)) {

                            if (fromEmptyRepo) {
                                try (RevWalk walk = new RevWalk(repo)) {
                                    RevCommit firstCommit = walk.parseCommit(objFirstCommitId);
                                    try (ObjectReader reader = repo.newObjectReader()) {
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
                                                List<DiffEntry> diffEntries = git.diff()
                                                        .setOldTree(fromCommitTreeParser)
                                                        .setNewTree(toCommitTreeParser)
                                                        .call();


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

        return operations;
    }

    @Override
    public String getRepoFirstCommitId(final String site) {
        String toReturn = EMPTY;
        try {
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration);
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
            throws GitAPIException {
        List<RepoOperation> toReturn = new ArrayList<RepoOperation>();

        for (DiffEntry diffEntry : diffEntries) {

            // Update the paths to have a preceding separator
            String pathNew = FILE_SEPARATOR + diffEntry.getNewPath();
            String pathOld = FILE_SEPARATOR + diffEntry.getOldPath();

            RepoOperation repoOperation = null;
            Iterable<RevCommit> iterable = null;
            RevCommit latestCommit = null;
            ZonedDateTime commitTime = null;
            String author = null;
            switch (diffEntry.getChangeType()) {
                case ADD:
                    iterable = git.log().addPath(diffEntry.getNewPath()).setMaxCount(1).call();
                    latestCommit = iterable.iterator().next();
                    commitTime = Instant.ofEpochSecond(latestCommit.getCommitTime()).atZone(UTC);
                    author = latestCommit.getAuthorIdent().getName();
                    repoOperation = new RepoOperation(CREATE, pathNew, commitTime, null,
                            latestCommit.getId().getName());
                    break;
                case MODIFY:
                    iterable = git.log().addPath(diffEntry.getNewPath()).setMaxCount(1).call();
                    latestCommit = iterable.iterator().next();
                    commitTime = Instant.ofEpochSecond(latestCommit.getCommitTime()).atZone(UTC);
                    author = latestCommit.getAuthorIdent().getName();
                    repoOperation = new RepoOperation(UPDATE, pathNew, commitTime, null,
                            latestCommit.getId().getName());
                    break;
                case DELETE:
                    iterable = git.log().addPath(diffEntry.getOldPath()).setMaxCount(1).call();
                    latestCommit = iterable.iterator().next();
                    commitTime = Instant.ofEpochSecond(latestCommit.getCommitTime()).atZone(UTC);
                    author = latestCommit.getAuthorIdent().getName();
                    repoOperation = new RepoOperation(DELETE, pathOld, commitTime, null,
                            latestCommit.getId().getName());
                    break;
                case RENAME:
                    iterable = git.log().addPath(diffEntry.getOldPath()).setMaxCount(1).call();
                    latestCommit = iterable.iterator().next();
                    commitTime = Instant.ofEpochSecond(latestCommit.getCommitTime()).atZone(UTC);
                    author = latestCommit.getAuthorIdent().getName();
                    repoOperation = new RepoOperation(MOVE, pathOld, commitTime, pathNew, commitId.getName());
                    break;
                case COPY:
                    iterable = git.log().addPath(diffEntry.getNewPath()).setMaxCount(1).call();
                    latestCommit = iterable.iterator().next();
                    commitTime = Instant.ofEpochSecond(latestCommit.getCommitTime()).atZone(UTC);
                    author = latestCommit.getAuthorIdent().getName();
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
        gitLogDao.markGitLogProcessed(params);
    }

    @Override
    public void insertGitLog(String siteId, String commitId, int processed) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("siteId", siteId);
        params.put("commitId", commitId);
        params.put("processed", processed);
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
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration);
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
}
