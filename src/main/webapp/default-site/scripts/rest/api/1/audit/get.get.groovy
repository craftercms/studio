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

/**
 * @auhor Dejan Brkic
 */

import groovy.json.JsonException
import groovy.json.JsonSlurper
import org.apache.commons.lang3.StringUtils
import org.craftercms.studio.api.v1.exception.SiteNotFoundException
import scripts.api.ActivityServices

def result = [:]
try {
    def site = params.site_id
    def user = ''
    if (params.user != null && params.user != '') {
        user = params.user
    }
    def slurper = new JsonSlurper()
    def actions = []
    if (params.actions != null && params.actions != '') {
        actions = slurper.parseText(params.actions)
    }

    def start = 0
    def number = 10

/** Validate Parameters */
    def invalidParams = false;
    def paramsList = []

// site_id
    try {
        if (StringUtils.isEmpty(site)) {
            invalidParams = true
            paramsList.add("site_id")
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("site_id")
    }

// start
    try {
        if (StringUtils.isNotEmpty(params.start)) {
            start = params.start.toInteger()
            if (start < 0) {
                invalidParams = true
                paramsList.add("start")
            }
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("start")
    }

// number
    try {
        if (StringUtils.isNotEmpty(params.number)) {
            number = params.number.toInteger()
            if (number < 0) {
                invalidParams = true
                paramsList.add("number")
            }
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add("number")
    }

    if (invalidParams) {
        response.setStatus(400)
        result.message = "Invalid parameter(s): " + paramsList
    } else {
        def context = ActivityServices.createContext(applicationContext, request)
        try {
            result.total = ActivityServices.getAuditLogTotal(context, site, user, actions)
            def activities = ActivityServices.getAuditLog(context, site, start, number, user, actions)
            result.message = "OK"
            response.setStatus(200)
            result.items = activities
        } catch (SiteNotFoundException e) {
            response.setStatus(404)
            result.message = "Site not found"
        } catch (Exception e) {
            response.setStatus(500)
            result.message = "Internal server error: \n" + e
        }
    }
} catch (JsonException e) {
    response.setStatus(400)
    result.message = "Bad Request"
}
return result
