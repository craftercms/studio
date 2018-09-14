package org.craftercms.studio.api.v2.exception;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;

public class UpgradeException extends ServiceLayerException {

    public UpgradeException(final String message, final Exception e) {
        super(message, e);
    }

}
