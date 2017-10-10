package org.craftercms.studio.api.v1.exception;

/**
 * {@link TranscoderException} used when there's an error in the configuration.
 *
 * @author avasquez
 */
public class TranscoderConfigurationException extends TranscoderException {

    public TranscoderConfigurationException(String message) {
        super(message);
    }

    public TranscoderConfigurationException(String message, Exception e) {
        super(message, e);
    }

}
