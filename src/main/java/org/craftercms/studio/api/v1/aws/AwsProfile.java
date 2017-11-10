package org.craftercms.studio.api.v1.aws;

import com.amazonaws.auth.AWSCredentials;

/**
 * Holds the basic information required by all services.
 *
 * @author joseross
 */
public abstract class AwsProfile {

    private AWSCredentials credentials;
    private String region;

    /**
     * Returns the AWS credentials used to authenticate to S3 and Elastic Transcoder.
     */
    public AWSCredentials getCredentials() {
        return credentials;
    }

    /**
     * Sets the AWS credentials used to authenticate to S3 and Elastic Transcoder.
     */
    public void setCredentials(AWSCredentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Returns the region of the S3/Elastic Transcoder
     */
    public String getRegion() {
        return region;
    }

    /**
     * Sets the region of the S3/Elastic Transcoder
     */
    public void setRegion(String region) {
        this.region = region;
    }

}
