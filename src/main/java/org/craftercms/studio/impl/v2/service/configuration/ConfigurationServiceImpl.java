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
package org.craftercms.studio.impl.v2.service.configuration;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.exception.ConfigurationException;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.*;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.*;

public class ConfigurationServiceImpl implements ConfigurationService {

    private ContentService contentService;
    private StudioConfiguration studioConfiguration;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, List<String>> geRoleMappings(String siteId) throws ConfigurationException {
        // TODO: Refactor this to use Apache's Commons Configuration
        Map<String, List<String>> roleMappings = new HashMap<>();
        String roleMappingsConfigPath = getSiteRoleMappingsConfigPath(siteId);
        Document document;

        try {
            document = contentService.getContentAsDocument(siteId, roleMappingsConfigPath);
            Element root = document.getRootElement();
            if (root.getName().equals(DOCUMENT_ROLE_MAPPINGS)) {
                List<Node> groupNodes = root.selectNodes(DOCUMENT_ELM_GROUPS_NODE);
                for (Node node : groupNodes) {
                    String name = node.valueOf(DOCUMENT_ATTR_PERMISSIONS_NAME);
                    if (StringUtils.isNotEmpty(name)) {
                        List<Node> roleNodes = node.selectNodes(DOCUMENT_ELM_PERMISSION_ROLE);
                        List<String> roles = new ArrayList<>();

                        for (Node roleNode : roleNodes) {
                            roles.add(roleNode.getText());
                        }

                        roleMappings.put(name, roles);
                    }
                }
            }
        } catch (DocumentException e) {
            throw new ConfigurationException("Error while reading role mappings file for site " + siteId + " @ " +
                                             roleMappingsConfigPath);
        }

        return roleMappings;
    }


    private String getSiteRoleMappingsConfigPath(String siteId) {
        return UrlUtils.concat(getSiteConfigPath(siteId), getSiteRoleMappingsConfigFileName());
    }

    private String getSiteConfigPath(String siteId) {
        String siteConfigPath = StringUtils.EMPTY;
        if (!StringUtils.isEmpty(studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE))) {
            siteConfigPath = studioConfiguration.getProperty(CONFIGURATION_SITE_MUTLI_ENVIRONMENT_CONFIG_BASE_PATH)
                    .replaceAll(PATTERN_ENVIRONMENT, studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE));
            if (!contentService.contentExists(siteId,siteConfigPath + FILE_SEPARATOR + getSiteRoleMappingsConfigFileName())) {
                siteConfigPath = studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH).replaceFirst(PATTERN_SITE, siteId);
            }
        } else {
            studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH).replaceFirst(PATTERN_SITE, siteId);
        }
        return siteConfigPath;
    }

    private String getSiteRoleMappingsConfigFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME);
    }

    @Required
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    @Required
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

}
