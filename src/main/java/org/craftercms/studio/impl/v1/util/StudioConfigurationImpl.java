/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
 *
 */

package org.craftercms.studio.impl.v1.util;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class StudioConfigurationImpl implements StudioConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(StudioConfigurationImpl.class);

    private Map<String, Object> properties = new HashMap<String, Object>();

    public void init() {
        loadConfig();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadConfig() {
        Map<String, Object> baseProperties = new HashMap<String, Object>();;
        Map<String, Object> overrideProperties = new HashMap<String, Object>();

        Resource resource = new ClassPathResource(configLocation);
        try (InputStream in = resource.getInputStream()) {
            Yaml yaml = new Yaml();
            baseProperties = yaml.loadAs(in, baseProperties.getClass());

            logger.debug("Loaded configuration from location: " + configLocation + "\n" + baseProperties.toString());
        } catch (IOException e) {
            logger.error("Failed to load studio configuration from: " + configLocation);
        }

        if (baseProperties.get(STUDIO_CONFIG_OVERRIDE_CONFIG) != null) {
            resource = new ClassPathResource(baseProperties.get(STUDIO_CONFIG_OVERRIDE_CONFIG).toString());

            try (InputStream in = resource.getInputStream()) {
                Yaml yaml = new Yaml();

                overrideProperties = yaml.loadAs(in, overrideProperties.getClass());
                if (overrideProperties != null) {
                    logger.debug("Loaded additional configuration from location: " + baseProperties.get
                            (STUDIO_CONFIG_OVERRIDE_CONFIG) + "\n" +
                            overrideProperties.toString());
                }
            } catch (IOException e) {
                logger.error("Failed to load studio configuration from: " + baseProperties.get(STUDIO_CONFIG_OVERRIDE_CONFIG));
            }
        }

        // Merge the base properties and additional properties
        for (Map.Entry<String, Object> entry: baseProperties.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }
        if (overrideProperties != null) {
            for (Map.Entry<String, Object> entry : overrideProperties.entrySet()) {
                properties.put(entry.getKey(), entry.getValue());
            }
        }

        logger.error("Cluster: " + getProperty(CLUSTERING_NODE_REGISTRATION, new HashMap<String, String>().getClass()));
    }

    @Override
    public String getProperty(String key) {
        return String.valueOf(getProperty(key, Object.class));
    }

    @Override
    public <T> T getProperty(String key, Class<T> clazz) {
        return clazz.cast(properties.get(key));
    }

    public String getConfigLocation() { return configLocation; }
    public void setConfigLocation(String configLocation) { this.configLocation = configLocation; }

    protected String configLocation;
}
