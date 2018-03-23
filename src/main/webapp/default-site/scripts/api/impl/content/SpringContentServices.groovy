package scripts.api.impl.content

import scripts.api.impl.search.SolrSearch;

/**
 * content services
 */
class SpringContentServices {

	static CONTENT_SERVICES_BEAN = "cstudioContentService"
	static ASSET_PROCESSING_SERVICE_BEAN = "studioAssetProcessingService"

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
	 * Write asset
	 * @param site - the project ID
	 * @param path - the path to wrtie the content
	 * @param content - the content to write
	 */
	def writeAsset(site, path, content){
		throw new Exception("NOT USED")
	}

	/**
	 * get the actual content at a given path
	 * @param site - the project ID
	 * @param path - the path of the content to get
	 */
	def getContent(site, path) {
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.getContentAsString(site, path)
	}

	/**
	 * get the actual content at a given path
	 * @param site - the project ID
	 * @param path - the path of the content to get
	 */
	def getContentAsStream(site, path) {
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.getContent(site, path)
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

	def getContentAtPath(site, path) {
		def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
		return springBackedService.getContent(site, path)
	}

	def lockContent(site, path) {
		def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN);
		springBackedService.lockContent(site, path);
	}

	def writeContentAsset(site, path, fileName, content, isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset) {
		def springBackendService = this.context.applicationContext.get(ASSET_PROCESSING_SERVICE_BEAN);
		return springBackendService.processAsset(site, path, fileName, content, isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset);
	}

    def reorderItems(site, path, before, after) {
        def springBackendService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN);
        return springBackendService.reorderItems(site, path, before, after, "default");
    }

    /**
     * rename folder
     * @param site - the project ID
     * @param path - the folder path to rename
     * @param name - the new folder name
     */
    def renameFolder(site, path, name){
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.renameFolder(site, path, name)
    }

    def pushToRemote(siteId, remoteName, remoteBranch) {
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.pushToRemote(siteId, remoteName, remoteBranch)
    }

    def pullFromRemote(siteId, remoteName, remoteBranch) {
        def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
        return springBackedService.pullFromRemote(siteId, remoteName, remoteBranch)
    }
}
