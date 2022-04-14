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
package org.craftercms.studio.impl.v2.repository.blob.s3;

import com.amazonaws.services.s3.model.*;
import org.apache.commons.lang3.ArrayUtils;
import org.craftercms.commons.file.blob.Blob;
import org.craftercms.commons.file.blob.exception.BlobStoreException;
import org.craftercms.commons.file.blob.impl.s3.AwsS3BlobStore;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStoreAdapter;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStore;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.*;
import static org.craftercms.studio.impl.v1.service.aws.AwsUtils.COPY_PART_SIZE;
import static org.craftercms.studio.impl.v1.service.aws.AwsUtils.MIN_PART_SIZE;
import static org.craftercms.studio.impl.v1.service.aws.AwsUtils.copyFile;
import static org.craftercms.studio.impl.v1.service.aws.AwsUtils.copyFolder;
import static org.craftercms.studio.impl.v1.service.aws.AwsUtils.uploadStream;

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

    public StudioAwsS3BlobStore(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
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

    // Start API 1

    @Override
    public boolean contentExists(String site, String path) {
        Mapping mapping = getMapping(publishingTargetResolver.getPublishingTarget());
        logger.debug("Checking if content exists at {0}", getFullKey(mapping, path));
        try {
            return getClient().doesObjectExist(mapping.target, getKey(mapping, path));
        } catch (Exception e) {
            throw new BlobStoreException("Error checking if content exists at " + getFullKey(mapping, path), e);
        }
    }

    @Override
    public boolean shallowContentExists(String site, String path) {
        return false;
    }

    @Override
    public InputStream getContent(String site, String path) {
        Mapping previewMapping = getMapping(publishingTargetResolver.getPublishingTarget());
        logger.debug("Getting content at {0}", getFullKey(previewMapping, path));
        try {
            S3Object object = getClient().getObject(previewMapping.target, getKey(previewMapping, path));
            return object.getObjectContent();
        } catch (Exception e) {
            throw new BlobStoreException("Error getting content at " + getFullKey(previewMapping, path), e);
        }
    }

    @Override
    public long getContentSize(String site, String path) {
        Mapping previewMapping = getMapping(publishingTargetResolver.getPublishingTarget());
        logger.debug("Getting content size at {0}", getFullKey(previewMapping, path));
        try {
            ObjectMetadata metadata =
                    getClient().getObjectMetadata(previewMapping.target, getKey(previewMapping, path));
            return metadata.getContentLength();
        } catch (Exception e) {
            throw new BlobStoreException("Error getting content size at " + getFullKey(previewMapping, path), e);
        }
    }

    @Override
    public String writeContent(String site, String path, InputStream content) {
        Mapping previewMapping = getMapping(publishingTargetResolver.getPublishingTarget());
        logger.debug("Uploading content to {0}", getFullKey(previewMapping, path));
        try {
            uploadStream(previewMapping.target,
                    getKey(previewMapping, path), getClient(), MIN_PART_SIZE, path, content);
            return OK;
        } catch (Exception e) {
            throw new BlobStoreException("Error uploading content at " + getFullKey(previewMapping, path), e);
        }
    }

    @Override
    public String createFolder(String site, String path, String name) {
        // Do nothing, S3 has no folders
        return OK;
    }

    @Override
    public String deleteContent(String site, String path, String approver) {
        Mapping previewMapping = getMapping(publishingTargetResolver.getPublishingTarget());
        logger.debug("Deleting content at {0}", getFullKey(previewMapping, path));
        if (!isFolder(path)) {
            try {
                getClient().deleteObject(previewMapping.target, getKey(previewMapping, path));
            } catch (Exception e) {
                throw new BlobStoreException("Error deleting content at " + getFullKey(previewMapping, path), e);
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
                        logger.debug("Deleting contents at {0} from bucket {1}",
                                Arrays.toString(keys), previewMapping.target);
                        try {
                            getClient().deleteObjects(new DeleteObjectsRequest(previewMapping.target).withKeys(keys));
                        } catch (Exception e) {
                            throw new BlobStoreException("Error deleting contents at " + Arrays.toString(keys) +
                                    " from bucket " + previewMapping.target, e);
                        }
                    }
                } catch (Exception e) {
                    throw new BlobStoreException("Error listing content at " + getFullKey(previewMapping, path), e);
                }
            } while(isNotEmpty(request.getContinuationToken()));
        }
        return OK;
    }

    @Override
    public Map<String, String> moveContent(String site, String fromPath, String toPath, String newName) {
        Mapping previewMapping = getMapping(publishingTargetResolver.getPublishingTarget());
        logger.debug("Moving content from {0} to {1}",
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
                            logger.debug("Moving content from {0} to {1}",
                                    getFullKey(previewMapping, key), getFullKey(previewMapping, toPath + "/" + filePath));
                            try {
                                copyFile(previewMapping.target, key, previewMapping.target,
                                        getKey(previewMapping, toPath + "/" + filePath), COPY_PART_SIZE, getClient());
                            } catch (Exception e) {
                                throw new BlobStoreException("Error copying content from " +
                                        getFullKey(previewMapping, key) + " to " +
                                        getFullKey(previewMapping, toPath + "/" + filePath), e);
                            }
                        }
                        try {
                            getClient().deleteObjects(new DeleteObjectsRequest(previewMapping.target).withKeys(keys));
                        } catch (Exception e) {
                            throw new BlobStoreException("Error deleting content at " + Arrays.toString(keys) +
                                    " from bucket " + previewMapping.target, e);
                        }
                    } catch (Exception e) {
                        throw new BlobStoreException("Error listing content at " +
                                getFullKey(previewMapping, fromPath), e);
                    }
                } while(isNotEmpty(request.getContinuationToken()));
            } else {
                try {
                    copyFile(previewMapping.target, getKey(previewMapping, fromPath),
                            previewMapping.target, getKey(previewMapping, toPath), COPY_PART_SIZE, getClient());
                    getClient().deleteObject(previewMapping.target, getKey(previewMapping, fromPath));
                } catch (Exception e) {
                    throw new BlobStoreException("Error moving content from " + getFullKey(previewMapping, fromPath)
                            + " to " + getFullKey(previewMapping, toPath), e);
                }
            }
        } else {
            //TODO: Check if this is really needed, it looks like newName is always null
            throw new UnsupportedOperationException();
        }
        return Collections.emptyMap();
    }

    @Override
    public String copyContent(String site, String fromPath, String toPath) {
        Mapping previewMapping = getMapping(publishingTargetResolver.getPublishingTarget());
        logger.debug("Copying content from {0} to {1}",
                getFullKey(previewMapping, fromPath), getFullKey(previewMapping, toPath));
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
                        logger.debug("Copying content from {0} to {1}",
                                getFullKey(previewMapping, key), getFullKey(previewMapping, toPath + "/" + filePath));
                        try {
                            copyFile(previewMapping.target, key, previewMapping.target,
                                    getKey(previewMapping, toPath + "/" + filePath), COPY_PART_SIZE, getClient());
                        } catch (Exception e) {
                            throw new BlobStoreException("Error copying content from " +
                                    getFullKey(previewMapping, key) + " to " +
                                    getFullKey(previewMapping, toPath + "/" + filePath), e);
                        }
                    }
                } catch (Exception e) {
                    throw new BlobStoreException("Error listing content at " + getFullKey(previewMapping, fromPath), e);
                }
            } while(isNotEmpty(request.getContinuationToken()));
        } else {
            try {
                copyFile(previewMapping.target, getKey(previewMapping, fromPath), previewMapping.target,
                        getKey(previewMapping, toPath), COPY_PART_SIZE, getClient());
            } catch (Exception e) {
                throw new BlobStoreException("Error copying content from " + getFullKey(previewMapping, fromPath)
                        + " to " + getFullKey(previewMapping, toPath), e);
            }
        }
        return OK;
    }

    @Override
    public void publish(String site, String sandboxBranch, List<DeploymentItemTO> deploymentItems, String environment,
                        String author, String comment) {
        Mapping previewMapping = getMapping(publishingTargetResolver.getPublishingTarget());
        Mapping envMapping = getMapping(environment);
        logger.debug("Publishing content from bucket {0} to bucket {1}", previewMapping.target, envMapping.target);
        for(DeploymentItemTO item : deploymentItems) {
            if (item.isDelete()) {
                logger.debug("Deleting content at {0}", getFullKey(envMapping, item.getPath()));
                try {
                    getClient().deleteObject(envMapping.target, getKey(envMapping, item.getPath()));
                    if (isNotEmpty(item.getOldPath())) {
                        logger.debug("Deleting content at {0}", getFullKey(envMapping, item.getOldPath()));
                        getClient().deleteObject(envMapping.target, getKey(envMapping, item.getOldPath()));
                    }
                } catch (Exception e) {
                    throw new BlobStoreException("Error deleting content at " +
                            getFullKey(previewMapping, item.getPath()), e);
                }
            } else if (item.isMove()) {
                logger.debug("Moving content from {0} to {1}",
                        getFullKey(envMapping, item.getOldPath()), getFullKey(envMapping, item.getPath()));
                try {
                    copyFile(envMapping.target, getKey(envMapping, item.getOldPath()), envMapping.target,
                            getKey(envMapping, item.getPath()), COPY_PART_SIZE, getClient());
                    getClient().deleteObject(envMapping.target, getKey(envMapping, item.getOldPath()));
                } catch (Exception e) {
                    throw new BlobStoreException("Error moving content from " +
                            getFullKey(envMapping, item.getOldPath()) + " to " +
                            getFullKey(envMapping, item.getPath()), e);
                }
            } else {
                logger.debug("Copying content from {0} to {1}",
                        getFullKey(previewMapping, item.getPath()), getFullKey(envMapping, item.getPath()));
                try {
                    copyFile(previewMapping.target, getKey(previewMapping, item.getPath()), envMapping.target,
                            getKey(envMapping, item.getPath()), COPY_PART_SIZE, getClient());
                } catch (Exception e) {
                    throw new BlobStoreException("Error copying content from " +
                            getFullKey(previewMapping, item.getPath()) + " to " +
                            getFullKey(envMapping, item.getPath()), e);
                }
            }
        }
    }

    @Override
    public void initialPublish(String siteId) throws SiteNotFoundException {
        Mapping previewMapping = getMapping(publishingTargetResolver.getPublishingTarget());
        Mapping liveMapping = getMapping(servicesConfig.getLiveEnvironment(siteId));

        logger.debug("Performing initial publish for site {0}", siteId);

        logger.debug("Performing initial publish for site {0} to live", siteId);
        copyFolder(previewMapping.target, previewMapping.prefix, liveMapping.target, liveMapping.prefix,
                MIN_PART_SIZE, getClient());

        if (servicesConfig.isStagingEnvironmentEnabled(siteId)) {
            Mapping statingMapping = getMapping(servicesConfig.getStagingEnvironment(siteId));

            logger.debug("Performing initial publish for site {0} to staging", siteId);
            copyFolder(previewMapping.target, previewMapping.prefix, statingMapping.target, statingMapping.prefix,
                    MIN_PART_SIZE, getClient());
        }
    }

}
