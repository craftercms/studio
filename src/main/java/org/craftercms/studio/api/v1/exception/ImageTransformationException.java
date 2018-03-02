package org.craftercms.studio.api.v1.exception;

public class ImageTransformationException extends ServiceException {

    public ImageTransformationException(String message) {
        super(message);
    }

    public ImageTransformationException(String message, Exception e) {
        super(message, e);
    }

}
