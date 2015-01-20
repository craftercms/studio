
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
import scripts.api.SecurityServices;

// extract parameters
def result = [:]
def user = params.user;
def site = params.site;
/* params check
if (site == null || site == "")
{
    status.code = 400;
    status.message = "Site must be provided.";
    status.redirect = true;
} else {
    if (user == undefined || user == "")
    {
        status.code = 400;
        status.message = "User must be provided.";
        status.redirect = true;
    }  else {
    */
def context = SecurityServices.createContext(applicationContext, request)
result.roles = SecurityServices.getUserRoles(context, site, user);
return result


