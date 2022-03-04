/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v1.aws.elastictranscoder;

import org.craftercms.commons.config.profiles.aws.AbstractAwsProfile;

import java.util.List;

/**
 * Holds the necessary information to request a transcoding job to the AWS Elastic Transcoder.
 *
 * @author avasquez
 */
public class TranscoderProfile extends AbstractAwsProfile {

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
