package org.craftercms.studio.impl.v1.aws.elastictranscoder;

import com.amazonaws.auth.BasicAWSCredentials;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderOutput;
import org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderProfile;
import org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderProfileReader;
import org.craftercms.studio.api.v1.exception.TranscoderConfigurationException;

/**
 * Default implementation of {@link TranscoderProfileReader}. It uses Apache Commons Configuration to read an XML transcoder profile like
 * the following:
 *
 * <pre>
 * &gt;?xml version="1.0" encoding="UTF-8"?&lt;
 * &gt;transcoderProfile&lt;
 *   &gt;credentials&lt;
 *     &gt;accessKey&lt;AKIAJYC26IEUXH72GLRA&gt;/accessKey&lt;
 *     &gt;secretKey&lt;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&gt;/secretKey&lt;
 *   &gt;/credentials&lt;
 *   &gt;region&lt;us-east-1&gt;/region&lt;
 *   &gt;pipelineId&lt;1506362170872-4fjm1z&gt;/pipelineId&lt;
 *   &gt;outputs&lt;
 *     &gt;output&lt;
 *       &gt;presetId&lt;1469045103530-uituq6&gt;/presetId&lt;
 *       &gt;outputKeySuffix&lt;-small.mp4&gt;/outputKeySuffix&lt;
 *     &gt;/output&lt;
 *     &gt;output&lt;
 *       &gt;presetId&lt;1468420224698-eqnmwc&gt;/presetId&lt;
 *       &gt;outputKeySuffix&lt;-medium.mp4&gt;/outputKeySuffix&lt;
 *     &gt;/output&lt;
 *     &gt;output&lt;
 *       &gt;presetId&lt;1468420080538-lcq01g&gt;/presetId&lt;
 *       &gt;outputKeySuffix&lt;-large.mp4&gt;/outputKeySuffix&lt;
 *     &gt;/output&lt;
 *    &gt;/outputs&lt;
 * &gt;/transcoderProfile&lt;
 * </pre>
 *
 */
public class XmlTranscoderProfileReader implements TranscoderProfileReader {

    @Override
    public TranscoderProfile readProfile(InputStream input) throws TranscoderConfigurationException {
        HierarchicalConfiguration config = getConfiguration(input);
        TranscoderProfile profile = new TranscoderProfile();

        String accessKey = getRequiredStringProperty(config, "credentials.accessKey");
        String secretKey = getRequiredStringProperty(config, "credentials.secretKey");

        profile.setCredentials(new BasicAWSCredentials(accessKey, secretKey));
        profile.setRegion(getRequiredStringProperty(config, "region"));
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

    protected HierarchicalConfiguration getConfiguration(InputStream input) throws TranscoderConfigurationException {
        try {
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class);
            XMLConfiguration config = builder.configure(params.xml()).getConfiguration();
            FileHandler fileHandler = new FileHandler(config);

            fileHandler.setEncoding("UTF-8");
            fileHandler.load(input);

            return config;
        } catch (ConfigurationException e) {
            throw new TranscoderConfigurationException("Unable to read the transcoder profile configuration", e);
        }
    }

    protected String getRequiredStringProperty(Configuration config, String key) throws TranscoderConfigurationException {
        String property = config.getString(key);
        if (StringUtils.isEmpty(property)) {
            throw new TranscoderConfigurationException("Missing required property '" + key + "'");
        } else {
            return property;
        }
    }

    public static List<HierarchicalConfiguration> getRequiredConfigurationsAt(HierarchicalConfiguration config,
                                                                              String key) throws TranscoderConfigurationException {
        List<HierarchicalConfiguration> configs = config.configurationsAt(key);
        if (CollectionUtils.isEmpty(configs)) {
            throw new TranscoderConfigurationException("Missing required property '" + key + "'");
        } else {
            return configs;
        }
    }

}
