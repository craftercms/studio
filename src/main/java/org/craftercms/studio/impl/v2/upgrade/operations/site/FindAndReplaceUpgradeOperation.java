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

import java.nio.file.Path;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;

import javax.sql.DataSource;

/**
 * Implementation of {@link org.craftercms.commons.upgrade.UpgradeOperation} that replaces text in the content repository.
 *
 * <p>Supported YAML properties:</p>
 * <ul>
 *     <li><strong>pattern</strong>: (required) the pattern to search in the files, can be a regular expression</li>
 *     <li><strong>replacement</strong>: (required) the expression to replace in the files, can use matched groups
 *     from the regular expression in the pattern</li>
 * </ul>
 *
 * @author joseross
 */
public class FindAndReplaceUpgradeOperation extends AbstractContentUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(FindAndReplaceUpgradeOperation.class);

    public static final String CONFIG_KEY_PATTERN = "pattern";
    public static final String CONFIG_KEY_REPLACEMENT = "replacement";

    /**
     * The pattern to search in the files
     */
    protected String pattern;

    /**
     * The expression to replace in the files
     */
    protected String replacement;

    public FindAndReplaceUpgradeOperation(StudioConfiguration studioConfiguration, DataSource dataSource) {
        super(studioConfiguration);
    }

    @Override
    protected void doInit(final HierarchicalConfiguration config) {
        super.doInit(config);
        pattern = config.getString(CONFIG_KEY_PATTERN);
        replacement = config.getString(CONFIG_KEY_REPLACEMENT);
    }

    @Override
    protected boolean shouldBeUpdated(StudioUpgradeContext context, Path file) {
        return true;
    }

    @Override
    protected void updateFile(StudioUpgradeContext context, Path path) throws UpgradeException {
        String content = readFile(path);
        String updated = null;
        if(StringUtils.isNotEmpty(content)) {
            updated = RegExUtils.replaceAll(content, pattern, replacement);
        }

        if(StringUtils.isNotEmpty(updated) && !StringUtils.equals(content, updated)) {
            logger.info("Updating file {0}", path);
            writeFile(path, updated);
        }
    }

}
