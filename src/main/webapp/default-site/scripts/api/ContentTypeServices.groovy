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
	def changeTemplate(site, item, template){

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
	def getAllowedContentTypesForPath(site, path){

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