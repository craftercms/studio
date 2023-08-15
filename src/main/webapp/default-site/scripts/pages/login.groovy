/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.core.util.ExceptionUtils
import org.craftercms.studio.api.v2.security.authentication.LockedException
import org.springframework.security.web.WebAttributes
import scripts.api.SecurityServices

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SECURITY_PASSWORD_REQUIREMENTS_MINIMUM_COMPLEXITY

def context = SecurityServices.createContext(applicationContext, request)

def studioConfigurationSB = context.applicationContext.get("studioConfiguration")

def passwordRequirementsMinComplexity = studioConfigurationSB
        .getProperty(SECURITY_PASSWORD_REQUIREMENTS_MINIMUM_COMPLEXITY).toInteger()

if (params.error) {
    def lastException = request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION)
    if (lastException != null) {
        LockedException lockedException = ExceptionUtils.getThrowableOfType(lastException, LockedException.class);
        if (lockedException != null) {
            model.errorMessage = lockedException.message
            model.lockedTimeSeconds = lockedException.lockedTimeSeconds
        }
    }
}

model.passwordRequirementsMinComplexity = passwordRequirementsMinComplexity
