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
 * @author Dejan Brkic
 */

import scripts.api.WorkflowServices;

// extract parameters
def result = [:];
def sort = params.sort;
def site = params.site;
def ascending = params.ascending;
if (ascending != null) {
    ascending = ascending.toBoolean();
} else {
    ascending = false
}
def inProgressOnly = params.inProgressOnly;
if (inProgressOnly != null) {
    inProgressOnly = inProgressOnly.toBoolean();
} else {
    inProgressOnly = false
}
def includeInProgress = params.includeInProgress;
if (includeInProgress != null) {
    includeInProgress = includeInProgress.toBoolean();
} else {
    includeInProgress = false
}
/*
if (site == undefined || site == "")
{
    status.code = 400;
    status.message = "Site must be provided.";
    status.redirect = true;
} else {
    if (ascending == undefined) {
        ascending = "false"; // 0 is descending, 1 is ascending
    }
    if (sort == undefined)
    {
        sort = "eventDate";
    }

    if ((inProgressOnly != undefined && inProgressOnly == 'true') ||
            (includeInProgress != undefined && includeInProgress == 'true')) {
        model.result = dmWorkflowService.getInProgressItems(site, sub, sort, ascending, inProgressOnly);
    } else {
        model.result = dmWorkflowService.getGoLiveItems(site, sub, sort, ascending);
    }
}*/
def context = WorkflowServices.createContext(applicationContext, request)
if (inProgressOnly || includeInProgress) {
    result.content = WorkflowServices.getInProgressItems(context, site, sort, ascending, inProgressOnly);
} else {
    result.content = WorkflowServices.getGoLiveItems(context, site, sort, ascending);
}

return result;