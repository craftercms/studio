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

package org.craftercms.studio.impl.v2.upgrade.operations;

import java.nio.file.Path;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;

/**
 * @author joseross
 */
public class FindAndReplaceUpgradeOperation extends AbstractContentUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(FindAndReplaceUpgradeOperation.class);

    public static final String CONFIG_KEY_PATTERN = "pattern";
    public static final String CONFIG_KEY_REPLACEMENT = "replacement";

    protected String pattern;
    protected String replacement;

    @Override
    protected void doInit(final Configuration config) {
        super.doInit(config);
        pattern = config.getString(CONFIG_KEY_PATTERN);
        replacement = config.getString(CONFIG_KEY_REPLACEMENT);
    }

    @Override
    protected boolean shouldBeUpdated(final String site, final Path file) {
        return true;
    }

    @Override
    protected void updateFile(final String site, final Path path) throws UpgradeException {
        String content = readFile(path);
        String updated = null;
        if(StringUtils.isNotEmpty(content)) {
            updated = StringUtils.replaceAll(content, pattern, replacement);
        }

        if(StringUtils.isNotEmpty(updated) && !StringUtils.equals(content, updated)) {
            logger.info("Updating file {0}", path);
            writeFile(path, updated);
        }
    }

}
