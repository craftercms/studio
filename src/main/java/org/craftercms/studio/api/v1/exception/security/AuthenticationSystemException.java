package org.craftercms.studio.api.v1.exception.security;

/**
 * Created by alfonso on 5/2/17.
 */
public class AuthenticationSystemException extends AuthenticationException {

    public AuthenticationSystemException(String message) {
        super(message);
    }

    public AuthenticationSystemException(String message, Throwable cause) {
        super(message, cause);
    }

}
