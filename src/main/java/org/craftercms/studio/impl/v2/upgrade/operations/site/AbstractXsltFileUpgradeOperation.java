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

package org.craftercms.studio.impl.v2.upgrade.operations.site;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.craftercms.studio.impl.v2.upgrade.operations.AbstractUpgradeOperation;
import org.craftercms.studio.impl.v2.utils.XsltUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;

import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.PARAM_KEY_SITE;
import static org.craftercms.studio.api.v2.upgrade.UpgradeConstants.PARAM_KEY_VERSION;

/**
 * Base implementation of {@link org.craftercms.commons.upgrade.UpgradeOperation} for all operations related to a XSLT template.
 *
 * <p>Supported YAML properties:</p>
 * <ul>
 *     <li><strong>template</strong>: (required) the path to the XSLT template to apply</li>
 * </ul>
 *
 * @author joseross
 * @since 3.1.1
 */
public abstract class AbstractXsltFileUpgradeOperation extends AbstractUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(AbstractXsltFileUpgradeOperation.class);

    public static final String CONFIG_KEY_TEMPLATE = "template";

    /**
     * Template file to be applied.
     */
    protected Resource template;

    public AbstractXsltFileUpgradeOperation(StudioConfiguration studioConfiguration) {
        super(studioConfiguration);
    }

    public void setTemplate(final Resource template) {
        this.template = template;
    }

    @Override
    protected void doInit(final HierarchicalConfiguration config) {
        if(template == null) {
            template = new ClassPathResource(config.getString(CONFIG_KEY_TEMPLATE));
        }
    }

    protected void executeTemplate(StudioUpgradeContext context, String path, OutputStream os) throws UpgradeException {
        var site = context.getTarget();
        var file = context.getFile(path);
        if(Files.exists(file)) {
            try(InputStream templateIs = template.getInputStream();
                InputStream sourceIs = Files.newInputStream(file)) {
                logger.info("Applying XSLT template {0} to file {1} for site {2}", template, path, site);
                Map<String, Object> params = Map.of(PARAM_KEY_SITE, site, PARAM_KEY_VERSION, nextVersion);
                XsltUtils.executeTemplate(templateIs, params, getURIResolver(context), sourceIs, os);
                trackChangedFiles(path);
            } catch (Exception e) {
                throw new UpgradeException("Error processing file", e);
            }
        } else {
            logger.warn("Source file {0} does not exist in site {1}", path, site);
        }
    }

    protected URIResolver getURIResolver(StudioUpgradeContext context) {
        return (href, base) -> {
            try {
                return new StreamSource(context.getRepositoryPath().resolve(href).toFile());
            } catch (Exception e) {
                logger.info("Error creating resolver for referencing documents inside xslt forms", e);
                return  null;
            }

        };
    }
}
