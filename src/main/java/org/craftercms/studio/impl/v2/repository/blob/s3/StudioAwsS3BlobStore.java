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
package org.craftercms.studio.impl.v2.repository.blob.s3;

import com.amazonaws.services.s3.model.*;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.craftercms.commons.aws.AwsUtils;
import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.file.blob.Blob;
import org.craftercms.commons.file.blob.exception.BlobStoreException;
import org.craftercms.commons.file.blob.impl.s3.AwsS3BlobStore;
import org.craftercms.studio.api.v1.exception.BlobNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;
import org.craftercms.studio.api.v2.exception.blob.BlobStoreNotWritableModeException;
import org.craftercms.studio.api.v2.repository.RepositoryChanges;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStore;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStoreAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.beans.ConstructorProperties;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.*;
import static org.craftercms.commons.config.ConfigUtils.getBooleanProperty;
import static org.craftercms.studio.impl.v1.service.aws.AwsUtils.*;

/**
 * Implementation of {@link StudioBlobStore} for AWS S3
 *
 * @author joseross
 * @since 3.1.6
 */
public class StudioAwsS3BlobStore extends AwsS3BlobStore implements StudioBlobStoreAdapter {

    private static final Logger logger = LoggerFactory.getLogger(StudioAwsS3BlobStore.class);

    public static final String OK = "OK";

    protected ServicesConfig servicesConfig;

    protected boolean readOnly;

    private final ThreadPoolTaskExecutor taskExecutor;

    @ConstructorProperties({"servicesConfig", "taskExecutor"})
    public StudioAwsS3BlobStore(final ServicesConfig servicesConfig, final ThreadPoolTaskExecutor taskExecutor) {
        this.servicesConfig = servicesConfig;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void doInit(HierarchicalConfiguration<ImmutableNode> config) throws ConfigurationException {
        super.doInit(config);
        readOnly = getBooleanProperty(config, CONFIG_KEY_READ_ONLY, false);
    }


    /**
     * Checks that the blob store is in writable mode (readOnly = false) and throws
     * an exception if it is not.
     *
     * @throws BlobStoreNotWritableModeException is the blob store is in read-only mode
     */
    protected void checkReadWriteMode() throws ServiceLayerException {
        if (readOnly) {
            throw new BlobStoreNotWritableModeException(format("BlobStore '%s' is in read-only mode", id));
        }
    }

    protected boolean isFolder(String path) {
        return isEmpty(getExtension(path));
    }

    protected String getFullKey(Mapping mapping, String path) {
        return mapping.target + "/" + getKey(mapping,path);
    }

    @Override
    public Blob getReference(String path) {
        Mapping mapping = getMapping(publishingTargetResolver.getPublishingTarget());
        try {
            ObjectMetadata metadata = getClient().getObjectMetadata(mapping.target, getKey(mapping, path));
            return new Blob(id, metadata.getETag());
        } catch (Exception e) {
            throw new BlobStoreException("Error creating reference for content at " + getFullKey(mapping, path), e);
        }
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    // Start API 1

    @Override
    public boolean contentExists(String site, String path) {
        Mapping mapping = getMapping(publishingTargetResolver.getPublishingTarget());
        logger.debug("Check if content exists at site '{}' path '{}'", site, getFullKey(mapping, path));
        try {
            return getClient().doesObjectExist(mapping.target, getKey(mapping, path));
        } catch (Exception e) {
            logger.error("Failed to check if content exists at site '{}' path '{}'",
                    site, getFullKey(mapping, path), e);
            throw new BlobStoreException(format("Failed to check if content exists at site '%s' path '%s'",
                    site, getFullKey(mapping, path)), e);
        }
    }

    @Override
    public void checkContentExists(String site, String path) throws ServiceLayerException {
        if (!contentExists(site, path)) {
            throw new BlobNotFoundException(path, site, format("Content does not exist in S3 Blobstore at '%s' for site '%s'", path, site));
        }
    }

    @Override
    public boolean shallowContentExists(String site, String path) {
        return false;
    }

    @Override
    public InputStream getContent(String site, String path, boolean shallow) {
        Mapping previewMapping = getMapping(publishingTargetResolver.getPublishingTarget());
        logger.debug("Get content from site '{}' path '{}'", site, getFullKey(previewMapping, path));
        try {
            S3Object object = getClient().getObject(previewMapping.target, getKey(previewMapping, path));
            return object.getObjectContent();
        } catch (Exception e) {
            logger.error("Failed to get content from site '{}' path '{}'",
                    site, getFullKey(previewMapping, path), e);
            throw new BlobStoreException(format("Failed to get content from site '%s' path '%s'",
                    site, getFullKey(previewMapping, path)), e);
        }
    }

    @Override
    public long getContentSize(String site, String path) {
        Mapping previewMapping = getMapping(publishingTargetResolver.getPublishingTarget());
        logger.debug("Get content size from site '{}' path '{}'", site, getFullKey(previewMapping, path));
        try {
            ObjectMetadata metadata =
                    getClient().getObjectMetadata(previewMapping.target, getKey(previewMapping, path));
            return metadata.getContentLength();
        } catch (Exception e) {
            logger.error("Failed to get content size from site '{}' path '{}'",
                    site, getFullKey(previewMapping, path), e);
            throw new BlobStoreException(format("Failed to get content size from site '%s' path '%s'",
                    site, getFullKey(previewMapping, path)), e);
        }
    }

    @Override
    public String writeContent(String site, String path, InputStream content) throws ServiceLayerException {
        checkReadWriteMode();
        Mapping previewMapping = getMapping(publishingTargetResolver.getPublishingTarget());
        logger.debug("Upload content to site '{}' path '{}'", site, getFullKey(previewMapping, path));
        try {
            uploadStream(previewMapping.target,
                    getKey(previewMapping, path), getClient(), MIN_PART_SIZE, path, content);
            return OK;
        } catch (Exception e) {
            logger.error("Failed to upload content to site '{}' path '{}'",
                    site, getFullKey(previewMapping, path), e);
            throw new BlobStoreException(format("Failed to upload content to site '%s' path '%s'",
                    site, getFullKey(previewMapping, path)), e);
        }
    }

    @Override
    public String createFolder(String site, String path, String name) throws ServiceLayerException {
        checkReadWriteMode();
        // Do nothing, S3 has no folders
        return OK;
    }

    @Override
    public String deleteContent(String site, String path, String approver) throws ServiceLayerException {
        checkReadWriteMode();
        Mapping previewMapping = getMapping(publishingTargetResolver.getPublishingTarget());
        logger.debug("Delete content at site '{}' path '{}'", site, getFullKey(previewMapping, path));
        if (!isFolder(path)) {
            try {
                getClient().deleteObject(previewMapping.target, getKey(previewMapping, path));
            } catch (Exception e) {
                logger.error("Failed to delete content at site '{}' path '{}'",
                        site, getFullKey(previewMapping, path), e);
                throw new BlobStoreException(format("Failed to delete content at site '%s' path '%s'",
                        site, getFullKey(previewMapping, path)), e);
            }
        } else {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(previewMapping.target)
                    .withPrefix(appendIfMissing(getKey(previewMapping, path), "/"));
            do {
                try {
                    ListObjectsV2Result result = getClient().listObjectsV2(request);
                    request.setContinuationToken(result.getContinuationToken());

                    String[] keys = result.getObjectSummaries().stream()
                            .map(S3ObjectSummary::getKey)
                            .collect(toList())
                            .toArray(new String[]{});
                    if (ArrayUtils.isNotEmpty(keys)) {
                        logger.trace("Delete content items at site '{}' paths '{}' from bucket '{}'",
                                site, Arrays.toString(keys), previewMapping.target);
                        try {
                            getClient().deleteObjects(new DeleteObjectsRequest(previewMapping.target).withKeys(keys));
                        } catch (Exception e) {
                            logger.error("Failed to delete content items at site '{}' paths '{}' from bucket '{}'",
                                    site, Arrays.toString(keys), previewMapping.target, e);
                            throw new BlobStoreException(format("Failed to delete content items at site '%s' " +
                                            "paths '%s' from bucket '%s'",
                                            site, Arrays.toString(keys), previewMapping.target), e);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to list content items at site '{}' path '{}'",
                            site, getFullKey(previewMapping, path), e);
                    throw new BlobStoreException(format("Failed to list content items at site '%s' path '%s'",
                            site, getFullKey(previewMapping, path)), e);
                }
            } while(isNotEmpty(request.getContinuationToken()));
        }
        return OK;
    }

    @Override
    public Map<String, String> moveContent(String site, String fromPath, String toPath, String newName) throws ServiceLayerException {
        checkReadWriteMode();
        Mapping previewMapping = getMapping(publishingTargetResolver.getPublishingTarget());
        logger.debug("Move content in site '{}' from '{}' to '{}'", site,
                getFullKey(previewMapping, fromPath), getFullKey(previewMapping, toPath));
        if (isEmpty(newName)) {
            if (isFolder(fromPath)) {
                ListObjectsV2Request request = new ListObjectsV2Request()
                        .withBucketName(previewMapping.target)
                        .withPrefix(appendIfMissing(getKey(previewMapping, fromPath), "/"));
                do {
                    try {
                        ListObjectsV2Result result = getClient().listObjectsV2(request);
                        request.setContinuationToken(result.getContinuationToken());

                        String[] keys = result.getObjectSummaries().stream()
                                .map(S3ObjectSummary::getKey)
                                .collect(toList())
                                .toArray(new String[]{});

                        for (String key : keys) {
                            String filePath =
                                    Paths.get(getKey(previewMapping, fromPath)).relativize(Paths.get(key)).toString();
                            logger.trace("Move content item in site '{}' from '{}' to '{}'",
                                    site,
                                    getFullKey(previewMapping, key),
                                    getFullKey(previewMapping,toPath + "/" + filePath));
                            try {
                                copyFile(previewMapping.target, key, previewMapping.target,
                                        getKey(previewMapping, toPath + "/" + filePath), COPY_PART_SIZE, getClient());
                            } catch (Exception e) {
                                logger.error("Failed to copy content in site '{}' from '{}' to '{}'",
                                        site,
                                        getFullKey(previewMapping, key),
                                        getFullKey(previewMapping, toPath + "/" + filePath),
                                        e);
                                throw new BlobStoreException(format("Failed to copy content in site '%s' from '%s' " +
                                        "to '%s'", site,
                                        getFullKey(previewMapping, key),
                                        getFullKey(previewMapping, toPath + "/" + filePath)), e);
                            }
                        }
                        try {
                            getClient().deleteObjects(new DeleteObjectsRequest(previewMapping.target).withKeys(keys));
                        } catch (Exception e) {
                            logger.error("Failed to delete content in site '{}' paths '{}' from bucket '{}'",
                                    site, Arrays.toString(keys), previewMapping.target, e);
                            throw new BlobStoreException(format("Failed to delete content in site '%s' paths " +
                                            "'%s' from bucket '%s'",
                                            site, Arrays.toString(keys), previewMapping.target), e);
                        }
                    } catch (Exception e) {
                        logger.error("Failed to list content from site '{}' paths '{}'",
                                site, getFullKey(previewMapping, fromPath), e);
                        throw new BlobStoreException(format("Failed to list content from site '%s' paths '%s'",
                                site, getFullKey(previewMapping, fromPath)), e);
                    }
                } while(isNotEmpty(request.getContinuationToken()));
            } else {
                try {
                    copyFile(previewMapping.target, getKey(previewMapping, fromPath),
                            previewMapping.target, getKey(previewMapping, toPath), COPY_PART_SIZE, getClient());
                    getClient().deleteObject(previewMapping.target, getKey(previewMapping, fromPath));
                } catch (Exception e) {
                    logger.error("Failed to move content in site '{}' from '{}' to '{}'",
                            site,
                            getFullKey(previewMapping, fromPath),
                            getFullKey(previewMapping, toPath),
                            e);
                    throw new BlobStoreException(format("Failed to move content in site '%s' from '%s' to '%s'",
                            site,
                            getFullKey(previewMapping, fromPath),
                            getFullKey(previewMapping, toPath)), e);
                }
            }
        } else {
            //TODO: Check if this is really needed, it looks like newName is always null
            throw new UnsupportedOperationException();
        }
        return Collections.emptyMap();
    }

    @Override
    public String copyContent(String site, String fromPath, String toPath) throws ServiceLayerException {
        checkReadWriteMode();
        Mapping previewMapping = getMapping(publishingTargetResolver.getPublishingTarget());
        logger.debug("Copy content in site '{}' from '{}' to '{}'",
                site, getFullKey(previewMapping, fromPath), getFullKey(previewMapping, toPath));
        if (isFolder(fromPath)) {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(previewMapping.target)
                    .withPrefix(appendIfMissing(getKey(previewMapping, fromPath), "/"));
            do {
                try {
                    ListObjectsV2Result result = getClient().listObjectsV2(request);
                    request.setContinuationToken(result.getContinuationToken());

                    String[] keys = result.getObjectSummaries().stream()
                            .map(S3ObjectSummary::getKey)
                            .collect(toList())
                            .toArray(new String[]{});

                    for (String key : keys) {
                        String filePath =
                                Paths.get(getKey(previewMapping, fromPath)).relativize(Paths.get(key)).toString();
                        logger.trace("Copy content in site '{}' from '{}' to '{}'",
                                site,
                                getFullKey(previewMapping, key),
                                getFullKey(previewMapping, toPath + "/" + filePath));
                        try {
                            copyFile(previewMapping.target, key, previewMapping.target,
                                    getKey(previewMapping, toPath + "/" + filePath), COPY_PART_SIZE, getClient());
                        } catch (Exception e) {
                            logger.error("Failed to copy content in site '{}' from '{}' to '{}'",
                                    site,
                                    getFullKey(previewMapping, key),
                                    getFullKey(previewMapping, toPath + "/" + filePath),
                                    e);
                            throw new BlobStoreException(format("Failed to copy content in site '%s' from '%s' to '%s'",
                                    site,
                                    getFullKey(previewMapping, key),
                                    getFullKey(previewMapping, toPath + "/" + filePath)), e);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to list content in site '{}' at '{}'",
                            site, getFullKey(previewMapping, fromPath), e);
                    throw new BlobStoreException(format("Failed to list content in site '%s' at '%s'",
                            site, getFullKey(previewMapping, fromPath)), e);
                }
            } while (isNotEmpty(request.getContinuationToken()));
        } else {
            try {
                copyFile(previewMapping.target, getKey(previewMapping, fromPath), previewMapping.target,
                        getKey(previewMapping, toPath), COPY_PART_SIZE, getClient());
            } catch (Exception e) {
                logger.error("Failed to copy content in site '{}' from '{}' to '{}'",
                        site,
                        getFullKey(previewMapping, fromPath),
                        getFullKey(previewMapping, toPath),
                        e);
                throw new BlobStoreException(format("Failed to copy content in site '%s' from '%s' to '%s'",
                        site,
                        getFullKey(previewMapping, fromPath),
                        getFullKey(previewMapping, toPath)), e);
            }
        }
        return OK;
    }

    @Override
    public void publish(String site, String sandboxBranch, List<DeploymentItemTO> deploymentItems, String environment,
                        String author, String comment) {
        // If store is in readonly mode, nothing to do here.
        if (readOnly) {
            logger.warn("Publish request ignored in blobstore '{}' because it is readonly", id);
            return;
        }

        Mapping previewMapping = getMapping(publishingTargetResolver.getPublishingTarget());
        Mapping envMapping = getMapping(environment);
        logger.debug("Publish content in site '{}' from bucket '{}' to bucket '{}'",
                site, previewMapping.target, envMapping.target);
        for (DeploymentItemTO item : deploymentItems) {
            if (item.isDelete()) {
                logger.trace("Delete content at site '{}' path '{}'", site, getFullKey(envMapping, item.getPath()));
                try {
                    getClient().deleteObject(envMapping.target, getKey(envMapping, item.getPath()));
                    if (isNotEmpty(item.getOldPath())) {
                        logger.trace("Delete content at site '{}' path '{}'",
                                site, getFullKey(envMapping, item.getOldPath()));
                        getClient().deleteObject(envMapping.target, getKey(envMapping, item.getOldPath()));
                    }
                } catch (Exception e) {
                    logger.error("Failed to delete content at site '{}' path '{}'",
                            site, getFullKey(previewMapping, item.getPath()), e);
                    throw new BlobStoreException(format("Failed to delete content at site '%s' path '%s'",
                            site, getFullKey(previewMapping, item.getPath())), e);
                }
            } else if (item.isMove()) {
                logger.trace("Move content in site '{}' from '{}' to '{}'",
                        site, getFullKey(envMapping, item.getOldPath()), getFullKey(envMapping, item.getPath()));
                try {
                    copyFile(previewMapping.target, getKey(previewMapping, item.getPath()), envMapping.target,
                            getKey(envMapping, item.getPath()), COPY_PART_SIZE, getClient());
                    if (!StringUtils.equals(item.getOldPath(), item.getPath())) {
                        getClient().deleteObject(envMapping.target, getKey(envMapping, item.getOldPath()));
                    }
                } catch (Exception e) {
                    logger.error("Failed to move content in site '{}' from '{}' to '{}'",
                            site,
                            getFullKey(envMapping, item.getOldPath()),
                            getFullKey(envMapping, item.getPath()),
                            e);
                    throw new BlobStoreException(format("Failed to move content in site '%s' from '%s' to '%s'",
                            site,
                            getFullKey(envMapping, item.getOldPath()),
                            getFullKey(envMapping, item.getPath())), e);
                }
            } else {
                logger.trace("Copy content in site '{}' from '{}' to '{}'",
                        site, getFullKey(previewMapping, item.getPath()), getFullKey(envMapping, item.getPath()));
                try {
                    copyFile(previewMapping.target, getKey(previewMapping, item.getPath()), envMapping.target,
                            getKey(envMapping, item.getPath()), COPY_PART_SIZE, getClient());
                } catch (Exception e) {
                    logger.error("Failed to copy content in site '{}' from '{}' to '{}'",
                            site,
                            getFullKey(previewMapping, item.getPath()),
                            getFullKey(envMapping, item.getPath()),
                            e);
                    throw new BlobStoreException(format("Failed to copy content in site '%s' from '%s' to '%s'",
                            site,
                            getFullKey(previewMapping, item.getPath()),
                            getFullKey(envMapping, item.getPath())), e);
                }
            }
        }
    }

    @Override
    public void initialPublish(String siteId) {
        // If store is in readonly mode, nothing to do here.
        if (readOnly) {
            logger.warn("Initial publish request ignored in blobstore '{}' because it is readonly", id);
            return;
        }
        Mapping previewMapping = getMapping(publishingTargetResolver.getPublishingTarget());
        Mapping liveMapping = getMapping(servicesConfig.getLiveEnvironment(siteId));

        logger.debug("Perform initial publish for site '{}' ", siteId);

        logger.debug("Perform initial publish for site '{}' to target 'live'", siteId);
        copyFolder(previewMapping.target, previewMapping.prefix, liveMapping.target, liveMapping.prefix,
                MIN_PART_SIZE, getClient());

        if (servicesConfig.isStagingEnvironmentEnabled(siteId)) {
            Mapping statingMapping = getMapping(servicesConfig.getStagingEnvironment(siteId));

            logger.debug("Perform initial publish for site '{}' to target 'staging'", siteId);
            copyFolder(previewMapping.target, previewMapping.prefix, statingMapping.target, statingMapping.prefix,
                    MIN_PART_SIZE, getClient());
        }
    }

    @Override
    public RepositoryChanges publishAll(String siteId, String publishingTarget, String comment) {
        // TODO: segregate these interfaces properly
        // this method should not be called
        throw new UnsupportedOperationException();
    }

    @Override
    public RepositoryChanges preparePublishAll(String siteId, String publishingTarget) {
        // TODO: segregate these interfaces properly
        // this method should not be called
        throw new UnsupportedOperationException();
    }

    @Override
    public void completePublishAll(String siteId, String publishingTarget, RepositoryChanges changes, String comment) {
        // If store is in readonly mode, nothing to do here.
        if (readOnly) {
            logger.warn("'Complete publish all' request ignored in blobstore '{}' because it is readonly", id);
            return;
        }
        Mapping previewMapping = getMapping(publishingTargetResolver.getPublishingTarget());
        Mapping targetMapping = getMapping(publishingTarget);

        logger.info("Perform Publish All for site '{}' to target '{}'", siteId, targetMapping);

        for (String updatedPath : changes.getUpdatedPaths()) {
            try {
                // TODO: check if readonly? Or just ignore?
                copyFile(previewMapping.target, getKey(previewMapping, updatedPath), targetMapping.target,
                        getKey(targetMapping, updatedPath), COPY_PART_SIZE, getClient());
            } catch (Exception e) {
                logger.error("Failed to copy '{}' from bucket '{}' to bucket '{}' for site '{}': {}", updatedPath, previewMapping.target,
                        targetMapping.target, siteId, e.getMessage());
                changes.getFailedPaths().add(updatedPath);
            }
        }

        DeleteObjectsRequest request = new DeleteObjectsRequest(targetMapping.target);
        for (List<String> batch : ListUtils.partition(new LinkedList<>(changes.getDeletedPaths()), DELETE_BATCH_SIZE)) {
            request.withKeys(batch.stream()
                                  .map(path -> getKey(targetMapping, path))
                                  .toArray(String[]::new));
            getClient().deleteObjects(request);
        }

        logger.info("Completed Publish All for site '{}' to target '{}'", siteId, targetMapping);
    }

    @Override
    public void cancelPublishAll(String siteId, String publishingTarget) {
        // TODO: segregate these interfaces properly
        // this method should not be called
        throw new UnsupportedOperationException();
    }

    @Override
    public void populateGitLog(String siteId) {
        // TODO: segregate these interfaces properly
        // this method should not be called
        throw new UnsupportedOperationException();
    }

    @Override
    public void duplicateSite(String sourceSiteId, String siteId, String sourceSandboxBranch, String sandboxBranch) {
        // TODO: segregate these interfaces properly
        throw new UnsupportedOperationException();
    }

    @Override
    public void copyBlobs(final StudioBlobStore source, final String environment, final List<String> items) {
        if (!(source instanceof StudioAwsS3BlobStore sourceStudioBlobStore)) {
            throw new UnsupportedOperationException("Source blob store is not an instance of StudioAwsS3BlobStore");
        }

        Mapping sourceMapping = sourceStudioBlobStore.getMapping(environment);
        Mapping targetMapping = getMapping(environment);

        try {
            if (targetMapping.equals(sourceMapping)) {
                logger.info("Source and target mappings are the same, skipping copy operation");
                return;
            }
            AwsUtils.copyObjects(getClient(), taskExecutor::getThreadPoolExecutor,
                    sourceMapping.target, sourceMapping.prefix,
                    targetMapping.target, targetMapping.prefix, items);
        } catch (InterruptedException e) {
            throw new BlobStoreException(format("Interrupted while waiting for S3 content duplication from site '%s' to '%s'", sourceMapping.target, targetMapping.target), e);
        }
    }
}
