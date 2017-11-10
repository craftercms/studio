package org.craftercms.studio.api.v1.aws.elastictranscoder;

import java.util.List;

import org.craftercms.studio.api.v1.aws.AwsProfile;

/**
 * Holds the necessary information to request a transcoding job to the AWS Elastic Transcoder.
 *
 * @author avasquez
 */
public class TranscoderProfile extends AwsProfile {

    private String pipelineId;
    private List<TranscoderOutput> outputs;

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
