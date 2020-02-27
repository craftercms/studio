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
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.dal.GitLog;
import org.craftercms.studio.api.v2.dal.GitLogDAO;
import org.craftercms.studio.api.v2.dal.RepoOperation;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.dao.DuplicateKeyException;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.craftercms.studio.api.v1.constant.GitRepositories.GLOBAL;
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
import static org.eclipse.jgit.lib.Constants.OBJ_TREE;
import static org.eclipse.jgit.revwalk.RevSort.REVERSE;

public class GitContentRepository implements ContentRepository {

    private static final Logger logger = LoggerFactory.getLogger(GitContentRepository.class);

    private StudioConfiguration studioConfiguration;
    private GitLogDAO gitLogDao;
    private SiteFeedMapper siteFeedMapper;

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
                                        operations.addAll(processDiffEntry(diffEntries, firstCommit.getId(),
                                                firstCommit.getCommitterIdent().getName(),
                                                Instant.ofEpochSecond(firstCommit.getCommitTime()).atZone(UTC)));
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
                                                operations.addAll(processDiffEntry(diffEntries, nextCommitId, author,
                                                        Instant.ofEpochSecond(commit.getCommitTime()).atZone(UTC)));
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
                                        operations.addAll(processDiffEntry(diffEntries, firstCommit.getId(),
                                                firstCommit.getCommitterIdent().getName(),
                                                Instant.ofEpochSecond(firstCommit.getCommitTime()).atZone(UTC)));
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
                                                        processDiffEntry(diffEntries, objCommitIdTo, null,null));
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

    private List<RepoOperation> processDiffEntry(List<DiffEntry> diffEntries, ObjectId commitId, String author,
                                                   ZonedDateTime commitTime) {
        List<RepoOperation> toReturn = new ArrayList<RepoOperation>();

        for (DiffEntry diffEntry : diffEntries) {

            // Update the paths to have a preceding separator
            String pathNew = FILE_SEPARATOR + diffEntry.getNewPath();
            String pathOld = FILE_SEPARATOR + diffEntry.getOldPath();

            RepoOperation repoOperation = null;
            switch (diffEntry.getChangeType()) {
                case ADD:
                    repoOperation = new RepoOperation(CREATE, pathNew, commitTime, null,
                            commitId.getName());
                    break;
                case MODIFY:
                    repoOperation = new RepoOperation(UPDATE, pathNew, commitTime, null, commitId.getName());
                    break;
                case DELETE:
                    repoOperation = new RepoOperation(DELETE, pathOld, commitTime, null, commitId.getName());
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
}
