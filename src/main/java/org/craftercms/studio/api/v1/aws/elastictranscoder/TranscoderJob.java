package org.craftercms.studio.api.v1.aws.elastictranscoder;

/**
 * Represents the metadata of an AWS Elastic Transcoder job.
 *
 * @author avasquez
 */
public class TranscoderJob {

    private String id;
    private String outputBucket;
    private String baseKey;

    /**
     * Returns the ID of the job.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the job.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name of the S3 output bucket where the transcoder will put the result files.
     */
    public String getOutputBucket() {
        return outputBucket;
    }

    /**
     * Sets the name of the S3 output bucket where the transcoder will put the result files.
     */
    public void setOutputBucket(String outputBucket) {
        this.outputBucket = outputBucket;
    }

    /**
     * Returns the base key of the collection of transcoded files. The final filenames will be {@code baseKey + all output key suffixes}.
     */
    public String getBaseKey() {
        return baseKey;
    }

    /**
     * Sets the base key of the collection of transcoded files. The final filenames will be {@code baseKey + all output key suffixes}.
     */
    public void setBaseKey(String baseKey) {
        this.baseKey = baseKey;
    }

}
