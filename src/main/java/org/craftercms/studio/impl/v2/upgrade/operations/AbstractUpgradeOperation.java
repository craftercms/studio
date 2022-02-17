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

package org.craftercms.studio.impl.v2.upgrade.operations;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.upgrade.impl.UpgradeContext;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.beans.ConstructorProperties;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * Provides access to system components for all upgrade operations.
 *
 * <p>Supported YAML properties:</p>
 * <ul>
 *     <li><strong>currentVersion</strong>: (required) the version number that will be upgraded</li>
 *     <li><strong>nextVersion</strong> (required) the version number to use after the upgrade</li>
 *     <li><strong>commitDetails</strong>(optional) any additional details to include in the commits if there are
 *     repository changes</li>
 * </ul>
 *
 * @author joseross
 */
public abstract class AbstractUpgradeOperation extends
        org.craftercms.commons.upgrade.impl.operations.AbstractUpgradeOperation<String>
        implements ServletContextAware {

    private static final Logger logger = LoggerFactory.getLogger(AbstractUpgradeOperation.class);

    public static final String CONFIG_KEY_COMMIT_DETAILS = "commitDetails";

    protected List<String> changedFiles;

    protected List<String> deletedFiles;

    /**
     * Additional details for the commit message (optional)
     */
    protected String commitDetails;

    /**
     * The Studio configuration.
     */
    protected StudioConfiguration studioConfiguration;

    /**
     * The servlet context.
     */
    protected ServletContext servletContext;

    @ConstructorProperties({"studioConfiguration"})
    public AbstractUpgradeOperation(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setServletContext(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void init(final String sourceVersion, final String targetVersion,
                     final HierarchicalConfiguration config) throws ConfigurationException {
        this.commitDetails = config.getString(CONFIG_KEY_COMMIT_DETAILS);

       super.init(sourceVersion, targetVersion, config);
    }

    @Override
    protected void doExecute(UpgradeContext<String> context) throws Exception {
        if (!(context instanceof StudioUpgradeContext)) {
            throw new IllegalArgumentException("The provided upgrade context is not supported");
        }

        var studioContext = (StudioUpgradeContext) context;

        doExecute(studioContext);

        commitAllChanges(studioContext);
    }

    protected abstract void doExecute(StudioUpgradeContext context) throws Exception;

    protected String getCommitMessage() {
        String header = "[Upgrade Manager] Upgrade from v" + currentVersion + " to v" + nextVersion;
        if(StringUtils.isNotEmpty(commitDetails)) {
            return header + ":\n" + commitDetails;
        } else {
            return header;
        }
    }

    protected Resource loadResource(String path) {
        return applicationContext.getResource(path);
    }

    protected void trackChangedFiles(String... files) {
        if (changedFiles == null) {
            changedFiles = new LinkedList<>();
        }

        logger.debug("Tracking changed files: {0}", Arrays.toString(files));
        changedFiles.addAll(Arrays.asList(files));
    }

    protected void trackDeletedFiles(String... files) {
        if (deletedFiles == null) {
            deletedFiles = new LinkedList<>();
        }

        logger.debug("Tracking deleted files: {0}", Arrays.toString(files));
        deletedFiles.addAll(Arrays.asList(files));
    }

    protected void commitAllChanges(StudioUpgradeContext context) throws Exception {
        if (isEmpty(changedFiles) && isEmpty(deletedFiles)) {
            logger.debug("No changes pending to commit");
            return;
        }

        logger.debug("Committing tracked files");
        context.commitChanges(getCommitMessage(), changedFiles, deletedFiles);
    }

}
