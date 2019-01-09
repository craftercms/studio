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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import com.jcraft.jsch.Session;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.commons.crypto.impl.PbkAesTextEncryptor;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.dal.RemoteRepository;
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
import org.eclipse.jgit.api.TransportConfigCallback;
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
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

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

    protected boolean createSiteInternal(String siteId, String searchEngine) {
        boolean result = true;

        logger.debug("Create preview deployer target site " + siteId);
        try {
            result = previewDeployer.createTarget(siteId, searchEngine);
        } catch (Exception e) {
            result = false;
            logger.error("Error while creating preview deployer target on cluster node for site : "
                    + siteId + ". Is the Preview Deployer running and configured correctly in " +
                    "Studio cluster node?", e);
        }


        if (result) {
            try {
                logger.debug("Create site from remote for site " + siteId);
                createSiteFromRemote();
                createdSites.add(siteId);
            } catch (InvalidRemoteRepositoryException | InvalidRemoteRepositoryCredentialsException | RemoteRepositoryNotFoundException | ServiceLayerException | CryptoException e) {
                logger.error("Error while creating site on cluster node for site : " + siteId +
                        ". Rolling back.", e);
                result = false;
            }

            if (!result) {
                contentRepository.deleteSite(siteId);

                boolean deleted = previewDeployer.deleteTarget(siteId);
                if (!deleted) {
                    logger.error("Error while rolling back/deleting site: " + siteId + " ID: " + siteId +
                            " on cluster node. This means the site's preview deployer target is still " +
                            "present, but the site is not successfully created.");
                }
            }
        }

        return result;
    }

    protected void updateContentInternal(String siteId, String lastCommitId) throws IOException, CryptoException, ServiceLayerException {
        logger.debug("Update sandbox for site " + siteId);
        boolean toRet = true;
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

    protected boolean cloneSiteInternal(String siteId, GitRepositories repoType)throws CryptoException, ServiceLayerException, InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException {
        // Clone from the first node in the cluster (it doesn't matter which one to clone from, so pick the first)
        // we will eventually to catch up to the latest
        ClusterMember remoteNode = clusterNodes.get(0);
        logger.debug("Cloning " + repoType.toString() + " repository for site " + siteId +
                " from " + remoteNode.getLocalAddress());
        boolean toRet = true;
        TextEncryptor encryptor = new PbkAesTextEncryptor(studioConfiguration.getProperty(SECURITY_CIPHER_KEY),
                studioConfiguration.getProperty(SECURITY_CIPHER_SALT));
        // prepare a new folder for the cloned repository
        Path siteSandboxPath = buildRepoPath(repoType);
        File localPath = siteSandboxPath.toFile();
        localPath.delete();
        // then clone
        logger.debug("Cloning from " + remoteNode.getGitUrl() + " to " + localPath);
        CloneCommand cloneCommand = Git.cloneRepository();
        Git cloneResult = null;

        try {
            final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(),".tmp");
            logger.debug("Add user credentials if provided");
            switch (remoteNode.getGitAuthType()) {
                case RemoteRepository.AuthenticationType.NONE:
                    logger.debug("No authentication");
                    break;
                case RemoteRepository.AuthenticationType.BASIC:
                    logger.debug("Basic authentication");
                    String hashedPassword = remoteNode.getGitPassword();
                    String password = encryptor.decrypt(hashedPassword);
                    UsernamePasswordCredentialsProvider credentialsProviderUP =
                            new UsernamePasswordCredentialsProvider(remoteNode.getGitUsername(), password);
                    cloneCommand.setTransportConfigCallback(new TransportConfigCallback() {
                        @Override
                        public void configure(Transport transport) {
                            SshTransport sshTransport = (SshTransport)transport;
                            ((SshTransport) transport).setSshSessionFactory(new JschConfigSessionFactory() {
                                @Override
                                protected void configure(OpenSshConfig.Host host, Session session) {
                                    Properties config = new Properties();
                                    config.put("StrictHostKeyChecking", "no");
                                    session.setConfig(config);
                                    session.setPassword(password);
                                }
                            });
                        }
                    });
                    cloneCommand.setCredentialsProvider(credentialsProviderUP);
                    break;
                case RemoteRepository.AuthenticationType.TOKEN:
                    logger.debug("Token based authentication");
                    String hashedToken = remoteNode.getGitToken();
                    String token = encryptor.decrypt(hashedToken);
                    UsernamePasswordCredentialsProvider credentialsProvider =
                            new UsernamePasswordCredentialsProvider(token, StringUtils.EMPTY);
                    cloneCommand.setTransportConfigCallback(new TransportConfigCallback() {
                        @Override
                        public void configure(Transport transport) {
                            SshTransport sshTransport = (SshTransport)transport;
                            ((SshTransport) transport).setSshSessionFactory(new JschConfigSessionFactory() {
                                @Override
                                protected void configure(OpenSshConfig.Host host, Session session) {
                                    Properties config = new Properties();
                                    config.put("StrictHostKeyChecking", "no");
                                    session.setConfig(config);
                                }
                            });
                        }
                    });
                    cloneCommand.setCredentialsProvider(credentialsProvider);
                    break;
                case RemoteRepository.AuthenticationType.PRIVATE_KEY:
                    logger.debug("Private key authentication");
                    String hashedPrivateKey = remoteNode.getGitPrivateKey();
                    String privateKey = encryptor.decrypt(hashedPrivateKey);
                    tempKey.toFile().deleteOnExit();
                    cloneCommand.setTransportConfigCallback(new TransportConfigCallback() {
                        @Override
                        public void configure(Transport transport) {
                            SshTransport sshTransport = (SshTransport)transport;
                            sshTransport.setSshSessionFactory(getSshSessionFactory(privateKey, tempKey));
                        }
                    });

                    break;
                default:
                    throw new ServiceLayerException("Unsupported authentication type " + remoteNode.getGitAuthType());
            }
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

        } catch (InvalidRemoteException e) {
            logger.error("Invalid remote repository: " + remoteNode.getGitRemoteName() +
                    " (" + remoteNode.getGitUrl() + ")", e);
            throw new InvalidRemoteRepositoryException("Invalid remote repository: " +
                    remoteNode.getGitRemoteName() + " (" + remoteNode.getGitUrl() + ")");
        } catch (TransportException e) {
            if (StringUtils.endsWithIgnoreCase(e.getMessage(), "not authorized")) {
                logger.error("Bad credentials or read only repository: " + remoteNode.getGitRemoteName() +
                                " (" + remoteNode.getGitUrl() + ")", e);
                throw new InvalidRemoteRepositoryCredentialsException("Bad credentials or read only repository: " +
                        remoteNode.getGitRemoteName() + " (" + remoteNode.getGitUrl() + ") for username "
                        + remoteNode.getGitUsername(), e);
            } else {
                logger.error("Remote repository not found: " + remoteNode.getGitRemoteName() +
                                " (" + remoteNode.getGitUrl() + ")",  e);
                throw new RemoteRepositoryNotFoundException("Remote repository not found: " +
                        remoteNode.getGitRemoteName() + " (" + remoteNode.getGitUrl() + ")");
            }
        } catch (GitAPIException | IOException e) {
            logger.error("Error while creating repository for site with path" + siteSandboxPath.toString(), e);
            toRet = false;
        } finally {
            if (cloneResult != null) {
                cloneResult.close();
            }
        }
        return toRet;
    }

    private void updateBranch(Git git, ClusterMember remoteNode) throws CryptoException, GitAPIException,
            IOException, ServiceLayerException {
        TextEncryptor encryptor = new PbkAesTextEncryptor(studioConfiguration.getProperty(SECURITY_CIPHER_KEY),
                studioConfiguration.getProperty(SECURITY_CIPHER_SALT));
        final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
        FetchCommand fetchCommand = git.fetch().setRemote(remoteNode.getGitRemoteName());
        fetchCommand = setAuthenticationForCommand(remoteNode, fetchCommand, tempKey);
        FetchResult fetchResult = fetchCommand.call();

        ObjectId commitToMerge = null;
        Ref r = null;
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
}
