/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_UI_RESOURCE_OVERRIDE_PATH

def resourceName = params.resource

if(resourceName == null) {
	throw new Exception("resource is a required parameter")
}

def studioConfiguration = applicationContext.get("studioConfiguration")
def classloader = this.getClass().getClassLoader().getParent().getParent().getParent()
def resource = classloader.getResourceAsStream(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_UI_RESOURCE_OVERRIDE_PATH) + "/" + resourceName)
if(resource != null) {
	if( resourceName.contains(".css")
	||  resourceName.contains(".js")) {
		return resource.text 	
	}
	else{
		response.getOutputStream().write(resource.bytes) 
	}
}
else {
	response.setStatus(404)
	return "NOT FOUND"
}
