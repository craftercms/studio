package org.craftercms.studio.api.v1.asset.processing;

import java.util.List;

import org.craftercms.studio.api.v1.asset.Asset;
import org.craftercms.studio.api.v1.exception.AssetProcessingException;

public interface AssetProcessorPipeline {

    List<Asset> processAsset(ProcessorPipelineConfiguration config, Asset input) throws AssetProcessingException;

}
