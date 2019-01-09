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

import scripts.api.SecurityServices

def result = [:]

try {
    def context = SecurityServices.createContext(applicationContext, request)

    result.success = SecurityServices.logout(context)

    if (result.success) {
        response.setStatus(200)
        result.message = "OK"
    } else {
        response.setStatus(500)
        result.message = "Internal server error"
    }
} catch(e) {
    response.setStatus(500)
    result.message = "Internal server error: \n" + e
}

return result
