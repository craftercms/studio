/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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

import scripts.api.SecurityServices

def result = [:]

try {
    def context = SecurityServices.createContext(applicationContext, request)
    result.success = SecurityServices.logout(context)

    result.user = null

    if (result.success) {
        result.type = "success"
        result.message = "Logout successful"
    } else {
        result.type = "failure"
        result.message = "Logout failed"
    }
} catch(err) {
    result.exception = err
    result.type = "error"
    result.message = "Error while logging out"
}
