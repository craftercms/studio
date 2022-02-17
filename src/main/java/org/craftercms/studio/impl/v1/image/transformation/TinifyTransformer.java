/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.impl.v1.image.transformation;

import com.tinify.Source;
import com.tinify.Tinify;

import java.nio.file.Path;
import java.util.Map;

import org.craftercms.studio.api.v1.exception.ImageTransformationException;
import org.craftercms.studio.api.v1.image.transformation.ImageTransformer;
import org.springframework.beans.factory.annotation.Required;

/**
 * Transformer that used the Java client of TinyPNG to compress JPEG/PNG images.
 *
 * @author avasquez
 * @see <a href="https://tinypng.com/developers/reference">TinyPNG</a>
 */
public class TinifyTransformer implements ImageTransformer {

    @Required
    public void setApiKey(String apiKey) {
        Tinify.setKey(apiKey);
    }

    @Override
    public void transform(Path sourcePath, Path targetPath, Map<String, String> parameters) throws ImageTransformationException {
        try {
            Source source = Tinify.fromFile(sourcePath.toAbsolutePath().toString());
            source.toFile(targetPath.toAbsolutePath().toString());
        } catch (Exception e) {
            throw new ImageTransformationException("Error while performing the Tinify transformation", e);
        }
    }

}
