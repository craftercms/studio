/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v1.aws.elastictranscoder;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderOutput;
import org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderProfile;
import org.craftercms.studio.api.v1.aws.AwsProfileReader;
import org.craftercms.studio.api.v1.exception.AwsConfigurationException;
import org.craftercms.studio.api.v1.aws.AbstractXmlProfileReader;

/**
 * ElasticTranscoder implementation of {@link AwsProfileReader}. It uses Apache Commons Configuration to read an XML
 * transcoder profile like the following:
 *
 * <pre>
 * &gt;profile&lt;
 *   &gt;id&lt;xxxxx&gt;/id&lt;
 *   &gt;credentials&lt;
 *     &gt;accessKey&lt;XXXXXXXXXXXXXXXXXXXX&gt;/accessKey&lt;
 *     &gt;secretKey&lt;XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX&gt;/secretKey&lt;
 *   &gt;/credentials&lt;
 *   &gt;region&lt;us-east-1&gt;/region&lt;
 *   &gt;pipelineId&lt;00000000000000000000&gt;/pipelineId&lt;
 *   &gt;outputs&lt;
 *     &gt;output&lt;
 *       &gt;presetId&lt;00000000000000000000&gt;/presetId&lt;
 *       &gt;outputKeySuffix&lt;-small.mp4&gt;/outputKeySuffix&lt;
 *     &gt;/output&lt;
 *     &gt;output&lt;
 *       &gt;presetId&lt;00000000000000000000&gt;/presetId&lt;
 *       &gt;outputKeySuffix&lt;-medium.mp4&gt;/outputKeySuffix&lt;
 *     &gt;/output&lt;
 *     &gt;output&lt;
 *       &gt;presetId&lt;00000000000000000000&gt;/presetId&lt;
 *       &gt;outputKeySuffix&lt;-large.mp4&gt;/outputKeySuffix&lt;
 *     &gt;/output&lt;
 *    &gt;/outputs&lt;
 * &gt;/profile&lt;
 * </pre>
 *
 */
public class XmlTranscoderProfileReader extends AbstractXmlProfileReader<TranscoderProfile> {

    @Override
    public TranscoderProfile readProfile(final HierarchicalConfiguration config) throws AwsConfigurationException {
        TranscoderProfile profile = new TranscoderProfile();
        readBasicProperties(config, profile);
        profile.setPipelineId(getRequiredStringProperty(config, "pipelineId"));

        List<HierarchicalConfiguration> outputConfigs = getRequiredConfigurationsAt(config, "outputs.output");
        List<TranscoderOutput> outputs = new ArrayList<>();

        for (HierarchicalConfiguration outputConfig : outputConfigs) {
            TranscoderOutput output = new TranscoderOutput();
            output.setPresetId(getRequiredStringProperty(outputConfig, "presetId"));
            output.setOutputKeySuffix(getRequiredStringProperty(outputConfig, "outputKeySuffix"));
            output.setThumbnailSuffixFormat(outputConfig.getString("thumbnailSuffixFormat"));

            outputs.add(output);
        }

        profile.setOutputs(outputs);

        return profile;
    }

}
