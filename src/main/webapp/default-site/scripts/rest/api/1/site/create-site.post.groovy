/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2015 Crafter Software Corporation.
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
import scripts.api.SiteServices;
import groovy.json.JsonSlurper

def result = [:]
def requestJson = request.reader.text
def slurper = new JsonSlurper()
def parsedReq = slurper.parseText(requestJson)

def blueprintName = parsedReq.blueprintName
def siteName = parsedReq.siteName
def siteId = parsedReq.siteId
def desc = parsedReq.desc

def context = SiteServices.createContext(applicationContext, request)
result = SiteServices.createSiteFromBlueprint(context, blueprintName, siteName, siteId, desc)

return result