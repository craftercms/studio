/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.DefaultExpressionEngine;
import org.apache.commons.configuration2.tree.DefaultExpressionEngineSymbols;
import org.apache.commons.configuration2.tree.ExpressionEngine;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.OverrideCombiner;
import org.craftercms.commons.config.YamlConfiguration;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.prependIfMissing;

public class StudioConfigurationImpl implements StudioConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(StudioConfigurationImpl.class);

    protected HierarchicalConfiguration<ImmutableNode> systemConfig;

    protected Cache configurationCache;

    protected String configLocation;

    public StudioConfigurationImpl(Cache configurationCache, String configLocation) {
        this.configurationCache = configurationCache;
        this.configLocation = configLocation;
    }

    public void init() {
        loadConfig();
    }

    @Override
    public void loadConfig() {
        YamlConfiguration baseConfig = new YamlConfiguration();
        YamlConfiguration overrideConfig = new YamlConfiguration();

        Resource resource = new ClassPathResource(configLocation);
        try (InputStream in = resource.getInputStream()) {
            baseConfig.setExpressionEngine(getExpressionEngine());
            baseConfig.read(in);

            logger.debug("Loaded configuration from location: {0} \n {1}", configLocation, baseConfig);
        } catch (IOException | ConfigurationException e) {
            logger.error("Failed to load studio configuration from: " + configLocation, e);
        }

        if (baseConfig.containsKey(STUDIO_CONFIG_OVERRIDE_CONFIG)) {
            String overrideConfigLocation = baseConfig.getString(STUDIO_CONFIG_OVERRIDE_CONFIG);
            resource = new ClassPathResource(overrideConfigLocation);

            try (InputStream in = resource.getInputStream()) {
                overrideConfig.setExpressionEngine(getExpressionEngine());
                overrideConfig.read(in);

                if (!overrideConfig.isEmpty()) {
                    logger.debug("Loaded additional configuration from location: {0} \n {1}",
                        overrideConfigLocation, overrideConfig);
                }
            } catch (IOException | ConfigurationException e) {
                logger.error("Failed to load studio configuration from: " + overrideConfigLocation, e);
            }
        }

        // Merge the base properties and additional properties
        if(!overrideConfig.isEmpty()) {
            CombinedConfiguration combinedConfig = new CombinedConfiguration(new OverrideCombiner());
            combinedConfig.setExpressionEngine(getExpressionEngine());
            combinedConfig.addConfiguration(overrideConfig);
            combinedConfig.addConfiguration(baseConfig);

            systemConfig = combinedConfig;
        } else {
            systemConfig = baseConfig;
        }
    }

    @SuppressWarnings("unchecked")
    private HierarchicalConfiguration<ImmutableNode> loadGlobalRepoConfig() {
        String cacheKey = prependIfMissing(systemConfig.getString(STUDIO_CONFIG_GLOBAL_REPO_OVERRIDE_CONFIG), "/");
        Path globalRepoOverrideConfigLocation = Paths.get(systemConfig.getString(REPO_BASE_PATH),
                systemConfig.getString(GLOBAL_REPO_PATH),
                systemConfig.getString(STUDIO_CONFIG_GLOBAL_REPO_OVERRIDE_CONFIG));
        HierarchicalConfiguration<ImmutableNode> config = systemConfig;
        Element element = configurationCache.get(cacheKey);
        if (element != null && !element.isExpired()) {
            config = (HierarchicalConfiguration<ImmutableNode>) element.getObjectValue();
        } else {
            if (systemConfig.containsKey(STUDIO_CONFIG_GLOBAL_REPO_OVERRIDE_CONFIG)) {
                FileSystemResource fsr = new FileSystemResource(globalRepoOverrideConfigLocation.toFile());
                if (fsr.exists()) {
                    try {
                        YamlConfiguration globalRepoOverrideConfig = new YamlConfiguration();
                        try (InputStream in = fsr.getInputStream()) {
                            globalRepoOverrideConfig.setExpressionEngine(getExpressionEngine());
                            globalRepoOverrideConfig.read(in);

                            if (!globalRepoOverrideConfig.isEmpty()) {
                                logger.debug("Loaded additional configuration from location: {0} \n {1}",
                                        fsr.getPath(), globalRepoOverrideConfig);
                                CombinedConfiguration combinedConfig = new CombinedConfiguration(new OverrideCombiner());
                                combinedConfig.setExpressionEngine(getExpressionEngine());
                                combinedConfig.addConfiguration(globalRepoOverrideConfig);
                                combinedConfig.addConfiguration(systemConfig);

                                config = combinedConfig;
                            }

                        }
                    } catch (IOException | ConfigurationException e) {
                        logger.error("Failed to load studio configuration from: " + fsr.getPath(), e);
                    }
                }
            }
            configurationCache.put(new Element(cacheKey, config));
        }

        return config;
    }

    protected ExpressionEngine getExpressionEngine() {
        DefaultExpressionEngineSymbols symbols =
            new DefaultExpressionEngineSymbols.Builder(DefaultExpressionEngineSymbols.DEFAULT_SYMBOLS)
                // Use a slash as property delimiter
                .setPropertyDelimiter("/")
                // A Backslash is used for escaping property delimiters
                .setEscapedDelimiter("\\/").create();
        return new DefaultExpressionEngine(symbols);
    }

    protected HierarchicalConfiguration<ImmutableNode> getConfig() {
        return loadGlobalRepoConfig();
    }

    @Override
    public String getProperty(String key) {
        return getConfig().getString(key);
    }

    @Override
    public <T> T getProperty(String key, Class<T> clazz) {
        return getConfig().get(clazz, key);
    }

    @Override
    public <T> T getProperty(String key, Class<T> clazz, T defaultVal) {
        return getConfig().get(clazz, key, defaultVal);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] getArray(String key, Class<T> clazz) {
        return (T[]) getConfig().getArray(clazz, key);
    }

    @Override
    public HierarchicalConfiguration<ImmutableNode> getSubConfig(String key) {
        try {
            return getConfig().configurationAt(key);
        } catch (Exception e) {
            logger.debug("Failed to load configuration value for key " + key + ". Returning null.");
        }
        return null;
    }

    @Override
    public List<HierarchicalConfiguration<ImmutableNode>> getSubConfigs(String key) {
        try {
            return getConfig().configurationsAt(key);
        } catch (Exception e) {
            logger.error("Failed to load values for " + key);
            return null;
        }
    }

}
