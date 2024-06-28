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

package org.craftercms.studio.impl.v1.service.aws;

import org.apache.commons.io.IOUtils;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.studio.api.v1.exception.AwsException;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.removeStart;

public abstract class AwsUtils {

    private static final Logger logger = LoggerFactory.getLogger(AwsUtils.class);

    public static final int MIN_PART_SIZE = 5 * 1024 * 1024;

    public static final int COPY_PART_SIZE = 1024 * 1024 * 1024;

    public static final long MAX_COPY_FILE_SIZE = 5L * 1024 * 1024 * 1024;

    public static final int DELETE_BATCH_SIZE = 1000;

    public static void uploadStream(String inputBucket, String inputKey, S3Client s3Client, int partSize,
                                    String filename, InputStream content) throws AwsException {
        List<CompletedPart> completedParts = new LinkedList<>();
        CreateMultipartUploadResponse initResult = null;
        try {
            int partNumber = 1;
            long totalBytes = 0;

            PutObjectRequest.Builder putRequestBuilder = PutObjectRequest.builder()
                    .bucket(inputBucket)
                    .key(inputKey)
                    .contentType(StudioUtils.getMimeType(filename));

            CreateMultipartUploadRequest initRequest = CreateMultipartUploadRequest.builder()
                    .bucket(inputBucket)
                    .key(inputKey)
                    .build();
            initResult = s3Client.createMultipartUpload(initRequest);
            byte[] buffer = new byte[partSize];
            int read;

            logger.debug("Start upload for file '{}'", filename);

            while (0 < (read = IOUtils.read(content, buffer))) {
                totalBytes += read;
                if (logger.isTraceEnabled()) {
                    logger.trace("Uploading part {} with size {} - total: {}", partNumber, read, totalBytes);
                }
                ByteArrayInputStream bais = new ByteArrayInputStream(buffer, 0, read);
                UploadPartRequest uploadRequest = UploadPartRequest.builder()
                        .uploadId(initResult.uploadId())
                        .bucket(inputBucket)
                        .key(inputKey)
                        .partNumber(partNumber)
                        .build();
                UploadPartResponse uploadResponse = s3Client.uploadPart(uploadRequest, RequestBody.fromInputStream(bais, read));
                completedParts.add(CompletedPart.builder().partNumber(partNumber).eTag(uploadResponse.eTag()).build());
                partNumber++;
            }

            if (totalBytes == 0) {
                // If the file is empty, use the simple upload instead of the multipart
                s3Client.abortMultipartUpload(
                        AbortMultipartUploadRequest.builder()
                                .bucket(inputBucket)
                                .key(inputKey)
                                .uploadId(initResult.uploadId())
                                .build());
                s3Client.putObject(putRequestBuilder.build(), RequestBody.fromBytes(new byte[0]));
            } else {
                CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                        .parts(completedParts)
                        .build();
                CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                        .bucket(inputBucket)
                        .key(inputKey)
                        .uploadId(initResult.uploadId())
                        .multipartUpload(completedMultipartUpload)
                        .build();

                s3Client.completeMultipartUpload(completeRequest);
            }

            logger.debug("Upload completed for file '{}'", filename);
        } catch (Exception e) {
            if (initResult != null) {
                s3Client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                        .bucket(inputBucket)
                        .key(inputKey)
                        .uploadId(initResult.uploadId())
                        .build());
            }
            throw new AwsException(format("Upload of file '%s' failed", filename), e);
        }
    }

    public static void copyFolder(String sourceBucket, String sourcePrefix, String destBucket, String destPrefix,
                                  int partSize, S3Client client) {
        logger.debug("Copy all files from '{}/{}' to '{}/{}'", sourceBucket, sourcePrefix, destBucket, destPrefix);
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(sourceBucket)
                .prefix(sourcePrefix)
                .build();
        ListObjectsV2Iterable response = client.listObjectsV2Paginator(request);
        for (S3Object object : response.contents()) {
            String relativePrefix = removeStart(object.key(), sourcePrefix);
            String newKey = removeStart(UrlUtils.concat(destPrefix, relativePrefix), "/");
            copyFile(sourceBucket, object.key(), destBucket, newKey, partSize, client);
        }
        logger.debug("Completed copy from '{}/{}' to '{}/{}'", sourceBucket, sourcePrefix, destBucket, destPrefix);
    }

    public static void copyFile(String sourceBucket, String sourceKey, String destBucket, String destKey,
                                int partSize, S3Client client) {
        logger.debug("Copy file from '{}/{}' to '{}/{}'", sourceBucket, sourceKey, destBucket, destKey);
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(sourceBucket)
                .key(sourceKey)
                .build();
        HeadObjectResponse headObjectResponse = client.headObject(headObjectRequest);
        long objectSize = headObjectResponse.contentLength();

        if (objectSize >= MAX_COPY_FILE_SIZE) {
            logger.debug("Start multipart copy for '{}/{}'", sourceBucket, sourceKey);
            CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
                    .bucket(destBucket)
                    .key(destKey)
                    .build();
            CreateMultipartUploadResponse createMultipartUploadResponse = client.createMultipartUpload(createMultipartUploadRequest);
            String uploadId = createMultipartUploadResponse.uploadId();

            long bytePosition = 0;
            int partNum = 1;
            List<CompletedPart> completedParts = new LinkedList<>();

            try {
                while (bytePosition < objectSize) {
                    logger.trace("Copy part '{}' for '{}/{}'", partNum, sourceBucket, sourceKey);
                    long lastByte = Math.min(bytePosition + partSize - 1, objectSize - 1);

                    UploadPartCopyRequest copyRequest = UploadPartCopyRequest.builder()
                            .sourceBucket(sourceBucket)
                            .sourceKey(sourceKey)
                            .destinationBucket(destBucket)
                            .destinationKey(destKey)
                            .uploadId(createMultipartUploadResponse.uploadId())
                            .copySourceRange("bytes=" + bytePosition + "-" + lastByte)
                            .partNumber(partNum)
                            .build();

                    UploadPartCopyResponse copyResponse = client.uploadPartCopy(copyRequest);
                    completedParts.add(CompletedPart.builder()
                            .partNumber(partNum)
                            .eTag(copyResponse.copyPartResult().eTag())
                            .build());
                    bytePosition += partSize;
                    partNum++;
                }

                CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                        .parts(completedParts)
                        .build();
                CompleteMultipartUploadRequest completeRequest =
                        CompleteMultipartUploadRequest.builder()
                                .bucket(destBucket)
                                .key(destKey)
                                .uploadId(uploadId)
                                .multipartUpload(completedMultipartUpload)
                                .build();
                client.completeMultipartUpload(completeRequest);
                logger.debug("Completed multipart copy for '{}/{}'", sourceBucket, sourceKey);
            } catch (Exception e) {
                client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                        .bucket(destBucket)
                        .key(destKey)
                        .uploadId(uploadId)
                        .build());
                throw e;
            }
        } else {
            logger.debug("Start copy operation for '{}/{}'", sourceBucket, sourceKey);
            CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                    .sourceBucket(sourceBucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(destBucket)
                    .destinationKey(destKey)
                    .build();
            client.copyObject(copyObjectRequest);
            logger.debug("Completed copy for '{}/{}'", sourceBucket, sourceKey);
        }
    }

    public static String getS3Url(String bucket, String key) {
        return format("s3://%s/%s", bucket, key);
    }

}
