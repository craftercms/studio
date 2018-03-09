package org.craftercms.studio.api.v1.asset.processing;

import org.craftercms.studio.api.v1.exception.AssetProcessingConfigurationException;

public interface AssetProcessorFactory {

    AssetProcessor getProcessor(ProcessorConfiguration config) throws AssetProcessingConfigurationException;

}
