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

import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.upgrade.UpgradePipeline;
import org.craftercms.commons.upgrade.VersionProvider;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.commons.upgrade.impl.UpgradeContext;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.springframework.core.io.Resource;

import java.beans.ConstructorProperties;

/**
 * Extension of {@link PrototypeUpgradePipelineFactoryImpl} for configuration files, the name of the pipeline
 * is build based on the configuration file provided in the upgrade context.
 *
 * @author joseross
 * @since 4.0.0
 */
public class ConfigurationUpgradePipelineFactoryImpl extends PrototypeUpgradePipelineFactoryImpl {

    public static final String CONFIG_PIPELINE_SUFFIX = ".pipeline";

    @ConstructorProperties({"configurationFile", "versionProvider", "pipelinePrototype"})
    public ConfigurationUpgradePipelineFactoryImpl(Resource configurationFile, VersionProvider<String> versionProvider,
                                                   String pipelinePrototype) {
        super(null, configurationFile, versionProvider, pipelinePrototype);
    }

    @Override
    public UpgradePipeline<String> getPipeline(UpgradeContext<String> context)
            throws UpgradeException, ConfigurationException {
        var studioContext = (StudioUpgradeContext) context;
        pipelineName = studioContext.getCurrentConfigName() + CONFIG_PIPELINE_SUFFIX;
        return super.getPipeline(context);
    }
}
