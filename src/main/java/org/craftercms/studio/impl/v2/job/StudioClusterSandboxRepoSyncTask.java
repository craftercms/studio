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

package org.craftercms.studio.impl.v2.job;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.ebus.PreviewEventContext;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.deployment.Deployer;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.service.cluster.StudioClusterUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteSetUrlCommand;
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
import org.eclipse.jgit.transport.URIish;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.ebus.EBusConstants.EVENT_PREVIEW_SYNC;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_SANDBOX_BRANCH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_SYNC_DB_COMMIT_MESSAGE_NO_PROCESSING;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SANDBOX_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SITES_REPOS_PATH;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CLUSTER_NODE_REMOTE_NAME_PREFIX;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_PARAMETER_URL;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.CONFIG_SECTION_REMOTE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;

public class StudioClusterSandboxRepoSyncTask extends StudioClockClusterTask {

    private static final Logger logger = LoggerFactory.getLogger(StudioClusterSandboxRepoSyncTask.class);

    private static final Map<String, ReentrantLock> singleWorkerLockMap = new HashMap<String, ReentrantLock>();
    protected static final List<String> createdSites = new ArrayList<String>();
    protected static final Map<String, Map<String, String>> remotesMap = new HashMap<String, Map<String, String>>();

    private StudioClusterUtils studioClusterUtils;
    private Deployer deployer;
    private DeploymentService deploymentService;
    private EventService eventService;
    private ClusterDAO clusterDao;

    public StudioClusterSandboxRepoSyncTask(int executeEveryNCycles,
                                            StudioClusterUtils studioClusterUtils,
                                            StudioConfiguration studioConfiguration,
                                            ContentRepository contentRepository,
                                            Deployer deployer,
                                            SiteService siteService,
                                            DeploymentService deploymentService,
                                            EventService eventService,
                                            ClusterDAO clusterDao) {
        super(executeEveryNCycles, studioConfiguration, siteService, contentRepository);
        this.studioClusterUtils = studioClusterUtils;
        this.deployer = deployer;
        this.deploymentService = deploymentService;
        this.eventService = eventService;
        this.clusterDao = clusterDao;
    }

    @Override
    protected void executeInternal(String siteId) {
        // Lock site and begin sync
        if (lockSiteInternal(siteId)) {
            // Log start time
            long startTime = System.currentTimeMillis();
            logger.debug("Worker starts syncing cluster node sandbox for site " + siteId);
            try {
                HierarchicalConfiguration<ImmutableNode> registrationData = studioClusterUtils.getClusterConfiguration();
                if (registrationData != null && !registrationData.isEmpty()) {
                    String localAddress = studioClusterUtils.getClusterNodeLocalAddress();
                    ClusterMember localNode = clusterDao.getMemberByLocalAddress(localAddress);
                    List<ClusterMember> clusterNodes = studioClusterUtils.getClusterNodes(localAddress);
                    SiteFeed siteFeed = siteService.getSite(siteId);
                    // Check if site exists
                    logger.debug("Check if site " + siteId + " exists in local repository");
                    boolean success = true;
                    boolean siteCheck = checkIfSiteRepoExists(siteId);

                    if (!siteCheck) {
                        // Site doesn't exist locally, create it
                        success = createSite(localNode.getId(), siteFeed.getId(), siteId, siteFeed.getSiteUuid(),
                                siteFeed.getSearchEngine(), clusterNodes);
                    } else {
                        if (clusterDao.existsClusterSiteSyncRepo(localNode.getId(), siteFeed.getId()) < 1) {
                            clusterDao.insertClusterSiteSyncRepo(localNode.getId(), siteFeed.getId(), null, null);
                        }
                    }

                    if (success) {
                        syncRemoteRepositories(siteId, localAddress);

                        // Get the site's database last commit ID
                        //String siteDatabaseLastCommitId = getDatabaseLastCommitId(siteId);

                        // Check if the site needs to be synced
                        boolean syncRequired = isSyncRequired(siteId, siteFeed.getLastCommitId());

                        if (syncRequired) {
                            try {
                                // Add the remote repositories to the local repository to sync from if not added already
                                logger.debug("Add remotes for site " + siteId);
                                addRemotes(siteId, clusterNodes);

                            } catch (InvalidRemoteUrlException | ServiceLayerException e) {
                                logger.error("Error while adding remotes on cluster node for site " + siteId);
                            }

                            try {
                                // Sync with remote and update the local cache with the last commit ID to speed things up
                                logger.debug("Update content for site " + siteId);
                                updateContent(localNode.getId(), siteFeed.getId(), siteId, siteFeed.getLastCommitId(),
                                        clusterNodes);
                            } catch (IOException | CryptoException | ServiceLayerException e) {
                                logger.error("Error while updating content for site " + siteId + " on cluster node.", e);
                            }
                        }
                    }
                }
            } catch (SiteNotFoundException e) {
                logger.error("Error while executing Cluster Node Sync Sandbox for site " + siteId, e);
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

    @Override
    protected boolean lockSiteInternal(String siteId) {
        ReentrantLock singleWorkerLock = singleWorkerLockMap.get(siteId);
        if (singleWorkerLock == null) {
            singleWorkerLock = new ReentrantLock();
            singleWorkerLockMap.put(siteId, singleWorkerLock);
        }
        return singleWorkerLock.tryLock();
    }

    @Override
    protected void unlockSiteInternal(String siteId) {
        ReentrantLock singleWorkerLock = singleWorkerLockMap.get(siteId);
        if (singleWorkerLock != null) {
            singleWorkerLock.unlock();
        }
    }

    protected boolean checkIfSiteRepoExists(String siteId) {
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

    private boolean createSite(long localNodeId, long sId, String siteId, String siteUuid, String searchEngine,
                               List<ClusterMember> clusterNodes) {
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
                result = createSiteFromRemote(siteId, clusterNodes);
                if (result) {
                    String commitId = contentRepository.getRepoLastCommitId(siteId);

                    clusterDao.insertClusterSiteSyncRepo(localNodeId, sId, commitId, commitId);
                    addSiteUuidFile(siteId, siteUuid);
                    deploymentService.syncAllContentToPreview(siteId, true);
                    createdSites.add(siteId);
                }
            } catch (ServiceLayerException | CryptoException |IOException e) {
                logger.error("Error while creating site on cluster node for site : " + siteId +
                        ". Rolling back.", e);
                result = false;
            }

            if (!result) {
                createdSites.remove(siteId);
                remotesMap.remove(siteId);
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

    protected boolean createSiteFromRemote(String siteId, List<ClusterMember> clusterNodes)
            throws CryptoException, ServiceLayerException {
        // Clone from the first node in the cluster (it doesn't matter which one to clone from, so pick the first)
        // we will eventually to catch up to the latest
        boolean cloned = false;
        int idx = 0;
        while (!cloned && idx < clusterNodes.size()) {
            ClusterMember remoteNode = clusterNodes.get(idx++);
            logger.debug("Cloning " + SANDBOX.toString() + " repository for site " + siteId +
                    " from " + remoteNode.getLocalAddress());

            // prepare a new folder for the cloned repository
            Path siteSandboxPath = buildRepoPath(siteId);
            File localPath = siteSandboxPath.toFile();
            localPath.delete();
            // then clone
            logger.debug("Cloning from " + remoteNode.getGitUrl() + " to " + localPath);
            CloneCommand cloneCommand = Git.cloneRepository();
            Git cloneResult = null;

            try {
                final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
                logger.debug("Add user credentials if provided");

                studioClusterUtils.configureAuthenticationForCommand(remoteNode, cloneCommand, tempKey);

                String cloneUrl = remoteNode.getGitUrl().replace("{siteId}", siteId);
                cloneUrl = cloneUrl + "/" + studioConfiguration.getProperty(SANDBOX_PATH);

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

    @Override
    protected Path buildRepoPath(String siteId) {
        return Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                        studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), siteId,
                        studioConfiguration.getProperty(SANDBOX_PATH));
    }

    private void addSiteUuidFile(String site, String siteUuid) throws IOException {
        Path path = Paths.get(studioConfiguration.getProperty(REPO_BASE_PATH),
                studioConfiguration.getProperty(SITES_REPOS_PATH), site,
                StudioConstants.SITE_UUID_FILENAME);
        String toWrite = StudioConstants.SITE_UUID_FILE_COMMENT + "\n" + siteUuid;
        Files.write(path, toWrite.getBytes());
    }

    private void syncRemoteRepositories(String siteId, String localAddress) {
        List<RemoteRepository> remoteRepositories =
                clusterDao.getMissingClusterNodeRemoteRepositories(localAddress, siteId);
        if (CollectionUtils.isNotEmpty(remoteRepositories)) {
            ClusterMember currentNode = clusterDao.getMemberByLocalAddress(localAddress);
            for (RemoteRepository remoteRepository : remoteRepositories) {
                try {
                    addRemoteRepository(siteId, remoteRepository);
                    clusterDao.addClusterRemoteRepository(currentNode.getId(), remoteRepository.getId());
                } catch (IOException | InvalidRemoteUrlException | ServiceLayerException e) {
                    logger.error("Error while adding remote " + remoteRepository.getRemoteName() +
                            " (url: " + remoteRepository.getRemoteUrl() + ") for site " + siteId, e);
                }
            }
        }
    }

    protected void addRemoteRepository(String siteId, RemoteRepository remoteRepository)
            throws IOException, InvalidRemoteUrlException, ServiceLayerException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        Repository repo = builder
                .setGitDir(buildRepoPath(siteId).resolve(GIT_ROOT).toFile())
                .readEnvironment()
                .findGitDir()
                .build();

        try (Git git = new Git(repo)) {

            logger.debug("Add " + remoteRepository.getRemoteName() + " as remote to SANDBOX");
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

    // Check if the site's commit id is behind the database, if so, it means the site on local disk needs to be sync'd
    private boolean isSyncRequired(String siteId, String siteDatabaseLastCommitId) {
        boolean syncRequired = true;

        // Get the repo's last commit id and compare it to the database
        String lastCommitIdRepo = contentRepository.getRepoLastCommitId(siteId);

        if (StringUtils.isNotEmpty(siteDatabaseLastCommitId) &&
                StringUtils.equals(lastCommitIdRepo, siteDatabaseLastCommitId)) {
            syncRequired = false;
        }

        return syncRequired;
    }

    protected void addRemotes(String siteId, List<ClusterMember> clusterNodes)
            throws InvalidRemoteUrlException, ServiceLayerException {
        Map<String, String> existingRemotes = remotesMap.get(siteId);
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
                addRemoteRepository(siteId, member, remoteUrl);

                existingRemotes.put(member.getGitRemoteName(), StringUtils.EMPTY);

            } catch (IOException e) {
                logger.error("Failed to open repository", e);
            }
        }
    }

    private void addRemoteRepository(String siteId, ClusterMember member, String remoteUrl)
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
                    logger.debug("Error while cleaning up remote repository", e);
                }
            }

            if (remotes.contains(member.getGitRemoteName())) {
                logger.debug("Remote " + member.getGitRemoteName() + " already exists for SANDBOX repo for site "
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
                logger.debug("Add " + member.getLocalAddress() + " as remote to SANDBOX");
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

    protected void updateContent(long localNodeId, long sId, String siteId, String lastCommitId,
                                 List<ClusterMember> clusterNodes)
            throws IOException, CryptoException, ServiceLayerException {
        logger.debug("Update sandbox for site " + siteId);

        Path siteSandboxPath = buildRepoPath(siteId).resolve(GIT_ROOT);
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
                    updateBranch(siteId, git, remoteNode);
                    String remoteLastCommitId = clusterDao.getNodeLastCommitId(remoteNode.getId(), sId);
                    remoteLastSyncCommits.put(remoteNode.getGitRemoteName(), remoteLastCommitId);
                }
            }

            String updatedCommitId = contentRepository.getRepoLastCommitId(siteId);
            clusterDao.updateNodeLastCommitId(localNodeId, sId, updatedCommitId);

            PreviewEventContext context = new PreviewEventContext();
            context.setSite(siteId);
            eventService.publish(EVENT_PREVIEW_SYNC, context);
        } catch (GitAPIException e) {
            logger.error("Error while syncing cluster node content for site " + siteId);
        }
    }

    private void updateBranch(String siteId, Git git, ClusterMember remoteNode) throws CryptoException, GitAPIException,
            IOException, ServiceLayerException {
        final Path tempKey = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
        FetchCommand fetchCommand = git.fetch().setRemote(remoteNode.getGitRemoteName());
        fetchCommand = studioClusterUtils.configureAuthenticationForCommand(remoteNode, fetchCommand, tempKey);
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
                MergeResult result = mergeCommand.call();
                if (result.getMergeStatus().isSuccessful()) {
                    deploymentService.syncAllContentToPreview(siteId, true);
                }
            }
        }

        Files.delete(tempKey);
    }

    @Override
    protected List<String> getCreatedSites() {
        return createdSites;
    }
}
