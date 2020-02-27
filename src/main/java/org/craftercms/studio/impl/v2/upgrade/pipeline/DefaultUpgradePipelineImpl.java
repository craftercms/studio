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

package org.craftercms.studio.impl.v2.upgrade.pipeline;

import java.util.List;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.upgrade.UpgradePipeline;
import org.craftercms.studio.api.v2.upgrade.UpgradeOperation;
import org.springframework.util.StopWatch;

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
     * Indicates if the pipeline should continue executing after an operation fails
     */
    protected boolean continueOnError = false;

    /**
     * List of all upgrade operations to be executed.
     */
    protected List<UpgradeOperation> operations;

    public void setName(final String name) {
        this.name = name;
    }

    public void setContinueOnError(final boolean continueOnError) {
        this.continueOnError = continueOnError;
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
        StopWatch watch = new StopWatch(name + " pipeline");
        logger.info("============================================================");
        logger.info("Starting execution of upgrade pipeline: {0}", name);
        for(UpgradeOperation operation : operations) {
            String operationName = operation.getClass().getSimpleName();
            logger.info("------- Starting execution of operation {0} -------", operationName);
            watch.start(operationName);
            try {
                operation.execute(site);
            } catch (UpgradeException e) {
                if (continueOnError) {
                    logger.error("Execution of operation {0} failed", e, operationName);
                } else {
                    throw e;
                }
            } finally {
                watch.stop();
                logger.info("------- Execution of operation {0} completed -------", operationName);
            }
        }
        logger.info("Execution of pipeline {0} completed", name);
        logger.info("============================================================");

        if(logger.getLevel().equals(Logger.LEVEL_DEBUG)) {
            logger.debug("Pipeline Duration:\n" + watch.prettyPrint());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return operations == null || operations.isEmpty();
    }

}
