
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

/*
{
"path":"${path}",
"formId":"${formId}"
}

 */

import scripts.api.ClipboardServices;
import scripts.api.ContentTypeServices;

def result = [:];
def site = params.site;
def path = params.path;

/*
if (!site) {
    status.code = 400;
    status.message = "Site is required.";
    status.redirect = true;
} else {
*/

//    var resultJson = connector.call("/cstudio/wcm/clipboard/duplicate?site=" + site + "&path=" + path);
def context = ClipboardServices.createContext(applicationContext, request);
def resultDuplicate = ClipboardServices.duplicate(context, site, path);

def resultContentType = ContentTypeServices.getContentTypeByPath(context, site, path);

result.path = resultDuplicate;
result.formId = resultContentType.form;

return result;


/*
    var duplicateJSON = eval('(' + resultJson + ')');

    var duplicatePath = duplicateJSON.path;
    var contentType = connector.call("/cstudio/wcm/contenttype/get-content-type-by-path?site=" + site + "&path=" + path);
    var contentTypeJSON = eval('(' + contentType + ')');
    var formName = contentTypeJSON.form;
    var formPath = "/page/site/cstudio/cstudio-webform?form=" + formName + "&id=" + duplicatePath + "&path=" + duplicatePath + "&edit=true&draft=true";
    model.path = duplicatePath;
    model.formId = formName;
}
*/