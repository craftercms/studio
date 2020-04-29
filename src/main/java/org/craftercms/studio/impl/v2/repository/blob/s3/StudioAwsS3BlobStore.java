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
package org.craftercms.studio.impl.v2.repository.blob.s3;

import com.amazonaws.services.s3.model.*;
import org.apache.commons.lang3.ArrayUtils;
import org.craftercms.commons.file.blob.Blob;
import org.craftercms.commons.file.blob.impl.s3.AwsS3BlobStore;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStoreAdapter;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStore;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.*;
import static org.craftercms.studio.impl.v1.service.aws.AwsUtils.MIN_PART_SIZE;
import static org.craftercms.studio.impl.v1.service.aws.AwsUtils.uploadStream;

/**
 * Implementation of {@link StudioBlobStore} for AWS S3
 *
 * @author joseross
 * @since 3.1.6
 */
public class StudioAwsS3BlobStore extends AwsS3BlobStore implements StudioBlobStoreAdapter {

    public static final String OK = "OK";

    protected boolean isFolder(String path) {
        return isEmpty(getExtension(path));
    }

    @Override
    public Blob getReference(String path) {
        Mapping mapping = getMapping(targetResolver.getTarget());
        ObjectMetadata metadata = getClient().getObjectMetadata(mapping.target, getKey(mapping, path));
        return new Blob(id, metadata.getETag());
    }

    // Start API 1

    @Override
    public boolean contentExists(String site, String path) {
        Mapping mapping = getMapping(targetResolver.getTarget());
        return getClient().doesObjectExist(mapping.target, getKey(mapping, path));
    }

    @Override
    public InputStream getContent(String site, String path) {
        Mapping previewMapping = getMapping(targetResolver.getTarget());
        S3Object object = getClient().getObject(previewMapping.target, getKey(previewMapping, path));
        return object.getObjectContent();
    }

    @Override
    public long getContentSize(String site, String path) {
        Mapping previewMapping = getMapping(targetResolver.getTarget());
        ObjectMetadata metadata =
                getClient().getObjectMetadata(previewMapping.target, getKey(previewMapping, path));
        return metadata.getContentLength();
    }

    @Override
    public String writeContent(String site, String path, InputStream content) throws ServiceLayerException {
        Mapping previewMapping = getMapping(targetResolver.getTarget());
        uploadStream(previewMapping.target,
                getKey(previewMapping, path), getClient(), MIN_PART_SIZE, path, content);
        return OK;
    }

    @Override
    public String createFolder(String site, String path, String name) {
        // Do nothing, S3 has no folders
        return OK;
    }

    @Override
    public String deleteContent(String site, String path, String approver) {
        Mapping previewMapping = getMapping(targetResolver.getTarget());
        if (!isFolder(path)) {
            getClient().deleteObject(previewMapping.target, getKey(previewMapping, path));
        } else {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(previewMapping.target)
                    .withPrefix(getKey(previewMapping, path));
            do {
                ListObjectsV2Result result = getClient().listObjectsV2(request);
                request.setContinuationToken(result.getContinuationToken());

                String[] keys = result.getObjectSummaries().stream()
                        .map(S3ObjectSummary::getKey)
                        .collect(toList())
                        .toArray(new String[] {});
                if (ArrayUtils.isNotEmpty(keys)) {
                    getClient().deleteObjects(new DeleteObjectsRequest(previewMapping.target).withKeys(keys));
                }
            } while(isNotEmpty(request.getContinuationToken()));
        }
        return OK;
    }

    @Override
    public Map<String, String> moveContent(String site, String fromPath, String toPath, String newName) {
        Mapping previewMapping = getMapping(targetResolver.getTarget());
        if (isEmpty(newName)) {
            if (isFolder(fromPath)) {
                ListObjectsV2Request request = new ListObjectsV2Request()
                        .withBucketName(previewMapping.target)
                        .withPrefix(getKey(previewMapping, fromPath));
                do {
                    ListObjectsV2Result result = getClient().listObjectsV2(request);
                    request.setContinuationToken(result.getContinuationToken());

                    String[] keys = result.getObjectSummaries().stream()
                            .map(S3ObjectSummary::getKey)
                            .collect(toList())
                            .toArray(new String[] {});

                    for (String key : keys) {
                        String filePath =
                                Paths.get(getKey(previewMapping, fromPath)).relativize(Paths.get(key)).toString();
                        getClient().copyObject(previewMapping.target, key, previewMapping.target,
                                getKey(previewMapping, toPath + "/" + filePath));
                    }
                    getClient().deleteObjects(new DeleteObjectsRequest(previewMapping.target).withKeys(keys));
                } while(isNotEmpty(request.getContinuationToken()));
            } else {
                getClient().copyObject(previewMapping.target, getKey(previewMapping, fromPath),
                                        previewMapping.target, getKey(previewMapping, toPath));
                getClient().deleteObject(previewMapping.target, getKey(previewMapping, fromPath));
            }
        } else {
            //TODO: Check if this is really needed, it looks like newName is always null
            throw new UnsupportedOperationException();
        }
        return Collections.emptyMap();
    }

    @Override
    public String copyContent(String site, String fromPath, String toPath) {
        Mapping previewMapping = getMapping(targetResolver.getTarget());
        if (isFolder(fromPath)) {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(previewMapping.target)
                    .withPrefix(getKey(previewMapping, fromPath));
            do {
                ListObjectsV2Result result = getClient().listObjectsV2(request);
                request.setContinuationToken(result.getContinuationToken());

                String[] keys = result.getObjectSummaries().stream()
                        .map(S3ObjectSummary::getKey)
                        .collect(toList())
                        .toArray(new String[] {});

                for (String key : keys) {
                    String filePath =
                            Paths.get(getKey(previewMapping, fromPath)).relativize(Paths.get(key)).toString();
                    getClient().copyObject(previewMapping.target, key, previewMapping.target,
                            getKey(previewMapping, toPath + "/" + filePath));
                }
            } while(isNotEmpty(request.getContinuationToken()));
        } else {
            getClient().copyObject(previewMapping.target, getKey(previewMapping, fromPath),
                                    previewMapping.target, getKey(previewMapping, toPath));
        }
        return OK;
    }

    @Override
    public void publish(String site, String sandboxBranch, List<DeploymentItemTO> deploymentItems, String environment,
                        String author, String comment) {
        Mapping previewMapping = getMapping(targetResolver.getTarget());
        Mapping envMapping = getMapping(environment);
        for(DeploymentItemTO item : deploymentItems) {
            if (item.isDelete()) {
                getClient().deleteObject(envMapping.target, getKey(envMapping, item.getPath()));
            } else if (item.isMove()) {
                getClient().copyObject(envMapping.target, getKey(envMapping, item.getOldPath()),
                                        envMapping.target, getKey(envMapping, item.getPath()));
                getClient().deleteObject(envMapping.target, getKey(envMapping, item.getOldPath()));
            } else {
                getClient().copyObject(previewMapping.target, getKey(previewMapping, item.getPath()),
                                        envMapping.target, getKey(envMapping, item.getPath()));
            }
        }
    }


}
