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
package org.craftercms.cstudio.alfresco.to;

import java.util.List;

/**
 * contains search criteria
 * 
 * @author hyanghee
 * 
 */
public class SearchCriteriaTO {

	/** site to search in **/
	protected String _site;
	/** content types to include in the search results **/
	protected List<String> _contentTypes;
	/** search keywords **/
	protected String _keyword;
	/** search filters **/
	protected List<FilterTO> _filters;
	/** search results columns **/
	protected List<SearchColumnTO> _columns;
	/** apply base search columns? **/
	protected boolean _applyBaseSearchableColumns;
	/** a list of aspects target content must have **/
	protected List<String> _includeAspects;
	/** a list of aspects target content must not have **/
	protected List<String> _excludeAspects;
	/** include working copy? **/
	protected boolean _excludeWorkingCopy;
	/** sort key **/
	protected String _sort;
	/** sort in ascending? **/
	protected boolean _ascending;

	/** default constructor **/
	public SearchCriteriaTO() {
	}

	/**
	 * constructor
	 * 
	 * @param site
	 * @param contentTypes
	 * @param keyword
	 * @param filters
	 * @param columns
	 * @param includeAspects
	 * @param excludeAspects
	 * @param applyBaseSearchableColumns
	 * @param excludeWorkingCopy
	 * @param sort
	 * @param ascending
	 */
	public SearchCriteriaTO(String site, List<String> contentTypes, String keyword, List<FilterTO> filters,
			List<SearchColumnTO> columns, List<String> includeAspects, List<String> excludeAspects,
			boolean applyBaseSearchableColumns, boolean excludeWorkingCopy, String sort, boolean ascending) {
		this._site = site;
		this._contentTypes = contentTypes;
		this._keyword = keyword;
		this._filters = filters;
		this._columns = columns;
		this._includeAspects = includeAspects;
		this._excludeAspects = excludeAspects;
		this._applyBaseSearchableColumns = applyBaseSearchableColumns;
		this._excludeWorkingCopy = excludeWorkingCopy;
		this._sort = sort;
		this._ascending = ascending;
	}

	/**
	 * @return the site
	 */
	public String getSite() {
		return _site;
	}

	/**
	 * @param site
	 *            the site to set
	 */
	public void setSite(String site) {
		this._site = site;
	}

	/**
	 * @return the contentTypes
	 */
	public List<String> getContentTypes() {
		return _contentTypes;
	}

	/**
	 * @param contentTypes
	 *            the contentTypes to set
	 */
	public void setContentTypes(List<String> contentTypes) {
		this._contentTypes = contentTypes;
	}

	/**
	 * @return the keyword
	 */
	public String getKeyword() {
		return _keyword;
	}

	/**
	 * @param keyword
	 *            the keyword to set
	 */
	public void setKeyword(String keyword) {
		this._keyword = keyword;
	}

	/**
	 * @return the filters
	 */
	public List<FilterTO> getFilters() {
		return _filters;
	}

	/**
	 * @param filters
	 *            the filters to set
	 */
	public void setFilters(List<FilterTO> filters) {
		this._filters = filters;
	}

	/**
	 * @return the columns
	 */
	public List<SearchColumnTO> getColumns() {
		return _columns;
	}

	/**
	 * @param columns
	 *            the columns to set
	 */
	public void setColumns(List<SearchColumnTO> columns) {
		this._columns = columns;
	}

	/**
	 * @return the applyBaseSearchableColumns
	 */
	public boolean isApplyBaseSearchableColumns() {
		return _applyBaseSearchableColumns;
	}

	/**
	 * @param applyBaseSearchableColumns
	 *            the applyBaseSearchableColumns to set
	 */
	public void setApplyBaseSearchableColumns(boolean applyBaseSearchableColumns) {
		this._applyBaseSearchableColumns = applyBaseSearchableColumns;
	}

	/**
	 * @return the includeAspects
	 */
	public List<String> getIncludeAspects() {
		return _includeAspects;
	}

	/**
	 * @param includeAspects
	 *            the includeAspects to set
	 */
	public void setIncludeAspects(List<String> includeAspects) {
		this._includeAspects = includeAspects;
	}

	/**
	 * @return the excludeAspects
	 */
	public List<String> getExcludeAspects() {
		return _excludeAspects;
	}

	/**
	 * @param excludeAspects
	 *            the excludeAspects to set
	 */
	public void setExcludeAspects(List<String> excludeAspects) {
		this._excludeAspects = excludeAspects;
	}

	/**
	 * @return the excludeWorkingCopy
	 */
	public boolean isExcludeWorkingCopy() {
		return _excludeWorkingCopy;
	}

	/**
	 * @param excludeWorkingCopy
	 *            the excludeWorkingCopy to set
	 */
	public void setExcludeWorkingCopy(boolean excludeWorkingCopy) {
		this._excludeWorkingCopy = excludeWorkingCopy;
	}

	/**
	 * @return the sort
	 */
	public String getSort() {
		return _sort;
	}

	/**
	 * @param sort
	 *            the sort to set
	 */
	public void setSort(String sort) {
		this._sort = sort;
	}

	/**
	 * @param ascending
	 *            the ascending to set
	 */
	public void setAscending(boolean ascending) {
		this._ascending = ascending;
	}

	/**
	 * @return the ascending
	 */
	public boolean isAscending() {
		return _ascending;
	}

}
