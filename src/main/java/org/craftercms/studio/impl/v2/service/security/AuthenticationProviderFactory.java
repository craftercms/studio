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

import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_EMAIL_HEADER;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_EMAIL_LDAP_ATTRIBUTE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_ENABLED;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_FIRST_NAME_HEADER;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_FIRST_NAME_LDAP_ATTRIBUTE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_GROUPS_HEADER;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_GROUP_NAME_LDAP_ATTRIBUTE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_GROUP_NAME_MATCH_INDEX_LDAP_ATTRIBUTE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_GROUP_NAME_REGEX_LDAP_ATTRIBUTE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_LAST_NAME_HEADER;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_LAST_NAME_LDAP_ATTRIBUTE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_LDAP_BASE_CONTEXT;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_LDAP_PASSWORD;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_LDAP_URL;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_LDAP_USERNAME;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_SECURE_KEY_HEADER;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_SECURE_KEY_HEADER_VALUE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE_DB;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE_HEADERS;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE_LDAP;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_USERNAME_HEADER;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_USERNAME_LDAP_ATTIBUTE;

public class AuthenticationProviderFactory {

    private AuthenticationProviderFactory() {
    }

    public static AuthenticationProvider getAuthenticationProvider(Map<String, Object> providerConfig) {
        AuthenticationProvider provider;
        switch (providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_TYPE).toString().toUpperCase()) {
            case AUTHENTICATION_CHAIN_PROVIDER_TYPE_DB:
                provider = createDbAuthenticationProvider(providerConfig);
                break;
            case AUTHENTICATION_CHAIN_PROVIDER_TYPE_LDAP:
                provider = createLdapAuthenticationProvider(providerConfig);
                break;
            case AUTHENTICATION_CHAIN_PROVIDER_TYPE_HEADERS:
                provider = createHeadersAuthenticationProvider(providerConfig);
                break;
            default:
                provider = null;
        }
        return provider;
    }

    private static DbAuthenticationProvider createDbAuthenticationProvider(Map<String, Object> providerConfig) {
        DbAuthenticationProvider provider = new DbAuthenticationProvider();
        boolean enabled = Boolean.parseBoolean(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_ENABLED).toString());
        provider.setEnabled(enabled);
        return provider;
    }

    private static LdapAuthenticationProvider createLdapAuthenticationProvider(Map<String, Object> providerConfig) {
        LdapAuthenticationProvider provider = new LdapAuthenticationProvider();
        boolean enabled = Boolean.parseBoolean(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_ENABLED).toString());
        provider.setEnabled(enabled);
        provider.setLdapUrl(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_LDAP_URL).toString());
        provider.setLdapUsername(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_LDAP_USERNAME).toString());
        provider.setLdapPassword(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_LDAP_PASSWORD).toString());
        provider.setLdapBaseContext(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_LDAP_BASE_CONTEXT).toString());
        provider.setUsernameLdapAttribute(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_USERNAME_LDAP_ATTIBUTE).toString());
        provider.setFirstNameLdapAttribute(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_FIRST_NAME_LDAP_ATTRIBUTE).toString());
        provider.setLastNameLdapAttribute(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_LAST_NAME_LDAP_ATTRIBUTE).toString());
        provider.setEmailLdapAttribute(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_EMAIL_LDAP_ATTRIBUTE).toString());
        provider.setGroupNameLdapAttribute(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_GROUP_NAME_LDAP_ATTRIBUTE).toString());
        provider.setGroupNameLdapAttributeRegex(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_GROUP_NAME_REGEX_LDAP_ATTRIBUTE).toString());
        provider.setGroupNameLdapAttributeMatchIndex(
                Integer.parseInt(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_GROUP_NAME_MATCH_INDEX_LDAP_ATTRIBUTE).toString()));
        return provider;
    }

    private static HeadersAuthenticationProvider createHeadersAuthenticationProvider(Map<String, Object> providerConfig) {
        HeadersAuthenticationProvider provider = new HeadersAuthenticationProvider();
        boolean enabled = Boolean.parseBoolean(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_ENABLED).toString());
        provider.setEnabled(enabled);
        provider.setSecureKeyHeader(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_SECURE_KEY_HEADER).toString());
        provider.setSecureKeyHeaderValue(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_SECURE_KEY_HEADER_VALUE).toString());
        provider.setUsernameHeader(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_USERNAME_HEADER).toString());
        provider.setFirstNameHeader(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_FIRST_NAME_HEADER).toString());
        provider.setLastNameHeader(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_LAST_NAME_HEADER).toString());
        provider.setEmailHeader(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_EMAIL_HEADER).toString());
        provider.setGroupsHeader(providerConfig.get(AUTHENTICATION_CHAIN_PROVIDER_GROUPS_HEADER).toString());
        return provider;
    }
}
