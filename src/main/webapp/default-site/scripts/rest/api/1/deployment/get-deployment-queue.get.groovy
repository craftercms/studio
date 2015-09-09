
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

/**
 * @author Dejan Brkic
 */

import scripts.api.DeploymentServices;

// extract parameters
def result = [:];
//def sort = params.sort;
def site = params.site;
//def ascending = params.ascending.toBoolean();
//def filterType = params.filterType;
/*
TODO: check parameters
if (site == undefined || site == "")
{
    status.code = 400;
    status.message = "Site must be provided.";
    status.redirect = true;
} else {
    var ascending = false;
    if (ascending != undefined && ascending == "1") {
        ascending = true; // 0 is descending, 1 is ascending
    }
    if (sort == undefined)
    {
        sort = "eventDate";
    }
*/

def context = DeploymentServices.createContext(applicationContext, request)
def items = DeploymentServices.getDeploymentQueue(context, site);
def total = 0;
for (task in items) {
    //total += task.numOfChildren;
}
//result.total = total;
//result.sortedBy = sort;
//result.ascending = ascending;
result.items = items;
return result;
