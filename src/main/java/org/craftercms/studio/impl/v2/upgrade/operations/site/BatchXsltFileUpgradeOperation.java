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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;

import javax.sql.DataSource;

/**
 * Implementation of {@link org.craftercms.commons.upgrade.UpgradeOperation} that updates multiple files using a XSLT template.
 *
 * <p>Supported YAML properties:</p>
 * <ul>
 *     <li><strong>regex</strong>: (required) the regex used to find files to update</li>
 * </ul>
 *
 * @author joseross
 * @since 3.1.1
 */
public class BatchXsltFileUpgradeOperation extends AbstractXsltFileUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(BatchXsltFileUpgradeOperation.class);

    public static final String CONFIG_KEY_REGEX = "regex";

    protected String regex;

    public BatchXsltFileUpgradeOperation(StudioConfiguration studioConfiguration, DataSource dataSource) {
        super(studioConfiguration);
    }

    @Override
    protected void doInit(final HierarchicalConfiguration config) {
        super.doInit(config);
        regex = config.getString(CONFIG_KEY_REGEX);
    }

    @Override
    public void doExecute(final StudioUpgradeContext context) throws UpgradeException {
        var site = context.getTarget();
        logger.debug("Looking site {0} for files that match: {1}", site, regex);
        Path repository = context.getRepositoryPath();
        try (Stream<Path> paths = Files.find(repository, Integer.MAX_VALUE,
            (path, attrs) -> repository.relativize(path).toString().matches(regex) )) {
            paths.forEach(path -> {
                logger.debug("Executing XSLT template in site {0} for file {1}", site, path);
                try {
                    Path temp = Files.createTempFile("upgrade-manager", "xslt");
                    try {
                        OutputStream os = Files.newOutputStream(temp);
                        executeTemplate(context, repository.relativize(path).toString(), os);
                        os.close();
                        if (Files.size(temp) > 0) {
                            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } finally {
                        Files.deleteIfExists(temp);
                    }
                } catch (Exception e) {
                    logger.error("Error upgrading file {0} in site {1}", e, site, path);
                }
            });
        } catch (IOException e) {
            throw new UpgradeException("Error searching for files in site " + site, e);
        }
    }

}
