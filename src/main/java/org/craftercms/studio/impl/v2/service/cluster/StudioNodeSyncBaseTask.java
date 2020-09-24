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

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.PUBLISHED_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SANDBOX_PATH;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CLUSTER_NODE_REMOTE_NAME_PREFIX;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_URL;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_SECTION_REMOTE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.deployment.Deployer;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.eclipse.jgit.api.DeleteBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.RemoteSetUrlCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;

public abstract class StudioNodeSyncBaseTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(StudioNodeSyncBaseTask.class);

    private static final String NON_SSH_GIT_URL_REGEX = "(file|https?|git)://.+";

    protected String siteId;
    protected String siteUuid;
    protected String searchEngine;
    protected List<ClusterMember> clusterNodes;
    protected Deployer deployer;
    protected StudioConfiguration studioConfiguration;
    protected ContentRepository contentRepository;
    protected ServicesConfig servicesConfig;
    protected SiteService siteService;
    protected DeploymentService deploymentService;
    protected EventService eventService;
    protected TextEncryptor encryptor;
    protected ClusterDAO clusterDao;
    protected String localAddress;

	// Abstract methods to be implemented by Sandbox/Published classes
	protected abstract boolean isSyncRequiredInternal(String siteId, String siteDatabaseLastCommitId);
	protected abstract void updateContentInternal(String siteId, String lastCommitId)
            throws IOException, CryptoException, ServiceLayerException;
	protected abstract boolean createSiteInternal(String siteId, String siteUuid, String searchEngine);
	protected abstract boolean lockSiteInternal(String siteId);
	protected abstract void unlockSiteInternal(String siteId);
	protected abstract boolean cloneSiteInternal(String siteId)
            throws CryptoException, ServiceLayerException, InvalidRemoteRepositoryException,
                   InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException;
    protected abstract boolean checkIfSiteRepoExistsInternal();
    protected abstract void addRemotesInternal()
            throws InvalidRemoteUrlException, ServiceLayerException, CryptoException;
    protected abstract void syncRemoteRepositoriesInternal();

    @Override
    public void run() {
        logger.debug("Starting Cluster Node Sync Sandbox task for site " + siteId);

        // Lock site and begin sync
		if (lockSiteInternal(siteId)) {
            // Log start time
            long startTime = System.currentTimeMillis();
            logger.debug("Worker starts syncing cluster node sandbox for site " + siteId);
            try {
                // Check if site exists
                logger.debug("Check if site " + siteId + " exists in local repository");
                boolean success = true;
                boolean siteCheck = checkIfSiteRepoExists();
                
                if (!siteCheck) {
                    // Site doesn't exist locally, create it
                    success = createSite(siteId, siteUuid, searchEngine);
                }

                if (success) {
                    syncRemoteRepositories();

                    // Get the site's database last commit ID
                    String siteDatabaseLastCommitId = getDatabaseLastCommitId(siteId);
                    
                    // Check if the site needs to be synced
                    boolean syncRequired = isSyncRequired(siteId, siteDatabaseLastCommitId);

                    if (syncRequired) {
                        try {
                            // Add the remote repositories to the local repository to sync from if not added already
                            logger.debug("Add remotes for site " + siteId);
                            addRemotes();

                        } catch (InvalidRemoteUrlException | ServiceLayerException | CryptoException e) {
                            logger.error("Error while adding remotes on cluster node for site " + siteId);
                        }

                        try {
                            // Sync with remote and update the local cache with the last commit ID to speed things up
                            logger.debug("Update content for site " + siteId);
                            updateContent(siteDatabaseLastCommitId);
                        } catch (IOException | CryptoException | ServiceLayerException e) {
                            logger.error("Error while updating content for site " + siteId + " on cluster node.", e);
                        }
                    }
                }
            } finally {
                unlockSiteInternal(siteId);
            }

            // Compute execution duration and log it
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Worker finished syncing cluster node for site " + siteId);
            logger.debug("Worker performed cluster node sync for site " + siteId + " in " + duration + "ms");
        } else {
            // Couldn't get the site lock, another worker is active, abandoning this cycle
            logger.debug("Unable to get cluster lock, another worker is holding the lock for site " + siteId);
        }
        logger.debug("Finished Cluster Node Sync task for site " + siteId);
    }

    // Create site helper
    protected boolean createSite(String siteId, String siteUuid, String searchEngine) {
        return createSiteInternal(siteId, siteUuid, searchEngine);
    }

    // Determine if the local repo needs to be sync'd
    protected boolean isSyncRequired(String siteId, String siteDatabaseLastCommitId) {
		return isSyncRequiredInternal(siteId, siteDatabaseLastCommitId);
	}

    protected String getDatabaseLastCommitId(String siteId) {
        String siteDatabaseLastCommitId = StringUtils.EMPTY;

        try {
            SiteFeed siteFeed = siteService.getSite(siteId);
            siteDatabaseLastCommitId = siteFeed.getLastCommitId();
        } catch (SiteNotFoundException e) {
            logger.error("Site " + siteId + " not found in the database");
        }

        return siteDatabaseLastCommitId;
    }

    protected boolean createSiteFromRemote(GitRepositories repoType)
            throws InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
            RemoteRepositoryNotFoundException, ServiceLayerException, CryptoException {
        ClusterMember remoteNode = clusterNodes.get(0);
        logger.debug("Create " + repoType.name() + " repository for site " + siteId + " from remote repository " +
                remoteNode.getLocalAddress());
        boolean toRet = cloneRepository(remoteNode);

        return toRet;
    }

    protected boolean cloneRepository(ClusterMember remoteNode)
            throws CryptoException, ServiceLayerException, InvalidRemoteRepositoryException,
            InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException {
		return cloneSiteInternal(siteId);
    }

    protected Path buildRepoPath(GitRepositories repoType) {
        Path path;
        switch (repoType) {
            case SANDBOX:
                path = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                        studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), siteId,
                        studioConfiguration.getProperty(SANDBOX_PATH));
                break;
            case PUBLISHED:
                path = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                        studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), siteId,
                        studioConfiguration.getProperty(PUBLISHED_PATH));
                break;
            default:
                path = null;
        }

        return path;
	}

    protected void addRemotes() throws InvalidRemoteUrlException, ServiceLayerException, CryptoException {
        addRemotesInternal();
    }

    protected void addRemoteRepository(ClusterMember member, String remoteUrl, GitRepositories repoType)
            throws IOException, InvalidRemoteUrlException, ServiceLayerException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        Repository repo = builder
                .setGitDir(buildRepoPath(repoType).resolve(GIT_ROOT).toFile())
                .readEnvironment()
                .findGitDir()
                .build();

        try (Git git = new Git(repo)) {

            Config storedConfig = repo.getConfig();
            Set<String> remotes = storedConfig.getSubsections(CONFIG_SECTION_REMOTE);

            if (remotes.contains(member.getGitRemoteName().replaceFirst(CLUSTER_NODE_REMOTE_NAME_PREFIX, ""))) {

                try {
                    removeRemote(git, member.getGitRemoteName().replaceFirst(CLUSTER_NODE_REMOTE_NAME_PREFIX, ""));
                } catch (GitAPIException e) {
                    logger.debug("Error while cleaning up remote repository", e);
                }
            }

            if (remotes.contains(member.getGitRemoteName())) {
                logger.debug("Remote " + member.getGitRemoteName() + " already exists for " + repoType.toString() +
                        " repo for site " + siteId);
                String storedRemoteUrl = storedConfig.getString(CONFIG_SECTION_REMOTE,
                        member.getGitRemoteName(), CONFIG_PARAMETER_URL);
                if (!StringUtils.equals(storedRemoteUrl, remoteUrl)) {
                    RemoteSetUrlCommand remoteSetUrlCommand = git.remoteSetUrl();
                    remoteSetUrlCommand.setName(member.getGitRemoteName());
                    remoteSetUrlCommand.setUri(new URIish(remoteUrl));
                    remoteSetUrlCommand.call();
                }
            } else {
                logger.debug("Add " + member.getLocalAddress() + " as remote to " + repoType.toString());
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
                         ") for site " + siteId, e);
            throw new ServiceLayerException("Error while adding remote " + member.getGitRemoteName() +
                                            " (url: " + remoteUrl + ") for site " + siteId, e);
        }
    }

    protected void addRemoteRepository(RemoteRepository remoteRepository, GitRepositories repoType)
            throws IOException, InvalidRemoteUrlException, ServiceLayerException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        Repository repo = builder
                .setGitDir(buildRepoPath(repoType).resolve(GIT_ROOT).toFile())
                .readEnvironment()
                .findGitDir()
                .build();

        try (Git git = new Git(repo)) {

            logger.debug("Add " + remoteRepository.getRemoteName() + " as remote to " + repoType.toString());
            RemoteAddCommand remoteAddCommand = git.remoteAdd();
            remoteAddCommand.setName(remoteRepository.getRemoteName());
            remoteAddCommand.setUri(new URIish(remoteRepository.getRemoteUrl()));
            remoteAddCommand.call();


        } catch (URISyntaxException e) {
            logger.error("Remote URL is invalid " + remoteRepository.getRemoteUrl(), e);
            throw new InvalidRemoteUrlException();
        } catch (GitAPIException e) {
            logger.error("Error while adding remote " + remoteRepository.getRemoteName() +
                    " (url: " + remoteRepository.getRemoteUrl() + ") for site " + siteId, e);
            throw new ServiceLayerException("Error while adding remote " + remoteRepository.getRemoteName() +
                    " (url: " + remoteRepository.getRemoteUrl() + ") for site " + siteId, e);
        }
    }

    private void removeRemote(Git git, String remoteName) throws GitAPIException {
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

    protected void updateContent(String lastCommitId) throws IOException, CryptoException, ServiceLayerException {
		updateContentInternal(siteId, lastCommitId);
    }

	protected <T extends TransportCommand> T configureAuthenticationForCommand(ClusterMember remoteNode, T gitCommand,
                                                                               final Path tempKey)
            throws CryptoException, IOException, ServiceLayerException {
        boolean sshProtocol = !remoteNode.getGitUrl().matches(NON_SSH_GIT_URL_REGEX);

        switch (remoteNode.getGitAuthType()) {
            case RemoteRepository.AuthenticationType.NONE:
                logger.debug("No authentication");
                break;
            case RemoteRepository.AuthenticationType.BASIC:
                logger.debug("Basic Authentication");
                if (StringUtils.isEmpty(remoteNode.getGitUsername()) ||
                        StringUtils.isEmpty(remoteNode.getGitPassword())) {
                    throw new ServiceLayerException("Username or password empty for basic authentication for cluster " +
                            "node " + remoteNode.getLocalAddress());
                }
                configureBasicAuthentication(remoteNode, gitCommand, encryptor, sshProtocol);
                break;
            case RemoteRepository.AuthenticationType.TOKEN:
                logger.debug("Token based Authentication");
                if (StringUtils.isEmpty(remoteNode.getGitToken())) {
                    throw new ServiceLayerException("Token is empty for token based authentication for cluster " +
                            "node " + remoteNode.getLocalAddress());
                }
                configureTokenAuthentication(remoteNode, gitCommand, encryptor, sshProtocol);
                break;
            case RemoteRepository.AuthenticationType.PRIVATE_KEY:
                if (!sshProtocol) {
                    throw new ServiceLayerException("Can't do private key authentication with non-ssh URLs");
                }

                logger.debug("Private Key Authentication");
                if (StringUtils.isEmpty(remoteNode.getGitPrivateKey())) {
                    throw new ServiceLayerException("Private key is empty for key based authentication for cluster " +
                            "node " + remoteNode.getLocalAddress());
                }
                configurePrivateKeyAuthentication(remoteNode, gitCommand, encryptor, tempKey);
                break;
            default:
                throw new ServiceLayerException("Unsupported authentication type " + remoteNode.getGitAuthType());
        }

        return gitCommand;
    }

    protected <T extends TransportCommand> void configureBasicAuthentication(
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

    protected <T extends TransportCommand> void configureTokenAuthentication(
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

    protected <T extends TransportCommand> void configurePrivateKeyAuthentication(
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

    protected boolean checkIfSiteRepoExists() {
        return checkIfSiteRepoExistsInternal();
    }

    protected static class StrictHostCheckingOffSshSessionFactory extends JschConfigSessionFactory {

        @Override
        protected void configure(OpenSshConfig.Host hc, Session session) {
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
        }

    }

    protected void syncRemoteRepositories() {
        syncRemoteRepositoriesInternal();
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteUuid() {
        return siteUuid;
    }

    public void setSiteUuid(String siteUuid) {
        this.siteUuid = siteUuid;
    }

    public List<ClusterMember> getClusterNodes() {
        return clusterNodes;
    }

    public void setClusterNodes(List<ClusterMember> clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    public Deployer getDeployer() {
        return deployer;
    }

    public void setDeployer(Deployer deployer) {
        this.deployer = deployer;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public String getSearchEngine() {
        return searchEngine;
    }

    public void setSearchEngine(String searchEngine) {
        this.searchEngine = searchEngine;
    }

    public DeploymentService getDeploymentService() {
        return deploymentService;
    }

    public void setDeploymentService(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public EventService getEventService() {
        return eventService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    //TODO: Check uses
    public void setEncryptor(TextEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    public ClusterDAO getClusterDao() {
        return clusterDao;
    }

    public void setClusterDao(ClusterDAO clusterDao) {
        this.clusterDao = clusterDao;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }
}
