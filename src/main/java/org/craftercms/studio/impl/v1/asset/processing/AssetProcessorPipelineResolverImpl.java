/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import org.craftercms.studio.api.v1.asset.processing.AssetProcessorResolver;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessorPipeline;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessorPipelineResolver;
import org.craftercms.studio.api.v1.asset.processing.ProcessorPipelineConfiguration;
import org.springframework.beans.factory.annotation.Required;

/**
 * Default implementation of {@link AssetProcessorPipelineResolver}.
 *
 * @author avasquez
 */
public class AssetProcessorPipelineResolverImpl implements AssetProcessorPipelineResolver {

    private AssetProcessorResolver processorResolver;

    @Required
    public void setProcessorResolver(AssetProcessorResolver processorResolver) {
        this.processorResolver = processorResolver;
    }

    @Override
    public AssetProcessorPipeline getPipeline(ProcessorPipelineConfiguration config) {
        return new AssetProcessorPipelineImpl(processorResolver);
    }

}
