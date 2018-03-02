package org.craftercms.studio.api.v1.asset.processing;

import java.util.Collection;

import org.craftercms.studio.api.v1.asset.Asset;
import org.craftercms.studio.api.v1.exception.AssetProcessingException;

public interface AssetProcessorPipeline {

    void init(ProcessorPipelineConfiguration config);

    Collection<Asset> processAsset(Asset input) throws AssetProcessingException;

}
