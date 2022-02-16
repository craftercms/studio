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

def webDavService = applicationContext["studioWebDavService"]

def sendError = { code, msg ->
    response.status = code
    return [
            hasErrors : true,
            errors: [ msg ]
    ]
}

def site = params.site_id
def profileId = params.profile
def path = params.path
def type = params.type

if(!site) {
    return sendError(400, "Missing parameter 'site_id'")
}

if(!profileId) {
    return sendError(400, "Missing parameter 'profileId'")
}

try {
    def items = webDavService.list(site, profileId, path, type)

    return items

} catch (e) {
    logger.error("Listing of path ${path} failed", e)
    return sendError(500, "Listing of path ${path} failed: " + e.getMessage() as String)
}