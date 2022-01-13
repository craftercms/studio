/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.DeleteBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;

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
import java.util.Properties;
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
    private static final String NON_SSH_GIT_URL_REGEX = "(file|https?|git)://.+";
    private static final String PROP_STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";
    private static final String PROP_STRICT_HOST_KEY_CHECKING_VALUE = "no";

    private TextEncryptor encryptor;
    private ClusterDAO clusterDao;
    private StudioConfiguration studioConfiguration;
    private GeneralLockService generalLockService;

    public StudioClusterUtils(ClusterDAO clusterDao, StudioConfiguration studioConfiguration, TextEncryptor encryptor
            , GeneralLockService generalLockService) {
        this.clusterDao = clusterDao;
        this.studioConfiguration = studioConfiguration;
        this.encryptor = encryptor;
        this.generalLockService = generalLockService;
    }

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
        remoteRemoveCommand.call();

        List<Ref> resultRemoteBranches = git.branchList()
                .setListMode(ListBranchCommand.ListMode.REMOTE)
                .call();

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
            delBranch.call();
        }
    }

    public <T extends TransportCommand> T configureAuthenticationForCommand(ClusterMember remoteNode,
                                                                            T gitCommand, final Path tempKey)
            throws CryptoException, IOException, ServiceLayerException {
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
                sshTransport.setSshSessionFactory(new StudioClusterUtils.StrictHostCheckingOffSshSessionFactory() {

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
                sshTransport.setSshSessionFactory(new StudioClusterUtils.StrictHostCheckingOffSshSessionFactory());
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
            sshTransport.setSshSessionFactory(new StudioClusterUtils.StrictHostCheckingOffSshSessionFactory() {
                @Override
                protected JSch createDefaultJSch(FS fs) throws JSchException {
                    JSch defaultJSch = super.createDefaultJSch(fs);
                    defaultJSch.addIdentity(tempKey.toAbsolutePath().toString());
                    return defaultJSch;
                }

            });
        });
    }

    private static class StrictHostCheckingOffSshSessionFactory extends JschConfigSessionFactory {

        @Override
        protected void configure(OpenSshConfig.Host hc, Session session) {
            Properties config = new Properties();
            config.put(PROP_STRICT_HOST_KEY_CHECKING, PROP_STRICT_HOST_KEY_CHECKING_VALUE);
            session.setConfig(config);
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

    public int getLockTTL() {
        return studioConfiguration.getProperty(PUBLISHING_SITE_LOCK_TTL, Integer.class);
    }

    public boolean cloneGlobalRepository(List<ClusterMember> clusterNodes)
            throws CryptoException, ServiceLayerException {
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
            } finally {
                generalLockService.unlock(gitLockKey);
            }
        } else {
            logger.debug("Failed to get lock " + gitLockKey);
        }
        return cloned;
    }
}
