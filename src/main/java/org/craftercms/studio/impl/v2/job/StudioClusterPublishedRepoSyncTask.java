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

package org.craftercms.studio.impl.v2.job;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.dal.ClusterSiteRecord;
import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.service.cluster.StudioClusterUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteSetUrlCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.URIish;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.craftercms.studio.api.v1.constant.GitRepositories.PUBLISHED;
import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_PUBLISHED_REPOSITORY_GIT_LOCK;
import static org.craftercms.studio.api.v1.dal.SiteFeed.STATE_CREATED;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.PUBLISHED_PATH;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CLUSTER_NODE_REMOTE_NAME_PREFIX;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_URL;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_SECTION_REMOTE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;

public class StudioClusterPublishedRepoSyncTask extends StudioClockClusterTask {

    private static final Logger logger = LoggerFactory.getLogger(StudioClusterPublishedRepoSyncTask.class);

    protected static final Map<String, Map<String, String>> remotesMap = new HashMap<String, Map<String, String>>();

    private StudioClusterUtils studioClusterUtils;
    private ClusterDAO clusterDao;
    private ServicesConfig servicesConfig;
    private SecurityService securityService;
    private UserServiceInternal userServiceInternal;
    private TextEncryptor encryptor;
    private GeneralLockService generalLockService;
    private RetryingRepositoryOperationFacade retryingRepositoryOperationFacade;

    public StudioClusterPublishedRepoSyncTask(int executeEveryNCycles,
                                              int offset,
                                              StudioClusterUtils studioClusterUtils,
                                              StudioConfiguration studioConfiguration,
                                              ContentRepository contentRepository,
                                              SiteService siteService,
                                              ClusterDAO clusterDao,
                                              ServicesConfig servicesConfig,
                                              SecurityService securityService,
                                              UserServiceInternal userServiceInternal,
                                              TextEncryptor encryptor,
                                              GeneralLockService generalLockService,
                                              RetryingRepositoryOperationFacade retryingRepositoryOperationFacade) {

        super(executeEveryNCycles, offset, studioConfiguration, siteService, contentRepository);
        this.studioClusterUtils = studioClusterUtils;
        this.clusterDao = clusterDao;
        this.servicesConfig = servicesConfig;
        this.securityService = securityService;
        this.userServiceInternal = userServiceInternal;
        this.encryptor = encryptor;
        this.generalLockService = generalLockService;
        this.retryingRepositoryOperationFacade = retryingRepositoryOperationFacade;
    }

    @Override
    protected void executeInternal(String siteId) {
        // Log start time
        long startTime = System.currentTimeMillis();
        logger.debug("Worker starts syncing cluster node published for site " + siteId);
        try {
            HierarchicalConfiguration<ImmutableNode> registrationData = studioClusterUtils.getClusterConfiguration();
            if (registrationData != null && !registrationData.isEmpty()) {
                String localAddress = studioClusterUtils.getClusterNodeLocalAddress();
                ClusterMember localNode = clusterDao.getMemberByLocalAddress(localAddress);
                List<ClusterMember> clusterNodes = studioClusterUtils.getClusterNodes(localAddress);
                SiteFeed siteFeed = siteService.getSite(siteId);
                List<ClusterSiteRecord> clusterSiteRecords = clusterDao.getSiteStateAcrossCluster(siteId);
                Optional<ClusterSiteRecord> localNodeRecord = clusterSiteRecords.stream()
                        .filter(x -> x.getClusterNodeId() == localNode.getId() && StringUtils.equals(x.getState(),
                                STATE_CREATED)).findFirst();
                if (!localNodeRecord.isPresent()) {
                    return;
                }
                long nodesCreated = clusterSiteRecords.stream()
                        .filter(x -> StringUtils.equals(x.getState(), STATE_CREATED)).count();
                if (nodesCreated < 1) {
                    return;
                }
                // Check if site exists
                logger.debug("Check if site " + siteId + " exists in local repository");
                boolean success = true;
                int publishedReposCreated = clusterSiteRecords.stream()
                        .mapToInt(ClusterSiteRecord::getPublishedRepoCreated)
                        .sum();
                if (publishedReposCreated > 0 || siteFeed.getPublishedRepoCreated() > 0) {
                    boolean siteCheck = checkIfSiteRepoExists(siteId);

                    if (!siteCheck) {
                        // Site doesn't exist locally, create it
                        success = createSite(localNode.getId(), siteFeed.getId(), siteId, siteFeed.getSandboxBranch());
                    } else {
                        clusterDao.setPublishedRepoCreated(localNode.getId(), siteFeed.getId());
                    }
                } else {
                    success = false;
                }


                if (success) {

                    try {
                        // Add the remote repositories to the local repository to sync from if not added already
                        logger.debug("Add remotes for site " + siteId);
                        addRemotes(siteId, clusterNodes);

                    } catch (InvalidRemoteUrlException | ServiceLayerException | CryptoException e) {
                        logger.error("Error while adding remotes on cluster node for site " + siteId);
                    }

                    try {
                        // Sync with remote and update the local cache with the last commit ID to speed things up
                        logger.debug("Update content for site " + siteId);
                        updateContent(siteFeed.getId(), siteId, clusterNodes, clusterSiteRecords);
                    } catch (IOException | CryptoException | ServiceLayerException e) {
                        logger.error("Error while updating content for site " + siteId + " on cluster node.", e);
                    }
                }
            }
        } catch (SiteNotFoundException e) {
            logger.error("Error while executing Cluster Node Sync Published for site " + siteId, e);
        }
        // Compute execution duration and log it
        long duration = System.currentTimeMillis() - startTime;
        logger.debug("Worker finished syncing cluster node for site " + siteId);
        logger.debug("Worker performed cluster node sync for site " + siteId + " in " + duration + "ms");
        logger.debug("Finished Cluster Node Sync task for site " + siteId);
    }

    protected boolean checkIfSiteRepoExists(String siteId) {
        boolean toRet = false;
        String firstCommitId = contentRepository.getRepoFirstCommitId(siteId);
        if (!StringUtils.isEmpty(firstCommitId)) {
            Repository repo = null;
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            try {
                repo = builder
                        .setMustExist(true)
                        .setGitDir(buildRepoPath(siteId).resolve(GIT_ROOT).toFile())
                        .readEnvironment()
                        .findGitDir()
                        .build();
            } catch (IOException e) {
                logger.info("Failed to open PUBLISHED repo for site " + siteId);
            }
            toRet = Objects.nonNull(repo) && repo.getObjectDatabase().exists();
        }
        return toRet;
    }

    @Override
    protected Path buildRepoPath(String siteId) {
        return Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), siteId,
                studioConfiguration.getProperty(PUBLISHED_PATH));
    }

    protected boolean createSite(long localNodeId, long sId, String siteId, String sandboxBranch) {
        boolean result = true;

        if (result) {
            try {
                logger.debug("Create " + PUBLISHED.name() + " repository from remote for site " + siteId);
                GitRepositoryHelper helper = GitRepositoryHelper.getHelper(studioConfiguration, securityService,
                        userServiceInternal, encryptor, generalLockService, retryingRepositoryOperationFacade);
                result = helper.createPublishedRepository(siteId, sandboxBranch);
                if (result) {
                    clusterDao.setPublishedRepoCreated(localNodeId, sId);
                }
            } catch (CryptoException e) {
                logger.error("Error while creating site on cluster node for site : " + siteId +
                        ". Rolling back.", e);
                result = false;
            }

            if (!result) {
                remotesMap.remove(siteId);
                contentRepository.deleteSite(siteId);
            }
        }

        return result;
    }

    protected void addRemotes(String siteId, List<ClusterMember> clusterNodes) throws InvalidRemoteUrlException,
            ServiceLayerException,
            CryptoException {
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
                addRemoteRepository(siteId, member, remoteUrl);

                existingRemotes.put(member.getGitRemoteName(), StringUtils.EMPTY);

            } catch (IOException e) {
                logger.error("Failed to open repository for site " + siteId, e);
            }
        }
    }

    protected void addRemoteRepository(String siteId, ClusterMember member, String remoteUrl)
            throws IOException, InvalidRemoteUrlException, ServiceLayerException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        Repository repo = builder
                .setGitDir(buildRepoPath(siteId).resolve(GIT_ROOT).toFile())
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
                    logger.debug("Error while cleaning up remote repository for site " + siteId, e);
                }
            }

            if (remotes.contains(member.getGitRemoteName())) {
                logger.debug("Remote " + member.getGitRemoteName() + " already exists for PUBLISHED repo for site "
                        + siteId);
                String storedRemoteUrl = storedConfig.getString(CONFIG_SECTION_REMOTE,
                        member.getGitRemoteName(), CONFIG_PARAMETER_URL);
                if (!StringUtils.equals(storedRemoteUrl, remoteUrl)) {
                    RemoteSetUrlCommand remoteSetUrlCommand = git.remoteSetUrl();
                    remoteSetUrlCommand.setName(member.getGitRemoteName());
                    remoteSetUrlCommand.setUri(new URIish(remoteUrl));
                    remoteSetUrlCommand.call();
                }
            } else {
                logger.debug("Add " + member.getLocalAddress() + " as remote to PUBLISHED");
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

    protected void updateContent(long sId, String siteId, List<ClusterMember> clusterNodes,
                                 List<ClusterSiteRecord> clusterSiteRecords) throws IOException,
            CryptoException,
            ServiceLayerException {
        logger.debug("Update published repo for site " + siteId);
        Path siteSandboxPath = buildRepoPath(siteId).resolve(GIT_ROOT);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repo = builder
                .setGitDir(siteSandboxPath.toFile())
                .readEnvironment()
                .findGitDir()
                .build();

        String gitLockKey = SITE_PUBLISHED_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, siteId);
        logger.debug("Git Lock Key: " + gitLockKey);
        try (Git git = new Git(repo)) {
            Set<String> environments = getAllPublishingEnvironments(siteId);
            logger.debug("Update published repo from all active cluster members");
            if (generalLockService.tryLock(gitLockKey)) {
                try {
                    for (ClusterMember remoteNode : clusterNodes) {
                        ClusterSiteRecord csr = clusterDao.getClusterSiteRecord(remoteNode.getId(), sId);
                        if (Objects.nonNull(csr) && csr.getPublishedRepoCreated() > 0) {
                            try {
                                logger.debug("Fetch from cluster member " + remoteNode.getLocalAddress());
                                final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
                                FetchCommand fetch = git.fetch().setRemote(remoteNode.getGitRemoteName());
                                fetch = studioClusterUtils.configureAuthenticationForCommand(remoteNode, fetch, tempKey);
                                fetch.call();
                                Files.delete(tempKey);
                            } catch (GitAPIException e) {
                                logger.error("Error while fetching published repo for site " + siteId + " from remote " +
                                        remoteNode.getGitRemoteName());
                                logger.error(e.getMessage());
                            }
                        }
                    }

                    for (String branch : environments) {
                        for (ClusterMember remoteNode : clusterNodes) {
                            ClusterSiteRecord csr = clusterDao.getClusterSiteRecord(remoteNode.getId(), sId);
                            if (Objects.nonNull(csr) && csr.getPublishedRepoCreated() > 0) {
                                try {
                                    updatePublishedBranch(siteId, git, remoteNode, branch);
                                } catch (GitAPIException e) {
                                    logger.error("Error while updating published repo for site " + siteId + " from remote " +
                                            remoteNode.getGitRemoteName() + " environment " + branch);
                                    logger.error(e.getMessage());
                                }
                            }
                        }
                    }
                } finally {
                    generalLockService.unlock(gitLockKey);
                }
            } else {
                logger.debug("Failed to get lock " + gitLockKey);
            }
        }

    }

    private Set<String> getAllPublishingEnvironments(String site) {
        Set<String> environments = new HashSet<String>();
        environments.add(servicesConfig.getLiveEnvironment(site));
        if (servicesConfig.isStagingEnvironmentEnabled(site)) {
            environments.add(servicesConfig.getStagingEnvironment(site));
        }
        return environments;
    }

    private void updatePublishedBranch(String siteId, Git git, ClusterMember remoteNode, String branch)
            throws CryptoException, GitAPIException, IOException, ServiceLayerException {
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

        PullCommand pullCommand = git.pull();
        pullCommand.setRemote(remoteNode.getGitRemoteName());
        pullCommand.setRemoteBranchName(branch);
        pullCommand = studioClusterUtils.configureAuthenticationForCommand(remoteNode, pullCommand, tempKey);
        pullCommand.call();

        Files.delete(tempKey);
    }
}
