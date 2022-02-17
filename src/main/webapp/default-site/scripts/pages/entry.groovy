/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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


import org.apache.commons.text.StringEscapeUtils
import org.apache.commons.lang3.StringUtils
import org.craftercms.studio.api.v1.log.LoggerFactory
import scripts.api.SecurityServices
import org.apache.commons.configuration2.BaseHierarchicalConfiguration
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_EMAIL_HEADER
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_ENABLED
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_FIRST_NAME_HEADER
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_LAST_NAME_HEADER
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_TYPE_HEADERS
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHENTICATION_CHAIN_PROVIDER_USERNAME_HEADER

import static org.craftercms.studio.api.v1.constant.StudioConstants.SECURITY_AUTHENTICATION_TYPE_HEADERS
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_AUTHENTICATION_CHAIN_CONFIG
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_PASSWORD_REQUIREMENTS_VALIDATION_REGEX

def logger = LoggerFactory.getLogger(this.class)

def context = SecurityServices.createContext(applicationContext, request)

def result = [:]
def currentUser = SecurityServices.getCurrentUser(context)
def email = ""
def firstname = ""
def lastname = ""
def authenticationType = ""
def profile = null
def studioConfigurationSB = context.applicationContext.get("studioConfiguration")
def passwordRequirementsRegex = studioConfigurationSB.getProperty(SECURITY_PASSWORD_REQUIREMENTS_VALIDATION_REGEX)
def userServiceSB = context.applicationContext.get("userService")

if (StringUtils.isEmpty(currentUser)) {
    def chainConfig =
            studioConfigurationSB.getSubConfigs(CONFIGURATION_AUTHENTICATION_CHAIN_CONFIG)
    def authenticationHeadersEnabled = false
    if (chainConfig != null) {
        chainConfig.stream().anyMatch { providerConfig ->
            providerConfig.getString(AUTHENTICATION_CHAIN_PROVIDER_TYPE).toUpperCase().equals(AUTHENTICATION_CHAIN_PROVIDER_TYPE_HEADERS) && providerConfig.getBoolean(AUTHENTICATION_CHAIN_PROVIDER_ENABLED)
        }
    }
    if (authenticationHeadersEnabled) {
        currentUser = request.getHeader(studioConfigurationSB.getProperty(AUTHENTICATION_CHAIN_PROVIDER_USERNAME_HEADER))
        email = request.getHeader(studioConfigurationSB.getProperty(AUTHENTICATION_CHAIN_PROVIDER_EMAIL_HEADER))
        firstname = request.getHeader(studioConfigurationSB.getProperty(AUTHENTICATION_CHAIN_PROVIDER_FIRST_NAME_HEADER))
        lastname = request.getHeader(studioConfigurationSB.getProperty(AUTHENTICATION_CHAIN_PROVIDER_LAST_NAME_HEADER))
        authenticationType = SECURITY_AUTHENTICATION_TYPE_HEADERS
    }
}

def authenticatedUser = null;

if (!authenticationType) {
    try {
        authenticatedUser = userServiceSB.getCurrentUser()
    } catch (error) {
        // do nothing
    }
}

try {
    profile = SecurityServices.getUserProfile(context, currentUser)
} catch(e) {
    profile = [:]
    profile.email = email
    profile.first_name = firstname
    profile.last_name = lastname
    profile.authentication_type = authenticationType;
}

model.username = currentUser
model.userEmail = profile.email 
model.userFirstName = profile.first_name
model.userLastName =  profile.last_name
model.authenticationType =  authenticatedUser?
        authenticatedUser.getAuthenticationType() as String : profile.authentication_type
model.cookieDomain = StringEscapeUtils.escapeXml10(request.getServerName())
model.passwordRequirementsRegex = passwordRequirementsRegex;
