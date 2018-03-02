package org.craftercms.studio.api.v1.image.transformation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.craftercms.studio.api.v1.exception.ImageTransformationException;

public interface ImageTransformer {

    void transform(Path sourcePath, Path targetPath, Map<String, String> parameters) throws ImageTransformationException;

}
