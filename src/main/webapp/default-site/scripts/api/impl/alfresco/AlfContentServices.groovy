package scripts.api.impl.alfresco

import groovy.json.JsonSlurper;
import scripts.api.impl.subsystems.search.SolrSearch;

/**
 * content services
 */
class AlfContentServices {

	static SERVER_PROPERTIES_BEAN_NAME = "studio.crafter.properties"
	static ALFRESCO_URL_PROPERTY = "alfrescoUrl"

	def context = null		
	
	def AlfContentServices(context) {
		this.context = context
	}

	def getAlfrescoUrl() {
		def propertiesMap = this.context.applicationContext.get(SERVER_PROPERTIES_BEAN_NAME)
		def alfrescoUrl = propertiesMap[ALFRESCO_URL_PROPERTY]
		return alfrescoUrl
	}

	def getAlfrescoTicket() {
		return this.context.token
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
	def getContent(site, path) {

		def alfServiceApiUrl = getAlfrescoUrl() +
			"/service/cstudio/wcm/content/get-content" +
			"?site=" + site +
			"&path=" + path +
			"&alf_ticket=" + getAlfrescoTicket()

		def response = (alfServiceApiUrl).toURL().getText()

		return response
	}

  	/**
  	 * check if content at path exits
  	 * @param site - the project ID
  	 * @param path - the path to check
  	 */
	def doesContentItemExist(site, path) {

		def alfServiceApiUrl = getAlfrescoUrl() +
			"/service/cstudio/wcm/content/content-exists" +
			"?site=" + site +
			"&path=" + path +
			"&alf_ticket=" + getAlfrescoTicket()

		def response = (alfServiceApiUrl).toURL().getText()
 
 		def result = new JsonSlurper().parseText( response )
 
		return result
	}

	/**
	 * get the tree of content items (metadata) beginning at a root
	 * @param site - the project ID
	 * @param rootPath - the path to root at
	 */
	def getContentItemTree(site, path){

		def alfServiceApiUrl = getAlfrescoUrl() +
			"/service/cstudio/wcm/content/get-pages" +
			"?site=" + site +
			"&path=" + path +
			"&depth=1" + 
			"&order=default" +
			"&alf_ticket=" + getAlfrescoTicket()

		def response = (alfServiceApiUrl).toURL().getText()
 
 		def result = new JsonSlurper().parseText( response )
 
		return result
	}

	/**
	 * get the content item (metadata) at a specific path
	 * @param site - the project ID
	 * @param path - the path of the content item
	 */
	def getContentItem(site, path) {
		def alfServiceApiUrl = getAlfrescoUrl() +
			"/service/cstudio/wcm/content/get-item" +
			"?site=" + site +
			"&path=" + path +
			"&populateDependencies=false" +
			"&alf_ticket=" + getAlfrescoTicket()

		def response = (alfServiceApiUrl).toURL().getText()
 
 		def result = new JsonSlurper().parseText( response )
 
		return result
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
	 * unlock a given item
	 * @param site - the project ID
	 * @param path - the path of the item to unlock
	 */
	def unlockContentItem(site, path) {

		def alfServiceApiUrl = getAlfrescoUrl() +
			"/service/cstudio/wcm/workflow/unlockItem" +
			"?site=" + site +
			"&path=" + path +
			"&alf_ticket=" + getAlfrescoTicket()

		def response = (alfServiceApiUrl).toURL().getText()
 
 		def result = new JsonSlurper().parseText( response )
 
		return result
	}

	/** 
	 * get the version history for an item
	 * @param site - the project ID
	 * @param path - the path of the item 
	 */
	def getContentItemVersionHistory(site, path) {

		def alfServiceApiUrl = getAlfrescoUrl() +
			"/service/cstudio/wcm/version/get-history" +
			"?site=" + site +
			"&path=" + path +
			"&maxhistory=100" +
			"&alf_ticket=" + getAlfrescoTicket()

		def response = (alfServiceApiUrl).toURL().getText()
 
 		def result = new JsonSlurper().parseText( response )
 
		return result
	}

	/** 
	 * revert a version (create a new version based on an old version)
	 * @param site - the project ID
	 * @param path - the path of the item to "revert"
	 * @param version - old version ID to base to version on
	 */
	def revertContentItem(site, path, version){
		def alfServiceApiUrl = getAlfrescoUrl() +
			"/service/cstudio/wcm/version/revert" +
			"?site=" + site +
			"&path=" + path +
			"&version=" + version +
			"&alf_ticket=" + getAlfrescoTicket()

		def response = (alfServiceApiUrl).toURL().getText()
 
 		def result = new JsonSlurper().parseText( response )

		return result		
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
}	
