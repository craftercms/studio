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
package org.craftercms.studio.impl.v1.asset.processing;

import java.beans.ConstructorProperties;
import java.nio.file.Path;
import java.util.Map;

import org.craftercms.studio.api.v1.exception.ImageTransformationException;
import org.craftercms.studio.api.v1.image.transformation.ImageTransformer;

/**
 * {@link org.craftercms.studio.api.v1.asset.processing.AssetProcessor} that uses and {@link ImageTransformer} to transform an
 * the input image.
 *
 * @author avasquez
 */
public class ImageTransformingProcessor extends AbstractAssetProcessor {

    private ImageTransformer transformer;

    @ConstructorProperties({"transformer"})
    public ImageTransformingProcessor(ImageTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    protected void doProcessAsset(Path inputFile, Path outputFile, Map<String, String> params) throws ImageTransformationException {
        transformer.transform(inputFile, outputFile, params);
    }

}
