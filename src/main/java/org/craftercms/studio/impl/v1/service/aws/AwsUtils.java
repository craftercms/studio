/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.IOUtils;
import org.craftercms.studio.api.v1.exception.AwsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;

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

        MimetypesFileTypeMap mimeMap = new MimetypesFileTypeMap();
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType(mimeMap.getContentType(filename));

        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(inputBucket, inputKey, meta);
        initResult = s3Client.initiateMultipartUpload(initRequest);
        byte[] buffer = new byte[partSize];
        int read;

        logger.debug("Starting upload for file '{}'", filename);

        while(0 < (read = IOUtils.read(content, buffer))) {
          if(logger.isTraceEnabled()) {
            totalBytes += read;
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

        CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(inputBucket,
          inputKey, initResult.getUploadId(), etags);

        s3Client.completeMultipartUpload(completeRequest);

        logger.debug("Upload completed for file '{}'", filename);

      } catch (Exception e) {
        if(initResult != null) {
          s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(inputBucket, inputKey,
            initResult.getUploadId()));
        }
        throw new AwsException("Upload of file '" + filename + "' failed", e);
      }
    }

}
