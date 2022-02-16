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

import org.craftercms.studio.api.v1.exception.AssetProcessingException;

/**
 * Resolves a {@link AssetProcessorPipeline} based on configuration.
 *
 * @author avasquez
 */
public interface AssetProcessorPipelineResolver {

    /**
     * Returns an {@link AssetProcessorPipeline} that's compatible with the specified configuration
     *
     * @param config the configuration
     *
     * @return the pipeline for the given configuration
     * @throws AssetProcessingException if there's an error while retrieving the processor or if the configuration is invalid
     */
    AssetProcessorPipeline getPipeline(ProcessorPipelineConfiguration config) throws AssetProcessingException;

}
