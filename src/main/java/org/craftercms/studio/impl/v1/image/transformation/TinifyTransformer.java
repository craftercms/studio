package org.craftercms.studio.impl.v1.image.transformation;

import com.tinify.Source;
import com.tinify.Tinify;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.craftercms.studio.api.v1.exception.ImageTransformationException;
import org.craftercms.studio.api.v1.image.transformation.ImageTransformer;
import org.springframework.beans.factory.annotation.Required;

public class TinifyTransformer implements ImageTransformer {

    @Required
    public void setApiKey(String apiKey) {
        Tinify.setKey(apiKey);
    }

    @Override
    public void transform(Path sourcePath, Path targetPath, Map<String, String> parameters) throws ImageTransformationException {
        try {
            Source source = Tinify.fromFile(sourcePath.toAbsolutePath().toString());
            source.toFile(sourcePath.toAbsolutePath().toString());
        } catch (Exception e) {
            throw new ImageTransformationException("Error while performing the Tinify transformation", e);
        }
    }

}
