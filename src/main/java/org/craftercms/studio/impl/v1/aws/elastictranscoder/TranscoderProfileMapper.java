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

package org.craftercms.studio.impl.v1.aws.elastictranscoder;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.config.ConfigurationResolver;
import org.craftercms.commons.config.profiles.aws.AbstractAwsProfile;
import org.craftercms.commons.config.profiles.aws.AbstractAwsProfileMapper;
import org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderOutput;
import org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderProfile;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;

import static org.craftercms.commons.config.ConfigUtils.getRequiredConfigurationsAt;
import static org.craftercms.commons.config.ConfigUtils.getRequiredStringProperty;
import static org.craftercms.commons.config.ConfigUtils.getStringProperty;

/**
 * ElasticTranscoder implementation of {@link AbstractAwsProfileMapper}. It uses Apache Commons Configuration to read an XML
 * transcoder profile like the following:
 *
 * <pre>
 * &lt;profile&gt;
 *   &lt;id&gt;xxxxx&lt;/id&gt;
 *   &lt;credentials&gt;
 *     &lt;accessKey&gt;XXXXXXXXXXXXXXXXXXXX&lt;/accessKey&gt;
 *     &lt;secretKey&gt;XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX&lt;/secretKey&gt;
 *   &lt;/credentials&gt;
 *   &lt;region&gt;us-east-1&lt;/region&gt;
 *   &lt;pipelineId&gt;00000000000000000000&lt;/pipelineId&gt;
 *   &lt;outputs&gt;
 *     &lt;output&gt;
 *       &lt;presetId&gt;00000000000000000000&lt;/presetId&gt;
 *       &lt;outputKeySuffix&gt;-small.mp4&lt;/outputKeySuffix&gt;
 *     &lt;/output&gt;
 *     &lt;output&gt;
 *       &lt;presetId&gt;00000000000000000000&lt;/presetId&gt;
 *       &lt;outputKeySuffix&gt;-medium.mp4&lt;/outputKeySuffix&gt;
 *     &lt;/output&gt;
 *     &lt;output&gt;
 *       &lt;presetId&gt;00000000000000000000&lt;/presetId&gt;
 *       &lt;outputKeySuffix&gt;-large.mp4&lt;/outputKeySuffix&gt;
 *     &lt;/output&gt;
 *    &lt;/outputs&gt;
 * &lt;/profile&gt;
 * </pre>
 *
 */
public class TranscoderProfileMapper extends AbstractAwsProfileMapper<TranscoderProfile> {

    public static final String SERVICE_NAME = "elasticTranscoder";

    @ConstructorProperties({"resolver"})
    public TranscoderProfileMapper(final ConfigurationResolver resolver) {
        super(SERVICE_NAME, resolver);
    }

    @Override
    protected TranscoderProfile mapProfile(HierarchicalConfiguration<ImmutableNode> profileConfig)
            throws ConfigurationException {
        TranscoderProfile profile = super.mapProfile(profileConfig);
        profile.setPipelineId(getRequiredStringProperty(profileConfig, "pipelineId"));

        List<HierarchicalConfiguration<ImmutableNode>> outputConfigs =
                getRequiredConfigurationsAt(profileConfig, "outputs.output");
        List<TranscoderOutput> outputs = new ArrayList<>();

        for (HierarchicalConfiguration outputConfig : outputConfigs) {
            TranscoderOutput output = new TranscoderOutput();
            output.setPresetId(getRequiredStringProperty(outputConfig, "presetId"));
            output.setOutputKeySuffix(getRequiredStringProperty(outputConfig, "outputKeySuffix"));
            output.setThumbnailSuffixFormat(getStringProperty(outputConfig, "thumbnailSuffixFormat"));

            outputs.add(output);
        }

        profile.setOutputs(outputs);

        return profile;
    }

    @Override
    protected AbstractAwsProfile createProfile() {
        return new TranscoderProfile();
    }

}
