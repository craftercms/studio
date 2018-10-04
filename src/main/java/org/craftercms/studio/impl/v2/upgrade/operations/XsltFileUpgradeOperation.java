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

package org.craftercms.studio.impl.v2.upgrade.operations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.configuration2.Configuration;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.upgrade.UpgradeOperation;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.PARAM_KEY_SITE;
import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.PARAM_KEY_VERSION;

/**
 * Implementation if {@link UpgradeOperation} that updates a file using a XSLT template.
 * @author joseross
 */
public class XsltFileUpgradeOperation extends AbstractUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(XsltFileUpgradeOperation.class);

    private static final String SAXON_CLASS = "net.sf.saxon.TransformerFactoryImpl";

    public static final String CONFIG_KEY_PATH = "path";
    public static final String CONFIG_KEY_TEMPLATE = "template";

    /**
     * Path of the file to update.
     */
    protected String path;

    /**
     * Template file to be applied.
     */
    protected Resource template;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final String sourceVersion, final String targetVersion, final Configuration config) {
        super.init(sourceVersion, targetVersion, config);
        path = config.getString(CONFIG_KEY_PATH);
        template = new ClassPathResource(config.getString(CONFIG_KEY_TEMPLATE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final String site) throws UpgradeException {
        try(InputStream templateIs = template.getInputStream()) {
            // Saxon is used to support XSLT 2.0
            Transformer transformer =
                TransformerFactory.newInstance(SAXON_CLASS, null)
                    .newTransformer(new StreamSource(templateIs));
            logger.info("Applying XSLT template {0} to file {1} for site {2}", template, path, site);
            if(contentRepository.contentExists(site, path)) {
                try(InputStream sourceIs = contentRepository.getContent(site, path)) {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    transformer.setParameter(PARAM_KEY_SITE, site);
                    transformer.setParameter(PARAM_KEY_VERSION, nextVersion);
                    transformer.transform(new StreamSource(sourceIs), new StreamResult(os));
                    writeToRepo(site, path, new ByteArrayInputStream(os.toByteArray()),
                        "Upgrade from v" + currentVersion + " to v" + nextVersion);
                }
            } else {
                logger.warn("Source file {0} doesn't exist in site {1}", path, site);
            }
        } catch (Exception e) {
            throw new UpgradeException("Error processing file", e);
        }
    }

}
