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
package org.craftercms.studio.impl.v2.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.plugin.model.Parameter;
import org.craftercms.commons.plugin.model.Plugin;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.MissingPluginParameterException;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import java.io.File;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_MODULE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH_PATTERN;
import static org.craftercms.studio.impl.v2.service.marketplace.internal.MarketplaceServiceInternalImpl.PLUGIN_CONFIG_FILENAME_CONFIG_KEY;
import static org.craftercms.studio.impl.v2.service.marketplace.internal.MarketplaceServiceInternalImpl.PLUGIN_CONFIG_MODULE_CONFIG_KEY;

/**
 * @author joseross
 * @since 4.0.0
 */
public abstract class PluginUtils {

    private static final Logger logger = LoggerFactory.getLogger(PluginUtils.class);

    /**
     * Validates that all required parameters are provided and have a value
     * @param plugin the plugin to validate
     * @param params the parameters to validate
     * @throws MissingPluginParameterException if any of the required parameters is not valid
     */
    public static void validatePluginParameters(final Plugin plugin, final Map<String, String> params)
            throws MissingPluginParameterException {
        if (CollectionUtils.isEmpty(plugin.getParameters())) {
            logger.debug("There are no parameters defined for plugin: {0}", plugin.getId());
            return;
        }

        for(Parameter param : plugin.getParameters()) {
            logger.debug("Checking parameter {0} for blueprint {1}", param.getName(), plugin.getId());
            if (param.isRequired()) {
                if (!params.containsKey(param.getName()) || StringUtils.isEmpty(params.get(param.getName()))) {
                    throw new MissingPluginParameterException(plugin, param);
                }
            } else {
                params.putIfAbsent(param.getName(), param.getDefaultValue());
            }
        }
        logger.debug("All required parameters are present for blueprint: {0}", plugin.getId());
    }

    public static String getPluginPath(String pluginId) {
        return pluginId.replaceAll("\\.", File.separator);
    }

    public static String getPluginConfigurationPath(StudioConfiguration studioConfiguration, String pluginId) {
        return String.join(File.separator, studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH_PATTERN),
                        getPluginPath(pluginId), studioConfiguration.getProperty(PLUGIN_CONFIG_FILENAME_CONFIG_KEY))
                .replaceAll(PATTERN_MODULE, studioConfiguration.getProperty(PLUGIN_CONFIG_MODULE_CONFIG_KEY));
    }

}
