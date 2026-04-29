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

package scripts.api

import scripts.api.ServiceFactory

/**
 * content services
 */
class ContentServices {

	/**
	 * create the context object
	 * @param applicationContext - studio application's contect (spring container etc)
	 * @param request - web request if in web request context
	 */
	static createContext(applicationContext, request) {
		return ServiceFactory.createContext(applicationContext, request)
	}

	/**
	 * get content orders for a given path (usually used for navigation)
	 * @param site - the project ID
	 * @param path - the parent path containing the ordered objects
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static getItemOrders(context, site, path) {
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.getItemOrders(site, path)
	}

	/**
	 * Get the content for a specific version
	 * @param site - the project ID
	 * @param path - the path of the item to "revert"
	 * @param version - old version ID to base to version on
	 * @paran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static getContentVersionAtPath(site, path, version, context) {
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.getContentVersionAtPath(site, path, version)
	}

	static getContentType(context, site, type) {
		def contentTypeServicesImpl = ServiceFactory.getContentTypeServices(context);
		return contentTypeServicesImpl.getContentType(site, type)
	}

	static reorderItems(context, site, path, before, after) {
		def contentServicesImpl = ServiceFactory.getContentServices(context);
		return contentServicesImpl.reorderItems(site, path, before, after);
	}
}
