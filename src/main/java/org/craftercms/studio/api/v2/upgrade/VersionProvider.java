package org.craftercms.studio.api.v2.upgrade;

import org.craftercms.studio.api.v2.exception.UpgradeException;

public interface VersionProvider {

    String getCurrentVersion() throws UpgradeException;

}
