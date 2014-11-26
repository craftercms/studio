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

import java.io.Serializable;
import java.util.List;


/**
 * A wrapper class for search result
 * 
 * @author hyanghee
 *
 */
public class SearchResultTO implements Serializable {

	/**
	 * 
	 */
	protected static final long serialVersionUID = 343473637428497817L;

	/** total number of items available **/
	protected int _total = 0;
	
	/** total number of pages available **/
	protected int _totalPages = 0;
	
	/** the number of records per page **/
	protected int _numOfItems= 0;
	
	/** current page number **/
	protected int _page = 1;
	
	/** sort key **/
	protected String _sort = null;
	
	/** result items **/
	protected List<Serializable> _items = null;
	
	protected String _keyword = null;
	
	/** whether search fails **/
	protected boolean _searchFailed = false;
	
	/** if search fails, the cause **/
	protected String _failCause = null;
	
	/**
	 * @return the total
	 */
	public int getTotal() {
		return _total;
	}

	/**
	 * @param total the total to set
	 */
	public void setTotal(int total) {
		this._total = total;
	}

	/**
	 * @return the numOfItems
	 */
	public int getNumOfItems() {
		return _numOfItems;
	}

	/**
	 * @param numOfItems the numOfItems to set
	 */
	public void setNumOfItems(int numOfItems) {
		this._numOfItems = numOfItems;
	}

	/**
	 * @return the page
	 */
	public int getPage() {
		return _page;
	}

	/**
	 * @param page the page to set
	 */
	public void setPage(int page) {
		this._page = page;
	}

	/**
	 * @return the sort
	 */
	public String getSort() {
		return _sort;
	}

	/**
	 * @param sort the sort to set
	 */
	public void setSort(String sort) {
		this._sort = sort;
	}

	/**
	 * @return the items
	 */
	public List<Serializable> getItems() {
		return _items;
	}

	/**
	 * @param items the items to set
	 */
	public void setItems(List<Serializable> items) {
		this._items = items;
	}

	/**
	 * @return the keyword
	 */
	public String getKeyword() {
		return _keyword;
	}

	/**
	 * @param keyword the keyword to set
	 */
	public void setKeyword(String keyword) {
		this._keyword = keyword;
	}

	/**
	 * @param searchFailed the searchFailed to set
	 */
	public void setSearchFailed(boolean searchFailed) {
		this._searchFailed = searchFailed;
	}

	/**
	 * @return the searchFailed
	 */
	public boolean isSearchFailed() {
		return _searchFailed;
	}

	/**
	 * @param failCause the failCause to set
	 */
	public void setFailCause(String failCause) {
		this._failCause = failCause;
	}

	/**
	 * @return the failCause
	 */
	public String getFailCause() {
		return _failCause;
	}

	/**
	 * @param totalPages the totalPages to set
	 */
	public void setTotalPages(int totalPages) {
		this._totalPages = totalPages;
	}

	/**
	 * @return the totalPages
	 */
	public int getTotalPages() {
		return _totalPages;
	}

}
