package org.craftercms.studio.api.v1.exception;

public class AssetProcessingException extends ServiceException {

    public AssetProcessingException(Throwable t) {
        super(t);
    }

    public AssetProcessingException(String message) {
        super(message);
    }

    public AssetProcessingException(String message, Exception e) {
        super(message, e);
    }

}
