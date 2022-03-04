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
package org.craftercms.studio.api.v1.asset.processing;

import java.util.List;

import org.craftercms.studio.api.v1.asset.Asset;
import org.craftercms.studio.api.v1.exception.AssetProcessingException;

/**
 * Represents a pipeline of {@link AssetProcessor}s.
 *
 * @author avasquez
 */
public interface AssetProcessorPipeline {

    /**
     * Processes the asset, only if there's a match with {@link ProcessorPipelineConfiguration#getInputPathPattern()}. If there's no
     * match, an empty list is returned. Multiple outputs can be returned depending whether or not the processors of the pipeline
     * have an output different than their input.
     *
     * @param config    the configuration to use for the pipeline execution
     * @param input     the input of the pipeline
     *
     * @return the outputs, or an empty list if the input was not processed
     * @throws AssetProcessingException if an error occurs
     */
    List<Asset> processAsset(ProcessorPipelineConfiguration config, Asset input) throws AssetProcessingException;

}
