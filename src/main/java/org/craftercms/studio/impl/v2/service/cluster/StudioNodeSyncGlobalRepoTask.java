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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.commons.crypto.impl.PbkAesTextEncryptor;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteSetUrlCommand;
import org.eclipse.jgit.api.TransportCommand;
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
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_SYNC_DB_COMMIT_MESSAGE_NO_PROCESSING;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_CIPHER_KEY;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_CIPHER_SALT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_URL;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_SECTION_REMOTE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;

public class StudioNodeSyncGlobalRepoTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(StudioNodeSyncGlobalRepoTask.class);

    private static final String NON_SSH_GIT_URL_REGEX = "(file|https?|git)://.+";

    private static ReentrantLock singleWorkerLock = new ReentrantLock();
    private static Map<String, String> existingRemotes = new HashMap<String, String>();

    private List<ClusterMember> clusterNodes;
    private ContentRepository contentRepository;
    private StudioConfiguration studioConfiguration;

    @Override
    public void run() {
        logger.error("Starting Cluster Node Sync Global repo task");

        // Lock site and begin sync
        if (singleWorkerLock.tryLock()) {
            // Log start time
            long startTime = System.currentTimeMillis();
            logger.debug("Worker starts syncing cluster node global repo");
            try {
                // Check if repo exists
                logger.debug("Check if global repository exists");
                boolean success = true;

                if (!checkIfRepoExists()) {
                    // Site doesn't exist locally, create it
                    success = cloneRepository();
                }

                if (success) {
                        try {
                            // Add the remote repositories to the local repository to sync from if not added already
                            logger.debug("Add remotes for global repository");
                            addRemotes();

                        } catch (InvalidRemoteUrlException | ServiceLayerException | CryptoException e) {
                            logger.error("Error while adding remotes on cluster node for global repo", e);
                        }

                        try {
                            // Sync with remote and update the local cache with the last commit ID to speed things up
                            logger.debug("Update content for global repo");
                            updateContent();
                        } catch (IOException | CryptoException | ServiceLayerException e) {
                            logger.error("Error while updating content for global repo on cluster node.", e);
                        }

                }
            } catch (ServiceLayerException | CryptoException e) {
                logger.error("Error while cloning global repository from other nodes", e);
            } finally {
                if (singleWorkerLock != null) {
                    singleWorkerLock.unlock();
                }
            }

            // Compute execution duration and log it
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Worker finished syncing cluster node for global repo");
            logger.debug("Worker performed cluster node sync for global repo in " + duration + "ms");
        } else {
            // Couldn't get the site lock, another worker is active, abandoning this cycle
            logger.debug("Unable to get cluster lock, another worker is holding the lock for global repo");
        }
        logger.debug("Finished Cluster Node Sync task for global repo");
    }

    private boolean checkIfRepoExists() {
        String firstCommitId = contentRepository.getRepoFirstCommitId(StringUtils.EMPTY);
        if (!StringUtils.isEmpty(firstCommitId)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean cloneRepository()
            throws CryptoException, ServiceLayerException {
        // Clone from the first node in the cluster (it doesn't matter which one to clone from, so pick the first)
        // we will eventually to catch up to the latest
        boolean cloned = false;
        int idx = 0;
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

            try {
                final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
                logger.debug("Add user credentials if provided");

                configureAuthenticationForCommand(remoteNode, cloneCommand, tempKey);

                String cloneUrl = remoteNode.getGitUrl().replace("/sites/{siteId}", "/global");

                logger.debug("Executing clone command");
                cloneResult = cloneCommand
                        .setURI(cloneUrl)
                        .setRemote(remoteNode.getGitRemoteName())
                        .setDirectory(localPath)
                        .setCloneAllBranches(true)
                        .call();
                Files.deleteIfExists(tempKey);
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

    private <T extends TransportCommand> T configureAuthenticationForCommand(ClusterMember remoteNode, T gitCommand,
                                                                             final Path tempKey)
            throws CryptoException, IOException, ServiceLayerException {
        TextEncryptor encryptor = new PbkAesTextEncryptor(studioConfiguration.getProperty(SECURITY_CIPHER_KEY),
                studioConfiguration.getProperty(SECURITY_CIPHER_SALT));
        boolean sshProtocol = !remoteNode.getGitUrl().matches(NON_SSH_GIT_URL_REGEX);

        switch (remoteNode.getGitAuthType()) {
            case RemoteRepository.AuthenticationType.NONE:
                logger.debug("No authentication");
                break;
            case RemoteRepository.AuthenticationType.BASIC:
                logger.debug("Basic Authentication");
                configureBasicAuthentication(remoteNode, gitCommand, encryptor, sshProtocol);
                break;
            case RemoteRepository.AuthenticationType.TOKEN:
                logger.debug("Token based Authentication");
                configureTokenAuthentication(remoteNode, gitCommand, encryptor, sshProtocol);
                break;
            case RemoteRepository.AuthenticationType.PRIVATE_KEY:
                if (!sshProtocol) {
                    throw new ServiceLayerException("Can't do private key authentication with non-ssh URLs");
                }

                logger.debug("Private Key Authentication");
                configurePrivateKeyAuthentication(remoteNode, gitCommand, encryptor, tempKey);
                break;
            default:
                throw new ServiceLayerException("Unsupported authentication type " + remoteNode.getGitAuthType());
        }

        return gitCommand;
    }

    private <T extends TransportCommand> void configureBasicAuthentication(
            ClusterMember remoteNode, T gitCommand, TextEncryptor encryptor, boolean sshProtocol) throws CryptoException {
        String password = encryptor.decrypt(remoteNode.getGitPassword());
        UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                remoteNode.getGitUsername(), password);

        if (sshProtocol) {
            gitCommand.setTransportConfigCallback(transport -> {
                SshTransport sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(new StrictHostCheckingOffSshSessionFactory() {

                    @Override
                    protected void configure(OpenSshConfig.Host host, Session session) {
                        super.configure(host, session);
                        session.setPassword(password);
                    }

                });
            });
        }

        gitCommand.setCredentialsProvider(credentialsProvider);
    }

    private <T extends TransportCommand> void configureTokenAuthentication(
            ClusterMember remoteNode, T gitCommand, TextEncryptor encryptor, boolean sshProtocol) throws CryptoException {
        UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                encryptor.decrypt(remoteNode.getGitToken()), StringUtils.EMPTY);

        if (sshProtocol) {
            gitCommand.setTransportConfigCallback(transport -> {
                SshTransport sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(new StrictHostCheckingOffSshSessionFactory());
            });
        }

        gitCommand.setCredentialsProvider(credentialsProvider);
    }

    private <T extends TransportCommand> void configurePrivateKeyAuthentication(
            ClusterMember remoteNode, T gitCommand, TextEncryptor encryptor, final Path tempKey)
            throws CryptoException, IOException  {
        String privateKey = encryptor.decrypt(remoteNode.getGitPrivateKey());
        try {
            Files.write(tempKey, privateKey.getBytes());
        } catch (IOException e) {
            throw new IOException("Failed to write private key for SSH connection to temp location", e);
        }

        tempKey.toFile().deleteOnExit();

        gitCommand.setTransportConfigCallback(transport -> {
            SshTransport sshTransport = (SshTransport)transport;
            sshTransport.setSshSessionFactory(new StrictHostCheckingOffSshSessionFactory() {

                @Override
                protected JSch createDefaultJSch(FS fs) throws JSchException {
                    JSch defaultJSch = super.createDefaultJSch(fs);
                    defaultJSch.addIdentity(tempKey.toAbsolutePath().toString());
                    return defaultJSch;
                }

            });
        });
    }

    protected void addRemotes() throws InvalidRemoteUrlException, ServiceLayerException, CryptoException {
        TextEncryptor encryptor = new PbkAesTextEncryptor(studioConfiguration.getProperty(SECURITY_CIPHER_KEY),
                studioConfiguration.getProperty(SECURITY_CIPHER_SALT));
        logger.debug("Add cluster members as remotes to local sandbox repository");
        for (ClusterMember member : clusterNodes) {
            if (existingRemotes != null && existingRemotes.containsKey(member.getGitRemoteName())) {
                continue;
            }

            try {
                if (existingRemotes == null) {
                    existingRemotes = new HashMap<String, String>();
                }

                String remoteUrl = member.getGitUrl().replace("/sites/{siteId}", "/global");
                addRemoteRepository(member, remoteUrl);
                existingRemotes.put(member.getGitRemoteName(), StringUtils.EMPTY);

            } catch (IOException e) {
                logger.error("Failed to open repository", e);
            }
        }
    }

    protected void addRemoteRepository(ClusterMember member, String remoteUrl)
            throws IOException, InvalidRemoteUrlException, ServiceLayerException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        Repository repo = builder
                .setGitDir(Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                        studioConfiguration.getProperty(StudioConfiguration.GLOBAL_REPO_PATH)).resolve(GIT_ROOT)
                        .toFile())
                .readEnvironment()
                .findGitDir()
                .build();

        try (Git git = new Git(repo)) {

            Config storedConfig = repo.getConfig();
            Set<String> remotes = storedConfig.getSubsections(CONFIG_SECTION_REMOTE);

            if (remotes.contains(member.getGitRemoteName())) {
                logger.debug("Remote " + member.getGitRemoteName() + " already exists for global repo");
                String storedRemoteUrl = storedConfig.getString(CONFIG_SECTION_REMOTE,
                        member.getGitRemoteName(), CONFIG_PARAMETER_URL);
                if (!StringUtils.equals(storedRemoteUrl, remoteUrl)) {
                    RemoteSetUrlCommand remoteSetUrlCommand = git.remoteSetUrl();
                    remoteSetUrlCommand.setName(member.getGitRemoteName());
                    remoteSetUrlCommand.setUri(new URIish(remoteUrl));
                    remoteSetUrlCommand.call();
                }
            } else {
                logger.debug("Add " + member.getLocalAddress() + " as remote to sandbox");
                RemoteAddCommand remoteAddCommand = git.remoteAdd();
                remoteAddCommand.setName(member.getGitRemoteName());
                remoteAddCommand.setUri(new URIish(remoteUrl));
                remoteAddCommand.call();
            }

        } catch (URISyntaxException e) {
            logger.error("Remote URL is invalid " + remoteUrl, e);
            throw new InvalidRemoteUrlException();
        } catch (GitAPIException e) {
            logger.error("Error while adding remote " + member.getGitRemoteName() + " (url: " + remoteUrl +
                    ") for global repo", e);
            throw new ServiceLayerException("Error while adding remote " + member.getGitRemoteName() +
                    " (url: " + remoteUrl + ") for global repo", e);
        }
    }

    protected void updateContent() throws IOException, CryptoException, ServiceLayerException {
        logger.debug("Update global repo");

        Path siteSandboxPath = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                studioConfiguration.getProperty(StudioConfiguration.GLOBAL_REPO_PATH)).resolve(GIT_ROOT);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repo = builder
                .setGitDir(siteSandboxPath.toFile())
                .readEnvironment()
                .findGitDir()
                .build();

        try (Git git = new Git(repo)) {
            logger.debug("Update content from each active cluster memeber");
            for (ClusterMember remoteNode : clusterNodes) {
                updateBranch(git, remoteNode);
            }
        } catch (GitAPIException e) {
            logger.error("Error while syncing cluster node global repo content", e);
        }
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
            r = fetchResult.getAdvertisedRef(Constants.MASTER);
            if (r == null) {
                r = fetchResult.getAdvertisedRef(Constants.R_HEADS + Constants.MASTER);
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

    public List<ClusterMember> getClusterNodes() {
        return clusterNodes;
    }

    public void setClusterNodes(List<ClusterMember> clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    private static class StrictHostCheckingOffSshSessionFactory extends JschConfigSessionFactory {

        @Override
        protected void configure(OpenSshConfig.Host hc, Session session) {
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
        }

    }
}
