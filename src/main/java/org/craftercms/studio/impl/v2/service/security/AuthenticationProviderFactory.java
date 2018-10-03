/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */

package org.craftercms.studio.impl.v2.service.security;

import org.craftercms.studio.api.v2.service.security.AuthenticationProvider;

import java.util.Map;

public class AuthenticationProviderFactory {

    public static AuthenticationProvider getAuthenticationProvider(Map<String, Object> providerConfig) {
        AuthenticationProvider provider;
        switch (providerConfig.get("provider").toString()) {
            case "DB":
                provider = createDbAuthenticationProvider(providerConfig);
                break;
            case "LDAP":
                provider = createLdapAuthenticationProvider(providerConfig);
                break;
            case "HEADERS":
                provider = createHeadersAuthenticationProvider(providerConfig);
                break;
            default:
                provider = null;
        }
        return provider;
    }

    private static DbAuthenticationProvider createDbAuthenticationProvider(Map<String, Object> providerConfig) {
        DbAuthenticationProvider provider = new DbAuthenticationProvider();
        boolean enabled = Boolean.parseBoolean(providerConfig.get("enabled").toString());
        provider.setEnabled(enabled);
        return provider;
    }

    private static LdapAuthenticationProvider createLdapAuthenticationProvider(Map<String, Object> providerConfig) {
        LdapAuthenticationProvider provider = new LdapAuthenticationProvider();
        boolean enabled = Boolean.parseBoolean(providerConfig.get("enabled").toString());
        provider.setEnabled(enabled);
        provider.setLdapUrl(providerConfig.get("ldapUrl").toString());
        provider.setLdapUsername(providerConfig.get("ldapUsername").toString());
        provider.setLdapPassword(providerConfig.get("ldapPassword").toString());
        provider.setLdapBaseContext(providerConfig.get("ldapBaseContext").toString());
        provider.setUsernameLdapAttribute(providerConfig.get("usernameLdapAttribute").toString());
        provider.setFirstNameLdapAttribute(providerConfig.get("firstNameLdapAttribute").toString());
        provider.setLastNameLdapAttribute(providerConfig.get("lastNameLdapAttribute").toString());
        provider.setEmailLdapAttribute(providerConfig.get("emailLdapAttribute").toString());
        provider.setGroupNameLdapAttribute(providerConfig.get("groupNameLdapAttribute").toString());
        provider.setGroupNameLdapAttributeRegex(providerConfig.get("groupNameLdapAttributeRegex").toString());
        provider.setGroupNameLdapAttributeMatchIndex(
                Integer.parseInt(providerConfig.get("groupNameLdapAttributeMatchIndex").toString()));
        return provider;
    }

    private static HeadersAuthenticationProvider createHeadersAuthenticationProvider(Map<String, Object> providerConfig) {
        HeadersAuthenticationProvider provider = new HeadersAuthenticationProvider();
        boolean enabled = Boolean.parseBoolean(providerConfig.get("enabled").toString());
        provider.setEnabled(enabled);
        provider.setSecureKeyHeader(providerConfig.get("secureKeyHeader").toString());
        provider.setSecureKeyHeaderValue(providerConfig.get("secureKeyHeaderValue").toString());
        provider.setUsernameHeader(providerConfig.get("usernameHeader").toString());
        provider.setFirstNameHeader(providerConfig.get("firstNameHeader").toString());
        provider.setLastNameHeader(providerConfig.get("lastNameHeader").toString());
        provider.setEmailHeader(providerConfig.get("emailHeader").toString());
        provider.setGroupsHeader(providerConfig.get("groupsHeader").toString());
        return provider;
    }
}
