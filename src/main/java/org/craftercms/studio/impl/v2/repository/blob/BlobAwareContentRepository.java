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
import org.craftercms.commons.file.blob.exception.BlobStoreConfigurationMissingException;
import org.craftercms.core.service.Item;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.exception.BlobNotFoundException;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.to.RemoteRepositoryInfoTO;
import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.api.v2.annotation.LogExecutionTime;
import org.craftercms.studio.api.v2.dal.RepoOperation;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.repository.PublishItemTO;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobAwareContentRepository;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStore;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStoreResolver;
import org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryImpl;
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
import java.util.*;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.union;
import static org.apache.commons.lang3.StringUtils.*;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.PackageType.PUBLISH_ALL;
import static org.eclipse.jgit.lib.Constants.HEAD;

/**
 * Implementation of {@link ContentRepository}, {@link org.craftercms.studio.api.v2.repository.ContentRepository}
 * that delegates calls to a {@link StudioBlobStore} when appropriate
 *
 * @author joseross
 * @since 3.1.6
 */
public class BlobAwareContentRepository implements org.craftercms.studio.api.v1.repository.GitContentRepository,
        StudioBlobAwareContentRepository,
        org.craftercms.studio.api.v2.repository.GitContentRepository {

    private static final Logger logger = LoggerFactory.getLogger(BlobAwareContentRepository.class);

    /**
     * The extension for the blob files
     */
    protected String fileExtension;

    protected GitContentRepositoryImpl localRepositoryV1;

    protected org.craftercms.studio.api.v2.repository.GitContentRepository localRepositoryV2;

    protected StudioBlobStoreResolver blobStoreResolver;
    private ServicesConfig servicesConfig;

    protected final ObjectMapper objectMapper = new XmlMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public void setServicesConfig(final ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public void setLocalRepositoryV1(GitContentRepositoryImpl localRepositoryV1) {
        this.localRepositoryV1 = localRepositoryV1;
    }

    public void setLocalRepositoryV2(org.craftercms.studio.api.v2.repository.GitContentRepository localRepositoryV2) {
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
        if (StringUtils.isEmpty(site)) {
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
        } catch (ServiceLayerException e) {
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
    public RepositoryItem[] getContentChildren(String site, String path) {
        RepositoryItem[] children = localRepositoryV1.getContentChildren(site, path);
        return Stream.of(children)
                .peek(item -> item.name = getOriginalPath(item.name))
                .toList()
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
        return localRepositoryV2.isFolder(siteId, path);
    }

    // TODO: Remove when the API is split

    @Override
    public boolean deleteSite(String siteId) {
        return localRepositoryV2.deleteSite(siteId);
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
    public List<String> getSubtreeItems(String site, String path, GitRepositories repoType, String branch) {
        return localRepositoryV2.getSubtreeItems(site, path, repoType, branch).stream()
                .map(this::getOriginalPath)
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
    public Item getItem(String siteId, String path, boolean flatten) {
        return localRepositoryV2.getItem(siteId, path, flatten);
    }

    @Override
    @LogExecutionTime
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
    public DetailedItem.Environment getItemEnvironmentProperties(String siteId, GitRepositories repo,
                                                                 String environment, String path) {
        return localRepositoryV2.getItemEnvironmentProperties(siteId, repo, environment, path);
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
    public boolean publishedRepositoryExists(String siteId) {
        return localRepositoryV2.publishedRepositoryExists(siteId);
    }

    @Override
    public String initialPublish(final String siteId) throws ServiceLayerException {
        List<StudioBlobStore> blobStores = blobStoreResolver.getAll(siteId);
        for (StudioBlobStore blobStore : blobStores) {
            blobStore.initialPublish(siteId);
        }
        return localRepositoryV2.initialPublish(siteId);
    }

    @Override
    public <T extends PublishItemTO> GitPublishChangeSet<T> publishAll(final PublishPackage publishPackage,
                                                                       final String publishingTarget,
                                                                       final Collection<T> publishItems) throws ServiceLayerException {
        return publishInternal(publishPackage, publishingTarget, publishItems);
    }

    @Override
    public <T extends PublishItemTO> GitPublishChangeSet<T> publish(final PublishPackage publishPackage,
                                                                 final String publishingTarget,
                                                                 final Collection<T> publishItems) throws ServiceLayerException {
        return publishInternal(publishPackage, publishingTarget, publishItems);
    }

    private <T extends PublishItemTO> GitPublishChangeSet<T> publishInternal(final PublishPackage publishPackage,
                                                                             final String publishingTarget,
                                                                             final Collection<T> publishItems) throws ServiceLayerException {
        List<StudioBlobStore> blobStores = blobStoreResolver.getAll(publishPackage.getSite().getSiteId());
        List<T> failedItems = new LinkedList<>();

        List<BlobAwarePublishItemTOWrapper<T>> gitRepoItems = new LinkedList<>();
        MultiValueMap<StudioBlobStore, BlobAwarePublishItemTOWrapper<T>> itemsByBlobStore = new LinkedMultiValueMap<>();

        for (T publishItem : publishItems) {
            Optional<StudioBlobStore> blobStore = blobStores.stream().filter(store -> store.isCompatible(publishItem.getPath())).findFirst();
            blobStore.ifPresentOrElse(
                    store -> itemsByBlobStore.add(store, new BlobAwarePublishItemTOWrapper<>(publishItem, getOriginalPath(publishItem.getPath()))),
                    () -> gitRepoItems.add(new BlobAwarePublishItemTOWrapper<>(publishItem, publishItem.getPath())));
        }

        itemsByBlobStore.forEach((blobStore, blobStoreItems) -> {
            StudioBlobStore.PublishChangeSet<BlobAwarePublishItemTOWrapper<T>> storeChangeset = blobStore.publish(publishPackage,
                    publishingTarget, blobStoreItems);

            failedItems.addAll(storeChangeset.failedItems().stream()
                    .map(BlobAwarePublishItemTOWrapper::getWrappedItem)
                    .toList());
            gitRepoItems.addAll(storeChangeset.successfulItems().stream()
                    .map(BlobAwarePublishItemTOWrapper::getWrappedItem)
                    .map(item -> new BlobAwarePublishItemTOWrapper<>(item, getRepoPath(item.getPath())))
                    .toList());
        });

        GitPublishChangeSet<BlobAwarePublishItemTOWrapper<T>> committedChangeset;
        if (isEmpty(failedItems) && publishPackage.getPackageType() == PUBLISH_ALL) {
            committedChangeset = localRepositoryV2.publishAll(publishPackage, publishingTarget, gitRepoItems);
        } else {
            committedChangeset = localRepositoryV2.publish(publishPackage, publishingTarget, gitRepoItems);
        }

        return new GitPublishChangeSet<>(committedChangeset.commitId(), committedChangeset.successfulItems().stream().map(BlobAwarePublishItemTOWrapper::getWrappedItem).toList(),
                union(failedItems, committedChangeset.failedItems().stream().map(BlobAwarePublishItemTOWrapper::getWrappedItem).toList()));
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

    @Override
    public List<String> getCommitIdsBetween(String siteId, final String commitFrom, final String commitTo) throws IOException {
        return localRepositoryV2.getCommitIdsBetween(siteId, commitFrom, commitTo);
    }

    @Override
    public List<String> getIntroducedCommits(String site, String baseCommit, String commitId) throws IOException, GitAPIException {
        return localRepositoryV2.getIntroducedCommits(site, baseCommit, commitId);
    }

    @Override
    public List<String> validatePublishCommits(final String siteId, final Collection<String> commitIds) throws IOException, ServiceLayerException {
        return localRepositoryV2.validatePublishCommits(siteId, commitIds);
    }

    @Override
    public void updateRef(final String siteId, final long packageId,
                          final String commitId, final String target) throws IOException {
        localRepositoryV2.updateRef(siteId, packageId, commitId, target);
    }
}
