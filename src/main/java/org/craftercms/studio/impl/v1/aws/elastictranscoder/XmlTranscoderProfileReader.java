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
