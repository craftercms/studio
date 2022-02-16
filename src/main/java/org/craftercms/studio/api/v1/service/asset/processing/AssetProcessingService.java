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
package org.craftercms.studio.api.v1.service.asset.processing;

import java.io.InputStream;
import java.util.Map;

/**
 * Service to run an asset through processor pipelines. Asset processing can be used to perform image transformation like
 * ImageMagick and Tinify.
 *
 * @author avasquez
 */
public interface AssetProcessingService {

    Map<String, Object> processAsset(String site, String path, String assetName, InputStream in, String isImage,
                                     String allowedWidth, String allowedHeight, String allowLessSize, String draft,
                                     String unlock, String systemAsset);

}
