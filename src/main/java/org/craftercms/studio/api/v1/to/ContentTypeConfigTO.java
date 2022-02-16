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
package org.craftercms.studio.api.v1.to;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;


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
	protected String name = null;
	
	/**
	 * content type display name 
	 */
	protected String label = null;
	
	/**
	 * content type form 
	 */
	protected String form = null;
	
	/**
	 * cotnent type form path
	 */
	protected String formPath = null;
	
	protected String type=null;
	
	/**
	 * create content in a folder wrapper? e.g. pageUrl: 101 means 101/index.xml instead of 101.xml
	 */
	protected boolean contentAsFolder = false;
	
	/**
	 * use rounded folder to arrange content?
	 */
	protected boolean useRoundedFolder = false;
	
	/** 
	 * path to the model instance file (WCM) 
	 */
	protected String modelInstancePath = null;
	
	/** list of roles allowed **/
	protected Set<String> allowedRoles = null;
	
	protected ZonedDateTime lastUpdated;
	
	/** list of delete association patterns that this content type is dependent on for deleting indexes in webproject**/
	protected List<DeleteDependencyConfigTO> deleteDependencies = null;
	
	/** list of copy association patterns **/
	protected List<CopyDependencyConfigTO> copyDepedencyPattern=null;

	/** is this content type previewable? **/
	protected boolean isPreviewable = false;
	
	protected String imageThumbnail;
	
	protected boolean noThumbnail;
	
	/**
	 * the list of included paths 
	 */
	protected List<String> pathIncludes;
	
	/**
	 * the list of excluded paths
	 */
	protected List<String> pathExcludes;

	/**
	 * the configuration noderef this content type is associated with
	 */
	protected String nodeRef;

	protected boolean quickCreate;

	protected String quickCreatePath;
	
	public String getImageThumbnail() {
		return imageThumbnail;
	}

	public void setImageThumbnail(String imageThumbnail) {
		this.imageThumbnail = imageThumbnail;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @param form the form to set
	 */
	public void setForm(String form) {
		this.form = form;
	}

	/**
	 * @return the form
	 */
	public String getForm() {
		return form;
	}

	/**
	 * @param allowedRoles the allowedRoles to set
	 */
	public void setAllowedRoles(Set<String> allowedRoles) {
		this.allowedRoles = allowedRoles;
	}

	/**
	 * @return the allowedRoles
	 */
	public Set<String> getAllowedRoles() {
		return allowedRoles;
	}

	/**
	 * @param formPath the formPath to set
	 */
	public void setFormPath(String formPath) {
		this.formPath = formPath;
	}

	/**
	 * @return the formPath
	 */
	public String getFormPath() {
		return formPath;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param modelInstancePath the modelInstancePath to set
	 */
	public void setModelInstancePath(String modelInstancePath) {
		this.modelInstancePath = modelInstancePath;
	}

	/**
	 * @return the modelInstancePath
	 */
	public String getModelInstancePath() {
		return modelInstancePath;
	}
	
	/**
	 * @param deleteDependencies the deleteAssociations to set
	 */
	public void setDeleteDependencies(List<DeleteDependencyConfigTO> deleteDependencies) {
		this.deleteDependencies = deleteDependencies;
	}

	/**
	 * @return the deleteAssociations
	 */
	public List<DeleteDependencyConfigTO> getDeleteDependencyPattern() {
		return deleteDependencies;
	}

	/**
	 * @param lastUpdated the lastUpdated to set
	 */
	public void setLastUpdated(ZonedDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	/**
	 * @return the lastUpdated
	 */
	public ZonedDateTime getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * @param contentAsFolder the contentAsFolder to set
	 */
	public void setContentAsFolder(boolean contentAsFolder) {
		this.contentAsFolder = contentAsFolder;
	}

	/**
	 * @return the contentAsFolder
	 */
	public boolean isContentAsFolder() {
		return contentAsFolder;
	}

	/**
	 * @return the isPreviewable
	 */
	public boolean isPreviewable() {
		return isPreviewable;
	}

	/**
	 * @param isPreviewable the isPreviewable to set
	 */
	public void setPreviewable(boolean isPreviewable) {
		this.isPreviewable = isPreviewable;
	}

	/**
	 * @param useRoundedFolder the useRoundedFolder to set
	 */
	public void setUseRoundedFolder(boolean useRoundedFolder) {
		this.useRoundedFolder = useRoundedFolder;
	}

	/**
	 * @return the useRoundedFolder
	 */
	public boolean isUseRoundedFolder() {
		return useRoundedFolder;
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
		return pathIncludes;
	}

	/**
	 * @param pathIncludes the pathIncludes to set
	 */
	public void setPathIncludes(List<String> pathIncludes) {
		this.pathIncludes = pathIncludes;
	}

	/**
	 * @return the pathExcludes
	 */
	public List<String> getPathExcludes() {
		return pathExcludes;
	}

	/**
	 * @param pathExcludes the pathExcludes to set
	 */
	public void setPathExcludes(List<String> pathExcludes) {
		this.pathExcludes = pathExcludes;
	}

	public boolean isNoThumbnail() {
		return noThumbnail;
	}

	public void setNoThumbnail(boolean noThumbnail) {
		this.noThumbnail = noThumbnail;
	}

	/**
	 * @return the nodeRef
	 */
	public String getNodeRef() {
		return nodeRef;
	}

	/**
	 * @param nodeRef the nodeRef to set
	 */
	public void setNodeRef(String nodeRef) {
		this.nodeRef = nodeRef;
	}
	
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

    public boolean isQuickCreate() {
        return quickCreate;
    }

    public void setQuickCreate(boolean quickCreate) {
        this.quickCreate = quickCreate;
    }

    public String getQuickCreatePath() {
        return quickCreatePath;
    }

    public void setQuickCreatePath(String quickCreatePath) {
        this.quickCreatePath = quickCreatePath;
    }

    /*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.name;
	}

}
