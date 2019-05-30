/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.api.v2.service.config;

import org.craftercms.studio.api.v2.exception.ConfigurationException;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Service that helps access different Studio configuration.
 *
 * @author avasquez
 */
public interface ConfigurationService {

    Map<String, List<String>> geRoleMappings(String siteId) throws ConfigurationException;

    String loadConfiguration(String siteId, String configurationFile, String environment);

    Document loadConfigurationDocument(String siteId, String configurationFile, String environment) throws DocumentException, IOException;

    void writeConfiguration(String siteId, String configurationFile, String environment, InputStream content);
}
