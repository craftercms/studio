/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.impl.v1.service.asset.processing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.commons.security.exception.PermissionException;
import org.craftercms.commons.validation.ValidationException;
import org.craftercms.commons.validation.validators.impl.EsapiValidator;
import org.craftercms.studio.api.v1.asset.Asset;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessingConfigReader;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessorPipeline;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessorPipelineResolver;
import org.craftercms.studio.api.v1.asset.processing.ProcessorPipelineConfiguration;
import org.craftercms.studio.api.v1.exception.AssetProcessingException;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.annotation.RequireSiteExists;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.craftercms.studio.api.v1.service.asset.processing.AssetProcessingService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.springframework.validation.Validator;

import static java.lang.String.format;
import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.CONTENT_PATH_WRITE;
import static org.craftercms.studio.api.v2.utils.StudioUtils.getStudioTemporaryFilesRoot;
import static org.craftercms.studio.controller.rest.ValidationUtils.validateValue;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_NAME;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_PATH;

/**
 * Default implementation of {@link AssetProcessingService}.
 *
 * @author avasquez
 */
public class AssetProcessingServiceImpl implements AssetProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(AssetProcessingServiceImpl.class);

    private String configPath;
    private ContentService contentService;
    private AssetProcessingConfigReader configReader;
    private AssetProcessorPipelineResolver pipelineResolver;

    public AssetProcessingServiceImpl(String configPath, ContentService contentService, AssetProcessingConfigReader configReader,
                                      AssetProcessorPipelineResolver pipelineResolver) {
        this.configPath = configPath;
        this.contentService = contentService;
        this.configReader = configReader;
        this.pipelineResolver = pipelineResolver;
    }

    @Override
    @RequireSiteExists
    public Map<String, Object> processAsset(@SiteId String site, String folder, String assetName, InputStream in, String isImage,
                                            String allowedWidth, String allowedHeight, String allowLessSize, String draft,
                                            String unlock, String systemAsset) throws ValidationException {
        Validator pathValidator = new EsapiValidator(CONTENT_PATH_WRITE);
        validateValue(pathValidator, folder, REQUEST_PARAM_PATH);
        validateValue(pathValidator, assetName, REQUEST_PARAM_NAME);

        String repoPath = UrlUtils.concat(folder, assetName);
        InputStream configIn;

        // TODO: SJ: Refactor with guard statements to reduce nesting and enhance readability

        try {
            contentService.checkWriteAssetPath(folder);

            try {
                configIn = contentService.getContent(site, configPath);
            } catch (ContentNotFoundException e) {
                // Ignore if file couldn't be found
                configIn = null;
            }

            if (configIn != null) {
                List<ProcessorPipelineConfiguration> pipelinesConfig = configReader.readConfig(configIn);
                if (CollectionUtils.isNotEmpty(pipelinesConfig)) {
                    Asset input = createAssetFromInputStream(repoPath, in);
                    try {
                        Set<Asset> finalOutputs = new LinkedHashSet<>();

                        for (ProcessorPipelineConfiguration pipelineConfig : pipelinesConfig) {
                            AssetProcessorPipeline pipeline = pipelineResolver.getPipeline(pipelineConfig);
                            List<Asset> outputs = pipeline.processAsset(pipelineConfig, input);

                            if (CollectionUtils.isNotEmpty(outputs)) {
                                finalOutputs.addAll(outputs);
                            }
                        }

                        if (CollectionUtils.isNotEmpty(finalOutputs)) {
                            List<Map<String, Object>> results = writeOutputs(site, finalOutputs, isImage, allowedWidth, allowedHeight,
                                                                             allowLessSize, draft, unlock, systemAsset);


                            // Return first result for now, might be good in the future to consider returning several results or just
                            // one main result specified by config -- Alfonso
                            if (CollectionUtils.isNotEmpty(results)) {
                                return results.get(0);
                            } else {
                                return Collections.emptyMap();
                            }
                        } else {
                            // No outputs mean that the input wasn't matched by any pipeline and processing was skipped
                            logger.debug("No pipeline matched for path '{}' in site '{}'. Skip asset processing ...",
                                    repoPath, site);

                            // We already read input so open the temp file
                            try (InputStream assetIn = Files.newInputStream(input.getFilePath())) {
                                return contentService.writeContentAsset(site, folder, assetName, assetIn, isImage, allowedWidth,
                                                                        allowedHeight, allowLessSize, draft, unlock, systemAsset);
                            }
                        }
                    } finally {
                        try {
                            Files.delete(input.getFilePath());
                        } catch (IOException e) {
                            // delete silently
                        }
                    }
                } else {
                    // Ignore if no pipelines config
                    logger.debug("No asset processing pipelines config found at '{}' in site '{}'. " +
                                    "Skip asset processing ...", repoPath, site);

                    return contentService.writeContentAsset(site, folder, assetName, in, isImage, allowedWidth, allowedHeight,
                                                            allowLessSize, draft, unlock, systemAsset);
                }
            } else {
                logger.debug("No asset processing config found at '{}' in site '{}'. Skip asset processing ...",
                        repoPath, site);

                return contentService.writeContentAsset(site, folder, assetName, in, isImage, allowedWidth, allowedHeight, allowLessSize,
                                                        draft, unlock, systemAsset);
            }
        } catch (PermissionException e) {
            logger.error("Current user does not have the required permissions", e);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to process asset '{}' in site '{}'", assetName, site, e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            result.put("error", e);

            return result;
        }
    }

    private Asset createAssetFromInputStream(String repoPath, InputStream in) throws AssetProcessingException {
        try {
            Path tmpFile = Files.createTempFile(getStudioTemporaryFilesRoot(), FilenameUtils.getBaseName(repoPath), "." +
                    FilenameUtils.getExtension(repoPath));

            try (OutputStream out = Files.newOutputStream(tmpFile)) {
                IOUtils.copy(in, out);
            }

            return new Asset(repoPath, tmpFile);
        } catch (IOException e) {
            throw new AssetProcessingException(format("Unable to create a temp file to hold the " +
                            "input stream of '%s'", repoPath), e);
        }
    }

    private List<Map<String, Object>> writeOutputs(String site, Collection<Asset> outputs, String isImage, String allowedWidth,
                                                   String allowedHeight, String allowLessSize, String draft, String unlock,
                                                   String systemAsset) throws AssetProcessingException {
        List<Map<String, Object>> results = new ArrayList<>();

        for (Asset output : outputs) {
            try {
                try (InputStream in = Files.newInputStream(output.getFilePath())) {
                    Map<String, Object> result =
                            contentService.writeContentAsset(site,
                                                          FilenameUtils.getFullPath(output.getRepoPath()),
                                                          FilenameUtils.getName(output.getRepoPath()),
                                                          in, isImage, allowedWidth, allowedHeight,
                                                          allowLessSize, draft, unlock, systemAsset);
                    if (MapUtils.isNotEmpty(result)) {
                        if (result.containsKey("error")) {
                            throw new AssetProcessingException("Error writing output " + output,
                                    (Exception)result.get("error"));
                        } else {
                            results.add(result);
                        }
                    }
                }
            } catch (IOException | ServiceLayerException e) {
                throw new AssetProcessingException("Error writing output " + output, e);
            }
        }

        return results;
    }

}
