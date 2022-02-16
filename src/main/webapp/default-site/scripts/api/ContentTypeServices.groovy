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
/**
 * content type services
 */
class ContentTypeServices {

    /**
     * create the context object
     * @param applicationContext - studio application's contect (spring container etc)
     * @param request - web request if in web request context
     */
    static createContext(applicationContext, request) {
        return ServiceFactory.createContext(applicationContext, request)
    }

	/**
	 * change content type
	 * @param site - the project ID
	 * @param item - the item ID
	 * @param template - the new type
	 */
	static changeContentType(context, site, path, type){
		def contentTypeServiceImpl = ServiceFactory.getContentTypeServices(context)
        return contentTypeServiceImpl.changeContentType(site, path, type)
	}
	
	/**
	 * get all content types for a given site
	 * @param site - the project ID
	 * @param searchable - include non-searchable types (true/false)
	 */
	static getContentTypes(context, site, searchable) {
        def contentTypeServiceImpl = ServiceFactory.getContentTypeServices(context)
        return contentTypeServiceImpl.getContentTypes(site, searchable)
	}

	/**
	 * get allowed content types for a given path
	 * @param site - the project ID
	 * @param path - the path
	 */
	static getAllowedContentTypesForPath(context, site, path){
		def contentTypeServiceImpl = ServiceFactory.getContentTypeServices(context)
		return contentTypeServiceImpl.getAllowedContentTypesForPath(site, path)
	}

	/**
	 * Get a content type definition
	 * @param site - the Project ID
	 * @param type - the content type
	 */
	def getContentType(site, type) {

	}

    def static getContentTypeByPath(context, site, path) {
        def contentTypeServiceImpl = ServiceFactory.getContentTypeServices(context)
        return contentTypeServiceImpl.getContentTypeByPath(site, path)
    }
}
