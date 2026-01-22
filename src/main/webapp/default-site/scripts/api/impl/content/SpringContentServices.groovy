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

package scripts.api.impl.content

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
	 * get the tree of content items (metadata) beginning at a root
	 * @param site - the project ID
	 * @param rootPath - the path to root at
	 */
	def getContentItemTree(site, path, depth) {
		def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
		return springBackedService.getContentItemTree(site, path, depth)
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
	 *  Get the content for a specific version
	 * @param site - the project ID
	 * @param path - the path of the item to retrieve
	 * @param version - old version ID to base to version on
	 */
	def getContentVersionAtPath(site, path, version) {
		def springBackedService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN)
		return springBackedService.getContentVersionAsString(site, path, version)
	}

	def reorderItems(site, path, before, after) {
		def springBackendService = this.context.applicationContext.get(CONTENT_SERVICES_BEAN);
		return springBackendService.reorderItems(site, path, before, after, "default");
	}
}
