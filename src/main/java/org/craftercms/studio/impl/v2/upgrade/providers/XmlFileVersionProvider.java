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

package org.craftercms.studio.impl.v2.upgrade.providers;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.exception.UpgradeNotSupportedException;
import org.craftercms.studio.api.v2.upgrade.VersionProvider;
import org.springframework.beans.factory.annotation.Required;
import org.w3c.dom.Document;

import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.VERSION_1_0;

/**
 * Implementation of {@inheritDoc} for XML files.
 * @author joseross
 */
public class XmlFileVersionProvider implements VersionProvider {

    /**
     * Name of the site.
     */
    protected String site;

    /**
     * Path of the file containing the version.
     */
    protected String path;

    /**
     * XPath expression to extract the version.
     */
    protected String xpath;

    protected ContentRepository contentRepository;

    public XmlFileVersionProvider(final String site, final String path) {
        this.site = site;
        this.path = path;
    }

    @Required
    public void setContentRepository(final ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void setSite(final String site) {
        this.site = site;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    @Required
    public void setXpath(final String xpath) {
        this.xpath = xpath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentVersion() throws UpgradeException {
        String currentVersion = VERSION_1_0;
        if(!contentRepository.contentExists(site, "/config/studio")) {
            throw new UpgradeNotSupportedException("Site '"+ site +"' from 2.5.x can't be automatically upgraded");
        } else if(contentRepository.contentExists(site, path)) {
            try(InputStream is = contentRepository.getContent(site, path)) {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                Document xmlDocument = builder.parse(is);
                XPath xPath = XPathFactory.newInstance().newXPath();
                String fileVersion = (String) xPath.compile(xpath).evaluate(xmlDocument, XPathConstants.STRING);
                if(StringUtils.isNotEmpty(fileVersion)) {
                    currentVersion = fileVersion;
                }
            } catch (Exception e) {
                throw new UpgradeException("Error reading version from file " + path + " in site " + site, e);
            }
        }
        return currentVersion;
    }

}
