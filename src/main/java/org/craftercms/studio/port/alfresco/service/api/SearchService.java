/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.alfresco.service.api;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.QName;
import org.dom4j.Document;

import org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException;
import org.craftercms.cstudio.alfresco.service.exception.InvalidTypeException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.service.exception.SiteNotFoundException;
import org.craftercms.cstudio.alfresco.to.SearchCriteriaTO;
import org.craftercms.cstudio.alfresco.to.SearchResultTO;

/**
 * This class provides access to Alfresco SearchService
 * 
 * @author hyanghee
 * 
 */
public interface SearchService {

	public static final String LANGUAGE_LUCENE = org.alfresco.service.cmr.search.SearchService.LANGUAGE_LUCENE;
	public static final String LANGUAGE_XPATH = org.alfresco.service.cmr.search.SearchService.LANGUAGE_XPATH;
	
	public static final String LANGUAGE_SOLR_FTS_ALFRESCO = org.alfresco.service.cmr.search.SearchService.LANGUAGE_SOLR_FTS_ALFRESCO;
	
	public static final String DEFAULT_QUERY_LANGUAGE = LANGUAGE_SOLR_FTS_ALFRESCO;
	
	/**
	 * find a nodeRef given the query from the store specified
	 * 
	 * @param store
	 *            StoreRef to find the node from
	 * @param query
	 *            lucene search query
	 * @return NodeRef if found. Otherwise it returns null
	 */
	public NodeRef findNode(StoreRef store, String query);

	/**
	 * find a nodeRef given the query from the store specified
	 * 
	 * @param store
	 *            StoreRef to find the node from
	 * @param query
	 *            lucene search query
	 * @return NodeRef if found. Otherwise it returns null
	 */
	public List<NodeRef> findNodes(StoreRef store, String query);

	/**
	 * find a nodeRef given the path to the content and the node name from the
	 * store specified
	 * 
	 * @param store
	 *            StoreRef to find the node from
	 * @param path
	 *            path to the target node
	 * @param name
	 *            node name
	 * @param direct
	 *            if true it only searches for a direct child. Otherwise, it
	 *            will search for all nodes at any depth and returns the first
	 *            matching node
	 * @return nodeRef if found. Otherwise it returns null
	 */
	public NodeRef findNodeFromPath(StoreRef store, String path, String name, boolean direct);

	/**
	 * Find a node from the WCM-id that is passed.
	 * 
	 * @param store
	 * 			StoreRef to find the node from	
	 * @param defaultWebApp
	 * @param wcmId
	 * 			the wcmId to search for	
	 * @return nodeRef if found. Otherwise it returns null
	 */
	public NodeRef findNodeFromWCMId(StoreRef store, Long wcmId);
	/**
	 * search content within the given site filter by the given lsit of filters
	 * 
	 * @param criteria
	 * 			search criteria
	 * @param pageSize 
	 * @param page 
	 * @return content items found
	 * @throws ServiceException
	 */
	public SearchResultTO search(SearchCriteriaTO criteria, int page, int pageSize) throws ServiceException;

	/**
	 * get search template for the given site
	 * 
	 * @param site
	 *            site name to search in
	 * @return search template in XML
	 * @throws SiteNotFoundException
	 * @throws ContentNotFoundException
	 * @throws InvalidTypeException
	 */
	public Document getSearchTemplate(String site) throws SiteNotFoundException, InvalidTypeException,
			ContentNotFoundException;

	/**
	 * search based on the search parameters given
	 * This method will invoke alfresco query method
	 * 
	 * @param sp
	 * @return SearchResult set
	 */
	public ResultSet query(SearchParameters sp);

}
