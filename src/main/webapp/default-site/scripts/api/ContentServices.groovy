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
	 * Write asset
	 * @param site - the project ID
	 * @param path - the path to wrtie the content
	 * @param content - the content to write
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static writeAsset(site, path, content, context){
		throw new Exception("NOT USED")
	}

	/**
	 * get the actual content at a given path
	 * @param site - the project ID
	 * @param path - the path of the content to get
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static getContent(site, path, edit, encoding, context) {
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		if (edit) {
			contentServicesImpl.lockContent(site, path);
		}
		return contentServicesImpl.getContent(site, path, encoding)
	}

	/**
	 * get the actual content at a given path
	 * @param site - the project ID
	 * @param path - the path of the content to get
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static getContentAsStream(site, path, context) {
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.getContentAsStream(site, path)
	}

	/**
	 * get the actual content at a given path
	 * @param site - the project ID
	 * @param path - the path of the content to get
	 * @oaran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static getContentAtPath(context, site, path) {
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.getContentAtPath(site, path)
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
	static getItemOrders(context, site, path) {
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.getItemOrders(site, path)
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
	 * Get the content for a specific version
	 * @param site - the project ID
	 * @param path - the path of the item to "revert"
	 * @param version - old version ID to base to version on
	 * @paran context - container for passing request, token and other values that may be needed by the implementation
	 */
	static getContentVersionAtPath(site, path, version, context){
		def contentServicesImpl = ServiceFactory.getContentServices(context)
		return contentServicesImpl.getContentVersionAtPath(site, path, version) 		
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

	static getPages(context, site, path, depth, order, checkChildren = true) {
		def contentServicesImpl = ServiceFactory.getContentServices(context);
		return contentServicesImpl.getContentItemTree(site, path, depth);//.getPages(site, path, depth, order, checkChildren)
	}

	static writeContentAsset(context, site, path, fileName, content, isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset) {
		def contentServicesImpl = ServiceFactory.getContentServices(context);
		return contentServicesImpl.writeContentAsset(site, path, fileName, content, isImage, allowedWidth, allowedHeight, allowLessSize, draft, unlock, systemAsset)
	}

	static reorderItems(context, site, path, before, after) {
        def contentServicesImpl = ServiceFactory.getContentServices(context);
        return contentServicesImpl.reorderItems(site, path, before, after);
    }

    /**
     * rename a folder
     * @param site - the project ID
     * @param path - the folder path to rename
     * @param name - the new folder name
     * @oaran context - container for passing request, token and other values that may be needed by the implementation
     */
    static renameFolder(site, path, name, context){
        def contentServicesImpl = ServiceFactory.getContentServices(context)
        return contentServicesImpl.renameFolder(site, path, name)
    }

    /**
     * Push content to remote
     * @param context container for passing request, token and other values that may be needed by the implementation
     * @param siteId site identifier
     * @param remoteName remote name
     * @param remoteBranch remote branch to push to
     * @return
     */
    static pushToRemote(context, siteId, remoteName, remoteBranch) {
        def contentServiceImpl = ServiceFactory.getContentServices(context)
        return contentServiceImpl.pushToRemote(siteId, remoteName, remoteBranch)
    }

    /**
     * Pull content from remote
     * @param context container for passing request, token and other values that may be needed by the implementation
     * @param siteId site identifier
     * @param remoteName remote name
     * @param remoteBranch remote branch to pull from
     * @return
     */
    static pullFromRemote(context, siteId, remoteName, remoteBranch) {
        def contentServiceImpl = ServiceFactory.getContentServices(context)
        return contentServiceImpl.pullFromRemote(siteId, remoteName, remoteBranch)
    }
}	
