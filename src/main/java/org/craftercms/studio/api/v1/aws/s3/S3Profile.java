package org.craftercms.studio.api.v1.aws.s3;

import org.craftercms.studio.api.v1.aws.AwsProfile;

/**
 * Holds the information to request an AWS S3 Upload.
 *
 * @author joseross
 */
public class S3Profile extends AwsProfile {

    /**
     * Name of the bucket where the upload will be made.
     */
    protected String bucketName;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(final String bucketName) {
        this.bucketName = bucketName;
    }

}
