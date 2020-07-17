/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.upgrade.UpgradeOperation;
import org.craftercms.studio.impl.v2.upgrade.DefaultUpgradeManagerImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Implementation of {@link UpgradeOperation} that updates a single file using a XSLT template.
 *
 * <p>Supported YAML properties:</p>
 * <ul>
 *     <li><strong>path</strong>: (optional) the relative path to update in the repository</li>
 *     <li><strong>target</strong>: (optional) the relative path in the repository to use as output</li>
 * </ul>
 *
 * @author joseross
 * @since 3.1.0
 */
public class XsltFileUpgradeOperation extends AbstractXsltFileUpgradeOperation {

    public static final String CONFIG_KEY_PATH = "path";

    public static final String CONFIG_KEY_TARGET = "target";

    /**
     * Path of the file to use as input
     */
    protected String path;

    /**
     * Path of the file to use as output
     */
    protected String target;

    public void setPath(final String path) {
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doInit(final HierarchicalConfiguration<ImmutableNode> config) {
        super.doInit(config);
        if (StringUtils.isEmpty(path)) {
            String configPath = config.getString(CONFIG_KEY_PATH);
            if (StringUtils.isNotEmpty(configPath)) {
                path = configPath;
            } else {
                path = DefaultUpgradeManagerImpl.getCurrentFile();
            }
        }
        target = config.getString(CONFIG_KEY_TARGET, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final String site) throws UpgradeException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        executeTemplate(site, path, os);
        if (os.size() > 0) {
            String targetPath = isNotEmpty(target)? target : path;
            writeToRepo(site, targetPath, new ByteArrayInputStream(os.toByteArray()));
        }
    }

}
