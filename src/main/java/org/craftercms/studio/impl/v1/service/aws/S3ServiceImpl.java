/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.commons.config.profiles.ConfigurationProfileNotFoundException;
import org.craftercms.commons.config.profiles.aws.S3Profile;
import org.craftercms.commons.file.stores.S3Utils;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.aws.s3.S3Output;
import org.craftercms.studio.api.v1.exception.AwsException;
import org.craftercms.studio.api.v1.service.aws.AbstractAwsService;
import org.craftercms.studio.api.v1.service.aws.S3Service;
import org.craftercms.studio.impl.v1.util.config.profiles.SiteAwareConfigProfileLoader;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.InputStream;

/**
 * Default implementation of {@link S3Service}.
 *
 * @author joseross
 * @deprecated This service has been replaced with {@link org.craftercms.studio.impl.v2.service.aws.s3.AwsS3ServiceImpl}
 */
@Deprecated
public class S3ServiceImpl extends AbstractAwsService<S3Profile> implements S3Service {

    protected int partSize;

    public S3ServiceImpl(SiteAwareConfigProfileLoader<S3Profile> profileLoader) {
        super(profileLoader);
        partSize = AwsUtils.MIN_PART_SIZE;
    }

    public void setPartSize(final int partSize) {
        this.partSize = partSize;
    }

    protected S3Client getS3Client(S3Profile profile) {
        return S3Utils.createClient(profile);
    }

    @Override
    public S3Output uploadFile(@ValidateStringParam String site,
                               @ValidateStringParam String profileId,
                               @ValidateStringParam String filename,
                               InputStream content) throws AwsException, ConfigurationProfileNotFoundException {
        S3Profile profile = getProfile(site, profileId);
        S3Client s3Client = getS3Client(profile);
        String inputBucket = profile.getBucketName();
        String inputKey = filename;

        AwsUtils.uploadStream(inputBucket, inputKey, s3Client, partSize, filename, content);

        S3Output output = new S3Output();
        output.setBucket(inputBucket);
        output.setKey(inputKey);
        return output;
    }

}
