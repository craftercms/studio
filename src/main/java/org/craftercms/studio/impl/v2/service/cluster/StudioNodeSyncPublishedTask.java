/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */

package org.craftercms.studio.impl.v2.service.cluster;

import static org.craftercms.studio.api.v1.constant.GitRepositories.PUBLISHED;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_CIPHER_KEY;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_CIPHER_SALT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.commons.crypto.impl.PbkAesTextEncryptor;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.to.PublishingTargetTO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class StudioNodeSyncPublishedTask extends StudioNodeSyncBaseTask {

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

    protected boolean createSiteInternal(String siteId) {
        return false;
    }

    // Published never clones, instead, it lets the sanbox process handle that. Return true.
    protected boolean cloneSiteInternal(String siteId, GitRepositories repoType)throws CryptoException, ServiceLayerException, InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException {
        return true;
    }

    protected void updateContentInternal(String siteId, String lastCommitId) throws IOException, CryptoException, ServiceLayerException {
        logger.debug("Update published repo for site " + siteId);
        boolean toRet = true;
        Path siteSandboxPath = buildRepoPath(PUBLISHED).resolve(GIT_ROOT);
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

            Set<String> environments = getAllPublishingEnvironments(siteId);
            logger.debug("Update published repo from all active cluster members");
            for (ClusterMember remoteNode : clusterNodes) {
                String remoteLastSyncCommit = remoteLastSyncCommits.get(remoteNode.getGitRemoteName());
                if (StringUtils.isEmpty(remoteLastSyncCommit) ||
                        !StringUtils.equals(lastCommitId, remoteLastSyncCommit)) {
                    logger.debug("Fetch from cluster member " + remoteNode.getLocalIp());
                    final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
                    FetchCommand fetch = git.fetch().setRemote(remoteNode.getGitRemoteName());
                    fetch = setAuthenticationForCommand(remoteNode, fetch, tempKey);
                    fetch.call();
                    Files.delete(tempKey);

                    logger.debug("Update all environments for site " + siteId + " from cluster member " +
                            remoteNode.getLocalIp());
                    for (String branch : environments) {
                        updatePublishedBranch(git, remoteNode, branch);
                    }
                    remoteLastSyncCommits.put(remoteNode.getGitRemoteName(), lastCommitId);
                }
            }
        } catch (GitAPIException e) {
            logger.error("Error while updating published repo for site " + siteId);
        }

    }

    private void updatePublishedBranch(Git git, ClusterMember remoteNode, String branch) throws CryptoException,
            GitAPIException, IOException, ServiceLayerException {
        logger.debug("Update published environment " + branch + " from " + remoteNode.getLocalIp() +
                " for site " + siteId);
        final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");

        TextEncryptor encryptor = new PbkAesTextEncryptor(studioConfiguration.getProperty(SECURITY_CIPHER_KEY),
                studioConfiguration.getProperty(SECURITY_CIPHER_SALT));

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

        logger.debug("Pull from remote " + remoteNode.getLocalIp());
        PullCommand pullCommand = git.pull();
        logger.debug("Set remote " + remoteNode.getGitUrl());
        pullCommand.setRemote(remoteNode.getGitRemoteName());
        pullCommand.setStrategy(MergeStrategy.THEIRS);
        pullCommand = setAuthenticationForCommand(remoteNode, pullCommand, tempKey);
        pullCommand.call();
        Files.delete(tempKey);
    }

    private Set<String> getAllPublishingEnvironments(String site) {
        Set<String> environments = new HashSet<String>();
        if (servicesConfig.isStagingEnvironmentEnabled(site)) {
            environments.add(servicesConfig.getLiveEnvironment(site));
            environments.add(servicesConfig.getStagingEnvironment(site));
        } else {
            List<PublishingTargetTO> publishingTargets = siteService.getPublishingTargetsForSite(site);

            if (publishingTargets != null && publishingTargets.size() > 0) {
                for (PublishingTargetTO target : publishingTargets) {
                    if (StringUtils.isNotEmpty(target.getRepoBranchName())) {
                        environments.add(target.getRepoBranchName());
                    }
                }
            }
        }
        return environments;
    }
}
