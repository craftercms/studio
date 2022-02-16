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

package org.craftercms.studio.impl.v2.upgrade.pipeline;

import java.beans.ConstructorProperties;
import java.util.List;

import org.craftercms.commons.upgrade.UpgradeOperation;
import org.craftercms.commons.upgrade.UpgradePipeline;
import org.craftercms.commons.upgrade.VersionProvider;
import org.craftercms.commons.upgrade.impl.pipeline.DefaultUpgradePipelineFactoryImpl;
import org.craftercms.commons.upgrade.impl.pipeline.DefaultUpgradePipelineImpl;

import org.springframework.core.io.Resource;


/**
 * Extension of {@link org.craftercms.commons.upgrade.impl.pipeline.DefaultUpgradePipelineFactoryImpl} that creates
 * instances of pipelines using Spring beans.
 *
 * @author joseross
 * @since 4.0.0
 */
public class PrototypeUpgradePipelineFactoryImpl extends DefaultUpgradePipelineFactoryImpl<String> {

    /**
     * Name of the pipeline to instantiate.
     */
    protected String pipelinePrototype;

    @ConstructorProperties({"pipelineName", "configurationFile", "versionProvider", "pipelinePrototype"})
    public PrototypeUpgradePipelineFactoryImpl(String pipelineName, Resource configurationFile,
                                               VersionProvider<String> versionProvider, String pipelinePrototype) {
        super(pipelineName, configurationFile, versionProvider);
        this.pipelinePrototype = pipelinePrototype;
    }

    @SuppressWarnings("unchecked")
    protected UpgradePipeline<String> createPipeline(String name, List<UpgradeOperation<String>> operations) {
        return (DefaultUpgradePipelineImpl<String>) applicationContext.getBean(pipelinePrototype, name, operations);
    }

}
