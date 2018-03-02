package org.craftercms.studio.api.v1.asset.processing;

public interface AssetProcessorPipelineFactory {

    AssetProcessorPipeline getPipeline(ProcessorPipelineConfiguration config);

}
