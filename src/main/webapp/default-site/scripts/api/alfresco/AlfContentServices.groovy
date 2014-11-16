package scripts.api.alfresco

/**
 * content services
 */
class AlfContentServices {

	static SERVER_PROPERTIES_BEAN_NAME = "studio.crafter.properties"
	static ALFRESCO_URL_PROPERTY = "alfrescoUrl"		

	static getAlfrescoUrl() {
		def propertiesMap = null //applicationContext.get(SERVER_PROPERTIES_BEAN_NAME)
		def alfrescoUrl = "http://127.0.0.1:8080/alfresco"; //propertiesMap[ALFRESCO_URL_PROPERTY]
		return alfrescoUrl;
	}

	static getAlfrescoTicket() {
		return "TICKET_9aaf3ad2ded3a4b038ab4838f8b4d0f1364b7dd1";
	}

	/**
	 * Write content
	 * @param site - the project ID
	 * @param path - the path to wrtie the content
	 * @param content - the content to write
	 */
	def writeContent(site, path, content){

	}

	/**
	 * Write asset
	 * @param site - the project ID
	 * @param path - the path to wrtie the content
	 * @param content - the content to write
	 */
	def writeAsset(site, path, content){

	}

	/**
	 * delete a content item
	 * @param site - the projectId
	 * @param path - the path to delete
	 */
	def deleteContent(site, path) {

	}

	/**
	 * get the actual content at a given path
	 * @param site - the project ID
	 * @param path - the path of the content to get
	 */
	static getContent(site, path) {

		def alfServiceApiUrl = getAlfrescoUrl() +
			"/service/cstudio/wcm/content/get-content" +
			"?site=" + site +
			"&path=" + path +
			"&alf_ticket=" + getAlfrescoTicket();

		def response = (alfServiceApiUrl).toURL().getText();

		return response
	}

  	/**
  	 * check if content at path exits
  	 * @param site - the project ID
  	 * @param path - the path to check
  	 */
	def doesContentItemExist(site, path) {

	}

	/**
	 * get the tree of content items (metadata) beginning at a root
	 * @param site - the project ID
	 * @param rootPath - the path to root at
	 */
	def getContentItemTree(site, rootPath){

	}

	/**
	 * get the content item (metadata) at a specific path
	 * @param site - the project ID
	 * @param path - the path of the content item
	 */
	def getContentItem(site, path) {

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
	def getContentItemOrders(site, path) {

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
	 * lock a given item
	 * @param site - the project ID
	 * @param path - the path of the item to lock
	 */
	def lockContentItem(site, path) {

	}

	/** 
	 * unlock a given item
	 * @param site - the project ID
	 * @param path - the path of the item to unlock
	 */
	def unlockContentItem(site, path) {

	}

	/** 
	 * get the version history for an item
	 * @param site - the project ID
	 * @param path - the path of the item 
	 */
	def getContentItemVersionHistory(site, path) {

	}

	/** 
	 * revert a version (create a new version based on an old version)
	 * @param site - the project ID
	 * @param path - the path of the item to "revert"
	 * @param version - old version ID to base to version on
	 */
	def revertContentItem(site, path, version){
		
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
	def search(site, keywords, filters, sort, page, resultsPerPage) {
	}
}	
