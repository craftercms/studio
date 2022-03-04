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
import java.util.ArrayList;
import java.util.List;


/**
 * contains the dependent file names of the content specified by the URI
 *
 * @author hyanghee
 *
 */
public class DmDependencyTO implements Serializable {


    private static final long serialVersionUID = -7530245501866637812L;
    protected List<DmDependencyTO> _assets;
    protected List<DmDependencyTO> _components;
    protected List<DmDependencyTO> _documents;
    protected List<DmDependencyTO> _pages;
    protected List<DmDependencyTO> _deletedItems;
    protected List<DmDependencyTO> _children;
    protected List<DmDependencyTO> _renderingTemplates;
    protected List<DmDependencyTO> _levelDescriptors;
    
    protected String _uri;
    protected String _submittedBy;

    protected List<String> _workflowTasks;

    protected boolean _deleted;
    protected boolean _isNow;
    protected boolean _isReference;
    protected boolean _submittedForDeletion;
    protected boolean _submitted;
    protected boolean _inProgress;
    protected boolean _sendEmail;

    protected ZonedDateTime _scheduledDate;
	protected boolean _deleteEmptyParentFolder;

    /**
     * @return the assets
     */
    public List<DmDependencyTO> getAssets() {
        return _assets;
    }

    /**
     * @param assets
     *            the assets to set
     */
    public void setAssets(List<DmDependencyTO> assets) {
        this._assets = assets;
    }

    /**
     * @return the components
     */
    public List<DmDependencyTO> getComponents() {
        return _components;
    }

    /**
     * @param components
     *            the components to set
     */
    public void setComponents(List<DmDependencyTO> components) {
        this._components = components;
    }

    /**
     * @return the documents
     */
    public List<DmDependencyTO> getDocuments() {
        return _documents;
    }

    /**
     * @param documents the documents to set
     */
    public void setDocuments(List<DmDependencyTO> documents) {
        this._documents = documents;
    }

    /**
     * @return the rendering templates
     */
    public List<DmDependencyTO> getRenderingTemplates() {
        return _renderingTemplates;
    }

    /**
     * @param templates the rendering templates to set
     */
    public void setRenderingTemplates(List<DmDependencyTO> templates) {
        this._renderingTemplates = templates;
    }

    /**
     * @return the level descriptors
     */
    public List<DmDependencyTO> getLevelDescriptors() {
        return _levelDescriptors;
    }

    /**
     * @param levelDescriptors the level descriptors to set
     */
    public void setLevelDescriptors(List<DmDependencyTO> levelDescriptors) {
        this._levelDescriptors = levelDescriptors;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return _uri;
    }

    /**
     * @param uri
     *            the uri to set
     */
    public void setUri(String uri) {
        this._uri = uri;
    }

    public void setPages(List<DmDependencyTO> pages) {
        this._pages = pages;
    }

    public List<DmDependencyTO> getPages() {
        return _pages;
    }

    /**
     * @return the deleted
     */
    public boolean isDeleted() {
        return _deleted;
    }

    /**
     * @param deleted the deleted to set
     */
    public void setDeleted(boolean deleted) {
        this._deleted = deleted;
    }

    /**
     * @return the isNow
     */
    public boolean isNow() {
        return _isNow;
    }

    /**
     * @param isNow
     *            the isNow to set
     */
    public void setNow(boolean isNow) {
        this._isNow = isNow;
    }

    public boolean isReference() {
        return _isReference;
    }

    public void setReference(boolean reference) {
        _isReference = reference;
    }

    /**
     * @return the submitted for deletion
     */
    public boolean isSubmittedForDeletion() {
        return _submittedForDeletion;
    }

    /**
     * @param submittedForDeletion for deletion to set
     */
    public void setSubmittedForDeletion(boolean submittedForDeletion) {
        this._submittedForDeletion = submittedForDeletion;
    }

    public boolean isSubmitted() {
        return _submitted;
    }

    public void setSubmitted(boolean _submitted) {
        this._submitted = _submitted;
    }

    public boolean isInProgress() {
        return _inProgress;
    }

    public void setInProgress(boolean _inProgress) {
        this._inProgress = _inProgress;
    }

    /**
     * @return the scheduledDate
     */
    public ZonedDateTime getScheduledDate() {
        return _scheduledDate;
    }

    /**
     * @param scheduledDate
     *            the scheduledDate to set
     */
    public void setScheduledDate(ZonedDateTime scheduledDate) {
        this._scheduledDate = scheduledDate;
    }

    public List<DmDependencyTO> flattenChildren(){
        List<DmDependencyTO> dmDependencyTOList = new ArrayList<DmDependencyTO>();
        _flatten(dmDependencyTOList, this);
        return dmDependencyTOList;
    }

    public List<DmDependencyTO> getDeletedItems() {
        return _deletedItems;
    }

    public void setDeletedItems(List<DmDependencyTO> deletedItems) {
        this._deletedItems = deletedItems;
    }

    /**
     * @return the children
     */
    public List<DmDependencyTO> getChildren() {
        return _children;
    }

    /**
     * @param children
     *            the children to set
     */
    public void setChildren(List<DmDependencyTO> children) {
        this._children = children;
    }

    /**
     * @return the sendEmail
     */
    public boolean isSendEmail() {
        return _sendEmail;
    }

    /**
     * @param sendEmail the sendEmail to set
     */
    public void setSendEmail(boolean sendEmail) {
        this._sendEmail = sendEmail;
    }

    public String getSubmittedBy() {
        return _submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this._submittedBy = submittedBy;
    }

    /**
     * @return the workflowTasks
     */
    public List<String> getWorkflowTasks() {
        return _workflowTasks;
    }

    /**
     * @param workflowTasks the workflowTasks to set
     */
    public void setWorkflowTasks(List<String> workflowTasks) {
        this._workflowTasks = workflowTasks;
    }
    
    public void setDeleteEmptyParentFolder(boolean _deleteEmptyParentFolder) {
		this._deleteEmptyParentFolder = _deleteEmptyParentFolder;
	}

	public boolean isDeleteEmptyParentFolder() {
		return _deleteEmptyParentFolder;
	}

    protected void _flatten(List<DmDependencyTO> dmDependencyTOList, DmDependencyTO parent) {
        List<DmDependencyTO> documents = parent.getDocuments();
        if (null != documents) {
            dmDependencyTOList.addAll(documents);
            for (DmDependencyTO document : documents) {
                _flatten(dmDependencyTOList, document);
            }
        }
        List<DmDependencyTO> components = parent.getComponents();
        if (null != components) {
            dmDependencyTOList.addAll(components);
            for (DmDependencyTO component : components) {
                _flatten(dmDependencyTOList, component);
            }
        }
        List<DmDependencyTO> levelDesciptors = parent.getLevelDescriptors();
        if (null != levelDesciptors) {
            dmDependencyTOList.addAll(levelDesciptors);
            for (DmDependencyTO levelDescriptor : levelDesciptors) {
                _flatten(dmDependencyTOList, levelDescriptor);
            }
        }
        List<DmDependencyTO> assets = parent.getAssets();
        if (null != assets) {
            dmDependencyTOList.addAll(assets);
        }
        List<DmDependencyTO> templates = parent.getRenderingTemplates();
        if (null != templates) {
            dmDependencyTOList.addAll(templates);
        }
    }

    /**
     * Return Component, Documents and Assets direct dependencies unlike the _flattenChildren
     * 
     * @return list of direct dependencies
     */
    
	public List<DmDependencyTO> getDirectDependencies() {
		List<DmDependencyTO> dependencyTO = new ArrayList<DmDependencyTO>();
    	if(_components!=null)
            dependencyTO.addAll(_components);
    	if(_documents!=null)
            dependencyTO.addAll(_documents);
    	if(_assets!=null)
            dependencyTO.addAll(_assets);
        if (_levelDescriptors != null)
            dependencyTO.addAll(_levelDescriptors);
        if (_renderingTemplates != null)
            dependencyTO.addAll(_renderingTemplates);

    	return dependencyTO;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this._uri;
	}
}
