package scripts.api.impl.content

import scripts.api.impl.search.SolrSearch;

/**
 * content services
 */
class SpringContentServices {

	static CONTENT_SERVICES_BEAN = "cstudioContentService"

	def context = null		
	
	def SpringContentServices(context) {
		this.context = context
	}

	/**
	 * Write content
	 * @param site - the project ID
	 * @param path - the path to wrtie the content
	 * @param content - the content to write
	 */
	def writeContent(site, path, content){
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.writeContent(site, path, content)
	}

    /**
     * create folder
     * @param site - the project ID
     * @param path - the path to create the folder in
     * @param name - the folder name to create
     */
    def createFolder(site, path, name){
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.createFolder(site, path, name)
    }

    /**
     * copy content from PathA to pathB
     *
     * @param site - the project ID
     * @param fromPath paths to content
     * @param toPath target path
     */
    def copyContent(site, fromPath, toPath){
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.copyContent(site, fromPath, toPath)
    }

    /**
     * move content from PathA to pathB
     *
     * @param site - the project ID
     * @param fromPath paths to content
     * @param toPath target path
     */
    def moveContent(site, fromPath, toPath){
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.moveContent(site, fromPath, toPath)
    }

    /**
	 * Write asset
	 * @param site - the project ID
	 * @param path - the path to wrtie the content
	 * @param content - the content to write
	 */
	def writeAsset(site, path, content){
		throw new Exception("NOT USED")
	}

	/**
	 * delete a content item
     *
	 * @param site - the projectId
	 * @param path - the path to delete
	 */
	def deleteContent(site, path) {
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.deleteContent(site, path)
	}

	/**
	 * get the actual content at a given path
	 * @param site - the project ID
	 * @param path - the path of the content to get
	 */
	def getContent(site, path) { 
		def contentPath = "/wem-projects/" + site + "/" + site + "/work-area" + path
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.getContentAsString(contentPath)
	}

	/**
	 * get the actual content at a given path
	 * @param site - the project ID
	 * @param path - the path of the content to get
	 */
	def getContentAsStream(site, path) { 
		def contentPath = "/wem-projects/" + site + "/" + site + "/work-area" + path
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.getContent(contentPath)
	}

  	/**
  	 * check if content at path exits
  	 * @param site - the project ID
  	 * @param path - the path to check
  	 */
	def doesContentItemExist(site, path) {
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.contentExists(site, path)
	}

	/**
	 * get the tree of content items (metadata) beginning at a root
	 * @param site - the project ID
	 * @param rootPath - the path to root at
	 */
	def getContentItemTree(site, path, depth){
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.getContentItemTree(site, path, depth)
	}

	/**
	 * get the content item (metadata) at a specific path
	 * @param site - the project ID
	 * @param path - the path of the content item
	 */
	def getContentItem(site, path) {
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.getContentItem(site, path, 0)
	}

	/**
	 * get all the content dependencies for a given path
	 * @param site - the project ID
	 * @param path - the path of the item
	 * @param filters - filters to apply to the dependencies
	 */
	def getContentDependencies(site, path, filters) {

	}

	/**
	 * get content orders for a given path (usually used for navigation)
  	 * @param site - the project ID
  	 * @param path - the parent path containing the ordered objects
  	 */
	def getItemOrders(site, path) {
		def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
		return springBackedService.getItemOrders(site, path);
	}

	/**
	 * Get the next value in the sequence for an order at a given path
	 * @param site - the project ID
	 * @param path - the path of the parent
	 */
	def getNextOrderInSequence(site, path) {

	}

	/**
	 * set the order of a group of items
	 * @param site - the project ID
	 * @param path - the list of paths for the items
	 * @param orders - the orders object
	 */
	def orderItems(site, path, orders) {

	}

	/** 
	 * unlock a given item
	 * @param site - the project ID
	 * @param path - the path of the item to unlock
	 */
	def unlockContentItem(site, path) {
		def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
		return springBackedService.unLockContent(site, path)
	}

	/** 
	 * get the version history for an item
	 * @param site - the project ID
	 * @param path - the path of the item 
	 */
	def getContentItemVersionHistory(site, path) {
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.getContentItemVersionHistory(site, path)		
	}

	/** 
	 *  Get the content for a specific version
	 * @param site - the project ID
	 * @param path - the path of the item to retrieve
	 * @param version - old version ID to base to version on
	 */
	def getContentVersionAtPath(site, path, version) {
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.getContentVersionAsString(site, path, version)			
	}

	/** 
	 * revert a version (create a new version based on an old version)
	 * @param site - the project ID
	 * @param path - the path of the item to "revert"
	 * @param version - old version ID to base to version on
	 */
	def revertContentItem(site, path, version, major, comment){
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.revertContentItem(site, path, version, major, comment)			
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
	def search(site, keywords, searchParams, sort, page, resultsPerPage) {
		return SolrSearch.search(site, keywords, searchParams, sort, page, resultsPerPage, this.context);
	}

	def writeContent(site, path, fileName, contentType, input, createFolders, edit, unlock) {
		def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN);
		return springBackedService.writeContent(site, path, fileName, contentType, input, createFolders, edit, unlock);
	}

    def writeContentAndRename(site, oldPath, targetPath, fileName, contentType, input, createFolders, edit, unlock, createFolder) {
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN);
        springBackedService.writeContentAndRename(site, oldPath, targetPath, fileName, contentType, input, createFolders, edit, unlock, createFolder);
    }

	def getContentAtPath(path) {
		def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
		return springBackedService.getContent(path)
	}

	def lockContent(site, path) {
		def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN);
		springBackedService.lockContent(site, path);
	}

	def writeContentAsset(site, path, fileName, content, isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset) {
		def springBackendService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN);
		return springBackendService.writeContentAsset(site, path, fileName, content, isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset);
	}

    def reorderItems(site, path, before, after) {
        def springBackendService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN);
        return springBackendService.reorderItems(site, path, before, after, "default");
    }

    def bulkRename(site, srcPath, targetPath, createFolder) {
        def springBackendService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN);
        return springBackendService.bulkRename(site, srcPath, targetPath, createFolder);
    }
}