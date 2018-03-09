package org.craftercms.studio.impl.v1.asset.processing;

import java.nio.file.Path;
import java.util.Map;

import org.craftercms.studio.api.v1.exception.ImageTransformationException;
import org.craftercms.studio.api.v1.image.transformation.ImageTransformer;

public class ImageTransformingProcessor extends AbstractAssetProcessor {

    private ImageTransformer transformer;

    public ImageTransformingProcessor(ImageTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    protected void doProcessAsset(Path inputFile, Path outputFile, Map<String, String> params) throws ImageTransformationException {
        transformer.transform(inputFile, outputFile, params);
    }

}
