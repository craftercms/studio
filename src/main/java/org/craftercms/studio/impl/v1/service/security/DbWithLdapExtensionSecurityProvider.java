/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2017 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v1.service.security;

import org.craftercms.studio.api.v1.dal.User;
import org.craftercms.studio.api.v1.exception.security.UserAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.core.AuthenticatedLdapEntryContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapEntryIdentification;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

import java.util.HashMap;
import java.util.Map;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.*;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class DbWithLdapExtensionSecurityProvider extends DbSecurityProvider {
    private final static Logger loggerr = LoggerFactory.getLogger(DbWithLdapExtensionSecurityProvider.class);

    @Override
    public String authenticate(String username, String password) {

        // Mapper for user data if user is successfully authenticated
        AuthenticatedLdapEntryContextMapper<User> mapper = new AuthenticatedLdapEntryContextMapper<User>() {
            @Override
            public User mapWithContext(DirContext dirContext, LdapEntryIdentification ldapEntryIdentification) {
                User user = new User();
                try {
                    // User entry - extract attributes
                    DirContextOperations dirContextOperations = (DirContextOperations)dirContext.lookup(ldapEntryIdentification.getRelativeName());
                    Attributes attributes = dirContextOperations.getAttributes();
                    user.setActive(1);
                    user.setEmail(attributes.get(studioConfiguration.getProperty(SECURITY_LDAP_USER_ATTRIBUTE_EMAIL)).get().toString());
                    user.setFirstname(attributes.get(studioConfiguration.getProperty(SECURITY_LDAP_USER_ATTRIBUTE_FIRST_NAME)).get().toString());
                    user.setLastname(attributes.get(studioConfiguration.getProperty(SECURITY_LDAP_USER_ATTRIBUTE_LAST_NAME)).get().toString());
                    user.setUsername(username);
                } catch (NamingException e) {
                    loggerr.error("Error getting details from LDAP for username " + username, e);
                    user = null;
                }
                return user;
            }
        };

        // Create ldap query to authenticate user
        LdapQuery ldapQuery = query().where(studioConfiguration.getProperty(SECURITY_LDAP_USER_ATTRIBUTE_USERNAME)).is(username);
        User user = null;
        try {
            user = ldapTemplate.authenticate(ldapQuery, password, mapper);
        } catch (EmptyResultDataAccessException e) {
            loggerr.error("User " + username + " not found with external security provider. Trying to authenticate against studio database", e);
            // When user not found try to authenticate against studio database
            return super.authenticate(username, password);
        } catch (AuthenticationException e) {
            loggerr.error("Authentication failed: ", e);
        }
        if (user != null) {
            // When user authenticated against LDAP, upsert user data into studio database
            boolean toRet = true;
            if (super.userExists(username)) {
                try {
                    updateUserInternal(user.getUsername(), user.getFirstname(), user.getLastname(), user.getEmail());
                } catch (UserNotFoundException e) {
                    loggerr.error("Error updating user " + username + " with data from external authentication provider", e);
                }
            } else {
                try {
                    createUser(user.getUsername(), password, user.getFirstname(), user.getLastname(), user.getEmail(), true);
                } catch (UserAlreadyExistsException e) {
                    loggerr.error("Error adding user " + username + " from external authentication provider", e);
                }
            }
            String token = createToken(user);
            storeSessionTicket(token);
            storeSessionUsername(username);
            return token;
        }
        return null;
    }

    private boolean updateUserInternal(String username, String firstName, String lastName, String email) throws UserNotFoundException {
        if (!userExists(username)) {
            throw new UserNotFoundException();
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("username", username);
            params.put("firstname", firstName);
            params.put("lastname", lastName);
            params.put("email", email);
            params.put("externallyManaged", 1);
            securityMapper.updateUser(params);
            return true;
        }
    }

    public LdapTemplate getLdapTemplate() { return ldapTemplate; }
    public void setLdapTemplate(LdapTemplate ldapTemplate) { this.ldapTemplate = ldapTemplate; }

    protected LdapTemplate ldapTemplate;
}
