package org.craftercms.studio.api.v1.exception;

/**
 * Exception thrown when an error occurs while transcoding a video.
 *
 * @author avasquez
 */
public class TranscoderException extends ServiceException {

    public TranscoderException(String message) {
        super(message);
    }

    public TranscoderException(String message, Exception e) {
        super(message, e);
    }

}
