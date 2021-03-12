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
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public abstract class AwsUtils {

    private static final Logger logger = LoggerFactory.getLogger(AwsUtils.class);

    public static final int MIN_PART_SIZE = 5 * 1024 * 1024;

    public static void uploadStream(String inputBucket, String inputKey, AmazonS3 s3Client, int partSize,
                                    String filename, InputStream content) throws AwsException {
        List<PartETag> etags = new LinkedList<>();
        InitiateMultipartUploadResult initResult = null;
        try {
            int partNumber = 1;
            long totalBytes = 0;

            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType(StudioUtils.getMimeType(filename));

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

    public static String getS3Url(String bucket, String key) {
        return String.format("s3://%s/%s", bucket, key);
    }

}
