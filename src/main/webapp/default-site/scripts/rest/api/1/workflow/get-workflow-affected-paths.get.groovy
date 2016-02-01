
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
import scripts.api.WorkflowServices;

def result = [:]
def site = params.site;
def path = params.path;
def valid = true
/*
if (site == undefined || site == "") {
    status.code = 400;
    status.message = "Site must be provided.";
    status.redirect = true;
    valid = true;
}
if (path == undefined || path == "") {
    status.code = 400;
    status.message = "Path must be provided.";
    status.redirect = true;
    valid = true;
}*/
if (valid) {
    def context = WorkflowServices.createContext(applicationContext, request)
    def items = WorkflowServices.getWorkflowAffectedPaths(context, site, path);
    def toRet = []
    items.each() {
        def item = [:];
        item.path = it.uri;
        item.browserUri = it.browserUri;
        item.name = it.internalName ?: it.name;
        toRet.add(item);
    }
    result.items = toRet;
}
return result;
