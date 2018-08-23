/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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

def webDavService = applicationContext["studioWebDavService"]

response.setHeader("Content-Type", "text/html")

def sendError = { code, msg ->
    response.status = code
    def writer = response.writer
    writer.println("<script>document.domain = \"${request.serverName}\";</script>")
    writer.println("{\"hasError\":true,\"errors\":[\"${msg}\"]}")
    writer.flush()
}

def site = params.site_id
def profileId = params.profile
def path = params.path

if(!site) {
    sendError(400, "Missing parameter 'site_id'")
}

if(!profileId) {
    sendError(400, "Missing parameter 'profileId'")
}

if(!path) {
    sendError(400, "Missing parameter 'path'")
}

try {
    def items = webDavService.list(site, profileId, path)

    def writer = response.writer
    writer.println("<script>document.domain = \"${request.serverName}\";</script>")
    writer.println("[${items.collect{ "{\"name\":\"${it.name}\",\"url\":\"${it.url}\",\"folder\":\"${it.folder}\"}" }.join(",")}]")
    writer.flush()
} catch (e) {
    logger.error("Listing of path ${path} failed", e)

    sendError(500, "Listing of path ${path} failed: " + e.message)
}