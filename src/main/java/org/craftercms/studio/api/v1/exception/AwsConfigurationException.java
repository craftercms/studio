package org.craftercms.studio.api.v1.exception;

/**
 * {@link AwsException} used when there's an error in the configuration.
 *
 * @author avasquez
 */
public class AwsConfigurationException extends AwsException {

    public AwsConfigurationException(String message) {
        super(message);
    }

    public AwsConfigurationException(String message, Exception e) {
        super(message, e);
    }

}
