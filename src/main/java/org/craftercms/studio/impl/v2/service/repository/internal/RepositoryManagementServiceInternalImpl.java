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

package org.craftercms.studio.impl.v2.service.repository.internal;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.repository.RemoteNotRemovableException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.dal.DiffConflictedFile;
import org.craftercms.studio.api.v2.dal.GitLog;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.dal.RemoteRepositoryDAO;
import org.craftercms.studio.api.v2.dal.RemoteRepositoryInfo;
import org.craftercms.studio.api.v2.dal.RepositoryStatus;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.service.repository.MergeResult;
import org.craftercms.studio.api.v2.service.repository.internal.RepositoryManagementServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.utils.GitUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.DeleteBranchCommand;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteListCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.NoRemoteRepositoryException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_SANDBOX_REPOSITORY_GIT_LOCK;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_COMMIT_MESSAGE_POSTSCRIPT;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_COMMIT_MESSAGE_PROLOGUE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_PULL_FROM_REMOTE_CONFLICT_NOTIFICATION_ENABLED;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_SANDBOX_BRANCH;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CLUSTER_NODE_REMOTE_NAME_PREFIX;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.LOCK_FILE;

public class RepositoryManagementServiceInternalImpl implements RepositoryManagementServiceInternal {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryManagementServiceInternalImpl.class);

    private static final String THEIRS = "theirs";
    private static final String OURS = "ours";

    private RemoteRepositoryDAO remoteRepositoryDao;
    private StudioConfiguration studioConfiguration;
    private NotificationService notificationService;
    private SecurityService securityService;
    private UserServiceInternal userServiceInternal;
    private org.craftercms.studio.api.v1.repository.ContentRepository contentRepository;
    private TextEncryptor encryptor;
    private ClusterDAO clusterDao;
    private GeneralLockService generalLockService;
    private SiteService siteService;
    private GitRepositoryHelper gitRepositoryHelper;
    private ContentRepository contentRepositoryV2;
    private int batchSizeGitLog = 1000;
    private RetryingRepositoryOperationFacade retryingRepositoryOperationFacade;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    @Override
    public boolean addRemote(String siteId, RemoteRepository remoteRepository)
            throws ServiceLayerException, InvalidRemoteUrlException, RemoteRepositoryNotFoundException {
        boolean isValid = false;
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        generalLockService.lock(gitLockKey);
        try {
            logger.debug("Add remote " + remoteRepository.getRemoteName() + " to the sandbox repo for the site " + siteId);
            Repository repo = gitRepositoryHelper.getRepository(siteId, SANDBOX);
            try (Git git = new Git(repo)) {

                Config storedConfig = repo.getConfig();
                Set<String> remotes = storedConfig.getSubsections("remote");

                if (remotes.contains(remoteRepository.getRemoteName())) {
                    throw new RemoteAlreadyExistsException(remoteRepository.getRemoteName());
                }

                RemoteAddCommand remoteAddCommand = git.remoteAdd();
                remoteAddCommand.setName(remoteRepository.getRemoteName());
                remoteAddCommand.setUri(new URIish(remoteRepository.getRemoteUrl()));
                retryingRepositoryOperationFacade.call(remoteAddCommand);

                try {
                    isValid = gitRepositoryHelper.isRemoteValid(git, remoteRepository.getRemoteName(),
                            remoteRepository.getAuthenticationType(), remoteRepository.getRemoteUsername(),
                            remoteRepository.getRemotePassword(), remoteRepository.getRemoteToken(),
                            remoteRepository.getRemotePrivateKey());
                } finally {
                    if (!isValid) {
                        RemoteRemoveCommand remoteRemoveCommand = git.remoteRemove();
                        remoteRemoveCommand.setRemoteName(remoteRepository.getRemoteName());
                        retryingRepositoryOperationFacade.call(remoteRemoveCommand);

                        ListBranchCommand listBranchCommand = git.branchList()
                                .setListMode(ListBranchCommand.ListMode.REMOTE);
                        List<Ref> resultRemoteBranches = retryingRepositoryOperationFacade.call(listBranchCommand);

                        List<String> branchesToDelete = new ArrayList<>();
                        for (Ref remoteBranchRef : resultRemoteBranches) {
                            if (remoteBranchRef.getName().startsWith(Constants.R_REMOTES +
                                    remoteRepository.getRemoteName())) {
                                branchesToDelete.add(remoteBranchRef.getName());
                            }
                        }
                        if (isNotEmpty(branchesToDelete)) {
                            DeleteBranchCommand delBranch = git.branchDelete();
                            String[] array = new String[branchesToDelete.size()];
                            delBranch.setBranchNames(branchesToDelete.toArray(array));
                            delBranch.setForce(true);
                            retryingRepositoryOperationFacade.call(delBranch);
                        }
                    }
                }

            } catch (URISyntaxException e) {
                logger.error("Remote URL is invalid " + remoteRepository.getRemoteUrl(), e);
                throw new InvalidRemoteUrlException();
            } catch (GitAPIException | IOException e) {
                if (e.getCause() instanceof NoRemoteRepositoryException) {
                    logger.error("Remote repository not found " + remoteRepository.getRemoteName() + " (url: " +
                            remoteRepository.getRemoteUrl() + ") " + "for site " + siteId, e);
                    throw new RemoteRepositoryNotFoundException("Remote repository not found " +
                            remoteRepository.getRemoteName() + " (url: " + remoteRepository.getRemoteUrl() +
                            ") for site " + siteId, e);
                } else {
                    logger.error("Error while adding remote " + remoteRepository.getRemoteName() + " (url: " +
                            remoteRepository.getRemoteUrl() + ") " + "for site " + siteId, e);
                    throw new ServiceLayerException("Error while adding remote " + remoteRepository.getRemoteName() +
                            " (url: " + remoteRepository.getRemoteUrl() + ") for site " + siteId, e);
                }
            }

            if (isValid) {
                insertRemoteToDb(siteId, remoteRepository);
            }
        } catch (CryptoException e) {
            throw new ServiceLayerException(e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
        return isValid;
    }

    private void insertRemoteToDb(String siteId, RemoteRepository remoteRepository) throws CryptoException {
        logger.debug("Inserting remote " + remoteRepository.getRemoteName() + " for site " + siteId +
                " into database.");
        Map<String, String> params = new HashMap<>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteRepository.getRemoteName());
        params.put("remoteUrl", remoteRepository.getRemoteUrl());
        params.put("authenticationType", remoteRepository.getAuthenticationType());
        params.put("remoteUsername", remoteRepository.getRemoteUsername());

        if (StringUtils.isNotEmpty(remoteRepository.getRemotePassword())) {
            logger.debug("Encrypt password before inserting to database");
            String hashedPassword = encryptor.encrypt(remoteRepository.getRemotePassword());
            params.put("remotePassword", hashedPassword);
        } else {
            params.put("remotePassword", remoteRepository.getRemotePassword());
        }
        if (StringUtils.isNotEmpty(remoteRepository.getRemoteToken())) {
            logger.debug("Encrypt token before inserting to database");
            String hashedToken = encryptor.encrypt(remoteRepository.getRemoteToken());
            params.put("remoteToken", hashedToken);
        } else {
            params.put("remoteToken", remoteRepository.getRemoteToken());
        }
        if (StringUtils.isNotEmpty(remoteRepository.getRemotePrivateKey())) {
            logger.debug("Encrypt private key before inserting to database");
            String hashedPrivateKey = encryptor.encrypt(remoteRepository.getRemotePrivateKey());
            params.put("remotePrivateKey", hashedPrivateKey);
        } else {
            params.put("remotePrivateKey", remoteRepository.getRemotePrivateKey());
        }

        logger.debug("Insert site remote record into database");
        retryingDatabaseOperationFacade.insertRemoteRepository(params);
    }

    @Override
    public List<RemoteRepositoryInfo> listRemotes(String siteId, String sandboxBranch) {
        List<RemoteRepositoryInfo> res = new ArrayList<>();
        Map<String, String> unreachableRemotes = new HashMap<>();
        try (Repository repo = gitRepositoryHelper.getRepository(siteId, SANDBOX)) {
            try (Git git = new Git(repo)) {
                RemoteListCommand remoteListCommand = git.remoteList();
                List<RemoteConfig> resultRemotes = retryingRepositoryOperationFacade.call(remoteListCommand);
                if (isNotEmpty(resultRemotes)) {
                    for (RemoteConfig conf : resultRemotes) {
                        try {
                            fetchRemote(siteId, git, conf);
                        } catch (Exception e) {
                            logger.warn("Failed to fetch from remote repository " + conf.getName());
                            unreachableRemotes.put(conf.getName(), e.getMessage());
                        }
                    }
                    Map<String, List<String>> remoteBranches = getRemoteBranches(git);
                    String sandboxBranchName = sandboxBranch;
                    if (StringUtils.isEmpty(sandboxBranchName)) {
                        sandboxBranchName = studioConfiguration.getProperty(REPO_SANDBOX_BRANCH);
                    }
                    res = getRemoteRepositoryInfo(resultRemotes, remoteBranches, unreachableRemotes, sandboxBranchName);
                }
            } catch (GitAPIException e) {
                logger.error("Error getting remote repositories for site " + siteId, e);
            }
        }
        return res;
    }

    private void fetchRemote(String siteId, Git git, RemoteConfig conf)
            throws CryptoException, IOException, ServiceLayerException, GitAPIException {
        RemoteRepository remoteRepository = getRemoteRepository(siteId, conf.getName());
        if (remoteRepository != null) {
            Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
            FetchCommand fetchCommand = git.fetch().setRemote(conf.getName());
            gitRepositoryHelper.setAuthenticationForCommand(fetchCommand,
                    remoteRepository.getAuthenticationType(), remoteRepository.getRemoteUsername(),
                    remoteRepository.getRemotePassword(), remoteRepository.getRemoteToken(),
                    remoteRepository.getRemotePrivateKey(), tempKey, true);
            retryingRepositoryOperationFacade.call(fetchCommand);
        }
    }

    private RemoteRepository getRemoteRepository(String siteId, String remoteName) {
        Map<String, String> params = new HashMap<>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteName);
        return remoteRepositoryDao.getRemoteRepository(params);
    }

    private Map<String, List<String>>  getRemoteBranches(Git git) throws GitAPIException {
        ListBranchCommand listBranchCommand = git.branchList()
                .setListMode(ListBranchCommand.ListMode.REMOTE);
        List<Ref> resultRemoteBranches = retryingRepositoryOperationFacade.call(listBranchCommand);
        Map<String, List<String>> remoteBranches = new HashMap<>();
        for (Ref remoteBranchRef : resultRemoteBranches) {
            String branchFullName = remoteBranchRef.getName().replace(Constants.R_REMOTES, "");
            String remotePart = StringUtils.EMPTY;
            String branchNamePart = StringUtils.EMPTY;
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
        return remoteBranches;
    }

    private List<RemoteRepositoryInfo> getRemoteRepositoryInfo(List<RemoteConfig> resultRemotes,
                                                               Map<String, List<String>> remoteBranches,
                                                               Map<String, String> unreachableRemotes,
                                                               String sandboxBranchName) {
        List<RemoteRepositoryInfo> res = new ArrayList<>();
        for (RemoteConfig conf : resultRemotes) {
            RemoteRepositoryInfo rri = new RemoteRepositoryInfo();
            rri.setName(conf.getName());
            if (MapUtils.isNotEmpty(unreachableRemotes) && unreachableRemotes.containsKey(conf.getName())) {
                rri.setReachable(false);
                rri.setUnreachableReason(unreachableRemotes.get(conf.getName()));
            }
            List<String> branches = remoteBranches.get(rri.getName());
            if (CollectionUtils.isEmpty(branches)) {
                branches = new ArrayList<>();
                branches.add(sandboxBranchName);
            }
            rri.setBranches(branches);

            StringBuilder sbUrl = new StringBuilder();
            if (isNotEmpty(conf.getURIs())) {
                for (int i = 0; i < conf.getURIs().size(); i++) {
                    sbUrl.append(conf.getURIs().get(i).toString());
                    if (i < conf.getURIs().size() - 1) {
                        sbUrl.append(":");
                    }
                }
            }
            rri.setUrl(sbUrl.toString());

            StringBuilder sbFetch = new StringBuilder();
            if (isNotEmpty(conf.getFetchRefSpecs())) {
                for (int i = 0; i < conf.getFetchRefSpecs().size(); i++) {
                    sbFetch.append(conf.getFetchRefSpecs().get(i).toString());
                    if (i < conf.getFetchRefSpecs().size() - 1) {
                        sbFetch.append(":");
                    }
                }
            }
            rri.setFetch(sbFetch.toString());

            StringBuilder sbPushUrl = new StringBuilder();
            if (isNotEmpty(conf.getPushURIs())) {
                for (int i = 0; i < conf.getPushURIs().size(); i++) {
                    sbPushUrl.append(conf.getPushURIs().get(i).toString());
                    if (i < conf.getPushURIs().size() - 1) {
                        sbPushUrl.append(":");
                    }
                }
            } else {
                sbPushUrl.append(rri.getUrl());
            }
            rri.setPushUrl(sbPushUrl.toString());
            res.add(rri);
        }
        return res;
    }

    @Override
    public MergeResult pullFromRemote(String siteId, String remoteName, String remoteBranch, String mergeStrategy)
            throws InvalidRemoteUrlException, ServiceLayerException, InvalidRemoteRepositoryCredentialsException,
                    RemoteRepositoryNotFoundException {
        logger.debug("Get remote data from database for remote " + remoteName + " and site " + siteId);
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        RemoteRepository remoteRepository = getRemoteRepository(siteId, remoteName);
        logger.debug("Prepare pull command");
        Repository repo = gitRepositoryHelper.getRepository(siteId, SANDBOX);
        generalLockService.lock(gitLockKey);
        Path tempKey = null;
        try (Git git = new Git(repo)) {
            PullCommand pullCommand = git.pull();
            logger.debug("Set remote " + remoteName);
            pullCommand.setRemote(remoteRepository.getRemoteName());
            logger.debug("Set branch to be " + remoteBranch);
            pullCommand.setRemoteBranchName(remoteBranch);
            tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
            gitRepositoryHelper.setAuthenticationForCommand(pullCommand, remoteRepository.getAuthenticationType(),
                    remoteRepository.getRemoteUsername(), remoteRepository.getRemotePassword(),
                    remoteRepository.getRemoteToken(), remoteRepository.getRemotePrivateKey(), tempKey, true);
            switch (mergeStrategy) {
                case THEIRS:
                    pullCommand.setStrategy(MergeStrategy.THEIRS);
                    break;
                case OURS:
                    pullCommand.setStrategy(MergeStrategy.OURS);
                    break;
                default:
                    break;
            }
            pullCommand.setFastForward(MergeCommand.FastForwardMode.NO_FF);
            PullResult pullResult = retryingRepositoryOperationFacade.call(pullCommand);
            String pullResultMessage = pullResult.toString();
            if (StringUtils.isNotEmpty(pullResultMessage)) {
                logger.info(pullResultMessage);
            }
            if (pullResult.isSuccessful()) {
                String lastCommitId = contentRepository.getRepoLastCommitId(siteId);
                contentRepositoryV2.upsertGitLogList(siteId, List.of(lastCommitId), false, false);

                List<String> newMergedCommits = extractCommitIdsFromPullResult(siteId, repo, pullResult);
                List<String> commitIds = new LinkedList<>();
                if (isNotEmpty(newMergedCommits)) {
                    logger.debug("Really pulled commits:");
                    int cnt = 0;
                    for (String commitId : newMergedCommits) {
                        logger.debug(commitId);
                        if (!StringUtils.equals(lastCommitId, commitId)) {
                            commitIds.add(commitId);
                            if (cnt++ >= batchSizeGitLog) {
                                contentRepositoryV2.upsertGitLogList(siteId, commitIds, true, true);
                                cnt = 0;
                                commitIds.clear();
                            }
                        }
                    }
                    if (isNotEmpty(commitIds)) {
                        contentRepositoryV2.upsertGitLogList(siteId, commitIds, true, true);
                    }
                }
                siteService.updateLastCommitId(siteId, lastCommitId);
                return MergeResult.from(pullResult, newMergedCommits);
            } else if (conflictNotificationEnabled()) {
                List<String> conflictFiles = new LinkedList<>();
                if (pullResult.getMergeResult() != null) {
                    conflictFiles.addAll(pullResult.getMergeResult().getConflicts().keySet());
                }
                notificationService.notifyRepositoryMergeConflict(siteId, conflictFiles);
            }
        } catch (InvalidRemoteException e) {
            logger.error("Remote is invalid " + remoteName, e);
            throw new InvalidRemoteUrlException();
        } catch (TransportException e) {
            GitUtils.translateException(e, logger, remoteName, remoteRepository.getRemoteUrl(),
                                        remoteRepository.getRemoteUsername());
        } catch (GitAPIException e) {
            logger.error("Error while pulling from remote " + remoteName + " branch "
                    + remoteBranch + " for site " + siteId, e);
            throw new ServiceLayerException("Error while pulling from remote " + remoteName + " branch "
                    + remoteBranch + " for site " + siteId, e);
        } catch (CryptoException | IOException e) {
            throw new ServiceLayerException(e);
        } finally {
            try {
                if (tempKey != null) {
                    Files.deleteIfExists(tempKey);
                }
            } catch (IOException e) {
                logger.warn("Error deleting file '{}'", tempKey);
            }
            generalLockService.unlock(gitLockKey);
        }

        return MergeResult.failed();
    }

    private List<String> extractCommitIdsFromPullResult(String siteId, Repository repo, PullResult pullResult) {
        List<String> commitIds = new LinkedList<>();
        ObjectId[] mergedCommits = pullResult.getMergeResult().getMergedCommits();
        for (ObjectId mergedCommit : mergedCommits) {
            try {
                RevCommit revCommit = repo.parseCommit(mergedCommit);
                commitIds.addAll(processCommitId(siteId, revCommit));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return commitIds;
    }

    private Set<String> processCommitId(String siteId, RevCommit revCommit) {
        Set<String> toRet = new HashSet<>();
        Queue<RevCommit> commitIdsQueue = new LinkedList<>();
        commitIdsQueue.offer(revCommit);
        while (!commitIdsQueue.isEmpty()) {
            RevCommit rc = commitIdsQueue.poll();
            if (Objects.nonNull(rc)) {
                String cId = rc.getName();
                if (!toRet.contains(cId)) {
                    GitLog gitLog = contentRepositoryV2.getGitLog(siteId, cId);
                    if (Objects.isNull(gitLog)) {
                        RevCommit[] parents = rc.getParents();
                        if (Objects.nonNull(parents) && parents.length > 0) {
                            for (RevCommit parent : parents) {
                                commitIdsQueue.offer(parent);
                            }
                        }
                        toRet.add(cId);
                    }
                }
            }
        }
        return toRet;
    }

    @Override
    public boolean pushToRemote(String siteId, String remoteName, String remoteBranch, boolean force)
            throws ServiceLayerException, InvalidRemoteUrlException, InvalidRemoteRepositoryCredentialsException,
                    RemoteRepositoryNotFoundException {
        logger.debug("Get remote data from database for remote " + remoteName + " and site " + siteId);
        RemoteRepository remoteRepository = getRemoteRepository(siteId, remoteName);

        logger.debug("Prepare push command.");
        Repository repo = gitRepositoryHelper.getRepository(siteId, SANDBOX);
        try (Git git = new Git(repo)) {
            Iterable<PushResult> pushResultIterable;
            PushCommand pushCommand = git.push();
            logger.debug("Set remote " + remoteName);
            pushCommand.setRemote(remoteRepository.getRemoteName());
            logger.debug("Set branch to be " + remoteBranch);
            RefSpec r = new RefSpec();
            r = r.setSourceDestination(Constants.R_HEADS + repo.getBranch(),
                    Constants.R_HEADS +  remoteBranch);
            pushCommand.setRefSpecs(r);
            Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
            gitRepositoryHelper.setAuthenticationForCommand(pushCommand, remoteRepository.getAuthenticationType(),
                    remoteRepository.getRemoteUsername(), remoteRepository.getRemotePassword(),
                    remoteRepository.getRemoteToken(), remoteRepository.getRemotePrivateKey(), tempKey, true);
            pushCommand.setForce(force);
            pushResultIterable = retryingRepositoryOperationFacade.call(pushCommand);
            Files.delete(tempKey);

            boolean toRet = true;
            for (PushResult pushResult : pushResultIterable) {
                String pushResultMessage = pushResult.getMessages();
                if (StringUtils.isNotEmpty(pushResultMessage)) {
                    logger.info(pushResultMessage);
                }
                Collection<RemoteRefUpdate> updates = pushResult.getRemoteUpdates();
                for (RemoteRefUpdate remoteRefUpdate : updates) {
                    switch (remoteRefUpdate.getStatus()) {
                        case REJECTED_NODELETE:
                            toRet = false;
                            logger.error("Remote ref " + remoteRefUpdate.getSrcRef() + " update was rejected, " +
                                    "because remote side doesn't support/allow deleting refs.\n" +
                                    remoteRefUpdate.getMessage());
                            break;
                        case REJECTED_NONFASTFORWARD:
                            toRet = false;
                            logger.error("Remote ref " + remoteRefUpdate.getSrcRef() + " update was rejected, as it " +
                                    "would cause non fast-forward update.\n" + remoteRefUpdate.getMessage());
                            break;
                        case REJECTED_REMOTE_CHANGED:
                            toRet = false;
                            logger.error("Remote ref " + remoteRefUpdate.getSrcRef() + " update was rejected, because" +
                                    " old object id on remote repository " + remoteRefUpdate.getRemoteName() +
                                    " wasn't the same as defined expected old object. \n" + remoteRefUpdate.getMessage());
                            break;
                        case REJECTED_OTHER_REASON:
                            toRet = false;
                            logger.error("Remote ref " + remoteRefUpdate.getSrcRef() + " update was rejected for "
                                    + "other reason.\n" + remoteRefUpdate.getMessage());
                        default:
                            break;
                    }
                }
            }
            return toRet;
        } catch (InvalidRemoteException e) {
            logger.error("Remote is invalid " + remoteName, e);
            throw new InvalidRemoteUrlException();
        } catch (TransportException e) {
            GitUtils.translateException(e, logger, remoteName, remoteRepository.getRemoteUrl(),
                                        remoteRepository.getRemoteUsername());
            return false;
        } catch (IOException | JGitInternalException | GitAPIException | CryptoException e) {
            logger.error("Error while pushing to remote " + remoteName + " branch "
                    + remoteBranch + " for site " + siteId, e);
            throw new ServiceLayerException("Error while pushing to remote " + remoteName + " branch "
                    + remoteBranch + " for site " + siteId, e);
        }
    }

    @Override
    public boolean removeRemote(String siteId, String remoteName) throws RemoteNotRemovableException {
        if (!isRemovableRemote(siteId, remoteName)) {
            throw new RemoteNotRemovableException("Remote repository " + remoteName + " is not removable");
        }
        logger.debug("Remove remote " + remoteName + " from the sandbox repo for the site " + siteId);

        Repository repo = gitRepositoryHelper.getRepository(siteId, SANDBOX);
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        generalLockService.lock(gitLockKey);
        try (Git git = new Git(repo)) {
            RemoteRemoveCommand remoteRemoveCommand = git.remoteRemove();
            remoteRemoveCommand.setRemoteName(remoteName);
            retryingRepositoryOperationFacade.call(remoteRemoveCommand);

            ListBranchCommand listBranchCommand = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE);
            List<Ref> resultRemoteBranches = retryingRepositoryOperationFacade.call(listBranchCommand);

            List<String> branchesToDelete = new ArrayList<>();
            for (Ref remoteBranchRef : resultRemoteBranches) {
                if (remoteBranchRef.getName().startsWith(Constants.R_REMOTES + remoteName)) {
                    branchesToDelete.add(remoteBranchRef.getName());
                }
            }
            if (isNotEmpty(branchesToDelete)) {
                DeleteBranchCommand delBranch = git.branchDelete();
                String[] array = new String[branchesToDelete.size()];
                delBranch.setBranchNames(branchesToDelete.toArray(array));
                delBranch.setForce(true);
                retryingRepositoryOperationFacade.call(delBranch);
            }
        } catch (GitAPIException e) {
            logger.error("Failed to remove remote " + remoteName + " for site " + siteId, e);
            return false;
        } finally {
            generalLockService.unlock(gitLockKey);
        }

        logger.debug("Remove remote record from database for remote " + remoteName + " and site " + siteId);
        Map<String, String> params = new HashMap<>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteName);
        retryingDatabaseOperationFacade.deleteRemoteRepository(params);

        return true;
    }

    private boolean isRemovableRemote(String siteId, String remoteName) {
        boolean toRet = true;
        if (StringUtils.startsWith(remoteName, CLUSTER_NODE_REMOTE_NAME_PREFIX)) {
            RemoteRepository remoteRepository = getRemoteRepository(siteId, remoteName);
            List<ClusterMember> clusterMembers = getClusterMembersByRemoteName(remoteName);
            toRet = !(Objects.isNull(remoteRepository) && isNotEmpty(clusterMembers));
        }
        return toRet;
    }

    private List<ClusterMember> getClusterMembersByRemoteName(String remoteName) {
        Map<String, String> params = new HashMap<>();
        params.put("remoteName", remoteName);
        return clusterDao.getMemberByRemoteName(params);
    }

    @Override
    public RepositoryStatus getRepositoryStatus(String siteId)
            throws ServiceLayerException {
        Repository repo = gitRepositoryHelper.getRepository(siteId, SANDBOX);
        RepositoryStatus repositoryStatus = new RepositoryStatus();
        logger.debug("Execute git status and return conflicting paths and uncommitted changes");
        try (Git git = new Git(repo)) {
            StatusCommand statusCommand = git.status();
            Status status = retryingRepositoryOperationFacade.call(statusCommand);
            repositoryStatus.setClean(status.isClean());
            repositoryStatus.setConflicting(status.getConflicting());
            repositoryStatus.setUncommittedChanges(status.getUncommittedChanges());
        } catch (GitAPIException e) {
            logger.error("Error while getting repository status for site " + siteId, e);
            throw new ServiceLayerException("Error getting repository status for site " + siteId, e);
        }
        return repositoryStatus;
    }

    @Override
    public boolean resolveConflict(String siteId, String path, String resolution)
            throws ServiceLayerException {
        Repository repo = gitRepositoryHelper.getRepository(siteId, SANDBOX);
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        generalLockService.lock(gitLockKey);
        ResetCommand resetCommand;
        CheckoutCommand checkoutCommand;
        try (Git git = new Git(repo)) {
            switch (resolution.toLowerCase()) {
                case "ours" :
                    logger.debug("Resolve conflict using OURS strategy for site " + siteId + " and path " + path);
                    logger.debug("Reset merge conflict in git index");
                    resetCommand = git.reset().addPath(gitRepositoryHelper.getGitPath(path));
                    retryingRepositoryOperationFacade.call(resetCommand);
                    logger.debug("Checkout content from HEAD of studio repository");
                    checkoutCommand =
                            git.checkout().addPath(gitRepositoryHelper.getGitPath(path)).setStartPoint(Constants.HEAD);
                    retryingRepositoryOperationFacade.call(checkoutCommand);
                    break;
                case "theirs" :
                    logger.debug("Resolve conflict using THEIRS strategy for site " + siteId + " and path " + path);
                    logger.debug("Reset merge conflict in git index");
                    resetCommand = git.reset().addPath(gitRepositoryHelper.getGitPath(path));
                    retryingRepositoryOperationFacade.call(resetCommand);
                    logger.debug("Checkout content from merge HEAD of remote repository");
                    List<ObjectId> mergeHeads = repo.readMergeHeads();
                    ObjectId mergeCommitId = mergeHeads.get(0);
                    checkoutCommand = git.checkout().addPath(gitRepositoryHelper.getGitPath(path))
                            .setStartPoint(mergeCommitId.getName());
                    retryingRepositoryOperationFacade.call(checkoutCommand);
                    break;
                default:
                    throw new ServiceLayerException("Unsupported resolution strategy for repository conflicts");
            }

            if (repo.getRepositoryState() == RepositoryState.MERGING_RESOLVED) {
                logger.debug("Merge resolved. Check if there are no uncommitted changes (repo is clean)");
                StatusCommand statusCommand = git.status();
                Status status = retryingRepositoryOperationFacade.call(statusCommand);
                if (!status.hasUncommittedChanges()) {
                    logger.debug("Repository is clean. Committing to complete merge");
                    String userName = securityService.getCurrentUser();
                    User user = userServiceInternal.getUserByIdOrUsername(-1, userName);
                    PersonIdent personIdent = gitRepositoryHelper.getAuthorIdent(user);
                    CommitCommand commitCommand = git.commit()
                            .setAllowEmpty(true)
                            .setMessage("Merge resolved. Repo is clean (no changes)")
                            .setAuthor(personIdent);
                    retryingRepositoryOperationFacade.call(commitCommand);
                }
            }
        } catch (GitAPIException | IOException | UserNotFoundException | ServiceLayerException e) {
            logger.error("Error while resolving conflict for site " + siteId + " using " + resolution + " resolution " +
                    "strategy", e);
            throw new ServiceLayerException("Error while resolving conflict for site " + siteId + " using " + resolution + " resolution " +
                    "strategy", e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
        return true;
    }

    @Override
    public DiffConflictedFile getDiffForConflictedFile(String siteId, String path)
            throws ServiceLayerException {
        DiffConflictedFile diffResult = new DiffConflictedFile();
        Repository repo = gitRepositoryHelper.getRepository(siteId, SANDBOX);
        try (Git git = new Git(repo)) {
            List<ObjectId> mergeHeads = repo.readMergeHeads();
            ObjectId mergeCommitId = mergeHeads.get(0);
            logger.debug("Get content for studio version of conflicted file " + path + " for site " + siteId);
            InputStream studioVersionIs = contentRepositoryV2.getContentByCommitId(siteId, path, Constants.HEAD)
                                                             .orElseThrow()
                                                             .getInputStream();
            diffResult.setStudioVersion(IOUtils.toString(studioVersionIs, UTF_8));
            logger.debug("Get content for remote version of conflicted file " + path + " for site " + siteId);
            InputStream remoteVersionIs = contentRepositoryV2.getContentByCommitId(siteId, path, mergeCommitId.getName())
                                                             .orElseThrow()
                                                             .getInputStream();
            diffResult.setRemoteVersion(IOUtils.toString(remoteVersionIs, UTF_8));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            logger.debug("Get diff between studio and remote version of conflicted file " + path + " for site "
                    + siteId);
            RevTree headTree = gitRepositoryHelper.getTreeForCommit(repo, Constants.HEAD);
            RevTree remoteTree = gitRepositoryHelper.getTreeForCommit(repo, mergeCommitId.getName());

            try (ObjectReader reader = repo.newObjectReader()) {
                CanonicalTreeParser headCommitTreeParser = new CanonicalTreeParser();
                CanonicalTreeParser remoteCommitTreeParser = new CanonicalTreeParser();
                headCommitTreeParser.reset(reader, headTree.getId());
                remoteCommitTreeParser.reset(reader, remoteTree.getId());

                // Diff the two commit Ids
                DiffCommand diffCommand = git.diff()
                        .setPathFilter(PathFilter.create(gitRepositoryHelper.getGitPath(path)))
                        .setOldTree(headCommitTreeParser)
                        .setNewTree(remoteCommitTreeParser)
                        .setOutputStream(baos);
                retryingRepositoryOperationFacade.call(diffCommand);
                diffResult.setDiff(baos.toString());
            }


        } catch (IOException | GitAPIException e) {
            logger.error("Error while getting diff for conflicting file " + path + " site " + siteId);
            throw new ServiceLayerException("Error while getting diff for conflicting file " + path + " site " + siteId);
        }
        return diffResult;
    }

    @Override
    public boolean commitResolution(String siteId, String commitMessage)
            throws ServiceLayerException {
        Repository repo = gitRepositoryHelper.getRepository(siteId, SANDBOX);
        logger.debug("Commit resolution for merge conflict for site " + siteId);
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        generalLockService.lock(gitLockKey);
        try (Git git = new Git(repo)) {
            StatusCommand statusCommand = git.status();
            Status status = retryingRepositoryOperationFacade.call(statusCommand);

            logger.debug("Add all uncommitted changes/files");
            AddCommand addCommand = git.add();
            for (String uncommitted : status.getUncommittedChanges()) {
                addCommand.addFilepattern(uncommitted);
            }
            retryingRepositoryOperationFacade.call(addCommand);
            logger.debug("Commit changes");
            CommitCommand commitCommand = git.commit();
            String userName = securityService.getCurrentUser();
            User user = userServiceInternal.getUserByIdOrUsername(-1, userName);
            PersonIdent personIdent = gitRepositoryHelper.getAuthorIdent(user);
            String prologue = studioConfiguration.getProperty(REPO_COMMIT_MESSAGE_PROLOGUE);
            String postscript = studioConfiguration.getProperty(REPO_COMMIT_MESSAGE_POSTSCRIPT);

            StringBuilder sbMessage = new StringBuilder();
            if (StringUtils.isNotEmpty(prologue)) {
                sbMessage.append(prologue).append("\n\n");
            }
            sbMessage.append(commitMessage);
            if (StringUtils.isNotEmpty(postscript)) {
                sbMessage.append("\n\n").append(postscript);
            }
            commitCommand.setCommitter(personIdent).setAuthor(personIdent).setMessage(sbMessage.toString());
            retryingRepositoryOperationFacade.call(commitCommand);
            return true;
        } catch (GitAPIException | UserNotFoundException | ServiceLayerException e) {
            logger.error("Error while committing conflict resolution for site " + siteId, e);
            throw new ServiceLayerException("Error while committing conflict resolution for site " + siteId, e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
    }

    @Override
    public boolean cancelFailedPull(String siteId) throws ServiceLayerException {
        logger.debug("To cancel failed pull, reset hard needs to be executed");
        Repository repo = gitRepositoryHelper.getRepository(siteId, SANDBOX);
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        generalLockService.lock(gitLockKey);
        try (Git git = new Git(repo)) {
            ResetCommand resetCommand = git.reset().setMode(ResetCommand.ResetType.HARD);
            retryingRepositoryOperationFacade.call(resetCommand);
        } catch (GitAPIException e) {
            logger.error("Error while canceling failed pull for site " + siteId, e);
            throw new ServiceLayerException("Reset hard failed for site " + siteId, e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
        return true;
    }

    @Override
    public boolean unlockRepository(String siteId, GitRepositories repositoryType) {
        boolean toRet = false;
        Repository repo = gitRepositoryHelper.getRepository(siteId, repositoryType);
        if (Objects.nonNull(repo)) {
            toRet = FileUtils.deleteQuietly(Paths.get(repo.getDirectory().getAbsolutePath(), LOCK_FILE).toFile());
        }
        return toRet;
    }

    @Override
    public boolean isCorrupted(String siteId, GitRepositories repositoryType) throws ServiceLayerException {
        Repository repository = gitRepositoryHelper.getRepository(siteId, repositoryType);
        if (repository == null) {
            throw new SiteNotFoundException();
        }
        try (Git git = Git.wrap(repository)) {
            git.status().call();
        } catch (JGitInternalException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CorruptObjectException || cause instanceof EOFException) {
                return true;
            }
        } catch (Exception e) {
            throw new ServiceLayerException("Unknown error checking repository", e);
        }
        return false;
    }

    @Override
    public void repairCorrupted(String siteId, GitRepositories repositoryType) throws ServiceLayerException {
        Repository repository = gitRepositoryHelper.getRepository(siteId, repositoryType);
        if (repository == null) {
            throw new SiteNotFoundException();
        }
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        generalLockService.lock(gitLockKey);
        try (Git git = Git.wrap(repository)) {
            FileUtils.forceDelete(repository.getIndexFile());
            ResetCommand resetCommand = git.reset().setMode(ResetCommand.ResetType.HARD);
            retryingRepositoryOperationFacade.call(resetCommand);
        } catch (Exception e) {
            throw new ServiceLayerException("Error repairing corrupted repository for site " + siteId, e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
    }

    private boolean conflictNotificationEnabled() {
        return Boolean.parseBoolean(
                studioConfiguration.getProperty(REPO_PULL_FROM_REMOTE_CONFLICT_NOTIFICATION_ENABLED));
    }

    public void setRemoteRepositoryDao(RemoteRepositoryDAO remoteRepositoryDao) {
        this.remoteRepositoryDao = remoteRepositoryDao;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public void setContentRepository(org.craftercms.studio.api.v1.repository.ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void setEncryptor(TextEncryptor encryptor) {
        this.encryptor = encryptor;
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

    public void setGitRepositoryHelper(GitRepositoryHelper gitRepositoryHelper) {
        this.gitRepositoryHelper = gitRepositoryHelper;
    }

    public void setContentRepositoryV2(ContentRepository contentRepositoryV2) {
        this.contentRepositoryV2 = contentRepositoryV2;
    }

    public void setBatchSizeGitLog(int batchSizeGitLog) {
        this.batchSizeGitLog = batchSizeGitLog;
    }

    public void setRetryingRepositoryOperationFacade(RetryingRepositoryOperationFacade retryingRepositoryOperationFacade) {
        this.retryingRepositoryOperationFacade = retryingRepositoryOperationFacade;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }
}
