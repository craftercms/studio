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
import scripts.api.SiteServices;

def result = [:];
def status = [:];
def site = params.site;
def path = params.path;
def applyEnv = params.applyEnv;

def valid = true;

if (path == null || path == '')
{
    status.message = "Path must be provided.";
    valid = false;
}
if (valid)
{
    def applyEnvironment = false;
    if (applyEnv != null && applyEnv == 'true') {
        applyEnvironment = true;
    }
    def context = SiteServices.createContext(applicationContext, request);
    result.result = SiteServices.getConfiguration(context, site, path, applyEnvironment);
}
else
{
    status.code = 400;
    status.redirect = true;
}
return result.result;
