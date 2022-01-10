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

package org.craftercms.studio.impl.v1.service.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.io.IOUtils;
import org.craftercms.studio.api.v1.exception.AwsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public abstract class AwsUtils {

    private static final Logger logger = LoggerFactory.getLogger(AwsUtils.class);

    public static final int MIN_PART_SIZE = 5 * 1024 * 1024;

    public static final int COPY_PART_SIZE = 1024 * 1024 * 1024;

    public static final long MAX_COPY_FILE_SIZE = 5L * 1024 * 1024 * 1024;

    public static void uploadStream(String inputBucket, String inputKey, AmazonS3 s3Client, int partSize,
                                    String filename, InputStream content) throws AwsException {
        List<PartETag> etags = new LinkedList<>();
        InitiateMultipartUploadResult initResult = null;
        try {
            int partNumber = 1;
            long totalBytes = 0;

            MimetypesFileTypeMap mimeMap = new MimetypesFileTypeMap();
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType(mimeMap.getContentType(filename));

            InitiateMultipartUploadRequest initRequest =
                    new InitiateMultipartUploadRequest(inputBucket, inputKey, meta);
            initResult = s3Client.initiateMultipartUpload(initRequest);
            byte[] buffer = new byte[partSize];
            int read;

            logger.debug("Starting upload for file '{}'", filename);

            while (0 < (read = IOUtils.read(content, buffer))) {
                totalBytes += read;
                if (logger.isTraceEnabled()) {
                    logger.trace("Uploading part {} with size {} - total: {}", partNumber, read, totalBytes);
                }
                ByteArrayInputStream bais = new ByteArrayInputStream(buffer, 0, read);
                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withUploadId(initResult.getUploadId())
                        .withBucketName(inputBucket)
                        .withKey(inputKey)
                        .withInputStream(bais)
                        .withPartNumber(partNumber)
                        .withPartSize(read)
                        .withLastPart(read < partSize);
                etags.add(s3Client.uploadPart(uploadRequest).getPartETag());
                partNumber++;
            }

            if (totalBytes == 0) {
                // If the file is empty, use the simple upload instead of the multipart
                s3Client.abortMultipartUpload(
                        new AbortMultipartUploadRequest(inputBucket, inputKey, initResult.getUploadId()));

                s3Client.putObject(inputBucket, inputKey, new ByteArrayInputStream(new byte[0]), meta);
            } else {
                CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(inputBucket,
                        inputKey, initResult.getUploadId(), etags);

                s3Client.completeMultipartUpload(completeRequest);
            }

            logger.debug("Upload completed for file '{}'", filename);

        } catch (Exception e) {
            if (initResult != null) {
                s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(inputBucket, inputKey,
                        initResult.getUploadId()));
            }
            throw new AwsException("Upload of file '" + filename + "' failed", e);
        }
    }

    public static void copyFile(String sourceBucket, String sourceKey, String destBucket, String destKey,
                                int partSize, AmazonS3 client) {
        logger.debug("Copying file from {}/{} to {}/{}", sourceBucket, sourceKey, destBucket, destKey);
        GetObjectMetadataRequest metadataRequest = new GetObjectMetadataRequest(sourceBucket, sourceKey);
        ObjectMetadata metadataResult = client.getObjectMetadata(metadataRequest);
        long objectSize = metadataResult.getContentLength();

        if (objectSize >= MAX_COPY_FILE_SIZE) {
            logger.debug("Starting multipart copy for {}/{}", sourceBucket, sourceKey);
            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(destBucket, destKey);
            InitiateMultipartUploadResult initResult = client.initiateMultipartUpload(initRequest);

            long bytePosition = 0;
            int partNum = 1;
            List<CopyPartResult> copyResponses = new LinkedList<>();

            try {
                while (bytePosition < objectSize) {
                    logger.debug("Copying part {} for {}/{}", partNum, sourceBucket, sourceKey);
                    long lastByte = Math.min(bytePosition + partSize - 1, objectSize - 1);

                    CopyPartRequest copyRequest = new CopyPartRequest()
                            .withSourceBucketName(sourceBucket)
                            .withSourceKey(sourceKey)
                            .withDestinationBucketName(destBucket)
                            .withDestinationKey(destKey)
                            .withUploadId(initResult.getUploadId())
                            .withFirstByte(bytePosition)
                            .withLastByte(lastByte)
                            .withPartNumber(partNum++);

                    copyResponses.add(client.copyPart(copyRequest));
                    bytePosition += partSize;
                }

                List<PartETag> etags = copyResponses.stream()
                        .map(r -> new PartETag(r.getPartNumber(), r.getETag()))
                        .collect(toList());

                CompleteMultipartUploadRequest completeRequest =
                        new CompleteMultipartUploadRequest(destBucket, destKey, initResult.getUploadId(), etags);
                client.completeMultipartUpload(completeRequest);
                logger.debug("Completed multipart copy for {}/{}", sourceBucket, sourceKey);
            } catch (Exception e) {
                if (initResult != null) {
                    client.abortMultipartUpload(
                            new AbortMultipartUploadRequest(destBucket, destKey, initResult.getUploadId()));
                }
                throw e;
            }


        } else {
            logger.debug("Starting copy operation for {}/{}", sourceBucket, sourceKey);
            client.copyObject(sourceBucket, sourceKey, destBucket, destKey);
            logger.debug("Completed copy for {}/{}", sourceBucket, sourceKey);
        }
    }

    public static String getS3Url(String bucket, String key) {
        return String.format("s3://%s/%s", bucket, key);
    }

}
