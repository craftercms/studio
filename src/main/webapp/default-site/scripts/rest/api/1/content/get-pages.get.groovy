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

/**
 * @author Dejan Brkic
 */

import scripts.api.ContentServices;

def result = [:]
def site = params.site;
def path = params.path;
def depth = params.depth ? params.depth.toInteger() : 666;
def order = params.order;

/**
 * After 11/15/16 depth was ignore and internal was
 * hardcoded to 2, this was fix ,but UI expect that min possible value is 2
 * **/
if(depth<=1){
    depth=2
}

/*
var valid = true;

if (depth == null) {
    depth = 0;
}
if (site == undefined || site == '')
{
    status.code = 400;
    status.message = "Site must be provided.";
    status.redirect = true;
    valid = false;
}
if (path == undefined || path == '')
{
    status.code = 400;
    status.message = "Path must be provided.";
    status.redirect = true;
    valid = false;
}
*/
def context = ContentServices.createContext(applicationContext, request)
result.item = ContentServices.getPages(context, site, path, depth, order, true);

return result;
