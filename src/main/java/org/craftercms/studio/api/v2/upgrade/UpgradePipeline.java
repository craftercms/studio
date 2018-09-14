package org.craftercms.studio.api.v2.upgrade;

import org.craftercms.studio.api.v2.exception.UpgradeException;

public interface UpgradePipeline {

    void execute(UpgradeContext context) throws UpgradeException;

}
