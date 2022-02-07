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

package org.craftercms.studio.impl.v2.service.cluster;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.utils.GitUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.DeleteBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CLUSTER_MEMBER_LOCAL_ADDRESS;
import static org.craftercms.studio.api.v1.constant.StudioConstants.DEFAULT_PUBLISHING_LOCK_OWNER_ID;
import static org.craftercms.studio.api.v1.constant.StudioConstants.GLOBAL_REPOSITORY_GIT_LOCK;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_LOCAL_ADDRESS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.CLUSTER_STATE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CLUSTERING_NODE_REGISTRATION;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.PUBLISHING_SITE_LOCK_TTL;

public class StudioClusterUtils {

    private static final Logger logger = LoggerFactory.getLogger(StudioClusterUtils.class);

    private TextEncryptor encryptor;
    private ClusterDAO clusterDao;
    private StudioConfiguration studioConfiguration;
    private RetryingRepositoryOperationFacade retryingRepositoryOperationFacade;
    private GeneralLockService generalLockService;
    private GitRepositoryHelper helper;

    public HierarchicalConfiguration<ImmutableNode> getClusterConfiguration() {
        return studioConfiguration.getSubConfig(CLUSTERING_NODE_REGISTRATION);
    }

    public String getClusterNodeLocalAddress() {
        HierarchicalConfiguration<ImmutableNode> registrationData = getClusterConfiguration();
        String localAddress = StringUtils.EMPTY;
        if (registrationData != null && !registrationData.isEmpty()) {
            localAddress = registrationData.getString(CLUSTER_MEMBER_LOCAL_ADDRESS);
        }
        return localAddress;
    }

    public List<ClusterMember> getClusterNodes(String localAddress) {
        Map<String, String> params = new HashMap<String, String>();
        params.put(CLUSTER_LOCAL_ADDRESS, localAddress);
        params.put(CLUSTER_STATE, ClusterMember.State.ACTIVE.toString());
        return clusterDao.getOtherMembers(params);
    }

    public void removeRemote(Git git, String remoteName) throws GitAPIException {
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
    }

    public String getLockOwnerId() {
        HierarchicalConfiguration<ImmutableNode> clusterConfig =
                studioConfiguration.getSubConfig(CLUSTERING_NODE_REGISTRATION);
        String clusterNodeId = StringUtils.EMPTY;
        if (Objects.nonNull(clusterConfig)) {
            clusterNodeId = clusterConfig.getString(CLUSTER_MEMBER_LOCAL_ADDRESS);
        }
        if  (StringUtils.isEmpty(clusterNodeId)) {
            try {
                clusterNodeId = InetAddress.getLocalHost().toString();
            } catch (UnknownHostException e) {
                clusterNodeId = DEFAULT_PUBLISHING_LOCK_OWNER_ID;
            }
        }
        return clusterNodeId;
    }

    public boolean cloneGlobalRepository(List<ClusterMember> clusterNodes)
            throws CryptoException, ServiceLayerException, InvalidRemoteRepositoryCredentialsException,
                    RemoteRepositoryNotFoundException, IOException {
        // Clone from the first node in the cluster (it doesn't matter which one to clone from, so pick the first)
        // we will eventually to catch up to the latest
        boolean cloned = false;
        int idx = 0;
        String gitLockKey = GLOBAL_REPOSITORY_GIT_LOCK;
        if (generalLockService.tryLock(gitLockKey)) {
            try {
                while (!cloned && idx < clusterNodes.size()) {
                    ClusterMember remoteNode = clusterNodes.get(idx++);
                    logger.debug("Cloning global repository from " + remoteNode.getLocalAddress());

                    // prepare a new folder for the cloned repository
                    Path siteSandboxPath = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                            studioConfiguration.getProperty(StudioConfiguration.GLOBAL_REPO_PATH));
                    File localPath = siteSandboxPath.toFile();
                    localPath.delete();
                    // then clone
                    logger.debug("Cloning from " + remoteNode.getGitUrl() + " to " + localPath);
                    CloneCommand cloneCommand = Git.cloneRepository();
                    Git cloneResult = null;

                    Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
                    try {
                        logger.debug("Add user credentials if provided");
                        helper.setAuthenticationForCommand(cloneCommand, remoteNode.getGitAuthType(),
                                remoteNode.getGitUsername(), remoteNode.getGitPassword(), remoteNode.getGitToken(),
                                remoteNode.getGitPrivateKey(), tempKey, true);

                        String cloneUrl = remoteNode.getGitUrl().replace("/sites/{siteId}", "/global");

                        logger.debug("Executing clone command");
                        cloneResult = cloneCommand
                                .setURI(cloneUrl)
                                .setRemote(remoteNode.getGitRemoteName())
                                .setDirectory(localPath)
                                .setCloneAllBranches(true)
                                .call();
                        cloned = true;

                    } catch (InvalidRemoteException e) {
                        logger.error("Invalid remote repository: " + remoteNode.getGitRemoteName() +
                                " (" + remoteNode.getGitUrl() + ")", e);
                    } catch (TransportException e) {
                        GitUtils.translateException(e, logger, remoteNode.getGitRemoteName(), remoteNode.getGitUrl(),
                                remoteNode.getGitUsername());
                    } catch (GitAPIException | IOException e) {
                        logger.error("Error while creating repository for site with path" + siteSandboxPath, e);
                    } finally {
                        Files.deleteIfExists(tempKey);
                        if (cloneResult != null) {
                            cloneResult.close();
                        }
                    }
                }
            } finally {
                generalLockService.unlock(gitLockKey);
            }
        } else {
            logger.debug("Failed to get lock " + gitLockKey);
        }
        return cloned;
    }

    public int getLockTTL() {
        return studioConfiguration.getProperty(PUBLISHING_SITE_LOCK_TTL, Integer.class);
    }

    public TextEncryptor getEncryptor() {
        return encryptor;
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

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public RetryingRepositoryOperationFacade getRetryingRepositoryOperationFacade() {
        return retryingRepositoryOperationFacade;
    }

    public void setRetryingRepositoryOperationFacade(RetryingRepositoryOperationFacade retryingRepositoryOperationFacade) {
        this.retryingRepositoryOperationFacade = retryingRepositoryOperationFacade;
    }

    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public void setHelper(GitRepositoryHelper helper) {
        this.helper = helper;
    }

}
