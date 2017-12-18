package org.craftercms.studio.api.v1.exception;

public class BoxException extends ServiceException {

    public BoxException(final String message) {
        super(message);
    }

    public BoxException(final String message, final Exception e) {
        super(message, e);
    }

}
