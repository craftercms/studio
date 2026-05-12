/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.studio.impl.v2.utils.security.SecurityUtils
import org.slf4j.LoggerFactory
import scripts.libs.EnvironmentOverrides

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_PASSWORD_REQUIREMENTS_MINIMUM_COMPLEXITY

def logger = LoggerFactory.getLogger(this.class)

def studioConfigurationSB = applicationContext.get("studioConfiguration")
def passwordRequirementsMinComplexity = studioConfigurationSB.getProperty(SECURITY_PASSWORD_REQUIREMENTS_MINIMUM_COMPLEXITY).toInteger()

def authenticatedUser = null;

try {
	authenticatedUser = SecurityUtils.getCurrentUser();
} catch (error) {
	// do nothing
}

if (authenticatedUser) {
	model.username = authenticatedUser.username
	model.userEmail = authenticatedUser.email
	model.userFirstName = authenticatedUser.firstName
	model.userLastName = authenticatedUser.lastName
	model.authenticationType = authenticatedUser.getAuthenticationType() as String
}
model.cookieDomain = StringEscapeUtils.escapeXml10(request.getServerName())
model.passwordRequirementsMinComplexity = passwordRequirementsMinComplexity;
model.envConfig = EnvironmentOverrides.getMinimalValuesForSite(applicationContext, request)
