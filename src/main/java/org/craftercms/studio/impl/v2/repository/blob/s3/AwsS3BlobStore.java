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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.ArrayUtils;
import org.craftercms.commons.aws.S3ClientCachingFactory;
import org.craftercms.commons.config.profiles.aws.S3Profile;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;
import org.craftercms.studio.api.v2.repository.blob.Blob;
import org.craftercms.studio.impl.v2.repository.blob.AbstractBlobStore;

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
 * Implementation of {@link org.craftercms.studio.api.v2.repository.blob.BlobStore} for AWS S3
 *
 * @author joseross
 * @since 3.1.6
 */
public class AwsS3BlobStore extends AbstractBlobStore<S3Profile> {

    public static final String OK = "OK";

    protected S3ClientCachingFactory clientFactory;

    public void setClientFactory(S3ClientCachingFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void doInit(HierarchicalConfiguration<ImmutableNode> config) {
        // do nothing
    }

    protected String getKey(Mapping mapping, String site, String path) {
        StringBuilder sb = new StringBuilder();
        if (isNotEmpty(mapping.prefix)) {
            sb.append(mapping.prefix).append("/");
        }
        sb.append(site).append(prependIfMissing(path, "/"));
        return  sb.toString();
    }

    protected boolean isFolder(String path) {
        return isEmpty(getExtension(path));
    }

    @Override
    public Blob getReference(String site, String path) {
        return new Blob(id, getKey(getPreviewMapping(), site, path));
    }

    protected AmazonS3 getClient() {
        return clientFactory.getClient(profile);
    }

    // Start API 1

    @Override
    public boolean contentExists(String site, String path) {
        Mapping previewMapping = getPreviewMapping();
        return getClient().doesObjectExist(previewMapping.target, getKey(previewMapping, site, path));
    }

    @Override
    public InputStream getContent(String site, String path) {
        Mapping previewMapping = getPreviewMapping();
        S3Object object = getClient().getObject(previewMapping.target, getKey(previewMapping, site, path));
        return object.getObjectContent();
    }

    @Override
    public long getContentSize(String site, String path) {
        Mapping previewMapping = getPreviewMapping();
        S3Object object = getClient().getObject(previewMapping.target, getKey(previewMapping, site, path));
        return object.getObjectMetadata().getContentLength();
    }

    @Override
    public String writeContent(String site, String path, InputStream content) throws ServiceLayerException {
        Mapping previewMapping = getPreviewMapping();
        uploadStream(previewMapping.target,
                getKey(previewMapping, site, path), getClient(), MIN_PART_SIZE, path, content);
        return OK;
    }

    @Override
    public String createFolder(String site, String path, String name) {
        // Do nothing, S3 has no folders
        return OK;
    }

    @Override
    public String deleteContent(String site, String path, String approver) {
        Mapping previewMapping = getPreviewMapping();
        if (!isFolder(path)) {
            getClient().deleteObject(previewMapping.target, getKey(previewMapping, site, path));
        } else {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(previewMapping.target)
                    .withPrefix(getKey(previewMapping, site, path));
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
        Mapping previewMapping = getPreviewMapping();
        if (isEmpty(newName)) {
            if (isFolder(fromPath)) {
                ListObjectsV2Request request = new ListObjectsV2Request()
                        .withBucketName(previewMapping.target)
                        .withPrefix(getKey(previewMapping, site, fromPath));
                do {
                    ListObjectsV2Result result = getClient().listObjectsV2(request);
                    request.setContinuationToken(result.getContinuationToken());

                    String[] keys = result.getObjectSummaries().stream()
                            .map(S3ObjectSummary::getKey)
                            .collect(toList())
                            .toArray(new String[] {});

                    for (String key : keys) {
                        String filePath =
                                Paths.get(getKey(previewMapping, site, fromPath)).relativize(Paths.get(key)).toString();
                        getClient().copyObject(previewMapping.target, key, previewMapping.target,
                                getKey(previewMapping, site, toPath + "/" + filePath));
                    }
                    getClient().deleteObjects(new DeleteObjectsRequest(previewMapping.target).withKeys(keys));
                } while(isNotEmpty(request.getContinuationToken()));
            } else {
                getClient().copyObject(previewMapping.target, getKey(previewMapping, site, fromPath),
                                        previewMapping.target, getKey(previewMapping, site, toPath));
                getClient().deleteObject(previewMapping.target, getKey(previewMapping, site, fromPath));
            }
        } else {
            //TODO: Check if this is really needed, it looks like newName is always null
            throw new UnsupportedOperationException();
        }
        return Collections.emptyMap();
    }

    @Override
    public String copyContent(String site, String fromPath, String toPath) {
        Mapping previewMapping = getPreviewMapping();
        if (isFolder(fromPath)) {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(previewMapping.target)
                    .withPrefix(getKey(previewMapping, site, fromPath));
            do {
                ListObjectsV2Result result = getClient().listObjectsV2(request);
                request.setContinuationToken(result.getContinuationToken());

                String[] keys = result.getObjectSummaries().stream()
                        .map(S3ObjectSummary::getKey)
                        .collect(toList())
                        .toArray(new String[] {});

                for (String key : keys) {
                    String filePath =
                            Paths.get(getKey(previewMapping, site, fromPath)).relativize(Paths.get(key)).toString();
                    getClient().copyObject(previewMapping.target, key, previewMapping.target,
                            getKey(previewMapping, site, toPath + "/" + filePath));
                }
            } while(isNotEmpty(request.getContinuationToken()));
        } else {
            getClient().copyObject(previewMapping.target, getKey(previewMapping, site, fromPath),
                                    previewMapping.target, getKey(previewMapping, site, toPath));
        }
        return OK;
    }

    @Override
    public void publish(String site, String sandboxBranch, List<DeploymentItemTO> deploymentItems, String environment,
                        String author, String comment) {
        Mapping previewMapping = getPreviewMapping();
        Mapping envMapping = getMapping(environment);
        for(DeploymentItemTO item : deploymentItems) {
            if (item.isDelete()) {
                getClient().deleteObject(envMapping.target, getKey(envMapping, site, item.getPath()));
            } else if (item.isMove()) {
                getClient().copyObject(envMapping.target, getKey(envMapping, site, item.getOldPath()),
                                        envMapping.target, getKey(envMapping, site, item.getPath()));
                getClient().deleteObject(envMapping.target, getKey(envMapping, site, item.getOldPath()));
            } else {
                getClient().copyObject(previewMapping.target, getKey(previewMapping, site, item.getPath()),
                                        envMapping.target, getKey(envMapping, site, item.getPath()));
            }
        }
    }


}
