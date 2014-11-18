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

import org.alfresco.service.namespace.QName;

/**
 * metadadta extraction configuration properties
 * 
 * @author hyanghee
 *
 */
public class MetaDataExtractionConfigTO {

	/** metadata name **/
	protected QName _name = null;
	/** xpath to get metadata value from the content being extracted **/
	protected String _xPath = null;
	/** is the metadata taxonomy? **/
	protected boolean _isTaxonomy = false;
	/** associated taxonomy qname **/
	protected QName _taxonomyName = null;
	/** 
	 * parent taxonomies of this metadata 
	 * the order in the list is important. The first one is the root parent of all
	 * and the entire list represents the path to the taxonomy we're looking for
	 */
	protected List<QName> _parentTaxonomies = null;
	/**
	 * @return the name
	 */
	public QName getName() {
		return _name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(QName name) {
		this._name = name;
	}
	/**
	 * @return the xPath
	 */
	public String getxPath() {
		return _xPath;
	}
	/**
	 * @param xPath the xPath to set
	 */
	public void setxPath(String xPath) {
		this._xPath = xPath;
	}
	/**
	 * @return the isTaxonomy
	 */
	public boolean isTaxonomy() {
		return _isTaxonomy;
	}
	/**
	 * @param isTaxonomy the isTaxonomy to set
	 */
	public void setTaxonomy(boolean isTaxonomy) {
		this._isTaxonomy = isTaxonomy;
	}
	/**
	 * @return the taxonomyName
	 */
	public QName getTaxonomyName() {
		return _taxonomyName;
	}
	/**
	 * @param taxonomyName the taxonomyName to set
	 */
	public void setTaxonomyName(QName taxonomyName) {
		this._taxonomyName = taxonomyName;
	}
	/**
	 * @return the parentTaxonomies
	 */
	public List<QName> getParentTaxonomies() {
		return _parentTaxonomies;
	}
	/**
	 * @param parentTaxonomies the parentTaxonomies to set
	 */
	public void setParentTaxonomies(List<QName> parentTaxonomies) {
		this._parentTaxonomies = parentTaxonomies;
	}

}
