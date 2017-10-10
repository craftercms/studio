package org.craftercms.studio.api.v1.aws.elastictranscoder;

import com.amazonaws.auth.AWSCredentials;

import java.util.List;

/**
 * Holds the necessary information to request a transcoding job to the AWS Elastic Transcoder.
 *
 * @author avasquez
 */
public class TranscoderProfile {

    private AWSCredentials credentials;
    private String region;
    private String pipelineId;
    private List<TranscoderOutput> outputs;

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

    /**
     * Returns the pipeline ID of the Elastic Transcoder.
     */
    public String getPipelineId() {
        return pipelineId;
    }

    /**
     * Sets the pipeline ID of the Elastic Transcoder.
     */
    public void setPipelineId(String pipelineId) {
        this.pipelineId = pipelineId;
    }

    /**
     * Returns the transcoder outputs that should be generated.
     */
    public List<TranscoderOutput> getOutputs() {
        return outputs;
    }

    /**
     * Sets the transcoder outputs that should be generated.
     */
    public void setOutputs(List<TranscoderOutput> outputs) {
        this.outputs = outputs;
    }

}
