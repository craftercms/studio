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

import scripts.api.SecurityServices
import scripts.api.UserServices

def result = [:]

def context = SecurityServices.createContext(applicationContext, request)

try {
    def success = SecurityServices.validateSession(context, request)
    if (success) {
        result.message = "OK"
        result.active = true
        result.user = UserServices.getCurrentUser(context)
        response.setStatus(200)
    } else {
        result.message = null
        result.active = false
        result.user = null
        response.setStatus(200)
    }
} catch (Exception e) {
    response.setStatus(500)
    result.message = "Internal server error: \n" + e
}

return result
