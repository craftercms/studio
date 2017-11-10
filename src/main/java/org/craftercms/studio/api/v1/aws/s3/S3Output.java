package org.craftercms.studio.api.v1.aws.s3;

/**
 * Represents metadata of an AWS S3 Upload.
 * @author joseross
 */
public class S3Output {

    /**
     * Name of the bucket where the upload was done.
     */
    private String bucket;

    /**
     * Key of the file uploaded.
     */
    private String key;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

}
