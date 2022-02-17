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
package org.craftercms.studio.impl.v2.upgrade.operations.file;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.removeStart;

/**
 * Extension of {@link RenameUpgradeOperation} that can rename multiple files based on path patterns
 *
 * <p>Supported YAML properties:</p>
 * <ul>
 *     <li><strong>basePath</strong>: (optional) relative path to search matches in the site</li>
 * </ul>
 *
 * @author joseross
 * @since 3.1.7
 */
public class TemplateRenameUpgradeOperation extends RenameUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(TemplateRenameUpgradeOperation.class);

    public static final String CONFIG_KEY_BASE_PATH = "basePath";

    /**
     * Relative path to search matches in the site
     */
    protected String basePath;

    public TemplateRenameUpgradeOperation(StudioConfiguration studioConfiguration, DataSource dataSource) {
        super(studioConfiguration);
    }

    @Override
    public void doInit(HierarchicalConfiguration config) {
        super.doInit(config);

        basePath = config.getString(CONFIG_KEY_BASE_PATH);
        oldPath = removeStart(oldPath, File.separator);
        newPath = removeStart(newPath, File.separator);
    }

    @Override
    public void doExecute(final StudioUpgradeContext context) throws UpgradeException {
        var site = context.getTarget();
        Path repo = context.getRepositoryPath();
        Path base;

        if (isNotEmpty(basePath)) {
            base = repo.resolve(removeStart(basePath, File.separator));
        } else {
            base = repo;
        }

        try {
            List<Path> matches = Files.walk(base)
                    .filter(path -> base.relativize(path).toString().matches(oldPath))
                    .map(base::relativize)
                    .collect(toList());

            logger.debug("Found {0} matches in site {1}", matches.size(), site);

            for(Path matchedPath : matches) {
                logger.debug("Processing file {0} in site {1}", matchedPath, site);
                Matcher matcher = Pattern.compile(oldPath).matcher(matchedPath.toString());
                if (matcher.matches()) { // we already know it matches but it needs to be called
                    String actualPath = newPath;
                    int total = matcher.groupCount();
                    for (int i = 1; i <= total; i++) {
                        actualPath = actualPath.replace("$" + i, matcher.group(i));
                    }
                    logger.debug("Renaming file {0} to {1} in site {2}", matchedPath, actualPath, site);
                    renamePath(base.resolve(matchedPath), base.resolve(actualPath));
                    trackChangedFiles(matchedPath.toString(), actualPath);
                }
            }
        } catch (IOException e) {
            throw new UpgradeException("Error renaming files " + oldPath + " to " + newPath + " in site " + site, e);
        }

    }

}
