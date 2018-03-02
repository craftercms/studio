package org.craftercms.studio.impl.v1.asset.processing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.asset.Asset;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessor;
import org.craftercms.studio.api.v1.asset.processing.ProcessorConfiguration;
import org.craftercms.studio.api.v1.exception.AssetProcessingException;

public abstract class AbstractAssetProcessor implements AssetProcessor {

    protected ProcessorConfiguration config;

    @Override
    public void init(ProcessorConfiguration config) {
        this.config = config;
    }

    @Override
    public Asset processAsset(Matcher inputPathMatcher, Asset input) throws AssetProcessingException {
        try {
            String outputRepoPath = getOutputRepoPath(inputPathMatcher);
            if (StringUtils.isEmpty(outputRepoPath)) {
                // No output repo path means write to the same input. So move the original file to a tmp location
                // and make the output file be the input file
                Path inputFile = moveToTmpFile(input.getRepoPath(), input.getFile());
                Path outputFile = input.getFile();

                try {
                    doProcessAsset(inputFile, outputFile, config.getParams());
                } finally {
                    Files.delete(inputFile);
                }

                return new Asset(input.getRepoPath(), outputFile);
            } else {
                Path inputFile = input.getFile();
                Path outputFile = createTmpFile(outputRepoPath);

                doProcessAsset(inputFile, outputFile, config.getParams());

                return new Asset(outputRepoPath, outputFile);
            }
        } catch (Exception e) {
            throw new AssetProcessingException("Error while executing asset processor of type '" + config.getType() + "'", e);
        }
    }

    private Path moveToTmpFile(String repoPath, Path file) throws IOException {
        Path tmpFile = createTmpFile(repoPath);

        return Files.move(file, tmpFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    private Path createTmpFile(String repoPath) throws IOException {
        return Files.createTempFile(FilenameUtils.getBaseName(repoPath), "." + FilenameUtils.getExtension(repoPath));
    }

    protected String getOutputRepoPath(Matcher inputPathMatcher) {
        if (StringUtils.isNotEmpty(config.getOutputPathFormat())) {
            int groupCount = inputPathMatcher.groupCount();
            String outputPath = config.getOutputPathFormat();

            for (int i = 1; i <= groupCount; i++) {
                outputPath = outputPath.replace("$" + i, inputPathMatcher.group(i));
            }

            return outputPath;
        } else {
            return null;
        }
    }

    protected abstract void doProcessAsset(Path inputFile, Path outputFile, Map<String, String> params) throws Exception;

}
