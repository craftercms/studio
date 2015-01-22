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
	 * @param path - the path to write the content
	 * @param content - the content to write
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static writeContent(site, path, content, context){
        def contentServicesImpl = ServiceFactory.getContentServices(context)
        return contentServicesImpl.writeContent(site, path, content)
	}

    /**
     * create a folder
     * @param site - the project ID
     * @param path - the path to create the folder in
     * @param name - the folder name to create
     * @oaran context - container for passing request, token and other values that may be needed by the implementation
     */
    static createFolder(site, path, name, context){
        def contentServicesImpl = ServiceFactory.getContentServices(context)
        return contentServicesImpl.createFolder(site, path, name)
    }

    /**
     * copy content from PathA to pathB
     *
     * @param site - the project ID
     * @param fromPath paths to content
     * @param toPath target path
     * @oaran context - container for passing request, token and other values that may be needed by the implementation
     */
    static copyContent(site, fromPath, toPath, context){
        def contentServicesImpl = ServiceFactory.getContentServices(context)
        return contentServicesImpl.copyContent(site, fromPath, toPath)
    }

    /**
     * move content from PathA to pathB
     *
     * @param site - the project ID
     * @param fromPath paths to content
     * @param toPath target path
     * @oaran context - container for passing request, token and other values that may be needed by the implementation
     */
    static moveContent(site, fromPath, toPath, context){
        def contentServicesImpl = ServiceFactory.getContentServices(context)
        return contentServicesImpl.moveContent(site, fromPath, toPath)
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
        def contentServicesImpl = ServiceFactory.getContentServices(context)
        return contentServicesImpl.deleteContent(site, path)
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
	 * get the actual content at a given path
	 * @param site - the project ID
	 * @param path - the path of the content to get
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static getContentAtPath(context, path) {
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.getContentAtPath(path)
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
	static getContentItemTree(site, path, depth, context){
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.getContentItemTree(site, path, depth) 
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
	static revertContentItem(site, path, version, major, comment, context){
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.revertContentItem(site, path, version, major, comment) 		
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

	static writeContentAndRename(context, site, oldPath, targetPath, fileName, contentType, input, createFolders, edit, unlock, createFolder) {
		def contentServicesImpl = ServiceFactory.getContentServices(context);
		return contentServicesImpl.writeContentAndRename(site, oldPath, targetPath, fileName, contentType, input, createFolders, edit, unlock, createFolder)
	}

	static writeContent(context, site, path, fileName, contentType, input, createFolders, edit, unlock) {
		def contentServicesImpl = ServiceFactory.getContentServices(context);
		return contentServicesImpl.writeContent(site, path, fileName, contentType, input, createFolders, edit, unlock)
	}

	static getContentType(context, site, type) {
		def contentTypeServicesImpl = ServiceFactory.getContentTypeServices(context);
		return contentTypeServicesImpl.getContentType(site, type)
	}
}	
