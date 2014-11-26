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
import java.util.Map;

import org.alfresco.service.namespace.QName;

public class SearchConfigTO {

	/** base columns to search on **/
	protected List<SearchColumnTO> _baseSearchableColumns = null;
	
	/** extractable metadata labels and qnames mapping for search results **/
	protected Map<QName, String> _extractableMetadata = null;
	
	/** content path to search in **/
	protected String _wcmSearchPath = "";
	
	/**
	 * search column to properties mapping
	 */
	protected Map<String, QName> _searchColumnMap = null;

	/**
	 * the maximum number of results to return
	 */
	protected int _maxCount = 0;
	
	/*
	 * searchable content types for WCM search
	 */
	protected List<String> _searchableContentTypes = null;
	/**
	 * @param baseSearchableColumns the baseSearchableColumns to set
	 */
	public void setBaseSearchableColumns(List<SearchColumnTO> baseSearchableColumns) {
		this._baseSearchableColumns = baseSearchableColumns;
	}

	/**
	 * @return the baseSearchableColumns
	 */
	public List<SearchColumnTO> getBaseSearchableColumns() {
		return _baseSearchableColumns;
	}

	/**
	 * @param wcmSearchPath the wcmSearchPath to set
	 */
	public void setWcmSearchPath(String wcmSearchPath) {
		this._wcmSearchPath = wcmSearchPath;
	}

	/**
	 * @return the wcmSearchPath
	 */
	public String getWcmSearchPath() {
		return _wcmSearchPath;
	}

	/**
	 * @param extractableMetadata the extractableMetadata to set
	 */
	public void setExtractableMetadata(Map<QName, String> extractableMetadata) {
		this._extractableMetadata = extractableMetadata;
	}

	/**
	 * @return the extractableMetadata
	 */
	public Map<QName, String> getExtractableMetadata() {
		return _extractableMetadata;
	}

	/**
	 * @return the searchColumnMap
	 */
	public Map<String, QName> getSearchColumnMap() {
		return _searchColumnMap;
	}

	/**
	 * @param searchColumnMap
	 *            the searchColumnMap to set
	 */
	public void setSearchColumnMap(Map<String, QName> searchColumnMap) {
		this._searchColumnMap = searchColumnMap;
	}

	/**
	 * @return the maxCount
	 */
	public int getMaxCount() {
		return _maxCount;
	}

	/**
	 * @param maxCount the maxCount to set
	 */
	public void setMaxCount(int maxCount) {
		this._maxCount = maxCount;
	}

	/**
	 * @param searchableContentTypes the searchableContentTypes to set
	 */
	public void setSearchableContentTypes(List<String> searchableContentTypes) {
		this._searchableContentTypes = searchableContentTypes;
	}

	/**
	 * @return the searchableContentTypes
	 */
	public List<String> getSearchableContentTypes() {
		return _searchableContentTypes;
	}

}
