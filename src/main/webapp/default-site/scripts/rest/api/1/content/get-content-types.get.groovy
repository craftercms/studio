
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

import scripts.api.ContentTypeServices;

def result = [:]
def site = params.site;
def path = params.path;
/*
if (site == undefined || site == '')
{
    status.code = 400;
    status.message = "Site must be provided.";
    status.redirect = true;
}
else
{
    if (type == undefined || type == '')
    {
        status.code = 400;
        status.message = "type must be provided.";
        status.redirect = true;
    }
    else
    {
        model.result = dmContentTypeService.getContentType(site, type);
    }

}
*/
def context = ContentTypeServices.createContext(applicationContext, request);
if (path != null) {
    result = ContentTypeServices.getAllowedContentTypesForPath(context, site, path);
} else {
    result = ContentTypeServices.getContentTypes(context, site, true);
}

return result