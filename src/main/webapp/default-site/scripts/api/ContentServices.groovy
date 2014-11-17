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
	 * Write content
	 * @param site - the project ID
	 * @param path - the path to wrtie the content
	 * @param content - the content to write
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static writeContent(site, path, content, context){

	}

	/**
	 * Write asset
	 * @param site - the project ID
	 * @param path - the path to wrtie the content
	 * @param content - the content to write
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static writeAsset(site, path, content, context){

	}

	/**
	 * delete a content item
	 * @param site - the projectId
	 * @param path - the path to delete
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static deleteContent(site, path, context) {

	}

	/**
	 * get the actual content at a given path
	 * @param site - the project ID
	 * @param path - the path of the content to get
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static getContent(site, path, context) {
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.getContent(site, path)
	}

  	/**
  	 * check if content at path exits
  	 * @param site - the project ID
  	 * @param path - the path to check
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
  	 */
	static doesContentItemExist(site, path, context) {
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.doesContentItemExist(site, path) 
	}

	/**
	 * get the tree of content items (metadata) beginning at a root
	 * @param site - the project ID
	 * @param rootPath - the path to root at
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static getContentItemTree(site, path, context){
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.getContentItemTree(site, path) 
	}

	/**
	 * get the content item (metadata) at a specific path
	 * @param site - the project ID
	 * @param path - the path of the content item
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static getContentItem(site, path, context) {
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.getContentItem(site, path) 
	}

	/**
	 * get all the content dependencies for a given path
	 * @param site - the project ID
	 * @param path - the path of the item
	 * @param filters - filters to apply to the dependencies
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static getContentDependencies(site, path, filters, context) {

	}

	/**
	 * get content orders for a given path (usually used for navigation)
  	 * @param site - the project ID
  	 * @param path - the parent path containing the ordered objects
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
  	 */
	static getContentItemOrders(site, path, context) {

	}

	/**
	 * Get the next value in the sequence for an order at a given path
	 * @param site - the project ID
	 * @param path - the path of the parent
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static getNextOrderInSequence(site, path, context) {

	}

	/**
	 * set the order of a group of items
	 * @param site - the project ID
	 * @param path - the list of paths for the items
	 * @param orders - the orders object
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static orderItems(site, path, orders, context) {

	}

	//  Currently not supporting this as edit operations will lock for the caller autormaticall
	//	/** 
	//	 * lock a given item
	//	 * @param site - the project ID
	//	 * @param path - the path of the item to lock
	//	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	//	 */
	//	static lockContentItem(site, path, context) {
	//	}

	/** 
	 * unlock a given item
	 * @param site - the project ID
	 * @param path - the path of the item to unlock
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static unlockContentItem(site, path, context) {
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.unlockContentItem(site, path) 
	}

	/** 
	 * get the version history for an item
	 * @param site - the project ID
	 * @param path - the path of the item 
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static getContentItemVersionHistory(site, path, context) {
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.getContentItemVersionHistory(site, path) 
	}

	/** 
	 * revert a version (create a new version based on an old version)
	 * @param site - the project ID
	 * @param path - the path of the item to "revert"
	 * @param version - old version ID to base to version on
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static revertContentItem(site, path, version, context){
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.revertContentItem(site, path, version) 		
	}

	/** 
	 * search the repository
	 * @param site - the project ID	 
	 * @param keywords - keywords
	 * @param filters - Filters object (document based)
	 * @param sort - sort object
	 * @param page - page to start on
	 * @param resultsPerPage - items to return per page
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static search(site, keywords, searchParams, sort, page, resultsPerPage, context) {
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.search(site, keywords, searchParams, sort, page, resultsPerPage)
	}
}	
