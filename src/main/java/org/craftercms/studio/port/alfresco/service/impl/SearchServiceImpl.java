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
package org.craftercms.cstudio.alfresco.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.ModelService;
import org.craftercms.cstudio.alfresco.service.api.NamespaceService;
import org.craftercms.cstudio.alfresco.service.api.SearchService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException;
import org.craftercms.cstudio.alfresco.service.exception.InvalidTypeException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.service.exception.SiteNotFoundException;
import org.craftercms.cstudio.alfresco.to.SearchConfigTO;
import org.craftercms.cstudio.alfresco.to.SearchCriteriaTO;
import org.craftercms.cstudio.alfresco.to.SearchResultTO;
import org.craftercms.cstudio.alfresco.util.SearchUtils;
import org.craftercms.cstudio.alfresco.util.api.SearchQueryBuilder;
import org.craftercms.cstudio.alfresco.util.api.SearchResultExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchServiceImpl extends AbstractRegistrableService implements SearchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchServiceImpl.class);

	/**
	 * a map of query language and query builders
	 */
	protected Map<String, SearchQueryBuilder> _queryBuilders;
 	
	/**
	 * search result extractor
	 */
	protected SearchResultExtractor _searchResultExtractor;
	
	/**
	 * contains the query language is going to be used to search information.
	 */
	protected String _queryLanguage = DEFAULT_QUERY_LANGUAGE;

    @Override
    public void register() {
        getServicesManager().registerService(SearchService.class, this);
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * org.craftercms.cstudio.alfresco.service.api.SearchService#findNode(java.lang
      * .String)
      */
	public NodeRef findNode(StoreRef store, String query) {
		// try {
		// 	NodeRef resultRef = null;
  //           org.alfresco.service.cmr.search.SearchService alfrescoSearchService = getService(org.alfresco.service.cmr.search.SearchService.class);
		// 	ResultSet results = alfrescoSearchService.query(store,
		// 			this._queryLanguage, query);
		// 	int length = results.length();
		// 	if (length > 0) {
		// 		resultRef = results.getNodeRef(0);
		// 	}
		// 	results.close();
		// 	return resultRef;
		// } catch (SearcherException e) {
		// 	if (LOGGER.isErrorEnabled()) {
		// 		LOGGER.error("Failed to search by query: " + query, e);
		// 	}
		// 	return null;
		// }
//PORT
return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.craftercms.cstudio.alfresco.service.api.SearchService#findNode(java.lang
	 * .String)
	 */
	public List<NodeRef> findNodes(StoreRef store, String query) {
//PORT
return null;

		// try {
  //           org.alfresco.service.cmr.search.SearchService alfrescoSearchService = getService(org.alfresco.service.cmr.search.SearchService.class);
		// 	ResultSet results = alfrescoSearchService.query(store, this._queryLanguage, query);
		// 	List<NodeRef> nodeRefs = results.getNodeRefs();
		// 	results.close();
		// 	return nodeRefs;
		// } catch (SearcherException e) {
		// 	if (LOGGER.isErrorEnabled()) {
		// 		LOGGER.error("Failed to search by query: " + query, e);
		// 	}
		// 	return null;
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.craftercms.cstudio.alfresco.service.api.SearchService#findNodeFromPath(
	 * org.alfresco.service.cmr.repository.StoreRef, java.lang.String,
	 * java.lang.String)
	 */
	public NodeRef findNodeFromPath(StoreRef store, String path, String name, boolean direct) {
//PORT
		return null;

		// try {
		// 	if (store != null) {
  //               NamespaceService namespaceService = getService(NamespaceService.class);
		// 		return findNode(store, SearchUtils.createPathQuery(path, direct) + " AND " 
		// 				+ SearchUtils.createTextQueryByValue(
		// 						ContentModel.PROP_NAME, name, this._queryLanguage, namespaceService));
		// 	} else {
		// 		return null;
		// 	}
		// } catch (SearcherException e) {
		// 	if (LOGGER.isErrorEnabled()) {
		// 		LOGGER.error("Failed to search by path: " + path + " and name: " + name, e);
		// 	}
		// 	return null;
		// }
	}

	public NodeRef findNodeFromWCMId(StoreRef store, Long wcmId){
//PORT
		return null;

		// if (store != null){
  //           NamespaceService namespaceService = getService(NamespaceService.class);
		// 	String query = SearchUtils.createNumericQueryByValue(CStudioContentModel.PROP_WCM_IDENTIFIABLE_ID, wcmId, this._queryLanguage, namespaceService);
		// 	return findNode(store, query);
		// }
		// else
		// 	return null;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.craftercms.cstudio.alfresco.service.api.SearchService#getSearchTemplate
	 * (java.lang.String)
	 */
	public Document getSearchTemplate(String site) throws SiteNotFoundException, InvalidTypeException, ContentNotFoundException {
//PORT
		return null;

  //       ServicesConfig servicesConfig = getService(ServicesConfig.class);
		// QName type = servicesConfig.getContentType(site);
		// if (type != null) {
  //           ModelService modelService = getService(ModelService.class);
  //           NamespaceService namespaceService = getService(NamespaceService.class);
		// 	return modelService.getModelTemplate(site, namespaceService.getPrefixedTypeName(type), true, true);
		// } else {
		// 	throw new SiteNotFoundException("Content type is not found for site: " + site);
		// }
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.SearchService#search(org.craftercms.cstudio.alfresco.to.SearchCriteriaTO, int, int)
	 */
	public SearchResultTO search(SearchCriteriaTO criteria, int page, int pageSize) {
//PORT
		return null;
		// SearchQueryBuilder builder = _queryBuilders.get(getQueryLanguage());
		// SearchResultTO searchResult = null;
		// if (pageSize <= 0) {
		// 	searchResult = createFailedSearchResult("Page Size cannot be zero or less.  Please set a positive number");
		// } else {
		// 	try {
		// 		if (builder != null) {
		// 			searchResult = executeSearchInternal(criteria, builder, page, pageSize);
		// 			searchResult.setSearchFailed(false);
		// 			// set the total number of pages
		// 			int pages = searchResult.getTotal() / pageSize;
		// 			if (searchResult.getTotal() % pageSize != 0) pages++;
		// 			searchResult.setTotalPages(pages);
		// 			searchResult.setNumOfItems(pageSize);
		// 		} else {
		// 			searchResult = createFailedSearchResult("No search query builder specified for " + getQueryLanguage()); 
		// 		}
		// 	} catch (ServiceException e) {
		// 		searchResult = createFailedSearchResult(e.getMessage()); 
		// 	}
		// }
		// searchResult.setKeyword(criteria.getKeyword());
		// searchResult.setSort(criteria.getSort());
		// return searchResult;
	}

	/*
	 * (non-Javadoc)
	 * @see org.craftercms.cstudio.alfresco.service.api.SearchService#query(org.alfresco.service.cmr.search.SearchParameters)
	 */
	public ResultSet query(SearchParameters sp) {
//PORT
		return null;
  //       org.alfresco.service.cmr.search.SearchService alfrescoSearchService = getService(org.alfresco.service.cmr.search.SearchService.class);
		// return alfrescoSearchService.query(sp);
	}


	/**
	 * create a failed search result with the given cause
	 * 
	 * @param cause
	 * @return search result
	 */
	protected SearchResultTO createFailedSearchResult(String cause) {
// PORT
		return null;
		// SearchResultTO searchResult = new SearchResultTO();
		// searchResult.setSearchFailed(true);
		// searchResult.setFailCause(cause);
		// searchResult.setTotal(0);
		// searchResult.setTotalPages(0);
		// searchResult.setNumOfItems(0);
		// return searchResult;
	}

	/**
	 * get query langauge 
	 * 
	 * @return query langauge
	 */
	/*protected String getQueryLanguage() {
		return SearchService.LANGUAGE_LUCENE;
	}*/
	
	/**
	 * search repository with the given search criteria 
	 * 
	 * @param criteria
	 * @param builder
	 * @param page 
	 * @param pageSize 
	 * @return search result items
	 * @throws ServiceException
	 */
	@SuppressWarnings("unchecked")
	protected SearchResultTO executeSearchInternal(SearchCriteriaTO criteria, SearchQueryBuilder builder, int page, int pageSize) throws ServiceException {
// PORT
		return null;
		// SearchResultTO result = new SearchResultTO();
		// SearchParameters parameters = new SearchParameters();
		// parameters.addStore(getStore(criteria.getSite()));
		// parameters.setLanguage(this.getQueryLanguage());
		// // set search query
		// String query = builder.createQuery(criteria);
		// parameters.setQuery(query);
		// // set the maximum number of search items to get
  //       ServicesConfig servicesConfig = getService(ServicesConfig.class);
		// SearchConfigTO config = servicesConfig.getDefaultSearchConfig(criteria.getSite());
		// //parameters.setLimit(config.getMaxCount());
		// //parameters.setLimitBy(LimitBy.FINAL_SIZE);
		// // set sorting
		// if (!StringUtils.isEmpty(criteria.getSort())) {
		// 	String[] sortTokens = criteria.getSort().split(",");
		// 	for (String token : sortTokens) {
		// 		token = token.trim();
		// 		if (!StringUtils.isEmpty(token)) {
		// 			parameters.addSort("@" + token, criteria.isAscending());
		// 		}
		// 	}
		// 	//parameters.addSort("@" + criteria.getSort(), criteria.isAscending());
		// 	/*
		// 	String dbg = "";
		// 	for (SearchParameters.SortDefinition ssd : parameters.getSortDefinitions()) {
		// 		dbg = dbg + "[" + ssd.getField() + ", " + ssd.getSortType() + "]";
		// 	}
		// 	LOGGER.debug("TANBUG: [" + criteria.getSort() + "] Search issued with query: " + parameters.getQuery() + ", sort: " + dbg);
		// 	*/
		// }
		// if (LOGGER.isDebugEnabled()) {
		// 	LOGGER.debug("Search issued with query: " + parameters.getQuery() + ", sort: " + parameters.getSortDefinitions());
		// }
		// // execute search
		// long startTime = System.currentTimeMillis();
  //       org.alfresco.service.cmr.search.SearchService alfrescoSearchService = getService(org.alfresco.service.cmr.search.SearchService.class);
		// ResultSet results = alfrescoSearchService.query(parameters);
		// if (LOGGER.isDebugEnabled()) {
		// 	LOGGER.debug("Search time: " + (System.currentTimeMillis() - startTime));
		// }
		// List<NodeRef> nodeRefs = results.getNodeRefs();
		// List<NodeRef> limitedResultSet = null;
		// results.close();
		// // blocked for Debug purpose only
		// /*
		// if (nodeRefs != null && nodeRefs.size() > config.getMaxCount()) {
		// 	limitedResultSet = nodeRefs.subList(0, config.getMaxCount());
		// } else {
		// 	//limitedResultSet = results.getNodeRefs();
		// 	limitedResultSet = nodeRefs;
		// }
		// */
		// limitedResultSet = nodeRefs; //results.getNodeRefs();
 	// 	if (LOGGER.isDebugEnabled()) {
		// 	LOGGER.debug("Search done. extracting result of " + ((limitedResultSet != null) ? limitedResultSet.size() : 0));
		// }
		// // extract search result items
		// List<Serializable> items = _searchResultExtractor.extract(criteria.getSite(), limitedResultSet, criteria.getColumns(), 
		// 		criteria.getSort(), criteria.isAscending(), page, pageSize);
		// result.setItems(items);
		// result.setTotal((limitedResultSet != null) ? limitedResultSet.size() : 0);
		// return result;
	}

	/**
	 * get store to search in
	 * 
	 * @return storeRef
	 */
	protected StoreRef getStore(String site) {
//PORT
return null;
//		return CStudioConstants.STORE_REF;
	}

	/**
	 * @param queryBuilders the queryBuilders to set
	 */
	public void setQueryBuilders(Map<String, SearchQueryBuilder> queryBuilders) {
//PORT
return null;
		// this._queryBuilders = queryBuilders;
	}

	/**
	 * @param searchResultExtractor the searchResultExtractor to set
	 */
	public void setSearchResultExtractor(SearchResultExtractor searchResultExtractor) {
//PORT
return null;
//		this._searchResultExtractor = searchResultExtractor;
	}
	
	public void setQueryLanguage(String queryLanguage) {
		this._queryLanguage = queryLanguage;
	}
	
	public String getQueryLanguage() {
		return this._queryLanguage;
	}

}
