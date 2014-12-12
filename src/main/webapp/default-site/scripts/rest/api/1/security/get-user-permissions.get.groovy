/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2014 Crafter Software Corporation.
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
import scripts.api.SecurityServices;
/**
 * Created by dejanbrkic on 12/10/14.
 */

def result = [:];
// extract parameters
def user = params.user;
def groups = params.groups;
def path = params.path;
def site = params.site;

/* check parameters
if (site == undefined || site == "")
{
    status.code = 400;
    status.message = "Site must be provided.";
    status.redirect = true;
} else {
    if (path == undefined || path == "")
    {
        status.code = 400;
        status.message = "Path must be provided.";
        status.redirect = true;
    } else {
*/
if (groups != null) {
    /* set the authorityDisplayName as the group ID */
    def groupList = new java.util.ArrayList();
    for (i in groups) {
        //log("Getting permissions for group: " + groups[i]);
        groupList.add(groups[i]);
    }
    groups = groupList;
}
def context = SecurityServices.createContext(applicationContext, request)
def items = SecurityServices.getUserPermissions(context, site, path, user, groups);



return result;
