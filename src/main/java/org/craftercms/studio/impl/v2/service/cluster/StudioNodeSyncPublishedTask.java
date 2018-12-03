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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.collections4.CollectionUtils;
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
import org.craftercms.studio.api.v1.to.PublishingTargetTO;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteSetUrlCommand;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import static org.craftercms.studio.api.v1.constant.GitRepositories.PUBLISHED;
import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_CIPHER_KEY;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.SECURITY_CIPHER_SALT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_URL;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_SECTION_REMOTE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;
import static org.eclipse.jgit.lib.Constants.DEFAULT_REMOTE_NAME;

public class StudioNodeSyncPublishedTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(StudioNodeSyncPublishedTask.class);

    protected static final Map<String, ReentrantLock> singleWorkerLockMap = new HashMap<String, ReentrantLock>();
    protected static final List<String> createdSites = new ArrayList<String>();
    protected static final Map<String, Map<String, String>> remotesMap = new HashMap<String, Map<String, String>>();


    protected String siteId;
    protected List<ClusterMember> clusterNodes;
    protected SearchService searchService;
    protected PreviewDeployer previewDeployer;
    protected StudioConfiguration studioConfiguration;
    protected ContentRepository contentRepository;
    protected ServicesConfig servicesConfig;
    protected SiteService siteService;

    @Override
    public void run() {
        logger.debug("Starting Cluster Node Sync task for site " + siteId);
        ReentrantLock singleWorkerLock = singleWorkerLockMap.get(siteId);
        if (singleWorkerLock == null) {
            singleWorkerLock = new ReentrantLock();
            singleWorkerLockMap.put(siteId, singleWorkerLock);
        }
        if (singleWorkerLock.tryLock()) {
            long startTime = System.currentTimeMillis();
            logger.debug("Worker starts syncing cluster node for site " + siteId);
            try {
                logger.debug("Check if site " + siteId + " exists in local repository");
                boolean success = false;
                boolean siteCheck = checkIfSiteRepoExists();
                if (!siteCheck) {
                    logger.debug("Create search index for site " + siteId);
                    try {
                        searchService.createIndex(siteId);
                        success = true;
                    } catch (ServiceLayerException e) {
                        logger.error("Error creating search index on cluster node for site " + siteId + "." +
                                " Is the Search running and configured correctly in Studio?", e);
                        success = false;
                    }


                    if (success) {
                        logger.debug("Create preview deployer target site " + siteId);
                        try {
                            success = previewDeployer.createTarget(siteId);
                        } catch (Exception e) {
                            success = false;
                            logger.error("Error while creating preview deployer target on cluster node for site : "
                                    + siteId + ". Is the Preview Deployer running and configured correctly in " +
                                    "Studio cluster node?", e);
                        }

                        if (!success) {
                            // Rollback search index creation
                            try {
                                searchService.deleteIndex(siteId);
                            } catch (ServiceLayerException e) {
                                logger.error("Error while rolling back/deleting site: " + siteId + ". This means the site search " +
                                        "index (core) is still present, but the site is not successfully created.", e);
                            }
                        }
                    }


                    if (success) {
                        try {
                            logger.debug("Create site from remote for site " + siteId);
                            createSiteFromRemote();
                            success = true;
                        } catch (InvalidRemoteRepositoryException | InvalidRemoteRepositoryCredentialsException | RemoteRepositoryNotFoundException | ServiceLayerException | CryptoException e) {
                            logger.error("Error while creating site on cluster node for site : " + siteId +
                                    ". Rolling back.", e);
                            success = false;
                        }

                        if (!success) {
                            contentRepository.deleteSite(siteId);

                            boolean deleted = previewDeployer.deleteTarget(siteId);
                            if (!deleted) {
                                logger.error("Error while rolling back/deleting site: " + siteId + " ID: " + siteId +
                                        " on cluster node. This means the site's preview deployer target is still " +
                                        "present, but the site is not successfully created.");
                            }

                            try {
                                searchService.deleteIndex(siteId);
                            } catch (ServiceLayerException e) {
                                logger.error("Error while rolling back/deleting site: " + siteId + ". This means the site search " +
                                        "index (core) is still present, but the site is not successfully created.", e);
                            }
                        }
                    }
                }

                boolean syncRequired = true;
                String siteLastCommitId = StringUtils.EMPTY;
                try {
                    SiteFeed siteFeed = siteService.getSite(siteId);
                    siteLastCommitId = siteFeed.getLastCommitId();
                    String lastCommitIdRepo = contentRepository.getRepoLastCommitId(siteId);
                    if (StringUtils.equals(lastCommitIdRepo, siteLastCommitId)) {
                        syncRequired = false;
                    }
                } catch (SiteNotFoundException e) {
                    logger.error("Site " + siteId + " not found in the database");
                }

                if (syncRequired && StringUtils.isNotEmpty(siteLastCommitId)) {
                    try {
                        logger.debug("Add remotes for site " + siteId);
                        addRemotes();

                    } catch (InvalidRemoteUrlException | ServiceLayerException | CryptoException e) {
                        logger.error("Error while adding remotes on cluster node for site " + siteId);
                    }

                    try {
                        logger.debug("Update publised repo for site " + siteId);
                        updatePublished(siteLastCommitId);
                    } catch (IOException | CryptoException | ServiceLayerException e) {
                        logger.error("Error while updating content for site " + siteId + " on cluster node.", e);
                    }
                }
            } finally {
                singleWorkerLock.unlock();
            }
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Worker finished syncing cluster node for site " + siteId);
            logger.debug("Worker performed cluster node sync for site " + siteId + " in " + duration + "ms");
        } else {
            logger.debug("Unable to get cluster lock, another worker is holding the lock for site " + siteId);
        }
        logger.debug("Finished Cluster Node Sync task for site " + siteId);
    }

    private boolean createSiteFromRemote()
            throws InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
            RemoteRepositoryNotFoundException, ServiceLayerException, CryptoException {
        ClusterMember remoteNode = clusterNodes.get(0);
        logger.debug("Create site " + siteId + " from remote repository " + remoteNode.getLocalIp());
        boolean toRet = cloneRepository(remoteNode, SANDBOX) && cloneRepository(remoteNode, PUBLISHED);

        return toRet;
    }

    private boolean cloneRepository(ClusterMember remoteNode, GitRepositories repoType)
            throws CryptoException, ServiceLayerException, InvalidRemoteRepositoryException,
            InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException {

        logger.debug("Cloning " + repoType.toString() + " repository for site " + siteId +
                " from " + remoteNode.getLocalIp());
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

    private void addOriginRemote() throws IOException, InvalidRemoteUrlException, ServiceLayerException {
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

    private Path buildRepoPath(GitRepositories repoType) {
        Path path;
        switch (repoType) {
            case SANDBOX:
                path = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                        studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), siteId,
                        studioConfiguration.getProperty(StudioConfiguration.SANDBOX_PATH));
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

    private void addRemotes() throws InvalidRemoteUrlException, ServiceLayerException, CryptoException {
        Map<String, String> existingRemotes = remotesMap.get(siteId);
        TextEncryptor encryptor = new PbkAesTextEncryptor(studioConfiguration.getProperty(SECURITY_CIPHER_KEY),
                studioConfiguration.getProperty(SECURITY_CIPHER_SALT));
        logger.debug("Add cluster members as remotes to local repository");
        for (ClusterMember member : clusterNodes) {
            if (existingRemotes != null && existingRemotes.containsKey(member.getGitRemoteName())) {
                continue;
            }
            FileRepositoryBuilder builder = new FileRepositoryBuilder();

            try {
                String remoteUrl =
                        member.getGitUrl().replace("{siteId}", siteId) + "/" + studioConfiguration.getProperty(StudioConfiguration.PUBLISHED_PATH);

                Repository repo = builder
                        .setGitDir(buildRepoPath(PUBLISHED).resolve(GIT_ROOT).toFile())
                        .readEnvironment()
                        .findGitDir()
                        .build();

                try (Git git = new Git(repo)) {

                    Config storedConfig = repo.getConfig();
                    Set<String> remotes = storedConfig.getSubsections(CONFIG_SECTION_REMOTE);

                    if (existingRemotes == null) {
                        existingRemotes = new HashMap<String, String>();
                        remotesMap.put(siteId, existingRemotes);
                    }
                    if (remotes.contains(member.getGitRemoteName())) {
                        logger.debug("Remote " + member.getGitRemoteName() + " already exists for published repo for " +
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

                        logger.debug("Add " + member.getLocalIp() + " as remote to published");
                        RemoteAddCommand remoteAddCommand = git.remoteAdd();
                        remoteAddCommand.setName(member.getGitRemoteName());
                        remoteAddCommand.setUri(new URIish(remoteUrl));
                        remoteAddCommand.call();
                    }
                    existingRemotes.put(member.getGitRemoteName(), StringUtils.EMPTY);
                } catch (URISyntaxException e) {
                    logger.error("Remote URL is invalid " + remoteUrl, e);
                    throw new InvalidRemoteUrlException();
                } catch (GitAPIException e) {
                    logger.error("Error while adding remote " + member.getGitRemoteName() + " (url: " + remoteUrl + ") for site " +
                            siteId, e);
                    throw new ServiceLayerException("Error while adding remote " + member.getGitRemoteName() + " (url: " + remoteUrl +
                            ") for site " + siteId, e);
                }
            } catch (IOException e) {
                logger.error("Failed to open published repositroy", e);
            }
        }
    }

    private void updatePublished(String lastCommitId) throws IOException, CryptoException, ServiceLayerException {
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

    private <T extends TransportCommand> T setAuthenticationForCommand(ClusterMember remoteNode, T gitCommand,
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

    private SshSessionFactory getSshSessionFactory(String remotePrivateKey, final Path tempKey) {
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

    private boolean checkIfSiteRepoExists() {
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
