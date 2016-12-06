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

    private Map<String, String> properties = new HashMap<String, String>();

    public void init() {
        loadConfiguration();
    }

    @Override
    public void loadConfiguration() {
        Resource resource = new ClassPathResource(configLocation);
        try (InputStream in = resource.getInputStream()) {
            Yaml yaml = new Yaml();
            properties = yaml.loadAs(in, properties.getClass());

            logger.debug("Loaded configuration:\n" + properties.toString());
        } catch (IOException e) {
            logger.error("Failed to load studio configuration from: " + configLocation);
        }
    }

    @Override
    public String getProperty(String key) {
        return properties.get(key);
    }

    public String getConfigLocation() { return configLocation; }
    public void setConfigLocation(String configLocation) { this.configLocation = configLocation; }

    protected String configLocation;
}
