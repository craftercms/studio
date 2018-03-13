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

import org.craftercms.studio.api.v1.asset.processing.AssetProcessor;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessorResolver;
import org.craftercms.studio.api.v1.asset.processing.ProcessorConfiguration;
import org.craftercms.studio.api.v1.exception.AssetProcessingConfigurationException;
import org.craftercms.studio.api.v1.exception.AssetProcessingException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Default implementation for {@link AssetProcessorResolver}.
 *
 * @author avasquez
 */
public class AssetProcessorResolverImpl implements AssetProcessorResolver, ApplicationContextAware {

    private String beanNameFormat;
    private ApplicationContext applicationContext;

    @Required
    public void setBeanNameFormat(String beanNameFormat) {
        this.beanNameFormat = beanNameFormat;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public AssetProcessor getProcessor(ProcessorConfiguration config) throws AssetProcessingException {
        String beanName = String.format(beanNameFormat, config.getType());
        AssetProcessor processor = applicationContext.getBean(beanName, AssetProcessor.class);

        if (processor != null) {
            return processor;
        } else {
            throw new AssetProcessingConfigurationException("Invalid processor type: " + config.getType());
        }
    }

}
