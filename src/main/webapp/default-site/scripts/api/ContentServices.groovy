package scripts.api

import scripts.api.ServiceFactory

/**
 * content services
 */
class ContentServices {

	static createContext(applicationContext, request) {
		return ServiceFactory.createContext(applicationContext, request)
	}

	/**
	 * Write content
	 * @param site - the project ID
	 * @param path - the path to wrtie the content
	 * @param content - the content to write
	 */
	static writeContent(site, path, content){

	}

	/**
	 * Write asset
	 * @param site - the project ID
	 * @param path - the path to wrtie the content
	 * @param content - the content to write
	 */
	static writeAsset(site, path, content){

	}

	/**
	 * delete a content item
	 * @param site - the projectId
	 * @param path - the path to delete
	 */
	static deleteContent(site, path) {

	}

	/**
	 * get the actual content at a given path
	 * @param site - the project ID
	 * @param path - the path of the content to get
	 */
	static getContent(site, path, context) {
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.getContent(site, path)
	}

  	/**
  	 * check if content at path exits
  	 * @param site - the project ID
  	 * @param path - the path to check
  	 */
	static doesContentItemExist(site, path) {

	}

	/**
	 * get the tree of content items (metadata) beginning at a root
	 * @param site - the project ID
	 * @param rootPath - the path to root at
	 */
	static getContentItemTree(site, path, context){
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.getContentItemTree(site, path) 
	}

	/**
	 * get the content item (metadata) at a specific path
	 * @param site - the project ID
	 * @param path - the path of the content item
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
	 */
	static getContentDependencies(site, path, filters) {

	}

	/**
	 * get content orders for a given path (usually used for navigation)
  	 * @param site - the project ID
  	 * @param path - the parent path containing the ordered objects
  	 */
	static getContentItemOrders(site, path) {

	}

	/**
	 * Get the next value in the sequence for an order at a given path
	 * @param site - the project ID
	 * @param path - the path of the parent
	 */
	static getNextOrderInSequence(site, path) {

	}

	/**
	 * set the order of a group of items
	 * @param site - the project ID
	 * @param path - the list of paths for the items
	 * @param orders - the orders object
	 */
	static orderItems(site, path, orders) {

	}

	/** 
	 * lock a given item
	 * @param site - the project ID
	 * @param path - the path of the item to lock
	 */
	static lockContentItem(site, path) {

	}

	/** 
	 * unlock a given item
	 * @param site - the project ID
	 * @param path - the path of the item to unlock
	 */
	static unlockContentItem(site, path) {

	}

	/** 
	 * get the version history for an item
	 * @param site - the project ID
	 * @param path - the path of the item 
	 */
	static getContentItemVersionHistory(site, path) {

	}

	/** 
	 * revert a version (create a new version based on an old version)
	 * @param site - the project ID
	 * @param path - the path of the item to "revert"
	 * @param version - old version ID to base to version on
	 */
	static revertContentItem(site, path, version){
		
	}

	/** 
	 * search the repository
	 * @param site - the project ID	 
	 * @param keywords - keywords
	 * @param filters - Filters object (document based)
	 * @param sort - sort object
	 * @param page - page to start on
	 * @param resultsPerPage - items to return per page
	 */
	static search(site, keywords, filters, sort, page, resultsPerPage) {
	}
}	
