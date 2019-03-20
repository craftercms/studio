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

package org.craftercms.studio.impl.v2.service.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.config.profiles.aws.S3Profile;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.exception.AwsException;
import org.craftercms.studio.api.v1.service.aws.AbstractAwsService;
import org.craftercms.studio.api.v2.service.aws.s3.AwsS3Service;
import org.craftercms.studio.impl.v1.service.aws.AwsUtils;
import org.craftercms.studio.model.aws.s3.S3Item;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Default implementation of {@link AwsS3Service}.
 *
 * @author joseross
 */
public class AwsS3ServiceImpl extends AbstractAwsService<S3Profile> implements AwsS3Service {

    public static final String DELIMITER = "/";
    public static final String ITEM_FILTER = "item";
    public static final String URL_FORMAT = "/remote-assets/s3/%s/%s";

    protected int partSize;

    public AwsS3ServiceImpl() {
        partSize = AwsUtils.MIN_PART_SIZE;
    }

    public void setPartSize(final int partSize) {
        this.partSize = partSize;
    }

    /** 
    * Add withEndpointConfiguration() to direct requests to a S3 compatible storage service
    */
    protected AmazonS3 getS3Client(S3Profile profile) {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
            .withCredentials(profile.getCredentialsProvider());
            
        if (StringUtils.isNotEmpty(profile.getEndpoint()) && StringUtils.isNotEmpty(profile.getRegion())){
            builder.withEndpointConfiguration(new AmazonS3ClientBuilder.EndpointConfiguration(profile.getEndpoint(), profile.getRegion()));
        } else if (StringUtils.isNotEmpty(profile.getRegion())) {
            builder.withRegion(profile.getRegion());
        }

        return builder.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @HasPermission(type = DefaultPermission.class, action = "s3 write")
    public S3Item uploadItem(@ValidateStringParam(name = "siteId") @ProtectedResourceId("siteId") String siteId,
                             @ValidateStringParam(name = "profileId") String profileId,
                             @ValidateStringParam(name ="path") String path,
                             @ValidateStringParam(name = "filename") String filename,
                             InputStream content) throws AwsException {
        S3Profile profile = getProfile(siteId, profileId);
        AmazonS3 s3Client = getS3Client(profile);
        String inputBucket = profile.getBucketName();
        String key = StringUtils.isNotEmpty(path)? StringUtils.appendIfMissing(path, DELIMITER) + filename : filename;

        AwsUtils.uploadStream(inputBucket, key, s3Client, partSize, filename, content);

        return new S3Item(filename, createUrl(profileId, key), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @HasPermission(type = DefaultPermission.class, action = "s3 read")
    public List<S3Item> listItems(@ValidateStringParam(name = "siteId") @ProtectedResourceId("siteId") String siteId,
                                  @ValidateStringParam(name = "profileId") String profileId,
                                  @ValidateStringParam(name = "path") String path,
                                  @ValidateStringParam(name = "type") String type) throws AwsException {
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
                    StringUtils.removeStart(o.getKey(), prefix), createUrl(profileId, o.getKey()), false))
                .forEach(items::add);

            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());

        return items;
    }

    protected String createUrl(String profileId, String key) {
        return String.format(URL_FORMAT, profileId, key);
    }

}
