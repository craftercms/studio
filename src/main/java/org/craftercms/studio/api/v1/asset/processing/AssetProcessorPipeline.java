package org.craftercms.studio.api.v1.asset.processing;

import java.util.Collection;

import org.craftercms.studio.api.v1.asset.Asset;
import org.craftercms.studio.api.v1.exception.AssetProcessingException;

public interface AssetProcessorPipeline {

    Collection<Asset> processAsset(ProcessorPipelineConfiguration config, Asset input) throws AssetProcessingException;

}
