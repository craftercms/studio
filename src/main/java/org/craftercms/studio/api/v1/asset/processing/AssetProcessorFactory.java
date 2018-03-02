package org.craftercms.studio.api.v1.asset.processing;

public interface AssetProcessorFactory {

    AssetProcessor getProcessor(ProcessorConfiguration config);

}
