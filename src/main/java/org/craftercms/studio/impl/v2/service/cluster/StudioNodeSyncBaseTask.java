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
import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.PUBLISHED_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SANDBOX_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_CIPHER_KEY;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_CIPHER_SALT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_URL;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_SECTION_REMOTE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;
import static org.eclipse.jgit.lib.Constants.DEFAULT_REMOTE_NAME;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.commons.crypto.impl.PbkAesTextEncryptor;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.dal.RemoteRepository;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.deployment.PreviewDeployer;
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
import org.craftercms.studio.api.v1.service.search.SearchService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteSetUrlCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;

public abstract class StudioNodeSyncBaseTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(StudioNodeSyncBaseTask.class);

    protected static final List<String> createdSites = new ArrayList<String>();
    protected static final Map<String, Map<String, String>> remotesMap =
            new HashMap<String, Map<String, String>>();

    protected String siteId;
    protected List<ClusterMember> clusterNodes;
    protected SearchService searchService;
    protected PreviewDeployer previewDeployer;
    protected StudioConfiguration studioConfiguration;
    protected ContentRepository contentRepository;
    protected ServicesConfig servicesConfig;
    protected SiteService siteService;

	// Abstract methods to be implemented by Sandbox/Published classes
	protected abstract boolean isSyncRequiredInternal(String siteId, String siteDatabaseLastCommitId);
	protected abstract void updateContentInternal(String siteId, String lastCommitId) throws IOException, CryptoException, ServiceLayerException;
	protected abstract boolean createSiteInternal(String siteId);
	protected abstract boolean lockSiteInternal(String siteId);
	protected abstract void unlockSiteInternal(String siteId);
	protected abstract boolean cloneSiteInternal(String siteId, GitRepositories repoType)throws CryptoException, ServiceLayerException, InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException;

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
                    success = createSite(siteId);
                }

                if (success) {
                    // Get the site's database last commit ID
                    String siteDatabaseLastCommitId = getDatbaseLastCommitId(siteId);
                    
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
    protected boolean createSite(String siteId) {
        return createSiteInternal(siteId);
    }

    // Determine if the local repo needs to be sync'd
    protected boolean isSyncRequired(String siteId, String siteDatabaseLastCommitId) {
		return isSyncRequiredInternal(siteId, siteDatabaseLastCommitId);
	}

    protected String getDatbaseLastCommitId(String siteId) {
        String siteDatabaseLastCommitId = StringUtils.EMPTY;

        try {
            SiteFeed siteFeed = siteService.getSite(siteId);
            siteDatabaseLastCommitId = siteFeed.getLastCommitId();
        } catch (SiteNotFoundException e) {
            logger.error("Site " + siteId + " not found in the database");
        }

        return siteDatabaseLastCommitId;
    }

    protected boolean createSiteFromRemote()
            throws InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
            RemoteRepositoryNotFoundException, ServiceLayerException, CryptoException {
        ClusterMember remoteNode = clusterNodes.get(0);
        logger.debug("Create site " + siteId + " from remote repository " + remoteNode.getLocalAddress());
        boolean toRet = cloneRepository(remoteNode, SANDBOX) && cloneRepository(remoteNode, PUBLISHED);

        return toRet;
    }

    protected boolean cloneRepository(ClusterMember remoteNode, GitRepositories repoType)
            throws CryptoException, ServiceLayerException, InvalidRemoteRepositoryException,
            InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException {
		return cloneSiteInternal(siteId, repoType);
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
                        studioConfiguration.getProperty(StudioConfiguration.PUBLISHED_PATH));
                break;
            default:
                path = null;
        }

        return path;
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

    protected void addRemotes() throws InvalidRemoteUrlException, ServiceLayerException, CryptoException {
        Map<String, String> existingRemotes = remotesMap.get(siteId);
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
                    remotesMap.put(siteId, existingRemotes);
                }

                String remoteUrl = member.getGitUrl().replace("{siteId}", siteId) + "/" +
                        studioConfiguration.getProperty(SANDBOX_PATH);
                addRemoteRepository(member, remoteUrl, SANDBOX);

                remoteUrl = member.getGitUrl().replace("{siteId}", siteId) + "/" +
                        studioConfiguration.getProperty(PUBLISHED_PATH);
                addRemoteRepository(member, remoteUrl, PUBLISHED);

                existingRemotes.put(member.getGitRemoteName(), StringUtils.EMPTY);

            } catch (IOException e) {
                logger.error("Failed to open repository", e);
            }
        }
    }

    protected void addRemoteRepository(ClusterMember member, String remoteUrl, GitRepositories repoType) throws IOException, InvalidRemoteUrlException, ServiceLayerException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        Repository repo = builder
                .setGitDir(buildRepoPath(repoType).resolve(GIT_ROOT).toFile())
                .readEnvironment()
                .findGitDir()
                .build();

        try (Git git = new Git(repo)) {

            Config storedConfig = repo.getConfig();
            Set<String> remotes = storedConfig.getSubsections(CONFIG_SECTION_REMOTE);

            if (remotes.contains(member.getGitRemoteName())) {
                logger.debug("Remote " + member.getGitRemoteName() + " already exists for sandbox repo for " +
                        "site " + siteId);
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
            logger.error("Error while adding remote " + member.getGitRemoteName() + " (url: " + remoteUrl + ") for site " +
                    siteId, e);
            throw new ServiceLayerException("Error while adding remote " + member.getGitRemoteName() + " (url: " + remoteUrl +
                    ") for site " + siteId, e);
        }
    }

    protected void updateContent(String lastCommitId) throws IOException, CryptoException, ServiceLayerException {
		updateContentInternal(siteId, lastCommitId);
    }

	protected <T extends TransportCommand> T setAuthenticationForCommand(ClusterMember remoteNode, T gitCommand,
                                                                       Path tempKey) throws CryptoException,
            ServiceLayerException {
        TextEncryptor encryptor = new PbkAesTextEncryptor(studioConfiguration.getProperty(SECURITY_CIPHER_KEY),
                studioConfiguration.getProperty(SECURITY_CIPHER_SALT));
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
                gitCommand.setTransportConfigCallback(new TransportConfigCallback() {
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
                gitCommand.setCredentialsProvider(credentialsProviderUP);
                break;
            case RemoteRepository.AuthenticationType.TOKEN:
                logger.debug("Token based authentication");
                String hashedToken = remoteNode.getGitToken();
                String token = encryptor.decrypt(hashedToken);
                UsernamePasswordCredentialsProvider credentialsProvider =
                        new UsernamePasswordCredentialsProvider(token, StringUtils.EMPTY);
                gitCommand.setTransportConfigCallback(new TransportConfigCallback() {
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
                        sshTransport.setCredentialsProvider(credentialsProvider);
                    }
                });
                break;
            case RemoteRepository.AuthenticationType.PRIVATE_KEY:
                logger.debug("Private key authentication");

                String hashedPrivateKey = remoteNode.getGitPrivateKey();
                String privateKey = encryptor.decrypt(hashedPrivateKey);
                tempKey.toFile().deleteOnExit();
                gitCommand.setTransportConfigCallback(new TransportConfigCallback() {
                    @Override
                    public void configure(Transport transport) {
                        SshTransport sshTransport = (SshTransport)transport;
                        sshTransport.setSshSessionFactory(getSshSessionFactory(privateKey, tempKey));
                    }
                });

                break;
            default:
                throw new ServiceLayerException("Unsupported authentication type " +
                        remoteNode.getGitAuthType());
        }

        return gitCommand;
    }

    protected SshSessionFactory getSshSessionFactory(String remotePrivateKey, final Path tempKey) {
        try {

            Files.write(tempKey, remotePrivateKey.getBytes());
            SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
                @Override
                protected void configure(OpenSshConfig.Host hc, Session session) {
                    Properties config = new Properties();
                    config.put("StrictHostKeyChecking", "no");
                    session.setConfig(config);
                }

                @Override
                protected JSch createDefaultJSch(FS fs) throws JSchException {
                    JSch defaultJSch = super.createDefaultJSch(fs);
                    defaultJSch.addIdentity(tempKey.toAbsolutePath().toString());
                    return defaultJSch;
                }
            };
            return sshSessionFactory;
        } catch (IOException e) {
            logger.error("Failed to create private key for SSH connection.", e);
        }
        return null;
    }

    protected boolean checkIfSiteRepoExists() {
        boolean toRet = false;
        if (createdSites.contains(siteId)) {
            toRet = true;
        } else {
            String firstCommitId = contentRepository.getRepoFirstCommitId(siteId);
            if (!StringUtils.isEmpty(firstCommitId)) {
                toRet = true;
                createdSites.add(siteId);
            }
        }
        return toRet;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public List<ClusterMember> getClusterNodes() {
        return clusterNodes;
    }

    public void setClusterNodes(List<ClusterMember> clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    public SearchService getSearchService() {
        return searchService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public PreviewDeployer getPreviewDeployer() {
        return previewDeployer;
    }

    public void setPreviewDeployer(PreviewDeployer previewDeployer) {
        this.previewDeployer = previewDeployer;
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
}
