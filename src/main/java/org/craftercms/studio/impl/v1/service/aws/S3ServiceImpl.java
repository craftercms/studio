package org.craftercms.studio.impl.v1.service.aws;

import java.io.InputStream;

import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.aws.s3.S3Output;
import org.craftercms.studio.api.v1.aws.s3.S3Profile;
import org.craftercms.studio.api.v1.exception.AwsException;
import org.craftercms.studio.api.v1.service.aws.AbstractAwsService;
import org.craftercms.studio.api.v1.service.aws.S3Service;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * Default implementation of {@link S3Service}.
 *
 * @author joseross
 * @deprecated This service has been replaced with {@link org.craftercms.studio.impl.v2.service.aws.s3.AwsS3ServiceImpl}
 */
@Deprecated
public class S3ServiceImpl extends AbstractAwsService<S3Profile> implements S3Service {

    protected int partSize;

    public S3ServiceImpl() {
        partSize = AwsUtils.MIN_PART_SIZE;
    }

    public void setPartSize(final int partSize) {
        this.partSize = partSize;
    }

    protected AmazonS3 getS3Client(S3Profile profile) {
        return AmazonS3ClientBuilder.standard()
            .withCredentials(profile.getCredentialsProvider())
            .withRegion(profile.getRegion())
            .build();
    }

    @Override
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

}
