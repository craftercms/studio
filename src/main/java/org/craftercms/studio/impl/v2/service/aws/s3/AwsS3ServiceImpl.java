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

package org.craftercms.studio.impl.v2.service.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.aws.S3ClientCachingFactory;
import org.craftercms.commons.config.profiles.aws.S3Profile;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.exception.AwsException;
import org.craftercms.studio.api.v1.service.aws.AbstractAwsService;
import org.craftercms.studio.api.v2.service.aws.s3.AwsS3Service;
import org.craftercms.studio.impl.v1.service.aws.AwsUtils;
import org.craftercms.studio.model.aws.s3.S3Item;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.appendIfMissing;
import static org.apache.commons.lang3.StringUtils.stripStart;

/**
 * Default implementation of {@link AwsS3Service}.
 *
 * @author joseross
 */
public class AwsS3ServiceImpl extends AbstractAwsService<S3Profile> implements AwsS3Service {

    public static final String ITEM_FILTER = "item";

    /**
     * The S3 client factory.
     */
    protected S3ClientCachingFactory clientFactory;

    /**
     * The part size used for S3 uploads
     */
    protected int partSize = AwsUtils.MIN_PART_SIZE;

    /**
     * The delimiter for S3 paths
     */
    protected String delimiter;

    /**
     * The URL pattern for the generated files
     */
    protected String urlPattern;

    @Required
    public void setClientFactory(S3ClientCachingFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public void setPartSize(final int partSize) {
        this.partSize = partSize;
    }

    @Required
    public void setDelimiter(final String delimiter) {
        this.delimiter = delimiter;
    }

    @Required
    public void setUrlPattern(final String urlPattern) {
        this.urlPattern = urlPattern;
    }

    protected AmazonS3 getS3Client(S3Profile profile) {
        return clientFactory.getClient(profile);
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
        String relativeKey = UrlUtils.concat(path, filename);
        String fullKey = UrlUtils.concat(profile.getPrefix(), relativeKey);

        AwsUtils.uploadStream(inputBucket, fullKey, s3Client, partSize, filename, content);

        return new S3Item(filename, createUrl(profileId, relativeKey), false);
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

        String fullPrefix = normalizePrefix(UrlUtils.concat(profile.getPrefix(), path));

        ListObjectsV2Request request = new ListObjectsV2Request()
                                            .withBucketName(profile.getBucketName())
                                            .withPrefix(fullPrefix)
                                            .withDelimiter(delimiter);

        ListObjectsV2Result result;

        do {
            result = client.listObjectsV2(request);
            result.getCommonPrefixes().stream()
                .map(p -> {
                    String relativeKey = StringUtils.removeStart(p, profile.getPrefix());
                    return new S3Item(StringUtils.removeEnd(relativeKey, delimiter), relativeKey, true);
                })
                .forEach(items::add);

            result.getObjectSummaries().stream()
                .filter(o -> !StringUtils.equals(o.getKey(), fullPrefix) &&
                                MimeType.valueOf(mimetypes.getMimetype(o.getKey())).isCompatibleWith(filerType))
                .map(o -> {
                    String relativeKey = StringUtils.removeStart(o.getKey(), profile.getPrefix());
                    return new S3Item(relativeKey, createUrl(profileId, relativeKey), false);
                })
                .forEach(items::add);

            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());

        return items;
    }

    protected String createUrl(String profileId, String key) {
        return Paths.get(String.format(urlPattern, profileId, key)).normalize().toString();
    }

    protected String normalizePrefix(String prefix) {
        if (StringUtils.isEmpty(prefix)) {
            return prefix;
        } else {
            return stripStart(appendIfMissing(prefix, delimiter), delimiter);
        }
    }

}
