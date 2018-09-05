package org.craftercms.studio.model;

public class AuthenticatedUser extends User {

    private AuthenticationType authenticationType;

    public AuthenticatedUser(User user) {
        setId(user.getId());
        setUsername(user.getUsername());
        setPassword(user.getPassword());
        setEmail(user.getEmail());
        setFirstName(user.getFirstName());
        setLastName(user.getLastName());
        setEnabled(user.isEnabled());
        setExternallyManaged(user.isExternallyManaged());
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

}
