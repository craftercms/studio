package org.craftercms.studio.api.v1.service.search;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;

/**
 * Created by Sumer Jabri on 2/15/17.
 */
public interface SearchService {

	/**
	 * Create a new index (core) in Crafter Search for a site
	 *
	 * @param siteId the Site ID for the site to build the index for
	 * @throws ServiceLayerException
	 */
	void createIndex(String siteId) throws ServiceLayerException;

	/**
	 * Delete a search index (core) in Crafter Search for a site
	 *
	 * @param siteId the Site ID for the site to delete the index for
	 * @throws ServiceLayerException
	 */
	public void deleteIndex(final String siteId) throws ServiceLayerException;
}