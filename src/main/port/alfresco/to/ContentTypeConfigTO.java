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
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.alfresco.service.namespace.QName;

/**
 * DM Content type configuration
 * 
 * @author hyanghee
 *
 */
public class ContentTypeConfigTO implements TimeStamped, Serializable {

	/**
	 * 
	 */
	protected static final long serialVersionUID = 1533739200033698413L;

	/** 
	 * site content name 
	 */
	protected String _name = null;
	
	/**
	 * site content type
	 */
	protected QName _contentType = null;

	/**
	 * is this WCM content type?
	 * this is default to true as it is no longer being set 
	 * from content type configuration file
	 */
	protected boolean _isWcm = true;
	
	/**
	 * content type display name 
	 */
	protected String _label = null;
	
	/**
	 * content type form 
	 */
	protected String _form = null;
	
	/**
	 * cotnent type form path
	 */
	protected String _formPath = null;
	
	protected String _type=null;
	
	/**
	 * create content in a folder wrapper? e.g. pageUrl: 101 means 101/index.xml instead of 101.xml
	 */
	protected boolean _contentAsFolder = false;
	
	/**
	 * use rounded folder to arrange content?
	 */
	protected boolean _useRoundedFolder = false; 
	
	/** 
	 * path to the model instance file (WCM) 
	 */
	protected String _modelInstancePath = null;
	
	/** list of roles allowed **/
	protected Set<String> _allowedRoles = null;

	/** content type search configuration **/
	protected SearchConfigTO _searchConfig = null;
	
	protected Date _lastUpdated;
	
	/** list of delete association patterns that this content type is dependent on for deleting indexes in webproject**/
	protected List<DeleteDependencyConfigTO> _deleteDependencies = null;
	
	/** list of copy association patterns **/
	protected List<CopyDependencyConfigTO> copyDepedencyPattern=null;

	/** is this content type previewable? **/
	protected boolean _isPreviewable = false;
	
	protected String imageThumbnail;
	
	protected boolean _noThumbnail;
	
	/**
	 * the list of included paths 
	 */
	protected List<String> _pathIncludes;
	
	/**
	 * the list of excluded paths
	 */
	protected List<String> _pathExcludes;

	/**
	 * the configuration noderef this content type is associated with
	 */
	protected String _nodeRef; 
	
	public String getImageThumbnail() {
		return imageThumbnail;
	}

	public void setImageThumbnail(String imageThumbnail) {
		this.imageThumbnail = imageThumbnail;
	}

	/**
	 * @return the contentType
	 */
	public QName getContentType() {
		return _contentType;
	}

	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(QName contentType) {
		_contentType = contentType;
	}
	
	/**
	 * @return the label
	 */
	public String getLabel() {
		return _label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this._label = label;
	}

	/**
	 * @param form the form to set
	 */
	public void setForm(String form) {
		this._form = form;
	}

	/**
	 * @return the form
	 */
	public String getForm() {
		return _form;
	}

	/**
	 * @param allowedRoles the allowedRoles to set
	 */
	public void setAllowedRoles(Set<String> allowedRoles) {
		this._allowedRoles = allowedRoles;
	}

	/**
	 * @return the allowedRoles
	 */
	public Set<String> getAllowedRoles() {
		return _allowedRoles;
	}

	/**
	 * @param formPath the formPath to set
	 */
	public void setFormPath(String formPath) {
		this._formPath = formPath;
	}

	/**
	 * @return the formPath
	 */
	public String getFormPath() {
		return _formPath;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this._name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * @param modelInstancePath the modelInstancePath to set
	 */
	public void setModelInstancePath(String modelInstancePath) {
		this._modelInstancePath = modelInstancePath;
	}

	/**
	 * @return the modelInstancePath
	 */
	public String getModelInstancePath() {
		return _modelInstancePath;
	}

	/**
	 * @param isWcm the isWcm to set
	 */
	public void setWcm(boolean isWcm) {
		this._isWcm = isWcm;
	}

	/**
	 * @return the isWcm
	 */
	public boolean isWcm() {
		return _isWcm;
	}
	
	/**
	 * @param deleteAssociations the deleteAssociations to set
	 */
	public void setDeleteDependencies(List<DeleteDependencyConfigTO> deleteDependencies) {
		this._deleteDependencies = deleteDependencies;
	}

	/**
	 * @return the deleteAssociations
	 */
	public List<DeleteDependencyConfigTO> getDeleteDependencyPattern() {
		return _deleteDependencies;
	}
	
	/**
	 * @param searchConfig the searchConfig to set
	 */
	public void setSearchConfig(SearchConfigTO searchConfig) {
		this._searchConfig = searchConfig;
	}

	/**
	 * @return the searchConfig
	 */
	public SearchConfigTO getSearchConfig() {
		return _searchConfig;
	}

	/**
	 * @param lastUpdated the lastUpdated to set
	 */
	public void setLastUpdated(Date lastUpdated) {
		this._lastUpdated = lastUpdated;
	}

	/**
	 * @return the lastUpdated
	 */
	public Date getLastUpdated() {
		return _lastUpdated;
	}

	/**
	 * @param contentAsFolder the contentAsFolder to set
	 */
	public void setContentAsFolder(boolean contentAsFolder) {
		this._contentAsFolder = contentAsFolder;
	}

	/**
	 * @return the contentAsFolder
	 */
	public boolean isContentAsFolder() {
		return _contentAsFolder;
	}

	/**
	 * @return the isPreviewable
	 */
	public boolean isPreviewable() {
		return _isPreviewable;
	}

	/**
	 * @param isPreviewable the isPreviewable to set
	 */
	public void setPreviewable(boolean isPreviewable) {
		this._isPreviewable = isPreviewable;
	}

	/**
	 * @param useRoundedFolder the useRoundedFolder to set
	 */
	public void setUseRoundedFolder(boolean useRoundedFolder) {
		this._useRoundedFolder = useRoundedFolder;
	}

	/**
	 * @return the useRoundedFolder
	 */
	public boolean isUseRoundedFolder() {
		return _useRoundedFolder;
	}
	
	
	public List<CopyDependencyConfigTO> getCopyDepedencyPattern() {
		return copyDepedencyPattern;
	}

	public void setCopyDepedencyPattern(
			List<CopyDependencyConfigTO> copyDepedencyPattern) {
		this.copyDepedencyPattern = copyDepedencyPattern;
	}

	/**
	 * @return the pathIncludes
	 */
	public List<String> getPathIncludes() {
		return _pathIncludes;
	}

	/**
	 * @param pathIncludes the pathIncludes to set
	 */
	public void setPathIncludes(List<String> pathIncludes) {
		this._pathIncludes = pathIncludes;
	}

	/**
	 * @return the pathExcludes
	 */
	public List<String> getPathExcludes() {
		return _pathExcludes;
	}

	/**
	 * @param pathExcludes the pathExcludes to set
	 */
	public void setPathExcludes(List<String> pathExcludes) {
		this._pathExcludes = pathExcludes;
	}

	public boolean isNoThumbnail() {
		return _noThumbnail;
	}

	public void setNoThumbnail(boolean noThumbnail) {
		this._noThumbnail = noThumbnail;
	}

	/**
	 * @return the nodeRef
	 */
	public String getNodeRef() {
		return _nodeRef;
	}

	/**
	 * @param nodeRef the nodeRef to set
	 */
	public void setNodeRef(String nodeRef) {
		this._nodeRef = nodeRef;
	}
	
	
	public String getType() {
		return _type;
	}

	public void setType(String type) {
		this._type = type;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this._name;
	}

}
