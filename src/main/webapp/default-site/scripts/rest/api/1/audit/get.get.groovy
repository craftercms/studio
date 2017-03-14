/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2017 Crafter Software Corporation.
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

/**
 * @auhor Dejan Brkic
 */

import groovy.json.JsonSlurper
import org.craftercms.studio.api.v1.exception.SiteNotFoundException
import scripts.api.ActivityServices

def result = [:]
def site = params.site_id
def start = 0
if (params.start != null && params.start != '') {
    start = params.start.toInteger()
}
def end = 10
if (params.end != null && params.end != '') {
    end = params.end.toInteger()
}
def user = ''
if (params.user != null && params.user != '') {
    user = params.user
}
def slurper = new JsonSlurper()
def actions = []
if (params.actions != null && params.actions != '') {
    actions = slurper.parseText(params.actions)
}

def context = ActivityServices.createContext(applicationContext, request)
try {
    def activities = ActivityServices.getAuditLog(context, site, start, end, user, actions)
    result.message = "OK"
    response.setStatus(200)
    result.total = activities.size()
    result.items = activities
} catch (SiteNotFoundException e) {
    response.setStatus(404)
    result.message = "Site not found"
} catch (Exception e) {
    response.setStatus(500)
    result.message = "Internal server error: \n" + e
}

return result
