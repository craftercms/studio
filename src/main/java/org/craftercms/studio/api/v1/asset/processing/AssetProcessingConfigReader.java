package org.craftercms.studio.api.v1.asset.processing;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.studio.api.v1.exception.AssetProcessingConfigurationException;

public interface AssetProcessingConfigReader {

    List<ProcessorPipelineConfiguration> readConfig(InputStream in) throws AssetProcessingConfigurationException;

    List<ProcessorPipelineConfiguration> readConfig(HierarchicalConfiguration config) throws AssetProcessingConfigurationException;

}
