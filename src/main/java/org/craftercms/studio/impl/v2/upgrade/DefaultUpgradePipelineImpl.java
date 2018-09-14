package org.craftercms.studio.impl.v2.upgrade;

import java.util.List;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.upgrade.UpgradeContext;
import org.craftercms.studio.api.v2.upgrade.UpgradePipeline;
import org.craftercms.studio.api.v2.upgrade.Upgrader;

public class DefaultUpgradePipelineImpl implements UpgradePipeline {

    private static final Logger logger = LoggerFactory.getLogger(DefaultUpgradePipelineImpl.class);

    protected List<Upgrader> upgraders;

    public DefaultUpgradePipelineImpl(final List<Upgrader> upgraders) {
        this.upgraders = upgraders;
    }

    @Override
    public void execute(final UpgradeContext context) throws UpgradeException {
        logger.info("Starting execution of upgrade pipeline");
        for(Upgrader upgrader : upgraders) {
            logger.info("Starting execution of upgrader {0}", upgrader.getClass().getSimpleName());
            upgrader.execute(context);
            logger.info("Execution of upgrader completed");
        }
        logger.info("Execution of pipeline completed");
    }

}
