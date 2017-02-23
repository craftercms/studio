
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
 *
 */

/**
 * @auhor Dejan Brkic
 */

import scripts.api.ActivityServices;

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
def actions = []
if (params.actions != null && params.actions != '') {
    actions = params.actions
}



def context = ActivityServices.createContext(applicationContext, request)
def activities = ActivityServices.getAuditLog(context, site, start, end, user, actions)

result.total = activities.size()
result.items = activities
return result
