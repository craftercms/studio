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

package org.craftercms.studio.impl.v2.service.cluster;

import static org.craftercms.studio.api.v1.constant.GitRepositories.PUBLISHED;
import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.REPO_SANDBOX_BRANCH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.REPO_SYNC_DB_COMMIT_MESSAGE_NO_PROCESSING;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_CIPHER_KEY;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_CIPHER_SALT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;

public class StudioNodeSyncSandboxTask extends StudioNodeSyncBaseTask {

    private static final Logger logger = LoggerFactory.getLogger(StudioNodeSyncSandboxTask.class);
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

    // Check if the site's commit id is behind the database, if so, it means the site on local disk needs to be sync'd
    protected boolean isSyncRequiredInternal(String siteId, String siteDatabaseLastCommitId) {
        boolean syncRequired = true;

        // Get the repo's last commit id and compare it to the database
        String lastCommitIdRepo = contentRepository.getRepoLastCommitId(siteId);
            
        if (StringUtils.isNotEmpty(siteDatabaseLastCommitId) && StringUtils.equals(lastCommitIdRepo, siteDatabaseLastCommitId)) {
            syncRequired = false;
        }
  
        return syncRequired;
    }

    protected boolean createSiteInternal(String siteId, String siteUuid, String searchEngine) {
        boolean result = true;

        logger.debug("Create Deployer targets site " + siteId);
        try {
            deployer.createTargets(siteId, searchEngine);
        } catch (Exception e) {
            result = false;
            logger.error("Error while creating Deployer targets on cluster node for site : " + siteId, e);
        }


        if (result) {
            try {
                logger.debug("Create site from remote for site " + siteId);
                result = createSiteFromRemote();
                if (result) {
                    addSiteUuidFile(siteId, siteUuid);
                    deploymentService.syncAllContentToPreview(siteId, true);
                    createdSites.add(siteId);
                }
            } catch (InvalidRemoteRepositoryException | InvalidRemoteRepositoryCredentialsException |
                    RemoteRepositoryNotFoundException | ServiceLayerException | CryptoException |IOException e) {
                logger.error("Error while creating site on cluster node for site : " + siteId +
                        ". Rolling back.", e);
                result = false;
            }

            if (!result) {
                createdSites.remove(siteId);
                contentRepository.deleteSite(siteId);

                try {
                    deployer.deleteTargets(siteId);
                } catch (Exception e) {
                    logger.error("Error while rolling back/deleting site: " + siteId + " ID: " + siteId +
                                 " on cluster node. This means the site's Deployer targets are still " +
                                 "present, but the site was not successfully created.");
                }
            }
        }

        return result;
    }

    protected void updateContentInternal(String siteId, String lastCommitId) throws IOException, CryptoException, ServiceLayerException {
        logger.debug("Update sandbox for site " + siteId);

        Path siteSandboxPath = buildRepoPath(SANDBOX).resolve(GIT_ROOT);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repo = builder
                .setGitDir(siteSandboxPath.toFile())
                .readEnvironment()
                .findGitDir()
                .build();

        Map<String, String> remoteLastSyncCommits = remotesMap.get(siteId);
        if (remoteLastSyncCommits == null || remoteLastSyncCommits.isEmpty()) {
            remoteLastSyncCommits = new HashMap<String, String>();
            remotesMap.put(siteId, remoteLastSyncCommits);
        }
        try (Git git = new Git(repo)) {
            logger.debug("Update content from each active cluster memeber");
            for (ClusterMember remoteNode : clusterNodes) {
                String remoteLastSyncCommit = remoteLastSyncCommits.get(remoteNode.getGitRemoteName());
                if (StringUtils.isEmpty(remoteLastSyncCommit) ||
                        !StringUtils.equals(lastCommitId, remoteLastSyncCommit)) {
                    updateBranch(git, remoteNode);
                    remoteLastSyncCommits.put(remoteNode.getGitRemoteName(), lastCommitId);
                }
            }
        } catch (GitAPIException e) {
            logger.error("Error while syncing cluster node content for site " + siteId);
        }
    }

    protected boolean cloneSiteInternal(String siteId, GitRepositories repoType)
            throws CryptoException, ServiceLayerException {
        // Clone from the first node in the cluster (it doesn't matter which one to clone from, so pick the first)
        // we will eventually to catch up to the latest
        boolean cloned = false;
        int idx = 0;
        while (!cloned && idx < clusterNodes.size()) {
            ClusterMember remoteNode = clusterNodes.get(idx++);
            logger.debug("Cloning " + repoType.toString() + " repository for site " + siteId +
                         " from " + remoteNode.getLocalAddress());

            // prepare a new folder for the cloned repository
            Path siteSandboxPath = buildRepoPath(repoType);
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
                switch (repoType) {
                    case SANDBOX:
                        cloneUrl = cloneUrl + "/" + studioConfiguration.getProperty(StudioConfiguration.SANDBOX_PATH);
                        break;
                    case PUBLISHED:
                        cloneUrl = cloneUrl + "/" + studioConfiguration.getProperty(StudioConfiguration.PUBLISHED_PATH);
                        break;
                    default:
                }

                logger.debug("Executing clone command");
                cloneResult = cloneCommand
                        .setURI(cloneUrl)
                        .setRemote(remoteNode.getGitRemoteName())
                        .setDirectory(localPath)
                        .setCloneAllBranches(true)
                        .call();
                Files.deleteIfExists(tempKey);

                logger.debug("If cloned repo was published repo, than add local sandbox as origin");
                if (repoType.equals(PUBLISHED)) {
                    try {
                        addOriginRemote();
                    } catch (InvalidRemoteUrlException e) {
                        logger.error("Failed to add sandbox as origin");
                    }
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

    private void updateBranch(Git git, ClusterMember remoteNode) throws CryptoException, GitAPIException,
            IOException, ServiceLayerException {
        final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
        FetchCommand fetchCommand = git.fetch().setRemote(remoteNode.getGitRemoteName());
        fetchCommand = configureAuthenticationForCommand(remoteNode, fetchCommand, tempKey);
        FetchResult fetchResult = fetchCommand.call();

        ObjectId commitToMerge;
        Ref r;
        if (fetchResult != null) {
            r = fetchResult.getAdvertisedRef(REPO_SANDBOX_BRANCH);
            if (r == null) {
                r = fetchResult.getAdvertisedRef(Constants.R_HEADS +
                        studioConfiguration.getProperty(REPO_SANDBOX_BRANCH));
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

    private void addSiteUuidFile(String site, String siteUuid) throws IOException {
        Path path = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), site,
                StudioConstants.SITE_UUID_FILENAME);
        String toWrite = StudioConstants.SITE_UUID_FILE_COMMENT + "\n" + siteUuid;
        Files.write(path, toWrite.getBytes());
    }

}
