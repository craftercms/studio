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

def boxService = applicationContext["studioBoxService"]

def site = params.site
def profileId = params.profileId
def fileId = params.fileId
def filename = params.filename

def result = [:]
def invalidParams = false
def paramsList = []

if(!site) {
    invalidParams = true
    paramsList += "site"
}

if(!profileId) {
    invalidParams = true
    paramsList += "profileId"
}

if(!fileId) {
    invalidParams = true
    paramsList += "fileId"
}

if(!filename) {
    invalidParams = true
    paramsList += "filename"
}

if(invalidParams) {
    response.setStatus(400)
    result.message = "Invalid parameter(s): " + paramsList
} else {
    result.url = boxService.getUrl(site, profileId, fileId, filename)
}

return result