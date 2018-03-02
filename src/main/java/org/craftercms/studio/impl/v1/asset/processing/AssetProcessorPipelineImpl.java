package org.craftercms.studio.impl.v1.asset.processing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.craftercms.studio.api.v1.asset.Asset;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessor;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessorFactory;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessorPipeline;
import org.craftercms.studio.api.v1.asset.processing.ProcessorConfiguration;
import org.craftercms.studio.api.v1.asset.processing.ProcessorPipelineConfiguration;
import org.craftercms.studio.api.v1.exception.AssetProcessingException;

public class AssetProcessorPipelineImpl implements AssetProcessorPipeline {

    private AssetProcessorFactory processorFactory;
    private ProcessorPipelineConfiguration config;
    private List<AssetProcessor> processors;

    public AssetProcessorPipelineImpl(AssetProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    @Override
    public void init(ProcessorPipelineConfiguration config) {
        this.config = config;
        this.processors = new ArrayList<>(config.getProcessorsConfig().size());

        for (ProcessorConfiguration processorConfig : config.getProcessorsConfig()) {
            processors.add(processorFactory.getProcessor(processorConfig));
        }
    }

    @Override
    public Collection<Asset> processAsset(Asset input) throws AssetProcessingException {
        Matcher inputPatMatcher = matchForProcessing(input);
        if (inputPatMatcher != null) {
            Set<Asset> outputs = new LinkedHashSet<>();
            Asset originalInput = input;

            if (config.isKeepOriginal()) {
                outputs.add(originalInput);
            }

            for (AssetProcessor processor : processors) {
                Asset output = processor.processAsset(inputPatMatcher, input);
                outputs.add(output);

                input = output;
            }

            if (!config.isKeepOriginal() && outputs.contains(originalInput)) {
                outputs.remove(originalInput);
            }

            return outputs;
        } else {
            return Collections.singletonList(input);
        }
    }

    private Matcher matchForProcessing(Asset input) {
        Pattern inputPathPattern = Pattern.compile(config.getInputPathPattern());
        Matcher inputPathMatcher = inputPathPattern.matcher(input.getRepoPath());

        if (inputPathMatcher.matches()) {
            return inputPathMatcher;
        } else {
            return null;
        }
    }

}
