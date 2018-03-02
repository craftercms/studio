package org.craftercms.studio.impl.v1.service.asset.processing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.craftercms.studio.api.v1.asset.Asset;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessingConfigReader;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessorPipeline;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessorPipelineFactory;
import org.craftercms.studio.api.v1.asset.processing.ProcessorPipelineConfiguration;
import org.craftercms.studio.api.v1.exception.AssetProcessingException;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.asset.processing.AssetProcessingService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.springframework.beans.factory.annotation.Required;

public class AssetProcessingServiceImpl implements AssetProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(AssetProcessingServiceImpl.class);

    private String configPath;
    private ContentService contentService;
    private AssetProcessingConfigReader configReader;
    private AssetProcessorPipelineFactory pipelineFactory;

    @Required
    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    @Required
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    @Required
    public void setConfigReader(AssetProcessingConfigReader configReader) {
        this.configReader = configReader;
    }

    @Required
    public void setPipelineFactory(AssetProcessorPipelineFactory pipelineFactory) {
        this.pipelineFactory = pipelineFactory;
    }

    @Override
    public void processAsset(String site, String path, String assetName, InputStream in, String isImage, String allowedWidth,
                             String allowedHeight, String allowLessSize, String draft, String unlock,
                             String systemAsset) throws AssetProcessingException {
        InputStream configIn = null;
        boolean skipProcessing = false;

        try {
            configIn = contentService.getContent(site, configPath);
        } catch (ContentNotFoundException e) {
           // Ignore if file couldn't be found
            logger.debug("No asset processing config found at {0}. Skipping asset processing...", path);

            skipProcessing = true;
        }

        List<ProcessorPipelineConfiguration> pipelinesConfig = configReader.readConfig(configIn);
        if (CollectionUtils.isNotEmpty(pipelinesConfig)) {
            Asset input = createAssetFromInputStream(path, in);
            Set<Asset> finalOutputs = new LinkedHashSet<>();

            for (ProcessorPipelineConfiguration pipelineConfig : pipelinesConfig) {
                AssetProcessorPipeline pipeline = pipelineFactory.getPipeline(pipelineConfig);
                Collection<Asset> outputs = pipeline.processAsset(pipelineConfig, input);

                if (CollectionUtils.isNotEmpty(outputs)) {
                    finalOutputs.addAll(outputs);
                }
            }

            try {
                writeOutputs(site, finalOutputs, isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset);
            } catch (Exception e) {
                throw new AssetProcessingException("Error while writing asset pipeline outputs: " + finalOutputs, e);
            }
        } else {
            // Ignore if no pipelines config
            logger.debug("No asset processing pipelines config found at {0}. Skipping asset processing...", path);

            skipProcessing = true;
        }

        if (skipProcessing) {
            try {
                contentService.writeContentAsset(site, path, assetName, in, isImage, allowedWidth, allowedHeight, allowLessSize,
                                                 draft, unlock, systemAsset);
            } catch (ServiceException e) {
                throw new AssetProcessingException(e.getMessage(), e);
            }
        }
    }

    private Asset createAssetFromInputStream(String repoPath, InputStream in) throws AssetProcessingException {
        try {
            Path tmpFile = Files.createTempFile(FilenameUtils.getBaseName(repoPath), "." + FilenameUtils.getExtension(repoPath));

            try (OutputStream out = Files.newOutputStream(tmpFile)) {
                IOUtils.copy(in, out);
            }

            return new Asset(repoPath, tmpFile);
        } catch (IOException e) {
            throw new AssetProcessingException("Unable to create temp file to hold input stream of " + repoPath, e);
        }
    }

    private void writeOutputs(String site, Collection<Asset> outputs, String isImage, String allowedWidth, String allowedHeight,
                              String allowLessSize, String draft, String unlock, String systemAsset) throws AssetProcessingException {
        if (CollectionUtils.isNotEmpty(outputs)) {
            for (Asset output : outputs) {
                try {
                    try (InputStream in = Files.newInputStream(output.getFile())) {
                        contentService.writeContentAsset(site, output.getRepoPath(), FilenameUtils.getName(output.getRepoPath()), in,
                                                         isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset);
                    }
                } catch (Exception e) {
                    throw new AssetProcessingException("Error writing output " + output, e);
                }
            }
        }
    }

}
