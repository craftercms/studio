package org.craftercms.studio.impl.v1.asset.processing;

import org.craftercms.studio.api.v1.asset.processing.AssetProcessorFactory;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessorPipeline;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessorPipelineFactory;
import org.craftercms.studio.api.v1.asset.processing.ProcessorPipelineConfiguration;
import org.springframework.beans.factory.annotation.Required;

public class AssetProcessorPipelineFactoryImpl implements AssetProcessorPipelineFactory {

    private AssetProcessorFactory processorFactory;

    @Required
    public void setProcessorFactory(AssetProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    @Override
    public AssetProcessorPipeline getPipeline(ProcessorPipelineConfiguration config) {
        return new AssetProcessorPipelineImpl(processorFactory);
    }

}
