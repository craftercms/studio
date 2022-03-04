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

package org.craftercms.studio.impl.v2.upgrade.providers;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.commons.upgrade.exception.UpgradeNotSupportedException;
import org.craftercms.commons.upgrade.impl.UpgradeContext;
import org.craftercms.commons.upgrade.impl.providers.AbstractVersionProvider;
import org.craftercms.core.util.XmlUtils;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.beans.ConstructorProperties;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.util.List;


/**
 * Implementation of {@link org.craftercms.commons.upgrade.VersionProvider} for XML files.
 *
 * @author joseross
 */
public class XmlFileVersionProvider extends AbstractVersionProvider<String> {

    /**
     * Path of the file containing the version.
     */
    protected String path;

    /**
     * XPath expression to extract the version.
     */
    protected String xpath;

    /**
     * Version returned if none is found.
     */
    protected String defaultVersion;

    /**
     * Indicates if the skip flag should be returned
     */
    protected boolean skipIfMissing = true;

    protected ContentRepository contentRepository;

    @ConstructorProperties({"path", "xpath", "defaultVersion", "contentRepository"})
    public XmlFileVersionProvider(String path, String xpath, String defaultVersion,
                                  ContentRepository contentRepository) {
        this.path = path;
        this.xpath = xpath;
        this.defaultVersion = defaultVersion;
        this.contentRepository = contentRepository;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public void setSkipIfMissing(final boolean skipIfMissing) {
        this.skipIfMissing = skipIfMissing;
    }

    protected String getFilePath(StudioUpgradeContext context) {
        return context.isConfigPresent()? context.getCurrentConfigPath() : path;
    }

    @Override
    protected String doGetVersion(UpgradeContext<String> context) throws Exception {
        String site = context.getTarget();
        String filePath = getFilePath((StudioUpgradeContext) context);
        String currentVersion = defaultVersion;
        if(!contentRepository.contentExists(site, "/config/studio")) {
            String firstCommit = contentRepository.getRepoFirstCommitId(site);
            if (StringUtils.isNotEmpty(firstCommit)) {
                throw new UpgradeNotSupportedException("Site '" + site + "' from 2.5.x can't be automatically upgraded");
            }
        } else if(!contentRepository.contentExists(site, filePath)) {
            logger.debug("Missing file {} in site {}", filePath, site);
            if (skipIfMissing) {
                return SKIP;
            } else {
                return defaultVersion;
            }
        } else {
            try(InputStream is = contentRepository.getContent(site, filePath)) {
                SAXReader reader = new SAXReader();
                Document document = reader.read(is);

                String fileVersion = XmlUtils.selectSingleNodeValue(document, xpath);
                if(StringUtils.isNotEmpty(fileVersion)) {
                    currentVersion = fileVersion;
                }
            } catch (Exception e) {
                throw new UpgradeException("Error reading version from file " + filePath + " in site " + site, e);
            }
        }
        return currentVersion;
    }

    @Override
    protected void doSetVersion(UpgradeContext<String> context, String newVersion) throws Exception {
        var studioContext = (StudioUpgradeContext) context;
        var actualPath = getFilePath(studioContext);
        var file = studioContext.getFile(actualPath);

        Document document;
        try(InputStream is = Files.newInputStream(file)) {
            SAXReader reader = new SAXReader();
            document = reader.read(is);
        }

        if (document != null) {
            Node versionNode = document.selectSingleNode(xpath);
            if (versionNode == null) {
                versionNode = DocumentHelper.makeElement(document, xpath);
            }
            versionNode.setText(newVersion);

            try(Writer writer = Files.newBufferedWriter(file)) {
                XMLWriter xmlWriter = new XMLWriter(writer, OutputFormat.createPrettyPrint());
                xmlWriter.write(document);
            }

            studioContext.commitChanges("[Upgrade Manager] Update version", List.of(actualPath), null);
        }
    }

}
