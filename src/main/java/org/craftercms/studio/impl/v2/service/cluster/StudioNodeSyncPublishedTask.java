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

package org.craftercms.studio.impl.v2.service.cluster;

import static org.craftercms.studio.api.v1.constant.GitRepositories.PUBLISHED;
import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.PUBLISHED_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_SYNC_DB_COMMIT_MESSAGE_NO_PROCESSING;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_SECTION_REMOTE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;
import static org.eclipse.jgit.lib.Constants.DEFAULT_REMOTE_NAME;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.annotation.RetryingOperation;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.URIish;

public class StudioNodeSyncPublishedTask extends StudioNodeSyncBaseTask {

    protected static final List<String> createdSites = new ArrayList<String>();
    protected static final Map<String, Map<String, String>> remotesMap = new HashMap<String, Map<String, String>>();

    private static final Logger logger = LoggerFactory.getLogger(StudioNodeSyncPublishedTask.class);

    protected static final Map<String, ReentrantLock> singleWorkerLockMap = new HashMap<String, ReentrantLock>();

    protected boolean lockSiteInternal(String siteId) {
        ReentrantLock singleWorkerLock = singleWorkerLockMap.get(siteId);
        if (singleWorkerLock == null) {
            singleWorkerLock = new ReentrantLock();
            singleWorkerLockMap.put(siteId, singleWorkerLock);
        }
        return singleWorkerLock.tryLock();
    }
    
    protected void unlockSiteInternal(String siteId) {
        ReentrantLock singleWorkerLock = singleWorkerLockMap.get(siteId);
        if (singleWorkerLock != null) {
            singleWorkerLock.unlock();
        }
    }

    // We always sync published since we're not tracking it yet
    // TODO: SJ: Be smarter about this, find a way to sync only when required and only from repos ahead of us
    // TODO: SJ: Consider adding a column to the cluster table indicating where every node is and sync from latest
    protected boolean isSyncRequiredInternal(String siteId, String siteDatabaseLastCommitId) {
        return true;
    }

    protected boolean createSiteInternal(String siteId, String siteUuid, String searchEngine) {
        boolean result = true;

        if (result) {
            try {
                logger.debug("Create " + PUBLISHED.name() + " repository from remote for site " + siteId);
                result = createSiteFromRemote(PUBLISHED);
                if (result) {
                    createdSites.add(siteId);
                }
            } catch (InvalidRemoteRepositoryException | InvalidRemoteRepositoryCredentialsException |
                    RemoteRepositoryNotFoundException | ServiceLayerException | CryptoException  e) {
                logger.error("Error while creating site on cluster node for site : " + siteId +
                        ". Rolling back.", e);
                result = false;
            }

            if (!result) {
                createdSites.remove(siteId);
                remotesMap.remove(siteId);
                contentRepository.deleteSite(siteId);
            }
        }

        return result;
    }

    // Published never clones, instead, it lets the sanbox process handle that. Return true.
    protected boolean cloneSiteInternal(String siteId)
            throws CryptoException, ServiceLayerException {
        // Clone from the first node in the cluster (it doesn't matter which one to clone from, so pick the first)
        // we will eventually to catch up to the latest
        boolean cloned = false;
        int idx = 0;
        while (!cloned && idx < clusterNodes.size()) {
            ClusterMember remoteNode = clusterNodes.get(idx++);
            logger.debug("Cloning " + PUBLISHED.toString() + " repository for site " + siteId +
                    " from " + remoteNode.getLocalAddress());

            // prepare a new folder for the cloned repository
            Path siteSandboxPath = buildRepoPath(PUBLISHED);
            File localPath = siteSandboxPath.toFile();
            localPath.delete();
            // then clone
            logger.debug("Cloning from " + remoteNode.getGitUrl() + " to " + localPath);
            CloneCommand cloneCommand = Git.cloneRepository();
            Git cloneResult = null;

            try {
                final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
                logger.debug("Add user credentials if provided");

                configureAuthenticationForCommand(remoteNode, cloneCommand, tempKey);

                String cloneUrl = remoteNode.getGitUrl().replace("{siteId}", siteId);
                cloneUrl = cloneUrl + "/" + studioConfiguration.getProperty(PUBLISHED_PATH);

                logger.debug("Executing clone command");
                cloneResult = cloneCommand
                        .setURI(cloneUrl)
                        .setRemote(remoteNode.getGitRemoteName())
                        .setDirectory(localPath)
                        .setCloneAllBranches(true)
                        .call();
                Files.deleteIfExists(tempKey);

                logger.debug("If cloned repo was published repo, than add local sandbox as origin");
                try {
                    addOriginRemote();
                } catch (InvalidRemoteUrlException e) {
                    logger.error("Failed to add sandbox as origin");
                }
                cloned = true;

            } catch (InvalidRemoteException e) {
                logger.error("Invalid remote repository: " + remoteNode.getGitRemoteName() +
                        " (" + remoteNode.getGitUrl() + ")", e);
            } catch (TransportException e) {
                if (StringUtils.endsWithIgnoreCase(e.getMessage(), "not authorized")) {
                    logger.error("Bad credentials or read only repository: " + remoteNode.getGitRemoteName() +
                            " (" + remoteNode.getGitUrl() + ")", e);
                } else {
                    logger.error("Remote repository not found: " + remoteNode.getGitRemoteName() +
                            " (" + remoteNode.getGitUrl() + ")", e);
                }
            } catch (GitAPIException | IOException e) {
                logger.error("Error while creating repository for site with path" + siteSandboxPath.toString(), e);
            } finally {
                if (cloneResult != null) {
                    cloneResult.close();
                }
            }
        }
        return cloned;
    }

    protected void addOriginRemote() throws IOException, InvalidRemoteUrlException, ServiceLayerException {
        logger.debug("Add sandbox as origin to published repo");
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repo = builder
                .setGitDir(buildRepoPath(PUBLISHED).resolve(GIT_ROOT).toFile())
                .readEnvironment()
                .findGitDir()
                .build();
        // Build a path for the site/sandbox
        Path siteSandboxPath = buildRepoPath(SANDBOX);
        // Built a path for the site/published
        Path sitePublishedPath = buildRepoPath(PUBLISHED);
        String remoteUrl = sitePublishedPath.relativize(siteSandboxPath).toString();
        try (Git git = new Git(repo)) {

            Config storedConfig = repo.getConfig();
            Set<String> remotes = storedConfig.getSubsections(CONFIG_SECTION_REMOTE);

            if (remotes.contains(DEFAULT_REMOTE_NAME)) {
                return;
            }

            RemoteAddCommand remoteAddCommand = git.remoteAdd();
            remoteAddCommand.setName(DEFAULT_REMOTE_NAME);
            remoteAddCommand.setUri(new URIish(remoteUrl));
            remoteAddCommand.call();
        } catch (URISyntaxException e) {
            logger.error("Remote URL is invalid " + remoteUrl, e);
            throw new InvalidRemoteUrlException();
        } catch (GitAPIException e) {
            logger.error("Error while adding remote " + DEFAULT_REMOTE_NAME + " (url: " + remoteUrl + ") for site " +
                    siteId, e);
            throw new ServiceLayerException("Error while adding remote " + DEFAULT_REMOTE_NAME + " (url: " + remoteUrl +
                    ") for site " + siteId, e);
        }
    }

    protected void updateContentInternal(String siteId, String lastCommitId) throws IOException, CryptoException,
                                                                                    ServiceLayerException {
        logger.debug("Update published repo for site " + siteId);
        Path siteSandboxPath = buildRepoPath(PUBLISHED).resolve(GIT_ROOT);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repo = builder
                .setGitDir(siteSandboxPath.toFile())
                .readEnvironment()
                .findGitDir()
                .build();

        try (Git git = new Git(repo)) {
            Set<String> environments = getAllPublishingEnvironments(siteId);
            logger.debug("Update published repo from all active cluster members");
            for (ClusterMember remoteNode : clusterNodes) {
                try {
                    logger.debug("Fetch from cluster member " + remoteNode.getLocalAddress());
                    final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
                    FetchCommand fetch = git.fetch().setRemote(remoteNode.getGitRemoteName());
                    fetch = configureAuthenticationForCommand(remoteNode, fetch, tempKey);
                    fetch.call();
                    Files.delete(tempKey);
                } catch (GitAPIException e) {
                    logger.error("Error while fetching published repo for site " + siteId + " from remote " +
                            remoteNode.getGitRemoteName());
                    logger.error(e.getMessage());
                }
                logger.debug("Update all environments for site " + siteId + " from cluster member " +
                        remoteNode.getLocalAddress());
                for (String branch : environments) {
                    try {
                        updatePublishedBranch(git, remoteNode, branch);
                    } catch (GitAPIException e) {
                        logger.error("Error while updating published repo for site " + siteId + " from remote " +
                                remoteNode.getGitRemoteName() + " environment " + branch);
                        logger.error(e.getMessage());
                    }
                }
            }
        }

    }

    @RetryingOperation
    public void updatePublishedBranch(Git git, ClusterMember remoteNode, String branch) throws CryptoException,
            GitAPIException, IOException, ServiceLayerException {
        logger.debug("Update published environment " + branch + " from " + remoteNode.getLocalAddress() +
                " for site " + siteId);
        final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");

        Repository repo = git.getRepository();
        Ref ref = repo.exactRef(Constants.R_HEADS + branch);
        boolean createBranch = (ref == null);

        logger.debug("Checkout " + branch);
        CheckoutCommand checkoutCommand = git.checkout()
                .setName(branch)
                .setCreateBranch(createBranch);
        if (createBranch) {
            checkoutCommand.setStartPoint(remoteNode.getGitRemoteName() + "/" + branch);
        }
        checkoutCommand.call();

        FetchCommand fetchCommand = git.fetch().setRemote(remoteNode.getGitRemoteName());
        fetchCommand = configureAuthenticationForCommand(remoteNode, fetchCommand, tempKey);
        FetchResult fetchResult = fetchCommand.call();

        ObjectId commitToMerge;
        Ref r;
        if (fetchResult != null) {
            r = fetchResult.getAdvertisedRef(branch);
            if (r == null) {
                r = fetchResult.getAdvertisedRef(Constants.R_HEADS + branch);
            }
            if (r != null) {
                commitToMerge = r.getObjectId();

                MergeCommand mergeCommand = git.merge();
                mergeCommand.setMessage(studioConfiguration.getProperty(REPO_SYNC_DB_COMMIT_MESSAGE_NO_PROCESSING));
                mergeCommand.setCommit(true);
                mergeCommand.include(remoteNode.getGitRemoteName(), commitToMerge);
                mergeCommand.setStrategy(MergeStrategy.THEIRS);
                mergeCommand.call();
            }
        }

        Files.delete(tempKey);
    }

    private Set<String> getAllPublishingEnvironments(String site) {
        Set<String> environments = new HashSet<String>();
        environments.add(servicesConfig.getLiveEnvironment(site));
        if (servicesConfig.isStagingEnvironmentEnabled(site)) {
            environments.add(servicesConfig.getStagingEnvironment(site));
        }
        return environments;
    }

    protected boolean checkIfSiteRepoExistsInternal() {
        boolean toRet = false;
        if (createdSites.contains(siteId)) {
            toRet = true;
        } else {
            String firstCommitId = contentRepository.getRepoFirstCommitId(siteId);
            if (!StringUtils.isEmpty(firstCommitId)) {
                toRet = true;
                createdSites.add(siteId);
            } else {
                Repository repo = null;
                FileRepositoryBuilder builder = new FileRepositoryBuilder();
                try {
                    repo = builder
                            .setMustExist(true)
                            .setGitDir(buildRepoPath(PUBLISHED).resolve(GIT_ROOT).toFile())
                            .readEnvironment()
                            .findGitDir()
                            .build();
                } catch (IOException e) {
                    logger.info("Failed to open PUBLISHED repo for site " + siteId);
                }
                toRet = Objects.nonNull(repo) && repo.getObjectDatabase().exists();
            }
        }
        return toRet;
    }

    @Override
    protected void addRemotesInternal() throws InvalidRemoteUrlException, ServiceLayerException, CryptoException {
        Map<String, String> existingRemotes = remotesMap.get(siteId);
        logger.debug("Add cluster members as remotes to local published repository");
        for (ClusterMember member : clusterNodes) {
            if (existingRemotes != null && existingRemotes.containsKey(member.getGitRemoteName())) {
                continue;
            }

            try {
                if (existingRemotes == null) {
                    existingRemotes = new HashMap<String, String>();
                    remotesMap.put(siteId, existingRemotes);
                }

                String remoteUrl = member.getGitUrl().replace("{siteId}", siteId) + "/" +
                        studioConfiguration.getProperty(PUBLISHED_PATH);
                addRemoteRepository(member, remoteUrl, PUBLISHED);

                existingRemotes.put(member.getGitRemoteName(), StringUtils.EMPTY);

            } catch (IOException e) {
                logger.error("Failed to open repository", e);
            }
        }
    }
}
