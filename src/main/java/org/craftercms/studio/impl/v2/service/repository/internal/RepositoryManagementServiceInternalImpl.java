/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
 */

package org.craftercms.studio.impl.v2.service.repository.internal;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.commons.crypto.impl.PbkAesTextEncryptor;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteAlreadyExistsException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.dal.RemoteRepositoryDAO;
import org.craftercms.studio.api.v2.dal.RemoteRepositoryInfo;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.service.repository.internal.RepositoryManagementServiceInternal;
import org.craftercms.studio.api.v2.util.GitRepositoryHelper;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.URIish;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.REPO_PULL_FROM_REMOTE_CONFLICT_NOTIFICATION_ENABLED;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.REPO_SANDBOX_BRANCH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_CIPHER_KEY;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_CIPHER_SALT;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.REJECTED_NODELETE;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.REJECTED_OTHER_REASON;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.REJECTED_REMOTE_CHANGED;

public class RepositoryManagementServiceInternalImpl implements RepositoryManagementServiceInternal {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryManagementServiceInternalImpl.class);

    private static final String THEIRS = "theirs";
    private static final String OURS = "ours";

    private RemoteRepositoryDAO remoteRepositoryDao;
    private StudioConfiguration studioConfiguration;
    private NotificationService notificationService;

    @Override
    public boolean addRemote(String siteId, RemoteRepository remoteRepository)
            throws ServiceLayerException, InvalidRemoteUrlException {
        boolean isValid = false;
        try {
            logger.debug("Add remote " + remoteRepository.getRemoteName() + " to the sandbox repo for the site " + siteId);
            GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration);
            Repository repo = helper.getRepository(siteId, SANDBOX);
            try (Git git = new Git(repo)) {

                Config storedConfig = repo.getConfig();
                Set<String> remotes = storedConfig.getSubsections("remote");

                if (remotes.contains(remoteRepository.getRemoteName())) {
                    throw new RemoteAlreadyExistsException(remoteRepository.getRemoteName());
                }

                RemoteAddCommand remoteAddCommand = git.remoteAdd();
                remoteAddCommand.setName(remoteRepository.getRemoteName());
                remoteAddCommand.setUri(new URIish(remoteRepository.getRemoteUrl()));
                remoteAddCommand.call();

                try {
                    isValid = helper.isRemoteValid(git, remoteRepository.getRemoteName(),
                            remoteRepository.getAuthenticationType(), remoteRepository.getRemoteUsername(),
                            remoteRepository.getRemotePassword(), remoteRepository.getRemoteToken(),
                            remoteRepository.getRemotePrivateKey());
                } finally {
                    if (!isValid) {
                        RemoteRemoveCommand remoteRemoveCommand = git.remoteRemove();
                        remoteRemoveCommand.setName(remoteRepository.getRemoteName());
                        remoteRemoveCommand.call();
                    }
                }

            } catch (URISyntaxException e) {
                logger.error("Remote URL is invalid " + remoteRepository.getRemoteUrl(), e);
                throw new InvalidRemoteUrlException();
            } catch (GitAPIException | IOException e) {
                logger.error("Error while adding remote " + remoteRepository.getRemoteName() + " (url: " +
                        remoteRepository.getRemoteUrl() + ") " + "for site " + siteId, e);
                throw new ServiceLayerException("Error while adding remote " + remoteRepository.getRemoteName() +
                        " (url: " + remoteRepository.getRemoteUrl() + ") for site " + siteId, e);
            }

            if (isValid) {
                insertRemoteToDb(siteId, remoteRepository);
            }
        } catch (CryptoException e) {
            throw new ServiceLayerException(e);
        }
        return isValid;
    }

    private void insertRemoteToDb(String siteId, RemoteRepository remoteRepository) throws CryptoException {
        logger.debug("Inserting remote " + remoteRepository.getRemoteName() + " for site " + siteId +
                " into database.");
        TextEncryptor encryptor = new PbkAesTextEncryptor(studioConfiguration.getProperty(SECURITY_CIPHER_KEY),
                studioConfiguration.getProperty(SECURITY_CIPHER_SALT));
        Map<String, String> params = new HashMap<String, String>();
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
        remoteRepositoryDao.insertRemoteRepository(params);
    }

    @Override
    public List<RemoteRepositoryInfo> listRemotes(String siteId, String sandboxBranch)
            throws ServiceLayerException, CryptoException {
        List<RemoteRepositoryInfo> res = new ArrayList<RemoteRepositoryInfo>();
        GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration);
        try (Repository repo = helper.getRepository(siteId, SANDBOX)) {
            try (Git git = new Git(repo)) {
                List<RemoteConfig> resultRemotes = git.remoteList().call();
                if (CollectionUtils.isNotEmpty(resultRemotes)) {
                    for (RemoteConfig conf : resultRemotes) {
                        fetchRemote(siteId, git, conf);
                    }
                    Map<String, List<String>> remoteBranches = getRemoteBranches(git);
                    String sandboxBranchName = sandboxBranch;
                    if (StringUtils.isEmpty(sandboxBranchName)) {
                        sandboxBranchName = studioConfiguration.getProperty(REPO_SANDBOX_BRANCH);
                    }
                    res = getRemoteRepositoryInfo(resultRemotes, remoteBranches, sandboxBranchName);
                }
            } catch (GitAPIException | CryptoException | IOException e) {
                logger.error("Error getting remote repositories for site " + siteId, e);
            }
        }
        return res;
    }

    private void fetchRemote(String siteId, Git git, RemoteConfig conf)
            throws CryptoException, IOException, ServiceLayerException, GitAPIException {
        GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration);
        RemoteRepository remoteRepository = getRemoteRepository(siteId, conf.getName());
        if (remoteRepository != null) {
            Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
            FetchCommand fetchCommand = git.fetch().setRemote(conf.getName());
            fetchCommand = helper.setAuthenticationForCommand(fetchCommand,
                    remoteRepository.getAuthenticationType(), remoteRepository.getRemoteUsername(),
                    remoteRepository.getRemotePassword(), remoteRepository.getRemoteToken(),
                    remoteRepository.getRemotePrivateKey(), tempKey, true);
            fetchCommand.call();
        }
    }

    private RemoteRepository getRemoteRepository(String siteId, String remoteName) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteName);
        return remoteRepositoryDao.getRemoteRepository(params);
    }

    private Map<String, List<String>>  getRemoteBranches(Git git) throws GitAPIException {
        List<Ref> resultRemoteBranches = git.branchList()
                .setListMode(ListBranchCommand.ListMode.REMOTE)
                .call();
        Map<String, List<String>> remoteBranches = new HashMap<String, List<String>>();
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
                remoteBranches.put(remotePart, new ArrayList<String>());
            }
            remoteBranches.get(remotePart).add(branchNamePart);
        }
        return remoteBranches;
    }

    private List<RemoteRepositoryInfo> getRemoteRepositoryInfo(List<RemoteConfig> resultRemotes,
                                                               Map<String, List<String>> remoteBranches,
                                                               String sandboxBranchName) {
        List<RemoteRepositoryInfo> res = new ArrayList<RemoteRepositoryInfo>();
        for (RemoteConfig conf : resultRemotes) {
            RemoteRepositoryInfo rri = new RemoteRepositoryInfo();
            rri.setName(conf.getName());
            List<String> branches = remoteBranches.get(rri.getName());
            if (CollectionUtils.isEmpty(branches)) {
                branches = new ArrayList<String>();
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
            rri.setPushUrl(sbPushUrl.toString());
            res.add(rri);
        }
        return res;
    }

    @Override
    public boolean pullFromRemote(String siteId, String remoteName, String remoteBranch, String mergeStrategy)
            throws InvalidRemoteUrlException, ServiceLayerException, CryptoException {
        logger.debug("Get remote data from database for remote " + remoteName + " and site " + siteId);
        RemoteRepository remoteRepository = getRemoteRepository(siteId, remoteName);
        logger.debug("Prepare pull command");
        GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration);
        Repository repo = helper.getRepository(siteId, SANDBOX);
        try (Git git = new Git(repo)) {
            PullResult pullResult = null;
            PullCommand pullCommand = git.pull();
            logger.debug("Set remote " + remoteName);
            pullCommand.setRemote(remoteRepository.getRemoteName());
            logger.debug("Set branch to be " + remoteBranch);
            pullCommand.setRemoteBranchName(remoteBranch);
            Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
            pullCommand = helper.setAuthenticationForCommand(pullCommand, remoteRepository.getAuthenticationType(),
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
            pullResult = pullCommand.call();
            Files.delete(tempKey);
            if (!pullResult.isSuccessful() && conflictNotificationEnabled()) {
                List<String> conflictFiles = new ArrayList<String>();
                if (pullResult.getMergeResult() != null) {
                    pullResult.getMergeResult().getConflicts().forEach((m, v) -> {
                        conflictFiles.add(m);
                    });
                }
                notificationService.notifyRepositoryMergeConflict(siteId, conflictFiles, Locale.ENGLISH);
            }
            return pullResult != null && pullResult.isSuccessful();
        } catch (InvalidRemoteException e) {
            logger.error("Remote is invalid " + remoteName, e);
            throw new InvalidRemoteUrlException();
        } catch (GitAPIException e) {
            logger.error("Error while pulling from remote " + remoteName + " branch "
                    + remoteBranch + " for site " + siteId, e);
            throw new ServiceLayerException("Error while pulling from remote " + remoteName + " branch "
                    + remoteBranch + " for site " + siteId, e);
        } catch (CryptoException | IOException e) {
            throw new ServiceLayerException(e);
        }
    }

    @Override
    public boolean pushToRemote(String siteId, String remoteName, String remoteBranch, boolean force)
            throws CryptoException, ServiceLayerException, InvalidRemoteUrlException {
        logger.debug("Get remote data from database for remote " + remoteName + " and site " + siteId);
        RemoteRepository remoteRepository = getRemoteRepository(siteId, remoteName);

        logger.debug("Prepare push command.");
        GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration);
        Repository repo = helper.getRepository(siteId, SANDBOX);
        try (Git git = new Git(repo)) {
            Iterable<PushResult> pushResultIterable = null;
            PushCommand pushCommand = git.push();
            logger.debug("Set remote " + remoteName);
            pushCommand.setRemote(remoteRepository.getRemoteName());
            logger.debug("Set branch to be " + remoteBranch);
            pushCommand.setRefSpecs(new RefSpec(remoteBranch + ":" + remoteBranch));
            Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
            pushCommand = helper.setAuthenticationForCommand(pushCommand, remoteRepository.getAuthenticationType(),
                    remoteRepository.getRemoteUsername(), remoteRepository.getRemotePassword(),
                    remoteRepository.getRemoteToken(), remoteRepository.getRemotePrivateKey(), tempKey, true);
            pushCommand.setForce(force);
            pushResultIterable = pushCommand.call();
            Files.delete(tempKey);

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
            logger.error("Remote is invalid " + remoteName, e);
            throw new InvalidRemoteUrlException();
        } catch (IOException | JGitInternalException | GitAPIException | CryptoException e) {
            logger.error("Error while pushing to remote " + remoteName + " branch "
                    + remoteBranch + " for site " + siteId, e);
            throw new ServiceLayerException("Error while pushing to remote " + remoteName + " branch "
                    + remoteBranch + " for site " + siteId, e);
        }
    }

    @Override
    public boolean removeRemote(String siteId, String remoteName) throws CryptoException {
        logger.debug("Remove remote " + remoteName + " from the sandbox repo for the site " + siteId);
        GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration);
        Repository repo = helper.getRepository(siteId, SANDBOX);
        try (Git git = new Git(repo)) {
            RemoteRemoveCommand remoteRemoveCommand = git.remoteRemove();
            remoteRemoveCommand.setName(remoteName);
            remoteRemoveCommand.call();

        } catch (GitAPIException e) {
            logger.error("Failed to remove remote " + remoteName + " for site " + siteId, e);
            return false;
        }

        logger.debug("Remove remote record from database for remote " + remoteName + " and site " + siteId);
        Map<String, String> params = new HashMap<String, String>();
        params.put("siteId", siteId);
        params.put("remoteName", remoteName);
        remoteRepositoryDao.deleteRemoteRepository(params);

        return true;
    }

    private boolean conflictNotificationEnabled() {
        return Boolean.parseBoolean(
                studioConfiguration.getProperty(REPO_PULL_FROM_REMOTE_CONFLICT_NOTIFICATION_ENABLED));
    }

    public RemoteRepositoryDAO getRemoteRepositoryDao() {
        return remoteRepositoryDao;
    }

    public void setRemoteRepositoryDao(RemoteRepositoryDAO remoteRepositoryDao) {
        this.remoteRepositoryDao = remoteRepositoryDao;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
