/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */

package org.craftercms.studio.impl.v1.service.aws;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.aws.s3.S3Item;
import org.craftercms.studio.api.v1.aws.s3.S3Output;
import org.craftercms.studio.api.v1.aws.s3.S3Profile;
import org.craftercms.studio.api.v1.exception.AwsException;
import org.craftercms.studio.api.v1.service.aws.AbstractAwsService;
import org.craftercms.studio.api.v1.service.aws.S3Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;

import static org.craftercms.studio.impl.v1.aws.s3.XmlS3ProfileReader.DEFAULT_DOMAIN;

/**
 * Default implementation of {@link S3Service}.
 *
 * @author joseross
 */
public class S3ServiceImpl extends AbstractAwsService<S3Profile> implements S3Service {

    public static final String DELIMITER = "/";
    public static final String ITEM_FILTER = "item";

    protected int partSize;

    public S3ServiceImpl() {
        partSize = AwsUtils.MIN_PART_SIZE;
    }

    public void setPartSize(final int partSize) {
        this.partSize = partSize;
    }

    protected AmazonS3 getS3Client(S3Profile profile) {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
            .withCredentials(profile.getCredentialsProvider());
        if(StringUtils.isNotEmpty(profile.getRegion())) {
            builder.withRegion(profile.getRegion());
        }
        return builder.build();
    }

    @Override
    @Deprecated
    public S3Output uploadFile(@ValidateStringParam(name = "site") String site,
                               @ValidateStringParam(name = "profileId") String profileId,
                               @ValidateStringParam(name = "filename") String filename,
                               InputStream content) throws AwsException {
        S3Profile profile = getProfile(site, profileId);
        AmazonS3 s3Client = getS3Client(profile);
        String inputBucket = profile.getBucketName();
        String inputKey = filename;

        AwsUtils.uploadStream(inputBucket, inputKey, s3Client, partSize, filename, content);

        S3Output output = new S3Output();
        output.setBucket(inputBucket);
        output.setKey(inputKey);
        return output;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public S3Item upload(@ValidateStringParam(name = "siteId") final String siteId,
                         @ValidateStringParam(name = "profileId") final String profileId,
                         @ValidateStringParam(name = "filename") final String filename,
                         final InputStream content) throws AwsException {
        S3Profile profile = getProfile(siteId, profileId);
        AmazonS3 s3Client = getS3Client(profile);
        String inputBucket = profile.getBucketName();

        AwsUtils.uploadStream(inputBucket, filename, s3Client, partSize, filename, content);

        return new S3Item(filename, createLink(profile, filename), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<S3Item> list(@ValidateStringParam(name = "siteId") final String siteId,
                             @ValidateStringParam(name = "profileId") final String profileId,
                             @ValidateStringParam(name = "path") final String path,
                             @ValidateStringParam(name = "type") final String type) throws AwsException {
        S3Profile profile = getProfile(siteId, profileId);
        AmazonS3 client = getS3Client(profile);
        List<S3Item> items = new LinkedList<>();

        Mimetypes mimetypes = Mimetypes.getInstance();
        MimeType filerType =
            StringUtils.isEmpty(type) || StringUtils.equals(type, ITEM_FILTER)? MimeTypeUtils.ALL : new MimeType(type);

        String prefix = StringUtils.isEmpty(path)? path : StringUtils.appendIfMissing(path, DELIMITER);

        ListObjectsV2Request request = new ListObjectsV2Request()
                                            .withBucketName(profile.getBucketName())
                                            .withPrefix(prefix)
                                            .withDelimiter(DELIMITER);

        ListObjectsV2Result result;

        do {
            result = client.listObjectsV2(request);
            result.getCommonPrefixes().stream()
                .map(p -> new S3Item(StringUtils.removeEnd(StringUtils.removeStart(p, prefix), DELIMITER), p, true))
                .forEach(items::add);

            result.getObjectSummaries().stream()
                .filter(o -> !StringUtils.equals(o.getKey(), prefix) &&
                                MimeType.valueOf(mimetypes.getMimetype(o.getKey())).isCompatibleWith(filerType))
                .map(o -> new S3Item(
                    StringUtils.removeStart(o.getKey(), prefix), createLink(profile, o.getKey()), false))
                .forEach(items::add);

            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());

        return items;
    }

    protected String createLink(S3Profile profile, String key) {
        String domain = profile.getDistributionDomain();
        if(StringUtils.equals(domain, DEFAULT_DOMAIN)) {
            return domain + DELIMITER + profile.getBucketName() + DELIMITER + key;
        } else {
            return domain + DELIMITER + key;
        }
    }

}
