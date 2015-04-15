
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

/**
 * @auhor Dejan Brkic
 */

import scripts.api.ActivityServices;

def result = [:]
def site = params.site;
def user = params.user;
def num = params.num.toInteger();
def excludeLive = params.excludeLive;
def valid = true;
def filterType = params.filterType;

/*
TODO: params check

if (site == undefined) {
    status.code = 400;
    status.message = "Site must be provided.";
    status.redirect = true;
    valid = false;
}
if (user == undefined) {
    status.code = 400;
    status.message = "Site must be provided.";
    status.redirect = true;
    valid = false;
}
if (valid) {
    if (num == undefined || num == "") {
        num = "10";
    }
    if (excludeLive != undefined && excludeLive == "true") {
        model.result = dmActivityService.getActivities(site, user, num, "eventDate", false, true,filterType);
    } else {
        model.result = dmActivityService.getActivities(site, user, num, "eventDate", false, false,filterType);
    }
}*/

def context = ActivityServices.createContext(applicationContext, request)
def activities;
if (excludeLive != null && excludeLive != "undefined" && excludeLive == "true") {
    activities = ActivityServices.getActivities(context, site, user, num, "eventDate", false, true, filterType);

} else {
    activities = ActivityServices.getActivities(context, site, user, num, "eventDate", false, false, filterType);
}
result.total = activities.size();
result.sortedBy = "eventDate";
result.ascending = "false";
result.documents = activities;
return result