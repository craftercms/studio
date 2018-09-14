package org.craftercms.studio.api.v2.upgrade;

import org.apache.commons.configuration2.Configuration;
import org.craftercms.studio.api.v2.exception.UpgradeException;

public interface Upgrader {

    void init(Configuration config);

    void execute(UpgradeContext context) throws UpgradeException;

}
