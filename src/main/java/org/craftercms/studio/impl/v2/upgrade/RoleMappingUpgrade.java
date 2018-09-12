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
 *
 */

package org.craftercms.studio.impl.v2.upgrade;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.constant.StudioXmlConstants;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.impl.v2.dal.RepositoryUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME;
import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.*;

/**
 * Implementation of {@link org.craftercms.studio.api.v2.upgrade.RepositoryUpgrade} that upgrades the
 * role mappings configuration for a given site.
 * @author joseross
 */
public class RoleMappingUpgrade extends AbstractRepositoryUpgrade {

    private static final Logger logger = LoggerFactory.getLogger(RoleMappingUpgrade.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldUpgradeRepo(final String site, final String currentVersion) {
        return currentVersion.startsWith(DB_VERSION_3_0_X) ||
            currentVersion.equals(DB_VERSION_3_1_0) ||
            currentVersion.equals(DB_VERSION_3_1_0_1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void upgradeRepo(final String site, final String currentVersion) {
        logger.info("Upgrading role mappings for site {0}", site);
        String siteConfigPath = studioConfiguration.getProperty(CONFIGURATION_SITE_CONFIG_BASE_PATH);
        siteConfigPath = siteConfigPath.replaceFirst(StudioConstants.PATTERN_SITE, site);
        String filename = studioConfiguration.getProperty(CONFIGURATION_SITE_ROLE_MAPPINGS_FILE_NAME);
        String siteRoleMappingsConfigFullPath = siteConfigPath + FILE_SEPARATOR + filename;

        Document document = null;
        try (InputStream is = contentRepository.getContent(site, siteRoleMappingsConfigFullPath)) {
            SAXReader saxReader = new SAXReader();
            try {
                saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
                saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            } catch (SAXException ex){
                logger.error("Unable to turn off external entity loading, This could be a security risk.", ex);
            }
            document = saxReader.read(is);
            if (document != null) {
                Element root = document.getRootElement();
                if (root.getName().equals(StudioXmlConstants.DOCUMENT_ROLE_MAPPINGS)) {
                    List<Element> groupNodes = root.selectNodes(StudioXmlConstants.DOCUMENT_ELM_GROUPS_NODE);
                    for (Element node : groupNodes) {
                        String name = node.valueOf(StudioXmlConstants.DOCUMENT_ATTR_PERMISSIONS_NAME);
                        if (!StringUtils.isEmpty(name)) {
                            Attribute attribute = node.attribute(StudioXmlConstants.DOCUMENT_ATTR_NAME);
                            if (currentVersion.startsWith(DB_VERSION_3_0_X)) {
                                attribute.setValue(StringUtils.lowerCase(site + "_" + name));
                            } else {
                                attribute.setValue(StringUtils.lowerCase(name));
                            }
                        }
                    }
                }

                RepositoryUtils.writeToRepo(studioConfiguration, site, siteRoleMappingsConfigFullPath,
                    IOUtils.toInputStream(document.asXML()), "Permission Mapping Upgrade");

            } else {
                logger.error("Permission mapping not found for " + site + ":" + filename);
            }
        } catch (ContentNotFoundException | IOException e) {
            logger.debug("Content not found for path {0}", e, siteRoleMappingsConfigFullPath);
        } catch (DocumentException e) {
            logger.error("Error while reading permission mapping for " + site + ":" + filename);
        }
    }

}
