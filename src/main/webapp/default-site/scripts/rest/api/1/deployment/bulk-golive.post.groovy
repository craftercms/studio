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
import scripts.api.DeploymentServices;

def site = params.site;
def path = params.path;
def environment = params.environment;
def result =[:]
/*if (site == undefined || site == "") {
    status.code = 400;
    status.message = "Site must be provided.";
    status.redirect = true;
} else if (path == undefined || path == "") {
    status.code = 400;
    status.message = "Path must be provided.";
    status.redirect = true;
} else if (environment == undefined || environment == "") {
    status.code = 400;
    status.message = "Publishing environment must be provided.";
    status.redirect = true;
} else {
    dmPublishService.bulkGoLive(site, environment, path);
}
*/
def context = DeploymentServices.createContext(applicationContext, request)
DeploymentServices.bulkGoLive(context, site, environment, path);
result = ["success":true]
