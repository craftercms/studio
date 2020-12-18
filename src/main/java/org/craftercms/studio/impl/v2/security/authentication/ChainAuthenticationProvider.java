/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.impl.v2.security.authentication;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.security.authentication.db.DbAuthenticationProvider;
import org.craftercms.studio.impl.v2.security.authentication.headers.HeadersAuthenticationProvider;
import org.craftercms.studio.impl.v2.security.authentication.ldap.LdapAuthenticationProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.LinkedList;
import java.util.List;

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
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_SECURE_KEY_HEADER;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_SECURE_KEY_HEADER_VALUE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE_DB;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE_HEADERS;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE_LDAP;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_USERNAME_HEADER;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_USERNAME_LDAP_ATTIBUTE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_AUTHENTICATION_CHAIN_CONFIG;

/**
 *  Implementation of {@link AuthenticationProvider} for Studio's authentication chain. Holds a list of authenticators
 *  initialized based on the main configuration.
 *
 * @author joseross
 * @since 3.2.0
 */
public class ChainAuthenticationProvider implements AuthenticationProvider, ApplicationContextAware, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ChainAuthenticationProvider.class);

    protected ApplicationContext appContext;

    protected StudioConfiguration studioConfiguration;

    protected List<AuthenticationProvider> providers = new LinkedList<>();

    public ChainAuthenticationProvider(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        appContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        var chainConfig = studioConfiguration.getSubConfigs(CONFIGURATION_AUTHENTICATION_CHAIN_CONFIG);
        chainConfig.forEach(providerConfig -> {
            if (!providerConfig.getBoolean(AUTHENTICATION_CHAIN_PROVIDER_ENABLED)) {
                return;
            }
            AuthenticationProvider provider;
            switch (providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_TYPE).toUpperCase()) {
                case AUTHENTICATION_CHAIN_PROVIDER_TYPE_DB:
                    provider = appContext.getBean(DbAuthenticationProvider.class);
                    break;
                case AUTHENTICATION_CHAIN_PROVIDER_TYPE_LDAP:
                    provider = initLdapAuthenticationProvider(providerConfig);
                    break;
                case AUTHENTICATION_CHAIN_PROVIDER_TYPE_HEADERS:
                    provider = initHeadersAuthenticationProvider(providerConfig);
                    break;
                default:
                    logger.warn("Unsupported authentication provider: {0}",
                            providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_TYPE));
                    provider = null;
            }
            if (provider != null) {
                providers.add(provider);
            }
        });
    }

    protected AuthenticationProvider initLdapAuthenticationProvider(HierarchicalConfiguration<?> providerConfig) {
        var provider = appContext.getBean(LdapAuthenticationProvider.class);
        provider.setLdapUrl(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_LDAP_URL));
        provider.setLdapUsername(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_LDAP_USERNAME));
        provider.setLdapPassword(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_LDAP_PASSWORD));
        provider.setLdapBaseContext(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_LDAP_BASE_CONTEXT));
        provider.setUsernameLdapAttribute(
                providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_USERNAME_LDAP_ATTIBUTE));
        provider.setFirstNameLdapAttribute(
                providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_FIRST_NAME_LDAP_ATTRIBUTE));
        provider.setLastNameLdapAttribute(
                providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_LAST_NAME_LDAP_ATTRIBUTE));
        provider.setEmailLdapAttribute(
                providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_EMAIL_LDAP_ATTRIBUTE));
        provider.setGroupNameLdapAttribute(
                providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_GROUP_NAME_LDAP_ATTRIBUTE));
        provider.setGroupNameLdapAttributeRegex(
                providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_GROUP_NAME_REGEX_LDAP_ATTRIBUTE));
        provider.setGroupNameLdapAttributeMatchIndex(Integer.parseInt(
                providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_GROUP_NAME_MATCH_INDEX_LDAP_ATTRIBUTE)));
        return provider;
    }

    protected AuthenticationProvider initHeadersAuthenticationProvider(HierarchicalConfiguration<?> providerConfig) {
        var provider = appContext.getBean(HeadersAuthenticationProvider.class);
        provider.setSecureKeyHeader(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_SECURE_KEY_HEADER));
        provider.setSecureKeyHeaderValue(
                providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_SECURE_KEY_HEADER_VALUE));
        provider.setUsernameHeader(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_USERNAME_HEADER));
        provider.setFirstNameHeader(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_FIRST_NAME_HEADER));
        provider.setLastNameHeader(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_LAST_NAME_HEADER));
        provider.setEmailHeader(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_EMAIL_HEADER));
        provider.setGroupsHeader(providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_GROUPS_HEADER));
        return provider;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        for (var provider : providers) {
            if (provider.supports(authentication.getClass())) {
                var result = provider.authenticate(authentication);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true; //Let any kind of auth pass, each provider will decide
    }

}
