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
package org.craftercms.studio.impl.v1.asset.processing;

import java.util.ArrayList;
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
import org.craftercms.studio.api.v1.asset.processing.AssetProcessorResolver;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessorPipeline;
import org.craftercms.studio.api.v1.asset.processing.ProcessorConfiguration;
import org.craftercms.studio.api.v1.asset.processing.ProcessorPipelineConfiguration;
import org.craftercms.studio.api.v1.exception.AssetProcessingException;

/**
 * Default implementation of {@link AssetProcessorPipeline}.
 *
 * @author avasquez
 */
public class AssetProcessorPipelineImpl implements AssetProcessorPipeline {

    private AssetProcessorResolver processorFactory;

    public AssetProcessorPipelineImpl(AssetProcessorResolver processorFactory) {
        this.processorFactory = processorFactory;
    }

    @Override
    public List<Asset> processAsset(ProcessorPipelineConfiguration config, Asset input) throws AssetProcessingException {
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

            return new ArrayList<>(outputs);
        } else {
            return Collections.emptyList();
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

    private Map<ProcessorConfiguration, AssetProcessor> getProcessors(ProcessorPipelineConfiguration config)
        throws AssetProcessingException {
        Map<ProcessorConfiguration, AssetProcessor> processors = new LinkedHashMap<>(config.getProcessorsConfig().size());

        for (ProcessorConfiguration processorConfig : config.getProcessorsConfig()) {
            processors.put(processorConfig, processorFactory.getProcessor(processorConfig));
        }

        return processors;
    }

}
