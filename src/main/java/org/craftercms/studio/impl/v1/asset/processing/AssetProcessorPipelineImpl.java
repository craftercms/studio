package org.craftercms.studio.impl.v1.asset.processing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

    public AssetProcessorPipelineImpl(AssetProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    @Override
    public Collection<Asset> processAsset(ProcessorPipelineConfiguration config, Asset input) throws AssetProcessingException {
        Matcher inputPatMatcher = matchForProcessing(config, input);
        if (inputPatMatcher != null) {
            Set<Asset> outputs = new LinkedHashSet<>();
            Asset originalInput = input;
            Map<ProcessorConfiguration, AssetProcessor> processors = getProcessors(config);

            if (config.isKeepOriginal()) {
                outputs.add(originalInput);
            }

            for (Map.Entry<ProcessorConfiguration, AssetProcessor> entry : processors.entrySet()) {
                Asset output = entry.getValue().processAsset(entry.getKey(), inputPatMatcher, input);
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

    private Matcher matchForProcessing(ProcessorPipelineConfiguration config, Asset input) {
        Pattern inputPathPattern = Pattern.compile(config.getInputPathPattern());
        Matcher inputPathMatcher = inputPathPattern.matcher(input.getRepoPath());

        if (inputPathMatcher.matches()) {
            return inputPathMatcher;
        } else {
            return null;
        }
    }

    private Map<ProcessorConfiguration, AssetProcessor> getProcessors(ProcessorPipelineConfiguration config) {
        Map<ProcessorConfiguration, AssetProcessor> processors = new LinkedHashMap<>(config.getProcessorsConfig().size());

        for (ProcessorConfiguration processorConfig : config.getProcessorsConfig()) {
            processors.put(processorConfig, processorFactory.getProcessor(processorConfig));
        }

        return processors;
    }

}
