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
package org.craftercms.studio.impl.v2.repository.blob;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.file.blob.Blob;
import org.craftercms.commons.file.blob.BlobStore;
import org.craftercms.commons.file.blob.exception.BlobStoreConfigurationMissingException;
import org.craftercms.core.service.Item;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;
import org.craftercms.studio.api.v1.to.RemoteRepositoryInfoTO;
import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.api.v2.dal.GitLog;
import org.craftercms.studio.api.v2.dal.PublishingHistoryItem;
import org.craftercms.studio.api.v2.dal.RepoOperation;
import org.craftercms.studio.api.v2.exception.RepositoryLockedException;
import org.craftercms.studio.api.v2.repository.RepositoryChanges;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStore;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStoreResolver;
import org.craftercms.studio.impl.v1.repository.git.GitContentRepository;
import org.craftercms.studio.model.rest.content.DetailedItem;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.core.io.Resource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.prependIfMissing;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;

/**
 * Implementation of {@link ContentRepository}, {@link org.craftercms.studio.api.v2.repository.ContentRepository}
 * that delegates calls to a {@link StudioBlobStore} when appropriate
 *
 * @author joseross
 * @since 3.1.6
 */
public class BlobAwareContentRepository implements ContentRepository,
        org.craftercms.studio.api.v2.repository.ContentRepository {

    private static final Logger logger = LoggerFactory.getLogger(BlobAwareContentRepository.class);

    /**
     * The extension for the blob files
     */
    protected String fileExtension;

    protected GitContentRepository localRepositoryV1;

    protected org.craftercms.studio.impl.v2.repository.GitContentRepository localRepositoryV2;

    protected StudioBlobStoreResolver blobStoreResolver;

    protected final ObjectMapper objectMapper = new XmlMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public void setLocalRepositoryV1(GitContentRepository localRepositoryV1) {
        this.localRepositoryV1 = localRepositoryV1;
    }

    public void setLocalRepositoryV2(org.craftercms.studio.impl.v2.repository.GitContentRepository localRepositoryV2) {
        this.localRepositoryV2 = localRepositoryV2;
    }

    public void setBlobStoreResolver(StudioBlobStoreResolver blobStoreResolver) {
        this.blobStoreResolver = blobStoreResolver;
    }

    protected String getOriginalPath(String path) {
        return StringUtils.removeEnd(path, "." + fileExtension);
    }

    protected String getPointerPath(String siteId, String path) {
        return isFolder(siteId, path)? path : StringUtils.appendIfMissing(path, "." + fileExtension);
    }

    protected String getPathFromPointerPath(String siteId, String pointerPath) {
        return isFolder(siteId, pointerPath)? pointerPath : StringUtils.removeEnd(pointerPath, "." + fileExtension);
    }

    protected String normalize(String path) {
        return Paths.get(path).normalize().toString();
    }

    protected StudioBlobStore getBlobStore(String site, String... paths)
            throws ServiceLayerException {
        if (isEmpty(site)) {
            return null;
        }

        if (ArrayUtils.isEmpty(paths)) {
            throw new IllegalArgumentException("At least one path needs to be provided");
        }

        return (StudioBlobStore) blobStoreResolver.getByPaths(site, paths);
    }

    protected boolean pointersExist(String siteId, String... paths) {
        return Stream.of(paths).
                allMatch(path -> {
                    // Check if the pointer path is not the same (this happens for folders)
                    String pointerPath = getPointerPath(siteId, path);
                    return !StringUtils.equals(path, pointerPath)
                            && localRepositoryV1.contentExists(siteId, pointerPath);
                });
    }

    // Start API 1

    @Override
    public boolean contentExists(String site, String path) {
        logger.debug("Checking if {0} exists in site {1}", path, site);
        try {
            if (!isFolder(site, path) && pointersExist(site, path)) {
                StudioBlobStore store = getBlobStore(site, path);
                if (store != null) {
                    return store.contentExists(site, normalize(path));
                }
            }
            return localRepositoryV1.contentExists(site, path);
        } catch (Exception e) {
            logger.error("Error checking if content {0} exist in site {1}", e, path, site);
            return false;
        }
    }

    @Override
    public boolean shallowContentExists(String site, String path) {
        logger.debug("Checking if {0} exists in site {1}", path, site);
        try {
            // Return only if the pointer exists, otherwise do the regular call
            if (!isFolder(site, path) && pointersExist(site, path)) {
                return true;
            }
            return localRepositoryV1.shallowContentExists(site, path);
        } catch (Exception e) {
            logger.error("Error checking if content {0} exist in site {1}", e, path, site);
            return false;
        }
    }

    @Override
    public InputStream getContent(String site, String path) {
        logger.debug("Getting content of {0} in site {1}", path, site);
        try {
            if (!isFolder(site, path) && pointersExist(site, path)) {
                StudioBlobStore store = getBlobStore(site, path);
                if (store != null) {
                    return store.getContent(site, normalize(path));
                }
            }
            return localRepositoryV1.getContent(site, path);
        } catch (Exception e) {
            logger.error("Error getting content {0} in site {1}", e, path, site);
            return null;
        }
    }

    @Override
    public long getContentSize(String site, String path) {
        logger.debug("Getting size of {0} in site {1}", path, site);
        try {
            if (pointersExist(site, path)) {
                StudioBlobStore store = getBlobStore(site, path);
                if (store != null) {
                    // Don't populate the file size for blob-store backed files due to performance reasons
                    return -1L;
                    // return store.getContentSize(site, normalize(path));
                }
            }
            return localRepositoryV2.getContentSize(site, path);
        } catch (Exception e) {
            logger.error("Error getting size for content {0} in site {1}", e, path, site);
            return -1L;
        }
    }

    @Override
    public String writeContent(String site, String path, InputStream content) throws ServiceLayerException {
        logger.debug("Writing {0} in site {1}", path, site);
        try {
            StudioBlobStore store = getBlobStore(site, path);
            if (store != null) {
                store.writeContent(site, normalize(path), content);
                Blob reference = store.getReference(normalize(path));
                return localRepositoryV1.writeContent(site, getPointerPath(site, path),
                        new ByteArrayInputStream(objectMapper.writeValueAsBytes(reference)));
            }
            return localRepositoryV1.writeContent(site, path, content);
        } catch (BlobStoreConfigurationMissingException e) {
            logger.debug("No blob store configuration found for site {0}, writing {1} to local repository", site, path);
            return localRepositoryV1.writeContent(site, path, content);
        } catch (RepositoryLockedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error writing content {0} in site {1}", e, path, site);
            throw new ServiceLayerException(e);
        }
    }

    @Override
    public String createFolder(String site, String path, String name) {
        logger.debug("Creating folder {0} in site {1}", path, site);
        try {
            StudioBlobStore store = getBlobStore(site, path);
            if (store != null) {
                store.createFolder(site, normalize(path), name);
            }
            return localRepositoryV1.createFolder(site, path, name);
        } catch (BlobStoreConfigurationMissingException e) {
            logger.debug("No blob store configuration found for site {0}, creating folder {1} in local repository",
                    site, path);
            return localRepositoryV1.createFolder(site, path, name);
        } catch (Exception e) {
            logger.error("Error creating folder {0} in site {1}", e, path, site);
            return null;
        }
    }

    @Override
    public String deleteContent(String site, String path, String approver) {
        logger.debug("Deleting {0} in site {1}", path, site);
        try {
            StudioBlobStore store = getBlobStore(site, path);
            if (store != null) {
                String result = store.deleteContent(site, normalize(path), approver);
                if (result != null) {
                    return localRepositoryV1.deleteContent(site, getPointerPath(site, path), approver);
                }
            }
            return localRepositoryV1.deleteContent(site, path, approver);
        } catch (BlobStoreConfigurationMissingException e) {
            logger.debug("No blob store configuration found for site {0}, deleting {1} in the local repository",
                    site, path);
            return localRepositoryV1.deleteContent(site, path, approver);
        } catch (Exception e) {
            logger.error("Error deleting content {0} in site {1}", e, path, site);
            return null;
        }
    }

    @Override
    public Map<String, String> moveContent(String site, String fromPath, String toPath, String newName) {
        logger.debug("Moving content from {0} to {1} in site {2}", fromPath, toPath, site);
        try {
            StudioBlobStore store = getBlobStore(site, fromPath, toPath);
            if (store != null) {
                Map<String, String> result = store.moveContent(site, normalize(fromPath), normalize(toPath), newName);
                if (result != null) {
                    boolean isFolder = isFolder(site, fromPath);
                    Map<String, String> diskResult =
                            localRepositoryV1.moveContent(site, isFolder ? fromPath : getPointerPath(site, fromPath),
                                    isFolder ? toPath : getPointerPath(site, toPath), newName);
                    Set<String> keys = new HashSet<>(diskResult.keySet());
                    keys.forEach(k -> {
                        String val = diskResult.get(k);
                        diskResult.put(getPathFromPointerPath(site, k), val);
                    });
                    return diskResult;
                }
            }
            return localRepositoryV1.moveContent(site, fromPath, toPath, newName);
        } catch (BlobStoreConfigurationMissingException e) {
            logger.debug("No blob store configuration found for site {0}, moving from {1} to {2} in local repository",
                    site, fromPath, toPath);
            return localRepositoryV1.moveContent(site, fromPath, toPath, newName);
        } catch (Exception e) {
            logger.error("Error moving content from {0} to {1} in site {2}", e, fromPath, toPath, site);
            return null;
        }
    }

    @Override
    public String copyContent(String site, String fromPath, String toPath) {
        logger.debug("Copying content from {0} to {1} in site {2}", fromPath, toPath, site);
        try {
            StudioBlobStore store = getBlobStore(site, fromPath, toPath);
            if (store != null) {
                String result = store.copyContent(site, normalize(fromPath), normalize(toPath));
                if (result != null) {
                    return localRepositoryV1.copyContent(site, getPointerPath(site, fromPath),
                            getPointerPath(site, toPath));
                }
            }
            return localRepositoryV1.copyContent(site, fromPath, toPath);
        } catch (BlobStoreConfigurationMissingException e) {
            logger.debug("No blob store configuration found for site {0}, copying from {1} to {2} in local repository",
                    site, fromPath, toPath);
            return localRepositoryV1.copyContent(site, fromPath, toPath);
        } catch (Exception e) {
            logger.error("Error copying content from {0} to {1} in site {2}", e, fromPath, toPath, site);
            return null;
        }
    }

    @Override
    public RepositoryItem[] getContentChildren(String site, String path) {
        RepositoryItem[] children = localRepositoryV1.getContentChildren(site, path);
        return Stream.of(children)
                .peek(item -> item.name = getOriginalPath(item.name))
                .collect(toList())
                .toArray(new RepositoryItem[children.length]);
    }

    @Override
    public VersionTO[] getContentVersionHistory(String site, String path) {
        logger.debug("Getting version history for {0} in site {1}", path, site);
        try {
            if (pointersExist(site, path)) {
                StudioBlobStore store = getBlobStore(site, path);
                if (store != null) {
                    return localRepositoryV1.getContentVersionHistory(site, getPointerPath(site, path));
                }
            }
            return localRepositoryV1.getContentVersionHistory(site, path);
        } catch (Exception e) {
            logger.error("Error getting version history for {0} in site {1}", e, path, site);
            return null;
        }
    }

    @Override
    public String createVersion(String site, String path, boolean majorVersion) {
        return localRepositoryV1.createVersion(site, path, majorVersion);
    }

    @Override
    public String createVersion(String site, String path, String comment, boolean majorVersion) {
        return localRepositoryV1.createVersion(site, path, comment, majorVersion);
    }

    @Override
    public String revertContent(String site, String path, String version, boolean major, String comment) {
        return localRepositoryV1.revertContent(site, path, version, major, comment);
    }

    @Override
    public Optional<Resource> getContentByCommitId(String site, String path, String commitId) {
        return localRepositoryV2.getContentByCommitId(site, path, commitId);
    }

    @Override
    public void lockItem(String site, String path) {
        localRepositoryV2.lockItem(site, path);
    }

    @Override
    public void lockItemForPublishing(String site, String path) {
        localRepositoryV1.lockItemForPublishing(site, path);
    }

    @Override
    public void unLockItem(String site, String path) {
        localRepositoryV1.unLockItem(site, path);
    }

    @Override
    public void unLockItemForPublishing(String site, String path) {
        localRepositoryV1.unLockItemForPublishing(site, path);
    }

    @Override
    public boolean isFolder(String siteId, String path) {
        return localRepositoryV1.isFolder(siteId, path);
    }

    // TODO: Remove when the API is split

    @Override
    public boolean deleteSite(String siteId) {
        return localRepositoryV1.deleteSite(siteId);
    }

    @Override
    public void initialPublish(String site, String sandboxBranch, String environment, String author, String comment)
            throws DeploymentException {
        localRepositoryV1.initialPublish(site, sandboxBranch, environment, author, comment);
    }

    protected DeploymentItemTO mapDeploymentItem(DeploymentItemTO item) {
        DeploymentItemTO pointer = new DeploymentItemTO();
        pointer.setPath(getPointerPath(item.getSite(), item.getPath()));
        pointer.setSite(item.getSite());
        pointer.setCommitId(item.getCommitId());
        pointer.setMove(item.isMove());
        pointer.setDelete(item.isDelete());
        pointer.setOldPath(isEmpty(item.getOldPath())? item.getOldPath() : getPointerPath(item.getSite(), item.getOldPath()));
        pointer.setPackageId(item.getPackageId());
        return pointer;
    }

    @Override
    public String getRepoLastCommitId(String site) {
        return localRepositoryV1.getRepoLastCommitId(site);
    }

    @Override
    public String getRepoFirstCommitId(String site) {
        return localRepositoryV1.getRepoFirstCommitId(site);
    }

    @Override
    public List<String> getEditCommitIds(String site, String path, String commitIdFrom, String commitIdTo) {
        return localRepositoryV1.getEditCommitIds(site, path, commitIdFrom, commitIdTo);
    }

    @Override
    public void insertFullGitLog(String siteId, int processed) {
        localRepositoryV1.insertFullGitLog(siteId, processed);
    }

    @Override
    public void deleteGitLogForSite(String siteId) {
        localRepositoryV1.deleteGitLogForSite(siteId);
    }

    @Override
    public boolean addRemote(String siteId, String remoteName, String remoteUrl, String authenticationType,
                             String remoteUsername, String remotePassword, String remoteToken, String remotePrivateKey)
            throws InvalidRemoteUrlException, ServiceLayerException {
        return localRepositoryV1.addRemote(siteId, remoteName, remoteUrl, authenticationType, remoteUsername,
                remotePassword, remoteToken, remotePrivateKey);
    }

    @Override
    public void removeRemoteRepositoriesForSite(String siteId) {
        localRepositoryV1.removeRemoteRepositoriesForSite(siteId);
    }

    @Override
    public List<RemoteRepositoryInfoTO> listRemote(String siteId, String sandboxBranch)
            throws ServiceLayerException {
        return localRepositoryV1.listRemote(siteId, sandboxBranch);
    }

    @Override
    public boolean pushToRemote(String siteId, String remoteName, String remoteBranch) throws ServiceLayerException,
            InvalidRemoteUrlException {
        return localRepositoryV1.pushToRemote(siteId, remoteName, remoteBranch);
    }

    @Override
    public boolean pullFromRemote(String siteId, String remoteName, String remoteBranch) throws ServiceLayerException,
            InvalidRemoteUrlException {
        return localRepositoryV1.pullFromRemote(siteId, remoteName, remoteBranch);
    }

    @Override
    public void resetStagingRepository(String siteId) throws ServiceLayerException {
        localRepositoryV1.resetStagingRepository(siteId);
    }

    @Override
    public void reloadRepository(String siteId) {
        localRepositoryV1.reloadRepository(siteId);
    }

    @Override
    public void cleanupRepositories(String siteId) {
        localRepositoryV1.cleanupRepositories(siteId);
    }

    // Start API 2

    @Override
    public boolean createSiteFromBlueprint(String blueprintLocation, String siteId, String sandboxBranch,
                                           Map<String, String> params, String creator) {
        return localRepositoryV2.createSiteFromBlueprint(blueprintLocation, siteId, sandboxBranch, params, creator);
    }

    @Override
    public void publish(String site, String sandboxBranch, List<DeploymentItemTO> deploymentItems, String environment,
                        String author, String comment) throws DeploymentException {
        logger.debug("Publishing items {0} to environment {1} in site {2}", deploymentItems, environment, site);
        Map<String, StudioBlobStore> stores = new LinkedHashMap<>();
        MultiValueMap<String, DeploymentItemTO> items = new LinkedMultiValueMap<>();
        List<DeploymentItemTO> localItems = new LinkedList<>();
        try {
            for (DeploymentItemTO item : deploymentItems) {
                if (pointersExist(site, item.getPath()) &&
                        (isEmpty(item.getOldPath()) || pointersExist(site, item.getOldPath()))) {
                    logger.debug("Looking blob store for item {0}", item);
                    StudioBlobStore store = getBlobStore(site, item.getPath());
                    if (store != null) {
                        stores.putIfAbsent(store.getId(), store);
                        items.add(store.getId(), item);
                        localItems.add(mapDeploymentItem(item));
                        continue;
                    }
                }
                localItems.add(item);
            }
            for (String storeId : stores.keySet()) {
                logger.debug("Publishing blobs to environment {0} using store {1} for site {2}",
                        environment, storeId, site);
                stores.get(storeId).publish(site, sandboxBranch, items.get(storeId), environment, author, comment);
            }
            logger.debug("Publishing local files to environment {0} for site {1}", environment, site);
            localRepositoryV2.publish(site, sandboxBranch, localItems, environment, author, comment);
        } catch (Exception e) {
            throw new DeploymentException("Error during deployment to environment " +
                    environment + " for site " + site, e);
        }
    }

    @Override
    public boolean commitIdExists(String site, String commitId) {
        return localRepositoryV2.commitIdExists(site, commitId);
    }

    @Override
    public boolean commitIdExists(String site, GitRepositories repoType, String commitId) {
        return localRepositoryV2.commitIdExists(site, repoType, commitId);
    }

    @Override
    public boolean createSiteCloneRemote(String siteId, String sandboxBranch, String remoteName, String remoteUrl,
                                         String remoteBranch, boolean singleBranch, String authenticationType,
                                         String remoteUsername, String remotePassword, String remoteToken,
                                         String remotePrivateKey, Map<String, String> params, boolean createAsOrphan,
                                         String creator)
            throws InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
            RemoteRepositoryNotFoundException, ServiceLayerException {
        return localRepositoryV2.createSiteCloneRemote(siteId, sandboxBranch, remoteName, remoteUrl, remoteBranch,
                singleBranch, authenticationType, remoteUsername, remotePassword, remoteToken, remotePrivateKey,
                params, createAsOrphan, creator);
    }

    @Override
    public boolean removeRemote(String siteId, String remoteName) {
        return localRepositoryV2.removeRemote(siteId, remoteName);
    }

    @Override
    public boolean repositoryExists(String site) {
        return localRepositoryV2.repositoryExists(site);
    }

    @Override
    public GitLog getGitLog(String siteId, String commitId) {
        return localRepositoryV2.getGitLog(siteId, commitId);
    }

    @Override
    public void markGitLogVerifiedProcessed(String siteId, String commitId) {
        localRepositoryV2.markGitLogVerifiedProcessed(siteId, commitId);
    }

    @Override
    public void insertGitLog(String siteId, String commitId, int processed) {
        localRepositoryV2.insertGitLog(siteId, commitId, processed);
    }

    @Override
    public void insertGitLog(String siteId, String commitId, int processed, int audited) {
        localRepositoryV2.insertGitLog(siteId, commitId, processed, audited);
    }

    @Override
    public List<String> getSubtreeItems(String site, String path) {
        return localRepositoryV2.getSubtreeItems(site, path).stream()
                .map(this::getOriginalPath)
                .collect(toList());
    }

    @Override
    public List<RepoOperation> getOperations(String site, String commitIdFrom, String commitIdTo) {
        return localRepositoryV2.getOperations(site, commitIdFrom, commitIdTo).stream()
                .peek(operation -> {
                    operation.setPath(getOriginalPath(operation.getPath()));
                    operation.setMoveToPath(getOriginalPath(operation.getMoveToPath()));
                })
                .collect(toList());
    }

    @Override
    public List<RepoOperation> getOperationsFromDelta(String site, String commitIdFrom, String commitIdTo) {
        return localRepositoryV2.getOperationsFromDelta(site, commitIdFrom, commitIdTo).stream()
                .peek(operation -> {
                    operation.setPath(getOriginalPath(operation.getPath()));
                    operation.setMoveToPath(getOriginalPath(operation.getMoveToPath()));
                })
                .collect(toList());
    }

    @Override
    public List<PublishingHistoryItem> getPublishingHistory(String siteId, String environment, String path,
                                                            String publisher, ZonedDateTime fromDate,
                                                            ZonedDateTime toDate, int limit) {
        return localRepositoryV2.getPublishingHistory(siteId, environment, path, publisher, fromDate, toDate, limit);
    }

    @Override
    public Item getItem(String siteId, String path, boolean flatten) {
        return localRepositoryV2.getItem(siteId, path, flatten);
    }

    @Override
    public String getLastEditCommitId(String siteId, String path) {
        return localRepositoryV2.getLastEditCommitId(siteId, path);
    }

    @Override
    public Map<String, String> getChangeSetPathsFromDelta(String site, String commitIdFrom, String commitIdTo) {
        Map<String, String> changeSet = localRepositoryV2.getChangeSetPathsFromDelta(site, commitIdFrom, commitIdTo);
        Map<String, String> newChangeSet = new TreeMap<>();

        changeSet.forEach((key, value) -> {
            String newKey = getOriginalPath(key);
            String newValue = getOriginalPath(value);
            newChangeSet.put(newKey, newValue);
        });

        return newChangeSet;
    }

    @Override
    public void markGitLogAudited(String siteId, String commitId) {
        localRepositoryV2.markGitLogAudited(siteId, commitId);
    }

    @Override
    public void updateGitlog(String siteId, String lastProcessedCommitId, int batchSize) {
        localRepositoryV2.updateGitlog(siteId, lastProcessedCommitId, batchSize);
    }

    @Override
    public List<GitLog> getUnauditedCommits(String siteId, int batchSize) {
        return localRepositoryV2.getUnauditedCommits(siteId, batchSize);
    }

    @Override
    public List<GitLog> getUnprocessedCommits(String siteId, long marker) {
        return localRepositoryV2.getUnprocessedCommits(siteId, marker);
    }

    @Override
    public DetailedItem.Environment getItemEnvironmentProperties(String siteId, GitRepositories repo,
                                                                 String environment, String path) {
        return localRepositoryV2.getItemEnvironmentProperties(siteId, repo, environment, path);
    }

    @Override
    public int countUnprocessedCommits(String siteId, long marker) {
        return localRepositoryV2.countUnprocessedCommits(siteId, marker);
    }

    @Override
    public void markGitLogProcessedBeforeMarker(String siteId, long marker, int processed) {
        localRepositoryV2.markGitLogProcessedBeforeMarker(siteId, marker, processed);
    }

    @Override
    public String getPreviousCommitId(String siteId, String commitId) {
        return localRepositoryV2.getPreviousCommitId(siteId, commitId);
    }

    @Override
    public void itemUnlock(String site, String path) {
        localRepositoryV2.itemUnlock(site, path);
    }

    @Override
    public void markGitLogVerifiedProcessedBulk(String siteId, List<String> commitIds) {
        localRepositoryV2.markGitLogVerifiedProcessedBulk(siteId, commitIds);
    }

    @Override
    public void upsertGitLogList(String siteId, List<String> commitIds, boolean processed, boolean audited) {
        localRepositoryV2.upsertGitLogList(siteId, commitIds, processed, audited);
    }

    @Override
    public boolean publishedRepositoryExists(String siteId) {
        return localRepositoryV2.publishedRepositoryExists(siteId);
    }

    @Override
    public void initialPublish(String siteId) throws SiteNotFoundException {
        try {
            List<StudioBlobStore> blobStores = blobStoreResolver.getAll(siteId);
            for (StudioBlobStore blobStore : blobStores) {
                blobStore.initialPublish(siteId);
            }
            localRepositoryV2.initialPublish(siteId);
        } catch (SiteNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error performing initial publish for site {0}", e, siteId);
        }
    }

    public RepositoryChanges publishAll(String siteId, String publishingTarget, String comment) throws ServiceLayerException {
        try {
            RepositoryChanges gitChanges = localRepositoryV2.preparePublishAll(siteId, publishingTarget);

            List<StudioBlobStore> blobStores = blobStoreResolver.getAll(siteId);
            for (StudioBlobStore blobStore : blobStores) {
                if (gitChanges.isInitialPublish()) {
                    blobStore.initialPublish(siteId);
                    continue;
                }

                // check if any of the changes belongs to the blob store
                Set<String> updatedBlobs = findCompatiblePaths(blobStore, gitChanges.getUpdatedPaths());
                Set<String> deletedBlobs = findCompatiblePaths(blobStore, gitChanges.getDeletedPaths());

                if (!(updatedBlobs.isEmpty() && deletedBlobs.isEmpty())) {
                    blobStore.completePublishAll(siteId, publishingTarget,
                                                 new RepositoryChanges(updatedBlobs, deletedBlobs), comment);
                }
            }

            localRepositoryV2.completePublishAll(siteId, publishingTarget, gitChanges, comment);

            Set<String> updatedFiles = translatePaths(gitChanges.getUpdatedPaths());
            Set<String> deletedFiles = translatePaths(gitChanges.getDeletedPaths());

            // Return an updated repository changes object with everything changed from git + blob
            return new RepositoryChanges(gitChanges.isInitialPublish(), updatedFiles, deletedFiles);
        } catch (Exception e) {
            localRepositoryV2.cancelPublishAll(siteId, publishingTarget);
            if (e instanceof ServiceLayerException) {
                throw e;
            } else {
                throw new ServiceLayerException("Error publishing all changes for site " + siteId + " in target " +
                                                publishingTarget, e);
            }
        }
    }

    protected Set<String> translatePaths(Set<String> paths) {
        return paths.stream()
                .map(this::getOriginalPath)
                .map(path -> prependIfMissing(path, FILE_SEPARATOR))
                .collect(toSet());
    }

    protected Set<String> findCompatiblePaths(BlobStore blobStore, Set<String> paths) {
        return paths.stream()
                    .map(path -> prependIfMissing(path, File.separator))
                    .filter(blobStore::isCompatible)
                    .map(this::getOriginalPath)
                    .collect(toSet());
    }

    @Override
    public RepositoryChanges preparePublishAll(String siteId, String publishingTarget) {
        // this method should not be called directly
        throw new UnsupportedOperationException();
    }

    @Override
    public void completePublishAll(String siteId, String publishingTarget, RepositoryChanges changes, String comment) {
        // this method should not be called directly
        throw new UnsupportedOperationException();
    }

    @Override
    public void cancelPublishAll(String siteId, String publishingTarget) {
        // this method should not be called directly
        throw new UnsupportedOperationException();
    }

    @Override
    public void populateGitLog(String siteId) throws GitAPIException, IOException {
        localRepositoryV2.populateGitLog(siteId);
    }

}
