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

import java.io.InputStream;
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.studio.api.v1.exception.AssetProcessingConfigurationException;

/**
 * Reads the configuration from an input stream or a Apache Commons Configuration object and maps it to actual asset processing
 * configuration objects like {@link ProcessorPipelineConfiguration} and {@link ProcessorConfiguration}.
 *
 * @author avasquez
 */
public interface AssetProcessingConfigReader {

    /**
     * Reads the configuration from the input stream and maps it to {@link ProcessorPipelineConfiguration} objects.
     *
     * @param in the input stream of the configuration
     *
     * @return the list with the pipeline configurations.
     *
     * @throws AssetProcessingConfigurationException if the configuration couldn't be read because of an error
     */
    List<ProcessorPipelineConfiguration> readConfig(InputStream in) throws AssetProcessingConfigurationException;

    /**
     * Reads the configuration from the Apache Commons Configuration and maps it to {@link ProcessorPipelineConfiguration} objects.
     *
     * @param config the Apache Commons Configuration
     *
     * @return the list with the pipeline configurations.
     *
     * @throws AssetProcessingConfigurationException if the configuration couldn't be read because of an error
     */
    List<ProcessorPipelineConfiguration> readConfig(HierarchicalConfiguration config) throws AssetProcessingConfigurationException;

}
