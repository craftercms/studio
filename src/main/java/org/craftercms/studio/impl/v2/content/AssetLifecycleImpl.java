/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.content;

import org.craftercms.studio.api.v1.asset.Asset;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessingConfigReader;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessorPipeline;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessorPipelineResolver;
import org.craftercms.studio.api.v1.asset.processing.ProcessorPipelineConfiguration;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.content.ContentLifecycle;
import org.craftercms.studio.api.v2.content.ContentLoader;
import org.craftercms.studio.api.v2.content.LifecycleContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static org.apache.commons.collections4.IterableUtils.isEmpty;

/**
 * Asset processing implementation of {@link ContentLifecycle}.
 * This class is responsible for executing the asset processing pipeline.
 */
public class AssetLifecycleImpl implements ContentLifecycle {
	private static final Logger logger = LoggerFactory.getLogger(AssetLifecycleImpl.class);

	private final AssetProcessorPipelineResolver pipelineResolver;
	private final AssetProcessingConfigReader configReader;
	private final String configPath;

	@ConstructorProperties({"pipelineResolver", "configReader", "configPath"})
	public AssetLifecycleImpl(AssetProcessorPipelineResolver pipelineResolver, AssetProcessingConfigReader configReader,
							  String configPath) {
		this.pipelineResolver = pipelineResolver;
		this.configReader = configReader;
		this.configPath = configPath;
	}

	@Override
	public void execute(String siteId, LifecycleContent lifecycleContent, ContentLoader loader) throws ServiceLayerException {
		List<ProcessorPipelineConfiguration> pipelinesConfig;
		try (InputStream configIn = loader.getContentRaw(siteId, configPath)) {
			if (configIn == null) {
				logger.debug("No asset processing pipelines config found at '{}' in site '{}'. " +
					"Skip asset processing ...", configPath, siteId);
				return;
			}
			pipelinesConfig = configReader.readConfig(configIn);
			if (isEmpty(pipelinesConfig)) {
				logger.debug("No asset processing pipelines config found at '{}' in site '{}'. " +
					"Skip asset processing ...", configPath, siteId);
				return;
			}
		} catch (IOException e) {
			throw new ServiceLayerException("Failed to load asset processing pipelines config", e);
		}

		String assetPath = lifecycleContent.getRepoPath();
		Path filePath;
		try {
			filePath = lifecycleContent.get(assetPath).filePath();
		} catch (IOException e) {
			logger.error("Failed to create temporary file for asset processing. Site '{}' path '{}'", siteId, assetPath, e);
			throw new ServiceLayerException(format("Failed to create temporary file for asset processing. Site '%s' path '%s'", siteId, assetPath), e);
		}

		Asset input = new Asset(assetPath, filePath);

		Set<Asset> outputs = new LinkedHashSet<>();
		for (ProcessorPipelineConfiguration pipelineConfig : pipelinesConfig) {
			AssetProcessorPipeline pipeline = pipelineResolver.getPipeline(pipelineConfig);
			outputs.addAll(pipeline.processAsset(pipelineConfig, input));
		}

		for (Asset output : outputs) {
			lifecycleContent.write(output.getRepoPath(), output.getFilePath());
		}
	}
}
