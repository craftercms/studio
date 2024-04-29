/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.config.PublishingTargetResolver;
import org.craftercms.commons.file.blob.Blob;
import org.craftercms.commons.file.blob.BlobStore;
import org.craftercms.commons.file.blob.exception.BlobStoreConfigurationMissingException;
import org.craftercms.core.service.Item;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.exception.BlobNotFoundException;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;
import org.craftercms.studio.api.v1.to.RemoteRepositoryInfoTO;
import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.api.v2.dal.GitLog;
import org.craftercms.studio.api.v2.dal.PublishingHistoryItem;
import org.craftercms.studio.api.v2.dal.RepoOperation;
import org.craftercms.studio.api.v2.exception.RepositoryLockedException;
import org.craftercms.studio.api.v2.repository.RepositoryChanges;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobAwareContentRepository;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStore;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStoreResolver;
import org.craftercms.studio.impl.v1.repository.git.GitContentRepository;
import org.craftercms.studio.model.history.ItemVersion;
import org.craftercms.studio.model.rest.content.DetailedItem;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.eclipse.jgit.lib.Constants.HEAD;

/**
 * Implementation of {@link ContentRepository}, {@link org.craftercms.studio.api.v2.repository.ContentRepository}
 * that delegates calls to a {@link StudioBlobStore} when appropriate
 *
 * @author joseross
 * @since 3.1.6
 */
public class BlobAwareContentRepository implements ContentRepository, StudioBlobAwareContentRepository {

    private static final Logger logger = LoggerFactory.getLogger(BlobAwareContentRepository.class);

    /**
     * The extension for the blob files
     */
    protected String fileExtension;

    protected GitContentRepository localRepositoryV1;

    protected org.craftercms.studio.api.v2.repository.ContentRepository localRepositoryV2;

    protected StudioBlobStoreResolver blobStoreResolver;
    private ServicesConfig servicesConfig;

    protected final ObjectMapper objectMapper = new XmlMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public void setServicesConfig(final ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public void setLocalRepositoryV1(GitContentRepository localRepositoryV1) {
        this.localRepositoryV1 = localRepositoryV1;
    }

    public void setLocalRepositoryV2(org.craftercms.studio.api.v2.repository.ContentRepository localRepositoryV2) {
        this.localRepositoryV2 = localRepositoryV2;
    }

    public void setBlobStoreResolver(StudioBlobStoreResolver blobStoreResolver) {
        this.blobStoreResolver = blobStoreResolver;
    }

    protected String getOriginalPath(String path) {
        return StringUtils.removeEnd(path, "." + fileExtension);
    }

    protected String getPointerPath(String siteId, String path) {
        return isFolder(siteId, path) ? path : appendIfMissing(path, "." + fileExtension);
    }

    protected String getPathFromPointerPath(String siteId, String pointerPath) {
        return isFolder(siteId, pointerPath) ? pointerPath : removeEnd(pointerPath, "." + fileExtension);
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
        logger.debug("Check if content '{}' exists in site '{}'", path, site);
        try {
            if (!isFolder(site, path) && pointersExist(site, path)) {
                StudioBlobStore store = getBlobStore(site, path);
                if (store != null) {
                    return store.contentExists(site, normalize(path));
                }
            }
            return localRepositoryV1.contentExists(site, path);
        } catch (Exception e) {
            logger.error("Failed to check if content exists in site '{}' path '{}'", site, path, e);
            return false;
        }
    }

    @Override
    public void checkContentExists(String site, String path) throws ServiceLayerException {
        if (!isFolder(site, path) && pointersExist(site, path)) {
            StudioBlobStore store = getBlobStore(site, path);
            if (store == null) {
                logger.error("Pointer exists for path '{}' in site '{}', but blob store could not be found", path, site);
                throw new BlobNotFoundException(path, site, format("Pointer exists for path '%s' in site '%s', " +
                        "but blob store could not be found", path, site));
            }

            store.checkContentExists(site, normalize(path));
        } else if (!localRepositoryV1.contentExists(site, path)) {
            throw new ContentNotFoundException(path, site, "Content not found");
        }
    }

    @Override
    public boolean shallowContentExists(String site, String path) {
        logger.debug("Check if content '{}' exists in site '{}'", path, site);
        try {
            // Return only if the pointer exists, otherwise do the regular call
            if (!isFolder(site, path) && pointersExist(site, path)) {
                return true;
            }
            return localRepositoryV2.shallowContentExists(site, path);
        } catch (Exception e) {
            logger.error("Failed to check if content exists in site '{}' path '{}'", site, path, e);
            return false;
        }
    }

    @Override
    public InputStream getContent(String site, String path, boolean shallow) {
        logger.debug("Get content from site '{}' path '{}'", site, path);
        try {
            if (!isFolder(site, path) && pointersExist(site, path)) {
                StudioBlobStore store = getBlobStore(site, path);
                if (store != null) {
                    return store.getContent(site, normalize(path), shallow);
                }
            }
            return localRepositoryV1.getContent(site, path, shallow);
        } catch (Exception e) {
            logger.error("Failed to get content from site '{}' path '{}'", site, path, e);
            return null;
        }
    }

    @Override
    public long getContentSize(String site, String path) {
        logger.debug("Get content size from site '{}' path '{}'", site, path);
        try {
            if (pointersExist(site, path)) {
                StudioBlobStore store = getBlobStore(site, path);
                if (store != null) {
                    // Don't populate the file size for blob-store backed files due to performance reasons
                    logger.trace("Returning -1 for the blobstore item size to avoid a slow API call");
                    return -1L;
                    // return store.getContentSize(site, normalize(path));
                }
            }
            return localRepositoryV2.getContentSize(site, path);
        } catch (Exception e) {
            logger.error("Failed to get content size from site '{}' path '{}'", site, path, e);
            return -1L;
        }
    }

    @Override
    public String writeContent(String site, String path, InputStream content) throws ServiceLayerException {
        logger.debug("Write content to site '{}' path '{}'", site, path);
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
            logger.debug("No blob store configuration found for site '{}', " +
                    "will write '{}' to the local repository", site, path);
            return localRepositoryV1.writeContent(site, path, content);
        } catch (RepositoryLockedException | ServiceLayerException e) {
            logger.error("Failed to write content to site '{}' path '{}'", site, path, e);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to write content to site '{}' path '{}'", site, path, e);
            throw new ServiceLayerException(e);
        }
    }

    @Override
    public String createFolder(String site, String path, String name) {
        logger.debug("Create folder in site '{}' path '{}'", site, path);
        try {
            StudioBlobStore store = getBlobStore(site, path);
            if (store != null) {
                store.createFolder(site, normalize(path), name);
            }
            return localRepositoryV1.createFolder(site, path, name);
        } catch (BlobStoreConfigurationMissingException e) {
            logger.debug("No blob store configuration found for site '{}', " +
                    "will create folder '{}' in the local repository", site, path);
            return localRepositoryV1.createFolder(site, path, name);
        } catch (Exception e) {
            logger.error("Failed to create folder in site '{}' path '{}'", site, path, e);
            return null;
        }
    }

    @Override
    public String deleteContent(String site, String path, String approver) throws ServiceLayerException {
        logger.debug("Delete content in site '{}' path '{}'", site, path);
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
            logger.debug("No blob store configuration found for site '{}', " +
                    "will delete '{}' in the local repository", site, path);
            return localRepositoryV1.deleteContent(site, path, approver);
        } catch (ServiceLayerException ex) {
            throw ex;
        } catch (Exception e) {
            logger.error("Failed to delete content in site '{}' path '{}'", site, path, e);
            throw new ServiceLayerException(format("Failed to delete content in site '%s' path '%s'", site, path), e);
        }
    }

    @Override
    public Map<String, String> moveContent(String site, String fromPath, String toPath, String newName) {
        logger.debug("Move content in site '{}' from '{}' to '{}'", site, fromPath, toPath);
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
            logger.debug("No blob store configuration found for site '{}', " +
                    "will move from '{}' to '{}' in the local repository", site, fromPath, toPath);
            return localRepositoryV1.moveContent(site, fromPath, toPath, newName);
        } catch (Exception e) {
            logger.error("Failed to move content in site '{}' from '{}' to '{}'", site, fromPath, toPath, e);
            return null;
        }
    }

    @Override
    public String copyContent(String site, String fromPath, String toPath) {
        logger.debug("Copy content in site '{}' from '{}' to '{}'", site, fromPath, toPath);
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
            logger.debug("No blob store configuration found for site '{}', " +
                    "will copy from '{}' to '{}' in the local repository", site, fromPath, toPath);
            return localRepositoryV1.copyContent(site, fromPath, toPath);
        } catch (Exception e) {
            logger.error("Failed to copy content in site '{}' from '{}' to '{}'", site, fromPath, toPath, e);
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
        logger.debug("Get version history for site '{}' path '{}'", site, path);
        try {
            if (pointersExist(site, path)) {
                StudioBlobStore store = getBlobStore(site, path);
                if (store != null) {
                    return localRepositoryV1.getContentVersionHistory(site, getPointerPath(site, path));
                }
            }
            return localRepositoryV1.getContentVersionHistory(site, path);
        } catch (Exception e) {
            logger.error("Failed to get version history for site '{}' path '{}'", site, path, e);
            return null;
        }
    }

    @Override
    public List<ItemVersion> getContentItemHistory(String site, String path) {
        logger.debug("Get version history for site '{}' path '{}'", site, path);
        try {
            if (pointersExist(site, path)) {
                StudioBlobStore store = getBlobStore(site, path);
                if (store != null) {
                    return localRepositoryV2.getContentItemHistory(site, getPointerPath(site, path));
                }
            }
            return localRepositoryV2.getContentItemHistory(site, path);
        } catch (Exception e) {
            logger.error("Failed to get version history for site '{}' path '{}'", site, path, e);
            return null;
        }
    }

    @Override
    public void duplicateSite(String sourceSiteId, String siteId, String sourceSandboxBranch, String sandboxBranch) throws IOException, ServiceLayerException {
        localRepositoryV2.duplicateSite(sourceSiteId, siteId, sourceSandboxBranch, sandboxBranch);
    }

    @Override
    public void duplicateBlobs(String sourceSiteId, String siteId) throws ServiceLayerException {
        logger.info("Duplicating preview blobs from site '{}' to site '{}'", sourceSiteId, siteId);
        duplicateBlobs(sourceSiteId, siteId, GitRepositories.SANDBOX, PublishingTargetResolver.PREVIEW, HEAD);

        if (publishedRepositoryExists(siteId)) {
            if (servicesConfig.isStagingEnvironmentEnabled(siteId)) {
                logger.info("Duplicating staging blobs from site '{}' to site '{}'", sourceSiteId, siteId);
                String stagingEnvironment = servicesConfig.getStagingEnvironment(siteId);
                if (localRepositoryV2.commitIdExists(sourceSiteId, GitRepositories.PUBLISHED, stagingEnvironment)) {
                    duplicateBlobs(sourceSiteId, siteId, GitRepositories.PUBLISHED, stagingEnvironment, stagingEnvironment);
                }
            }

            logger.info("Duplicating live blobs from site '{}' to site '{}'", sourceSiteId, siteId);
            String liveEnvironment = servicesConfig.getLiveEnvironment(siteId);
            duplicateBlobs(sourceSiteId, siteId, GitRepositories.PUBLISHED, liveEnvironment, liveEnvironment);
        }
    }

    /**
     * Duplicates the blobs from the source site to the target site
     *
     * @param sourceSiteId the source site
     * @param siteId       the target site
     * @param repoType     the repository type
     * @param environment  the environment
     * @param revstr       A git object references expression (e.g.: HEAD, branch name, commit id)
     * @throws ServiceLayerException if an error occurs during the operation
     */
    private void duplicateBlobs(String sourceSiteId, String siteId, GitRepositories repoType, String environment, String revstr) throws ServiceLayerException {
        List<String> siteItemPaths = localRepositoryV2.getItemPaths(sourceSiteId, repoType, revstr)
                .stream().filter(p -> p.endsWith("." + fileExtension)).toList();
        MultiKeyMap<StudioBlobStore, List<String>> copyItems = new MultiKeyMap<>();
        for (String path : siteItemPaths) {
            String assetPath = getOriginalPath(path);
            StudioBlobStore sourceBlobStore = blobStoreResolver.getByPaths(sourceSiteId, assetPath);
            StudioBlobStore targetBlobStore = blobStoreResolver.getByPaths(siteId, assetPath);
            copyItems.compute(new MultiKey<>(sourceBlobStore, targetBlobStore),
                    (MultiKey<? extends StudioBlobStore> k, List<String> currentPaths) -> {
                if (currentPaths == null) {
                    currentPaths = new LinkedList<>();
                }
                currentPaths.add(assetPath);
                return currentPaths;
            });
        }

        for (Map.Entry<MultiKey<? extends StudioBlobStore>, List<String>> copyItem : copyItems.entrySet()) {
            StudioBlobStore sourceBlobStore = copyItem.getKey().getKey(0);
            StudioBlobStore targetBlobStore = copyItem.getKey().getKey(1);
            List<String> paths = copyItem.getValue();
            try {
                targetBlobStore.copyBlobs(sourceBlobStore, environment, paths);
            } catch (Exception e) {
                logger.error("Failed to copy blob from source site '{}' to target site '{}'", sourceSiteId, siteId, e);
            }
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
        pointer.setOldPath(isEmpty(item.getOldPath()) ? item.getOldPath() : getPointerPath(item.getSite(), item.getOldPath()));
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
        logger.debug("Publish the items '{}' in site '{}' to target '{}'", deploymentItems, site, environment);
        Map<String, StudioBlobStore> stores = new LinkedHashMap<>();
        MultiValueMap<String, DeploymentItemTO> items = new LinkedMultiValueMap<>();
        List<DeploymentItemTO> localItems = new LinkedList<>();
        try {
            for (DeploymentItemTO item : deploymentItems) {
                boolean pointerExists = pointersExist(site, item.getPath()) &&
                        (isEmpty(item.getOldPath()) || pointersExist(site, item.getOldPath()));
                if (pointerExists || item.isDelete() || item.isMove()) {
                    logger.trace("Look for the blob store for the item at site '{}' path '{}'", site, item);
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
                logger.trace("Publish the blobs in site '{}' to target '{}' using the store '{}'",
                        site, environment, storeId);
                stores.get(storeId).publish(site, sandboxBranch, items.get(storeId), environment, author, comment);
            }
            logger.debug("Publish the local files in site '{}' to target '{}'", site, environment);
            localRepositoryV2.publish(site, sandboxBranch, localItems, environment, author, comment);
        } catch (Exception e) {
            logger.error("Failed to publish items in site '{}' to target '{}'", site, environment, e);
            throw new DeploymentException(format("Failed to publish items in site '%s' to target '%s'",
                    site, environment), e);
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
    public List<String> getSubtreeItems(String site, String path, GitRepositories repoType, String branch) {
        return localRepositoryV2.getSubtreeItems(site, path, repoType, branch).stream()
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
            logger.error("Failed to perform the initial publish for site '{}'", siteId, e);
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
                Collection<String> updatedBlobs = findCompatiblePaths(blobStore, gitChanges.getUpdatedPaths());
                Collection<String> deletedBlobs = findCompatiblePaths(blobStore, gitChanges.getDeletedPaths());

                if (!(updatedBlobs.isEmpty() && deletedBlobs.isEmpty())) {
                    RepositoryChanges blobChanges = new RepositoryChanges(updatedBlobs, deletedBlobs);
                    blobStore.completePublishAll(siteId, publishingTarget,
                            blobChanges, comment);
                    // Translate paths back to xxxx.blob
                    blobChanges.getFailedPaths().stream()
                            .map(this::getRepoPath)
                            .forEach(gitChanges.getFailedPaths()::add);
                }
            }

            localRepositoryV2.completePublishAll(siteId, publishingTarget, gitChanges, comment);

            Collection<String> updatedFiles = translatePaths(gitChanges.getUpdatedPaths());
            Collection<String> deletedFiles = translatePaths(gitChanges.getDeletedPaths());
            Collection<String> failedFiles = translatePaths(gitChanges.getFailedPaths());

            // Return an updated repository changes object with everything changed from git + blob
            return new RepositoryChanges(gitChanges.isInitialPublish(), updatedFiles, deletedFiles, failedFiles);
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

    /**
     * Gets an asset path and translate it
     * to the actual file path in git repo.
     * e.g.:
     * /static-assets/test/my-image.png
     * to
     * static-assets/test/my-image.png.blob
     *
     * @param blobPath the asset path
     * @return the git repo path
     */
    protected String getRepoPath(final String blobPath) {
        return removeStart(appendIfMissing(blobPath, "." + fileExtension), File.separator);
    }

    protected Collection<String> translatePaths(Collection<String> paths) {
        return paths.stream()
                .map(this::getOriginalPath)
                .map(path -> prependIfMissing(path, FILE_SEPARATOR))
                .collect(toList());
    }

    protected Collection<String> findCompatiblePaths(BlobStore blobStore, Collection<String> paths) {
        return paths.stream()
                .map(path -> prependIfMissing(path, File.separator))
                .filter(blobStore::isCompatible)
                .map(this::getOriginalPath)
                .collect(toList());
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
