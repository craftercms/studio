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

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.service.security.SecurityProvider;


/**
 * Easily register any security provide with the mapper
 * @author russdanner
 *
 */
public class MappedSecurityProviderRegistration {

    private static final Logger logger = LoggerFactory.getLogger(MappedSecurityProviderRegistration.class);

    private MappedSecurityProvider securityProvider;
    public MappedSecurityProvider getMappedSecurityProvider() { return securityProvider; }
    public void setMappedSecurityProvider(MappedSecurityProvider provider) { securityProvider = provider; }

    private SecurityProvider targetSecurityProvider;
    public SecurityProvider getSecurityProvider() { return targetSecurityProvider; }
    public void setSecurityProvider(SecurityProvider provider) { targetSecurityProvider = provider; }

    private String providerType; 
    public String getProviderType() { return providerType; }
    public void setProviderType(String type) { providerType = type; }

    public void registerProvider() {
        securityProvider.registerSecurityProvider(providerType, targetSecurityProvider);
    }
}
