package org.craftercms.studio.api.v1.service.search;

import org.craftercms.studio.api.v1.exception.ServiceException;

/**
 * Created by Sumer Jabri on 2/15/17.
 */
public interface SearchService {

	/**
	 * Create a new index (core) in Crafter Search for a site
	 *
	 * @param siteId the Site ID for the site to build the index for
	 * @throws ServiceException
	 */
	void createIndex(String siteId) throws ServiceException;
}