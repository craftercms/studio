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

package org.craftercms.studio.impl.v2.upgrade.pipeline;

import java.util.List;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.upgrade.UpgradePipeline;
import org.craftercms.studio.api.v2.upgrade.UpgradeOperation;

/**
 * Default implementation for {@link UpgradePipeline}.
 * @author joseross
 */
public class DefaultUpgradePipelineImpl implements UpgradePipeline {

    private static final Logger logger = LoggerFactory.getLogger(DefaultUpgradePipelineImpl.class);

    /**
     * Name of the pipeline.
     */
    protected String name;

    /**
     * List of all upgrade operations to be executed.
     */
    protected List<UpgradeOperation> operations;

    public void setName(final String name) {
        this.name = name;
    }

    public void setOperations(final List<UpgradeOperation> operations) {
        this.operations = operations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final String site) throws UpgradeException {
        if(isEmpty()) {
            return;
        }
        logger.info("============================================================");
        logger.info("Starting execution of upgrade pipeline: {0}", name);
        for(UpgradeOperation operation : operations) {
            logger.info("------- Starting execution of operation {0} -------", operation.getClass().getSimpleName());
            operation.execute(site);
            logger.info("------- Execution of operation completed -------");
        }
        logger.info("Execution of pipeline {0} completed", name);
        logger.info("============================================================");
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public boolean isEmpty() {
        return operations == null || operations.isEmpty();
    }

}
