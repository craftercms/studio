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
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.*;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.event.site.SyncFromRepoEvent;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.service.repository.MergeResult;
import org.craftercms.studio.api.v2.service.repository.internal.RepositoryManagementServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.utils.GitUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.NoRemoteRepositoryException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.task.TaskExecutor;
import org.springframework.lang.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.*;
import static org.craftercms.studio.api.v2.utils.StudioUtils.getStudioTemporaryFilesRoot;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.LOCK_FILE;

public class RepositoryManagementServiceInternalImpl implements RepositoryManagementServiceInternal, ApplicationContextAware {

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
    private GeneralLockService generalLockService;
    private SiteService siteService;
    private GitRepositoryHelper gitRepositoryHelper;
    private ContentRepository contentRepositoryV2;
    private int batchSizeGitLog = 1000;
    private RetryingRepositoryOperationFacade retryingRepositoryOperationFacade;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;
    protected TaskExecutor taskExecutor;
    private ApplicationContext applicationContext;

    @Override
    public boolean addRemote(String siteId, RemoteRepository remoteRepository)
            throws ServiceLayerException, InvalidRemoteUrlException, RemoteRepositoryNotFoundException {
        boolean isValid = false;
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        generalLockService.lock(gitLockKey);
        try {
            logger.debug("Add the remote '{}' to the sandbox repository in site '{}'",
                    remoteRepository.getRemoteName(), siteId);
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
                logger.error("Failed to add the remote '{}' URL '{}' to site '{}' because the URL is invalid",
                        remoteRepository.getRemoteName(), remoteRepository.getRemoteUrl(), siteId, e);
                throw new InvalidRemoteUrlException();
            } catch (GitAPIException | IOException e) {
                if (e.getCause() instanceof NoRemoteRepositoryException) {
                    logger.error("Failed to add the remote '{}' URL '{}' to site '{}' because the remote repository " +
                            "was not found",
                            remoteRepository.getRemoteName(), remoteRepository.getRemoteUrl(), siteId, e);
                    throw new RemoteRepositoryNotFoundException(format("Failed to add the remote '%s' URL '%s'" +
                                    " to site '%s' because the remote repository was not found",
                                    remoteRepository.getRemoteName(), remoteRepository.getRemoteUrl(), siteId), e);
                } else {
                    logger.error("Failed to add the remote '{}' URL '{}' to site '{}'",
                            remoteRepository.getRemoteName(), remoteRepository.getRemoteUrl(), siteId, e);
                    throw new ServiceLayerException(format("Failed to add the remote '%s' URL '%s'" +
                                    " to site '%s'",
                            remoteRepository.getRemoteName(), remoteRepository.getRemoteUrl(), siteId), e);
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
        // TODO: SJ: Avoid using string literals
        logger.debug("Insert the remote repository '{}' from site '{}' into the database",
                remoteRepository.getRemoteName(), siteId);
        Map<String, String> params = new HashMap<>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteRepository.getRemoteName());
        params.put("remoteUrl", remoteRepository.getRemoteUrl());
        params.put("authenticationType", remoteRepository.getAuthenticationType());
        params.put("remoteUsername", remoteRepository.getRemoteUsername());

        if (isNotEmpty(remoteRepository.getRemotePassword())) {
            logger.trace("Encrypt the password before inserting into the database for site '{}'", siteId);
            String hashedPassword = encryptor.encrypt(remoteRepository.getRemotePassword());
            params.put("remotePassword", hashedPassword);
        } else {
            params.put("remotePassword", remoteRepository.getRemotePassword());
        }
        if (isNotEmpty(remoteRepository.getRemoteToken())) {
            logger.trace("Encrypt the token before inserting into the database for site '{}'", siteId);
            String hashedToken = encryptor.encrypt(remoteRepository.getRemoteToken());
            params.put("remoteToken", hashedToken);
        } else {
            params.put("remoteToken", remoteRepository.getRemoteToken());
        }
        if (isNotEmpty(remoteRepository.getRemotePrivateKey())) {
            logger.trace("Encrypt the private key before inserting into the database for site '{}'", siteId);
            String hashedPrivateKey = encryptor.encrypt(remoteRepository.getRemotePrivateKey());
            params.put("remotePrivateKey", hashedPrivateKey);
        } else {
            params.put("remotePrivateKey", remoteRepository.getRemotePrivateKey());
        }

        logger.debug("Insert the site remote record into database for site '{}'", siteId);
        retryingDatabaseOperationFacade.retry(() -> remoteRepositoryDao.insertRemoteRepository(params));
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
                            logger.warn("Failed to fetch from the remote repository '{}' in site '{}'",
                                    conf.getName(), siteId, e);
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
                logger.error("Failed to get the remote repositories for site '{}'", siteId, e);
            }
        }
        return res;
    }

    private void fetchRemote(String siteId, Git git, RemoteConfig conf)
            throws CryptoException, IOException, ServiceLayerException, GitAPIException {
        RemoteRepository remoteRepository = getRemoteRepository(siteId, conf.getName());
        if (remoteRepository != null) {
            Path tempKey = Files.createTempFile(getStudioTemporaryFilesRoot(), UUID.randomUUID().toString(), TMP_FILE_SUFFIX);
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
        logger.debug("Get the git remote repository information from the database for remote '{}' in site '{}'",
                remoteName, siteId);
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        RemoteRepository remoteRepository = getRemoteRepository(siteId, remoteName);
        if (remoteRepository == null) {
            throw new RemoteRepositoryNotFoundException(format("Remote repository '%s' does not exist in site '%s'", remoteName, siteId));
        }
        logger.trace("Prepare the JGit pull command in site '{}'", siteId);
        Repository repo = gitRepositoryHelper.getRepository(siteId, SANDBOX);
        generalLockService.lock(gitLockKey);
        Path tempKey = null;
        try (Git git = new Git(repo)) {
            PullCommand pullCommand = git.pull();
            logger.trace("Set the JGit pull command remote to '{}' in site '{}'", remoteName, siteId);
            pullCommand.setRemote(remoteRepository.getRemoteName());
            logger.trace("Set the JGit pull command branch to '{}' in site '{}'", remoteBranch, siteId);
            pullCommand.setRemoteBranchName(remoteBranch);
            tempKey = Files.createTempFile(getStudioTemporaryFilesRoot(), UUID.randomUUID().toString(), TMP_FILE_SUFFIX);
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
            if (isNotEmpty(pullResultMessage)) {
                logger.info("Git pull in site '{}' returned '{}'", siteId, pullResultMessage);
            }
            if (pullResult.isSuccessful()) {
                applicationContext.publishEvent(new SyncFromRepoEvent(siteId));
                List<String> newMergedCommits = extractCommitIdsFromPullResult(repo, pullResult);
                return MergeResult.from(pullResult, newMergedCommits);
            } else if (conflictNotificationEnabled()) {
                List<String> conflictFiles = new LinkedList<>();
                if (pullResult.getMergeResult() != null) {
                    conflictFiles.addAll(pullResult.getMergeResult().getConflicts().keySet());
                }
                notificationService.notifyRepositoryMergeConflict(siteId, conflictFiles);
            }
        } catch (InvalidRemoteException e) {
            logger.error("Failed to pull from the remote '{}' in site '{}' because the remote is invalid",
                    remoteName, siteId, e);
            throw new InvalidRemoteUrlException();
        } catch (TransportException e) {
            // TODO: SJ: Seems like the actual logging is being done inside the util, not great, need to fix
            GitUtils.translateException(e, logger, remoteName, remoteRepository.getRemoteUrl(),
                                        remoteRepository.getRemoteUsername());
        } catch (GitAPIException e) {
            logger.error("Failed to pull from remote '{}' branch '{}' in site '{}'",
                    remoteName, remoteBranch, siteId, e);
            throw new ServiceLayerException(format("Failed to pull from remote '%s' branch '%s' in site '%s'",
                    remoteName, remoteBranch, siteId), e);
        } catch (CryptoException | IOException e) {
            throw new ServiceLayerException(e);
        } finally {
            try {
                if (tempKey != null) {
                    Files.deleteIfExists(tempKey);
                }
            } catch (IOException e) {
                logger.warn("Failed to delete the file '{}'", tempKey, e);
            }
            generalLockService.unlock(gitLockKey);
        }

        return MergeResult.failed();
    }

    private List<String> extractCommitIdsFromPullResult(Repository repo, PullResult pullResult) {
        List<String> commitIds = new LinkedList<>();
        ObjectId[] mergedCommits = pullResult.getMergeResult().getMergedCommits();
        for (ObjectId mergedCommit : mergedCommits) {
            try {
                RevCommit revCommit = repo.parseCommit(mergedCommit);
                commitIds.add(revCommit.getName());
            } catch (IOException e) {
                logger.error("Failed to parse commit '{}'", mergedCommit.getName(), e);
            }
        }
        return commitIds;
    }

    @Override
    public boolean pushToRemote(String siteId, String remoteName, String remoteBranch, boolean force)
            throws ServiceLayerException, InvalidRemoteUrlException, InvalidRemoteRepositoryCredentialsException,
                    RemoteRepositoryNotFoundException {
        logger.debug("Get the git remote repository information from the database for remote '{}' in site '{}'",
                remoteName, siteId);
        RemoteRepository remoteRepository = getRemoteRepository(siteId, remoteName);

        logger.trace("Prepare the JGit push command in site '{}'", siteId);
        Repository repo = gitRepositoryHelper.getRepository(siteId, SANDBOX);
        try (Git git = new Git(repo)) {
            Iterable<PushResult> pushResultIterable;
            PushCommand pushCommand = git.push();
            logger.trace("Set the JGit push command remote to '{}' in site '{}'", remoteName, siteId);
            pushCommand.setRemote(remoteRepository.getRemoteName());
            logger.trace("Set the JGit push command branch to '{}' in site '{}'", remoteBranch, siteId);
            RefSpec r = new RefSpec();
            r = r.setSourceDestination(Constants.R_HEADS + repo.getBranch(),
                    Constants.R_HEADS +  remoteBranch);
            pushCommand.setRefSpecs(r);
            Path tempKey = Files.createTempFile(getStudioTemporaryFilesRoot(), UUID.randomUUID().toString(), TMP_FILE_SUFFIX);
            gitRepositoryHelper.setAuthenticationForCommand(pushCommand, remoteRepository.getAuthenticationType(),
                    remoteRepository.getRemoteUsername(), remoteRepository.getRemotePassword(),
                    remoteRepository.getRemoteToken(), remoteRepository.getRemotePrivateKey(), tempKey, true);
            pushCommand.setForce(force);
            pushResultIterable = retryingRepositoryOperationFacade.call(pushCommand);
            Files.delete(tempKey);

            boolean toRet = true;
            for (PushResult pushResult : pushResultIterable) {
                String pushResultMessage = pushResult.getMessages();
                if (isNotEmpty(pushResultMessage)) {
                    logger.info("Git push in site '{}' returned '{}'", siteId, pushResultMessage);
                }
                Collection<RemoteRefUpdate> updates = pushResult.getRemoteUpdates();
                for (RemoteRefUpdate remoteRefUpdate : updates) {
                    switch (remoteRefUpdate.getStatus()) {
                        case REJECTED_NODELETE:
                            toRet = false;
                            logger.error("Failed to push to remote '{}' ref '{}' from site '{}'. Remote side " +
                                    "doesn't support/allow deleting refs\n'{}'",
                                    remoteName, remoteRefUpdate.getSrcRef(), siteId, remoteRefUpdate.getMessage());
                            break;
                        case REJECTED_NONFASTFORWARD:
                            toRet = false;
                            logger.error("Failed to push to remote '{}' ref '{}' from site '{}'. Push would " +
                                            "cause a non-fast-forward update\n'{}'",
                                    remoteName, remoteRefUpdate.getSrcRef(), siteId, remoteRefUpdate.getMessage());
                            break;
                        case REJECTED_REMOTE_CHANGED:
                            toRet = false;
                            logger.error("Failed to push to remote '{}' ref '{}' from site '{}'. The remote " +
					    "has changed\n'{}'",
                                    remoteName, remoteRefUpdate.getSrcRef(), siteId, remoteRefUpdate.getMessage());
                            break;
                        case REJECTED_OTHER_REASON:
                            toRet = false;
                            logger.error("Failed to push to remote '{}' ref '{}' from site '{}'. Message:\n'{}'",
                                    remoteName, remoteRefUpdate.getSrcRef(), siteId, remoteRefUpdate.getMessage());
                        default:
                            break;
                    }
                }
            }
            return toRet;
        } catch (InvalidRemoteException e) {
            logger.error("Failed to push to the remote '{}' from site '{}', the remote is invalid. ",
                    remoteName, siteId, e);
            throw new InvalidRemoteUrlException();
        } catch (TransportException e) {
            GitUtils.translateException(e, logger, remoteName, remoteRepository.getRemoteUrl(),
                                        remoteRepository.getRemoteUsername());
            return false;
        } catch (IOException | JGitInternalException | GitAPIException | CryptoException e) {
            logger.error("Failed to push to the remote '{}' branch '{}' from site '{}'",
                    remoteName, remoteBranch, siteId, e);
            throw new ServiceLayerException(format("Failed to push to the remote '%s' branch '%s' from site '%s'",
                    remoteName, remoteBranch, siteId), e);
        }
    }

    @Override
    public boolean removeRemote(String siteId, String remoteName) throws RemoteNotRemovableException {
        if (!isRemovableRemote(siteId, remoteName)) {
            throw new RemoteNotRemovableException("Remote repository " + remoteName + " is not removable");
        }
        logger.debug("Remove the remote '{}' from the sandbox repository in site '{}'", remoteName, siteId);

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
            logger.error("Failed to remove the remote '{}' from site '{}'", remoteName, siteId, e);
            return false;
        } finally {
            generalLockService.unlock(gitLockKey);
        }

        logger.debug("Remove the database record for remote '{}' from site '{}'", remoteName, siteId);
        Map<String, String> params = new HashMap<>();

	// TODO: SJ: Avoid using string literals
        params.put("siteId", siteId);
        params.put("remoteName", remoteName);
        retryingDatabaseOperationFacade.retry(() -> remoteRepositoryDao.deleteRemoteRepository(params));

        return true;
    }

    private boolean isRemovableRemote(String siteId, String remoteName) {
        return true;
    }

    @Override
    public RepositoryStatus getRepositoryStatus(String siteId)
            throws ServiceLayerException {
        Repository repo = gitRepositoryHelper.getRepository(siteId, SANDBOX);
        RepositoryStatus repositoryStatus = new RepositoryStatus();
        logger.trace("Execute git status in site '{}' and return any conflicting paths and uncommitted changes",
                siteId);
        try (Git git = new Git(repo)) {
            StatusCommand statusCommand = git.status();
            Status status = retryingRepositoryOperationFacade.call(statusCommand);
            repositoryStatus.setClean(status.isClean());
            repositoryStatus.setConflicting(status.getConflicting());
            repositoryStatus.setUncommittedChanges(status.getUncommittedChanges());
        } catch (GitAPIException e) {
            logger.error("Failed to execute git status in site '{}'", siteId, e);
            throw new ServiceLayerException(format("Failed to execute git status in site '%s'", siteId), e);
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
                    logger.debug("Resolve conflicts using _OURS_ strategy for site '{}' path '{}'", siteId, path);
                    logger.trace("Reset merge conflict in git index in site '{}'", siteId);
                    resetCommand = git.reset().addPath(gitRepositoryHelper.getGitPath(path));
                    retryingRepositoryOperationFacade.call(resetCommand);
                    logger.trace("Checkout the content from local merge HEAD in site '{}'", siteId);
                    checkoutCommand =
                            git.checkout().addPath(gitRepositoryHelper.getGitPath(path)).setStartPoint(Constants.HEAD);
                    retryingRepositoryOperationFacade.call(checkoutCommand);
                    break;
                case "theirs" :
                    logger.debug("Resolve conflicts using _THEIRS_ strategy for site '{}' path '{}'", siteId, path);
                    logger.trace("Reset merge conflict in git index in site '{}'", siteId);
                    resetCommand = git.reset().addPath(gitRepositoryHelper.getGitPath(path));
                    retryingRepositoryOperationFacade.call(resetCommand);
                    logger.trace("Checkout the content from remote merge HEAD in site '{}'", siteId);
                    List<ObjectId> mergeHeads = repo.readMergeHeads();
                    ObjectId mergeCommitId = mergeHeads.get(0);
                    checkoutCommand = git.checkout().addPath(gitRepositoryHelper.getGitPath(path))
                            .setStartPoint(mergeCommitId.getName());
                    retryingRepositoryOperationFacade.call(checkoutCommand);
                    break;
                default:
                    logger.error("Unsupported resolution strategy for repository conflicts " +
                            "in site '{}", siteId);
                    throw new ServiceLayerException(format("Unsupported resolution strategy for repository conflicts " +
                            "in site '%s'", siteId));
            }

            if (repo.getRepositoryState() == RepositoryState.MERGING_RESOLVED) {
                logger.debug("Check for any uncommitted changes and make sure the repo is clean in site '{}'.",
                        siteId);
                StatusCommand statusCommand = git.status();
                Status status = retryingRepositoryOperationFacade.call(statusCommand);
                if (!status.hasUncommittedChanges()) {
                    logger.debug("The repository is clean. Commit to complete the merge in site '{}'.", siteId);
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
            logger.error("Failed to resolve conflicts in site '{}' using the resolution strategy '{}'",
                    siteId, resolution, e);
            throw new ServiceLayerException(format("Failed to resolve conflicts in site '%s' using the resolution " +
                            "strategy '%s'", siteId, resolution), e);
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
            if (mergeHeads == null) {
                // No merge head
                return diffResult;
            }
            ObjectId mergeCommitId = mergeHeads.get(0);
            logger.debug("Get the local content of the conflicting file from site '{}' path '{}'", siteId, path);
            InputStream studioVersionIs = contentRepositoryV2.getContentByCommitId(siteId, path, Constants.HEAD)
                                                             .orElseThrow()
                                                             .getInputStream();
            diffResult.setStudioVersion(IOUtils.toString(studioVersionIs, UTF_8));
            logger.debug("Get the remote content of the conflicting file from site '{}' path '{}'", siteId, path);
            InputStream remoteVersionIs = contentRepositoryV2.getContentByCommitId(siteId, path, mergeCommitId.getName())
                                                             .orElseThrow()
                                                             .getInputStream();
            diffResult.setRemoteVersion(IOUtils.toString(remoteVersionIs, UTF_8));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            logger.debug("Diff the local and remote versions of the conflicting file in site '{}' path '{}'", siteId, path);
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
            logger.error("Failed to diff the conflicting file in site '{}' path '{}'", siteId, path, e);
            throw new ServiceLayerException(format("Failed to diff the conflicting file in site '%s' path '%s'",
                    siteId, path), e);
        }
        return diffResult;
    }

    @Override
    public boolean commitResolution(String siteId, String commitMessage)
            throws ServiceLayerException {
        Repository repo = gitRepositoryHelper.getRepository(siteId, SANDBOX);
        logger.debug("Commit after resolving the merge conflicts in site '{}'", siteId);
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        generalLockService.lock(gitLockKey);
        try (Git git = new Git(repo)) {
            StatusCommand statusCommand = git.status();
            Status status = retryingRepositoryOperationFacade.call(statusCommand);

            logger.trace("Add all uncommitted files in site '{}'", siteId);
            AddCommand addCommand = git.add();
            for (String uncommitted : status.getUncommittedChanges()) {
                addCommand.addFilepattern(uncommitted);
            }
            retryingRepositoryOperationFacade.call(addCommand);
            logger.trace("Commit the changes in site '{}'", siteId);
            CommitCommand commitCommand = git.commit();
            String userName = securityService.getCurrentUser();
            User user = userServiceInternal.getUserByIdOrUsername(-1, userName);
            PersonIdent personIdent = gitRepositoryHelper.getAuthorIdent(user);
            String prologue = studioConfiguration.getProperty(REPO_COMMIT_MESSAGE_PROLOGUE);
            String postscript = studioConfiguration.getProperty(REPO_COMMIT_MESSAGE_POSTSCRIPT);

            StringBuilder sbMessage = new StringBuilder();
            if (isNotEmpty(prologue)) {
                sbMessage.append(prologue).append("\n\n");
            }
            sbMessage.append(commitMessage);
            if (isNotEmpty(postscript)) {
                sbMessage.append("\n\n").append(postscript);
            }
            commitCommand.setCommitter(personIdent).setAuthor(personIdent).setMessage(sbMessage.toString());
            retryingRepositoryOperationFacade.call(commitCommand);
            return true;
        } catch (GitAPIException | UserNotFoundException | ServiceLayerException e) {
            logger.error("Failed to commit the conflict resolution in site '{}'", siteId, e);
            throw new ServiceLayerException(format("Failed to commit the conflict resolution in site '%s'",
                    siteId), e);
        } finally {
            generalLockService.unlock(gitLockKey);
        }
    }

    @Override
    public boolean cancelFailedPull(String siteId) throws ServiceLayerException {
        logger.debug("Cancel the failed pull operation by performing a 'reset --hard' in site '{}'", siteId);
        Repository repo = gitRepositoryHelper.getRepository(siteId, SANDBOX);
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        generalLockService.lock(gitLockKey);
        try (Git git = new Git(repo)) {
            ResetCommand resetCommand = git.reset().setMode(ResetCommand.ResetType.HARD);
            retryingRepositoryOperationFacade.call(resetCommand);
        } catch (GitAPIException e) {
            logger.error("Failed to cancel the pull operation in site '{}'", siteId, e);
            throw new ServiceLayerException(format("Failed to cancel the pull operation in site '%s'", siteId), e);
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

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
