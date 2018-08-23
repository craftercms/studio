package org.craftercms.studio.api.v2.exception;

import org.craftercms.studio.api.v1.exception.ServiceException;

/**
 * Exception thrown when there's an error while reading a configuration.
 *
 * @author avasquez
 */
public class ConfigurationException extends ServiceException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Exception e) {
        super(message, e);
    }

}
