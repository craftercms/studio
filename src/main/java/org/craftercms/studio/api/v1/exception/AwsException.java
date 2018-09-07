package org.craftercms.studio.api.v1.exception;

/**
 * Exception thrown when an error occurs while transcoding a video.
 *
 * @author avasquez
 */
public class AwsException extends ServiceLayerException {

    public AwsException(String message) {
        super(message);
    }

    public AwsException(String message, Exception e) {
        super(message, e);
    }

}
