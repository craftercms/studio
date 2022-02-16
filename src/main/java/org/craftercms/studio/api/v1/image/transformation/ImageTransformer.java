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
package org.craftercms.studio.api.v1.image.transformation;

import java.nio.file.Path;
import java.util.Map;

import org.craftercms.studio.api.v1.exception.ImageTransformationException;

/**
 * Perform image transformation from the source path to the target path.
 *
 * @author avasquez
 */
public interface ImageTransformer {

    /**
     * Transforms the image at {@code sourcePath} and saves it into {@code targetPath}.
     *
     * @param sourcePath    the source image path
     * @param targetPath    the target path where to put the transformed image.
     * @param parameters    additional parameters needed by the transformer
     *
     * @throws ImageTransformationException if an error occurred
     */
    void transform(Path sourcePath, Path targetPath, Map<String, String> parameters) throws ImageTransformationException;

}
