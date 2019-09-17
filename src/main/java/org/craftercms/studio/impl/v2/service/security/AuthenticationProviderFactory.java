/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.service.security;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.craftercms.studio.api.v2.service.security.AuthenticationProvider;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_EMAIL_HEADER;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_EMAIL_LDAP_ATTRIBUTE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_ENABLED;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_FIRST_NAME_HEADER;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_FIRST_NAME_LDAP_ATTRIBUTE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_GROUPS_HEADER;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_GROUP_NAME_LDAP_ATTRIBUTE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_GROUP_NAME_MATCH_INDEX_LDAP_ATTRIBUTE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_GROUP_NAME_REGEX_LDAP_ATTRIBUTE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_LAST_NAME_HEADER;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_LAST_NAME_LDAP_ATTRIBUTE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_LDAP_BASE_CONTEXT;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_LDAP_PASSWORD;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_LDAP_URL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_LDAP_USERNAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_LOGOUT_ENABLED;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_LOGOUT_URL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_SECURE_KEY_HEADER;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_SECURE_KEY_HEADER_VALUE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE_DB;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE_HEADERS;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE_LDAP;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_USERNAME_HEADER;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_USERNAME_LDAP_ATTIBUTE;

public class AuthenticationProviderFactory {

    private AuthenticationProviderFactory() {
    }

    public static AuthenticationProvider getAuthenticationProvider(HierarchicalConfiguration<ImmutableNode> providerConfig) {
        AuthenticationProvider provider;
        switch (providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_TYPE).toUpperCase()) {
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

    private static DbAuthenticationProvider createDbAuthenticationProvider(HierarchicalConfiguration<ImmutableNode> providerConfig) {
        DbAuthenticationProvider provider = new DbAuthenticationProvider();
        boolean enabled = providerConfig.getBoolean(AUTHENTICATION_CHAIN_PROVIDER_ENABLED);
        provider.setEnabled(enabled);
        return provider;
    }

    private static LdapAuthenticationProvider createLdapAuthenticationProvider(HierarchicalConfiguration<ImmutableNode> providerConfig) {
        LdapAuthenticationProvider provider = new LdapAuthenticationProvider();
        boolean enabled = providerConfig.getBoolean(AUTHENTICATION_CHAIN_PROVIDER_ENABLED);
        provider.setEnabled(enabled);
        provider.setLdapUrl(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_LDAP_URL));
        provider.setLdapUsername(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_LDAP_USERNAME));
        provider.setLdapPassword(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_LDAP_PASSWORD));
        provider.setLdapBaseContext(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_LDAP_BASE_CONTEXT));
        provider.setUsernameLdapAttribute(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_USERNAME_LDAP_ATTIBUTE));
        provider.setFirstNameLdapAttribute(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_FIRST_NAME_LDAP_ATTRIBUTE));
        provider.setLastNameLdapAttribute(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_LAST_NAME_LDAP_ATTRIBUTE));
        provider.setEmailLdapAttribute(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_EMAIL_LDAP_ATTRIBUTE));
        provider.setGroupNameLdapAttribute(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_GROUP_NAME_LDAP_ATTRIBUTE));
        provider.setGroupNameLdapAttributeRegex(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_GROUP_NAME_REGEX_LDAP_ATTRIBUTE));
        provider.setGroupNameLdapAttributeMatchIndex(
                Integer.parseInt(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_GROUP_NAME_MATCH_INDEX_LDAP_ATTRIBUTE)));
        return provider;
    }

    private static HeadersAuthenticationProvider createHeadersAuthenticationProvider(HierarchicalConfiguration<ImmutableNode> providerConfig) {
        HeadersAuthenticationProvider provider = new HeadersAuthenticationProvider();
        boolean enabled = providerConfig.getBoolean(AUTHENTICATION_CHAIN_PROVIDER_ENABLED);
        provider.setEnabled(enabled);
        provider.setSecureKeyHeader(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_SECURE_KEY_HEADER));
        provider.setSecureKeyHeaderValue(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_SECURE_KEY_HEADER_VALUE));
        provider.setUsernameHeader(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_USERNAME_HEADER));
        provider.setFirstNameHeader(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_FIRST_NAME_HEADER));
        provider.setLastNameHeader(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_LAST_NAME_HEADER));
        provider.setEmailHeader(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_EMAIL_HEADER));
        provider.setGroupsHeader(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_GROUPS_HEADER));
        provider.setLogoutEnabled(providerConfig.getBoolean(AUTHENTICATION_CHAIN_PROVIDER_LOGOUT_ENABLED));
        provider.setLogoutUrl(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_LOGOUT_URL));
        return provider;
    }
}
