package org.craftercms.studio.impl.v2.service.security;

import org.craftercms.studio.model.AuthenticationType;

/**
 * Simple class that represents a Studio authentication.
 *
 * @author avasquez
 */
public class Authentication {

    private String username;
    private String token;
    private AuthenticationType authenticationType;

    public Authentication(String username, String token, AuthenticationType authenticationType) {
        this.username = username;
        this.token = token;
        this.authenticationType = authenticationType;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

}
