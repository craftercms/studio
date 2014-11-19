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
package org.craftercms.cstudio.alfresco.dm.to;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.util.DmContentItemComparator;
import org.craftercms.cstudio.alfresco.util.ContentFormatUtils;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastTable;

/**
 * This class contains DM content item metadata
 * 
 * @author hyanghee
 * @author Dejan Brkic
 * 
 */
public class DmContentItemTO implements Serializable {

    private static final long serialVersionUID = 4692872202161051204L;

    /** the name of item **/
	protected String _name;
	
	/** the name of item specified by the creator **/
	protected String _internalName;
	
	/** wcm content type for associating this content with a form **/
	protected String _contentType;
	
	/** wcm content meta description **/
	protected String _metaDescription;
	
	/** form id **/
	protected String _form;
	
	/** wcm form page path associated with the content type **/
	protected String _formPagePath;
	
	/** user name **/
	protected String _user;
	
	/** the first name of the last editor **/
	protected String _userFirstName;
	
	/** the last name of the last editor **/
	protected String _userLastName;
	
	/** the last edited date **/
	protected Date _eventDate = null;
	
	/** the total number of children under this content item **/
	protected int _numOfChildren = 0;
	
	/** the path **/
	protected String _path;
	
	/**
	 * the madatory parent must be tied together with this item in workflow
	 * submission
	 **/
	protected String _mandatoryParent;
	
	/** the web project default web app **/
	protected String _defaultWebApp;
	
	/** scheduled date if the item is in workflow and a laucn date is scheduled **/
	protected Date _scheduledDate = null;
	
	/**
	 * the first name of the user who submitted this item to workflow if
	 * submitted
	 **/
	protected String _submittedByFirstName;
	
	/**
	 * the last name of the user who submitted this item to workflow if
	 * submitted
	 **/
	protected String _submittedByLastName;

    protected String _submissionComment;
	
	/** who lockekd this content? **/
	protected String _lockOwner;
	
	protected int _width;
	
	protected int _height;
	
	/** is the item to be submitted upon approval */
	protected boolean _isNow = false;
	
	/**
	 * a flag to indicate that this content is new - isNew flag is
	 * only for user representation
	 **/
	protected boolean _isNewFile = false;
	
	/** is the preview page available for this content? **/
	protected boolean _isPreviewable = false;
	
	/** status flags **/
	protected boolean _isInProgress = false;
	protected boolean _isSubmitted = false;
	protected boolean _isScheduled = false;
	protected boolean _isNavigation = false;
	protected boolean _isFloating = false;
	protected boolean _isComponent = false;
	protected boolean _isAsset = false;
	protected boolean _isContainer = false;
	protected boolean _isDocument = false;
	protected boolean _isNew = false;
	protected boolean _isDisabled = false;
	protected boolean _isLevelDescriptor = false;
	protected boolean _isInFlight;
    protected boolean _renderingTemplate = false;
	
	/**
	 * is this item deleted from repo
	 */
	protected boolean _isDeleted = false;
	
	protected boolean _isDirectory = false;
	
	/**
	 * is this item submitted for deletion
	 */
	protected boolean _submittedForDeletion = false;
	
	/** navigation child content items **/
	protected List<DmContentItemTO> _children;
	
	/** components that this content item is dependent on **/
	protected List<DmContentItemTO> _components;
	
	/** documents that this content item is dependent on **/
	protected List<DmContentItemTO> _documents;
	
	/** static assets that this content item is dependent on **/
	protected List<DmContentItemTO> _assets;
    
    /** rendering templates that this contetn item is dependent on **/
    protected List<DmContentItemTO> _renderingTemplates;
	
	protected List<DmContentItemTO> _pages;
	
	/** deleted item dependencies **/
	protected List<DmContentItemTO> _deletedItems;
	
	/** list of level descriptors located underneath of the current level item **/
	protected List<DmContentItemTO> _levelDescriptors;
	
	/** content order metadata **/
	protected List<DmOrderTO> _orders;
	
	/**
	 * the URI e.g.
	 * /site/website/product_servcies/upgrade.xml
	 **/
	protected String _uri = null;
	
	/**
	 * browser URI that the end user is aware of. e.g.
	 * /product_servcies/upgrade.xml
	 **/
	protected String _browserUri = null;
	
	/**
	 * the root path of the top-level category of this item belongs to. e.g.
	 * /site/website (for /product_servcies/upgrade.xml)
	 **/
	protected String _categoryRoot = null;
	
	/**
	 * the nodeRef of the corresponding DM content if the content was published
	 * from DM
	 **/
	protected String _nodeRef = null;
	
	/** workflow id if any associted **/
	protected String _workflowId = null;
	
	/** timezone value - defaulted to GMT **/
	protected String _timezone = null;
	
	/** cstudio-core:title -- for debug purpose; internal replaced by this. **/
	protected String _title = null;
	
	protected boolean _hideInAuthoring = false;
	
	protected String _parentPath = null;
	
	protected boolean _isReference = false;
	
	protected Date _lastEditDate;

    protected boolean _skipDependencies = false;
	
	/** additional properties map **/
	protected Map<String, String> _properties = new FastMap<String, String>();

    /**
	 * default constructor
	 */
	public DmContentItemTO() { }
	
	public DmContentItemTO(DmContentItemTO item) {
		this(item, true);
	}
	
	/**
	 * copy constructor
	 * 
	 * @param item
	 */
	public DmContentItemTO(DmContentItemTO item, boolean cloneChildren) {
		this._name = item._name;
		this._internalName = item._internalName;
		this._contentType = item._contentType;
		this._metaDescription = item._metaDescription;
		this._form = item._form;
		this._formPagePath = item._formPagePath;
		this._user = item._user;
		this._userFirstName = item._userFirstName;
		this._userLastName = item._userLastName;
		this._eventDate = item._eventDate;
		this._numOfChildren = item._numOfChildren;
		this._path = item._path;
		this._mandatoryParent = item._mandatoryParent;
		this._defaultWebApp = item._defaultWebApp;
		this._isDirectory = item._isDirectory;
		this._submittedByFirstName = item._submittedByFirstName;
		this._submittedByLastName = item._submittedByLastName;
        this._submissionComment = item._submissionComment;
		this._lockOwner = item._lockOwner;

		this._isNow = item._isNow;
		this._isNewFile = item._isNewFile;
		this._isPreviewable = item._isPreviewable;
		this._isInProgress = item._isInProgress;
		this._isSubmitted = item._isSubmitted;
		this._isScheduled = item._isScheduled;
		this._isNavigation = item._isNavigation;
		this._isFloating = item._isFloating;
		this._isComponent = item._isComponent;
		this._isAsset = item._isAsset;
		this._isContainer = item._isContainer;
		this._isDocument = item._isDocument;
		this._isNew = item._isNew;
		this._isDisabled = item._isDisabled;
		this._isLevelDescriptor = item._isLevelDescriptor;
		this._isDeleted = item._isDeleted;
		this._submittedForDeletion = item._submittedForDeletion;
		this._uri = item._uri;
		this._browserUri = item._browserUri;
		this._categoryRoot = item._categoryRoot;
		this._nodeRef = item._nodeRef;
		this._title = item._title;
		this._hideInAuthoring = item._hideInAuthoring;
		this._timezone = item._timezone;
		this._parentPath = item._parentPath;

        // copy dates
        if (item._scheduledDate != null) {
            this._scheduledDate = new Date(item._scheduledDate.getTime());
        }
        if (item._eventDate != null) {
            this._eventDate = new Date(item._eventDate.getTime());
        }

        if (item._lastEditDate != null) {
            this._lastEditDate = new Date(item._lastEditDate.getTime());
        }
        
        // copy all collections
        if (cloneChildren) {
            if (item._children != null) {
                this._children = new FastTable<DmContentItemTO>(item._children.size());
                for (DmContentItemTO child : item._children) {
                    this._children.add(new DmContentItemTO(child));
                }
            }
        }
        if (item._components != null) {
			this._components = new FastTable<DmContentItemTO>(item._components.size());
			for (DmContentItemTO component : item._components) {
				this._components.add(new DmContentItemTO(component));
			}
		}
		if (item._assets != null) {
			this._assets = new FastTable<DmContentItemTO>(item._assets.size());
			for (DmContentItemTO asset : item._assets) {
				this._assets.add(new DmContentItemTO(asset));
			}
		}
		if (item._documents != null) {
			this._documents = new FastTable<DmContentItemTO>(item._documents.size());
			for (DmContentItemTO document : item._documents) {
				this._documents.add(new DmContentItemTO(document));
			}
		}
		if (item._deletedItems != null) {
			this._deletedItems = new FastTable<DmContentItemTO>(item._deletedItems.size());
			for (DmContentItemTO deletedItem : item._deletedItems) {
				this._deletedItems.add(new DmContentItemTO(deletedItem));
			}
		}
		if (item._pages != null) {
			this._pages = new FastTable<DmContentItemTO>(item._pages.size());
			for (DmContentItemTO page : item._pages) {
				this._pages.add(new DmContentItemTO(page));
			}
		}
        if (item._renderingTemplates != null) {
            this._renderingTemplates = new FastTable<DmContentItemTO>(item._renderingTemplates.size());
            for (DmContentItemTO template : item._renderingTemplates) {
                this._renderingTemplates.add(new DmContentItemTO(template));
            }
        }
		if (item._levelDescriptors != null) {
			this._levelDescriptors = new FastTable<DmContentItemTO>(item._levelDescriptors.size());
			for (DmContentItemTO levelDescriptor : item._levelDescriptors) {
				this._levelDescriptors.add(new DmContentItemTO(levelDescriptor));
			}
		}
		if (item._orders != null) {
			this._orders = new FastList<DmOrderTO>(item._orders.size());
			for (DmOrderTO order : item._orders) {
				this._orders.add(new DmOrderTO(order));
			}
		}
		if (item._properties != null) {
			this._properties = new FastMap<String, String>();
			for (String key : item._properties.keySet()) {
				this._properties.put(key, item._properties.get(key));
			}
		}
		// copy image properties
		this._width = item._width;
		this._height = item._height;

        this._skipDependencies = item._skipDependencies;
	}
	
	public boolean isReference() {
		return _isReference;
	}

	public void setReference(boolean isReference) {
		this._isReference = isReference;
	}

	public String getParentPath() {
		return _parentPath;
	}

	public void setParentPath(String parentPath) {
		this._parentPath = parentPath;
	}

	public boolean isHideInAuthoring() {
		return _hideInAuthoring;
	}

	public void setHideInAuthoring(boolean hideInAuthoring) {
		this._hideInAuthoring = hideInAuthoring;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		this._name = name;
	}

	public String getInternalName() {
		return _internalName;
	}

	public void setInternalName(String internalName) {
		this._internalName = internalName;
	}

	public String getUri() {
		return _uri;
	}

	public void setUri(String uri) {
		this._uri = uri;
	}

	public String getUser() {
		return _user;
	}

	public void setUser(String user) {
		this._user = user;
	}

	public String getUserFirstName() {
		return _userFirstName;
	}

	public void setUserFirstName(String userFirstName) {
		this._userFirstName = userFirstName;
	}

	public String getUserLastName() {
		return _userLastName;
	}

	public void setUserLastName(String userLastName) {
		this._userLastName = userLastName;
	}
	
	public String getEventDate() {
		if (_eventDate != null) {
			SimpleDateFormat format = new SimpleDateFormat(CStudioConstants.DATE_PATTERN_WORKFLOW);
			String dateStr = ContentFormatUtils.formatDate(format, _eventDate, _timezone);
			return dateStr;
		} else {
			return null;
		}
	}
	
	public String getLastEditDateAsString() {
		if (_lastEditDate != null) {
			SimpleDateFormat format = new SimpleDateFormat(CStudioConstants.DATE_PATTERN_WORKFLOW);
			String dateStr = ContentFormatUtils.formatDate(format, _lastEditDate, _timezone);
			return dateStr;
		} else {
			return null;
		}
	}
	
	public String getScheduledDate() {
		if (_scheduledDate != null) {
			SimpleDateFormat format = new SimpleDateFormat(CStudioConstants.DATE_PATTERN_WORKFLOW);
			String dateStr = ContentFormatUtils.formatDate(format, _scheduledDate, _timezone);
			return dateStr;
		} else {
			return null;
		}
	}

	public void setScheduledDate(Date scheduledDate) {
		this._scheduledDate = scheduledDate;
	}
	
	public Date getEventDateAsDate() {
		return _eventDate;
	}
	
	public Date getScheduledDateAsDate() {
		return _scheduledDate;
	}

	public void setEventDate(Date eventDate) {
		this._eventDate = eventDate;
	}

	public boolean isInProgress() {
		return _isInProgress;
	}

	public void setInProgress(boolean isInProgress) {
		this._isInProgress = isInProgress;
	}

	public boolean isSubmitted() {
		return _isSubmitted;
	}

	public void setSubmitted(boolean isSubmitted) {
		this._isSubmitted = isSubmitted;
	}
	
	public boolean isLive() {
		return !(this.isInProgress() || this.isSubmitted() || this.isScheduled() || this.isSubmittedForDeletion() || this.isNew());
	}
	
	public boolean isScheduled() {
        if (_isScheduled && _scheduledDate==null){
            return false;
        }
		return _isScheduled;
	}
	
	public void setScheduled(boolean isScheduled) {
		this._isScheduled = isScheduled;
	}
	
	public boolean isNavigation() {
		return _isNavigation;
	}
	
	public void setNavigation(boolean isNavigation) {
		this._isNavigation = isNavigation;
	}
	
	public boolean isFloating() {
		return _isFloating;
	}
	
	public void setFloating(boolean isFloating) {
		this._isFloating = isFloating;
	}
	
	public boolean isComponent() {
		return _isComponent;
	}
	
	public void setComponent(boolean isComponent) {
		this._isComponent = isComponent;
	}
	
	public boolean isDocument() {
		return _isDocument;
	}
	
	public void setDocument(boolean isDocument) {
		this._isDocument = isDocument;
	}
	
	public boolean isContainer() {
		return _isContainer;
	}
	
	public void setContainer(boolean isContainer) {
		_isContainer = isContainer;
	}
	
	public boolean isNew() {
		return _isNew;
	}
	
	public void setNew(boolean isNew) {
		this._isNewFile = isNew;
		this._isNew = isNew;
	}
	
	public boolean isDisabled() {
		return _isDisabled;
	}
	
	public void setDisabled(boolean isDisabled) {
		this._isDisabled = isDisabled;
	}

    public boolean isSkipDependencies() { return _skipDependencies; }

    public void setSkipDependencies(boolean skipDependencies) { this._skipDependencies = skipDependencies; }
	
	public List<DmContentItemTO> getChildren() {
		return _children;
	}
	
	public void setChildren(final List<DmContentItemTO> children) {
		this._children = children;
	}
	
	public String getPath() {
		return _path;
	}
	
	public void setPath(String path) {
		this._path = path;
	}

	public String getDefaultWebApp() {
		return _defaultWebApp;
	}
	
	public void setDefaultWebApp(String defaultWebApp) {
		this._defaultWebApp = defaultWebApp;
	}
	
	public void addChild(DmContentItemTO itemToAdd, boolean recursive, boolean renamed) {

		if (_uri != null && _uri.equals(itemToAdd.getUri())) {
			// do not add itself
			return;
		}

		if (recursive && (_isNewFile || _isDeleted || renamed) && !_submittedForDeletion) {
			itemToAdd.setMandatoryParent(_uri);
		}

		if (_children == null) {
			_children = new FastTable<DmContentItemTO>();
		}
		_children.add(itemToAdd);
		_numOfChildren++;
	}
	
	public void addChild(final DmContentItemTO itemToAdd, DmContentItemComparator comparator, boolean recursive) {
		addChild(itemToAdd, comparator, recursive, false);
	}
	
	/**
	 * add an item to the list of child items
	 * 
	 * @param itemToAdd
	 * @param comparator
	 *            child item sort comparator
	 * @param recursive
	 *            recursively add the item to the parent-child hierarchy?
	 */
	public void addChild(final DmContentItemTO itemToAdd, DmContentItemComparator comparator, boolean recursive,
			boolean renamed) {
		if (_uri != null && _uri.equals(itemToAdd.getUri())) {
			// do not add itself
			return;
		}

		// if this content (parent) is a new file, set the mandatory parent of
		// child items to be this content
		// do not overwrite the mandatory parent in non-recursive case
		// do not set mandatory parent if item is submittedForDeletion.
		if (recursive && (_isNewFile || _isDeleted || renamed) && !_submittedForDeletion
				&& !itemToAdd.isSubmittedForDeletion()) {
			itemToAdd.setMandatoryParent(_uri);
		}
		if (_children != null) {
			if (_children.contains(itemToAdd)) {
				return;
			}
			boolean added = false;
			// position to add the item
			int pos = 0;
			// list to hold any child items found to be add to the itemToAdd
			List<Integer> childPositions = new ArrayList<Integer>(_children.size());
			for (int index = 0; index < _children.size(); index++) {
				DmContentItemTO child = _children.get(index);
				String childUri = child.getBrowserUri();
				String itemToAddUri = itemToAdd.getBrowserUri();
				// for recursive case, check if the item being added should
				// belong to one of the current level items
				// or one of the current level items should belong to the item
				// being added
				if (recursive) {
					if (comparator.compare(child, itemToAdd) < 0) {
						pos = index + 1 + 0;
					}
					// if the new item's URI starts with the URI of one of the
					// item
					// add the new item as a child of the item found

					if (itemToAddUri.startsWith(childUri + "/")) {
						child.addChild(itemToAdd, comparator, recursive);
						added = true;
						break;
					} else {
						// if one of the item's URI starts with the URI of the
						// new item
						// add the item to the new item add replace it with the
						// new item
						if (childUri.startsWith(itemToAddUri + "/")) {
							if (childPositions.size() == 0) {
								// add the itemToAdd to the first child location
								// and add the first child to itemToAdd
								itemToAdd.addChild(child, comparator, recursive);
								_children.set(index, itemToAdd);
								added = true;
							}
							childPositions.add(index);
						}
					}
					// for non-recursive case, add the item being added to the
					// current position
					// if the current item is greater than the item
				} else {
					if (comparator.compare(itemToAdd, child) < 0) {
						_children.add(index, itemToAdd);
						added = true;
						break;
					} else {
						pos = index + 1;
					}
				}
			}
			// if not added, add the new item to the right position
			if (!added) {
				_children.add(pos, itemToAdd);
			}
			// if recursive case, check if there are more children to be added
			// to itemToAdd
			if (recursive && childPositions.size() > 1) {
				for (int childIndex = 1; childIndex < childPositions.size(); childIndex++) {
					int targetPosition = childPositions.get(childIndex);
					// if there are more than 2 children added,
					// make sure reduce the index by the number of children
					// added - 1
					// since the list changes
					if (childIndex > 1) {
						targetPosition -= childIndex - 1;
					}
					itemToAdd.addChild(_children.get(targetPosition), comparator, recursive);
					_children.remove(targetPosition);
				}
			}
		} else {
			_children = new FastTable<DmContentItemTO>();
			_children.add(itemToAdd);
		}
		// increase the number of children by 1
		_numOfChildren++;
	}
	
	public void addChild(final DmContentItemTO itemToAdd, DmContentItemComparator comparator, boolean recursive,
			ChildFilter childFilter) {
		if (_uri != null && _uri.equals(itemToAdd.getUri())) {
			// do not add itself
			return;
		}

		// if this content (parent) is a new file, set the mandatory parent of
		// child items to be this content
		// do not overwrite the mandatory parent in non-recursive case
		if (recursive && (_isNewFile || _isDeleted)) {
			itemToAdd.setMandatoryParent(_uri);
		}
		if (_children != null) {
			if (_children.contains(itemToAdd)) {
				return;
			}
			boolean added = false;
			// position to add the item
			int pos = 0;
			// list to hold any child items found to be add to the itemToAdd
			List<Integer> childPositions = new ArrayList<Integer>(_children.size());
			for (int index = 0; index < _children.size(); index++) {
				DmContentItemTO child = _children.get(index);
				String childUri = child.getBrowserUri();
				String itemToAddUri = itemToAdd.getBrowserUri();
				// for recursive case, check if the item being added should
				// belong to one of the current level items
				// or one of the current level items should belong to the item
				// being added
				if (recursive) {
					if (comparator.compare(child, itemToAdd) < 0) {
						pos = index + 1 + 0;
					}
					// if the new item's URI starts with the URI of one of the
					// item
					// add the new item as a child of the item found

					if (itemToAddUri.startsWith(childUri + "/")) {
						child.addChild(itemToAdd, comparator, recursive);
						added = true;
						break;
					} else {
						// if one of the item's URI starts with the URI of the
						// new item
						// add the item to the new item add replace it with the
						// new item
						if (childUri.startsWith(itemToAddUri + "/")) {
							if (childPositions.size() == 0) {
								// add the itemToAdd to the first child location
								// and add the first child to itemToAdd
								itemToAdd.addChild(child, comparator, recursive);
								if (childFilter.accept(itemToAdd)) {
									_children.set(index, itemToAdd);
								}
								added = true;
							}
							childPositions.add(index);
						}
					}
					// for non-recursive case, add the item being added to the
					// current position
					// if the current item is greater than the item
				} else {
					if (comparator.compare(itemToAdd, child) < 0) {
						if (childFilter.accept(itemToAdd)) {
							_children.add(index, itemToAdd);
							added = true;
						}
						break;
					} else {
						pos = index + 1;
					}
				}
			}
			// if not added, add the new item to the right position
			if (!added) {
				if (childFilter.accept(itemToAdd)) {
					_children.add(pos, itemToAdd);
				}
			}
			// if recursive case, check if there are more children to be added
			// to itemToAdd
			if (recursive && childPositions.size() > 1) {
				for (int childIndex = 1; childIndex < childPositions.size(); childIndex++) {
					int targetPosition = childPositions.get(childIndex);
					// if there are more than 2 children added,
					// make sure reduce the index by the number of children
					// added - 1
					// since the list changes
					if (childIndex > 1) {
						targetPosition -= childIndex - 1;
					}
					itemToAdd.addChild(_children.get(targetPosition), comparator, recursive);
					_children.remove(targetPosition);
				}
			}
		} else {
			_children = new FastTable<DmContentItemTO>();
			if (childFilter.accept(itemToAdd)) {
				_children.add(itemToAdd);
			}
		}
		// increase the number of children by 1
		_numOfChildren++;

	}
	
	public interface ChildFilter {

		public boolean accept(DmContentItemTO to);
	}

	public class AcceptAllChildFilter implements ChildFilter {

		public boolean accept(DmContentItemTO to) {
			return true;
		}
	}
	
	public List<DmContentItemTO> getComponents() {
		return _components;
	}
	
	public void setComponents(List<DmContentItemTO> components) {
		_components = components;
	}
	
	public List<DmContentItemTO> getAssets() {
		return _assets;
	}
	
	public void setAssets(List<DmContentItemTO> assets) {
		_assets = assets;
	}
	
	public List<DmContentItemTO> getPages() {
		return _pages;
	}
	
	public void setPages(List<DmContentItemTO> pages) {
		this._pages = pages;
	}
	
	public List<DmContentItemTO> getDeletedItems() {
		return _deletedItems;
	}
	
	public void setDeletedItems(List<DmContentItemTO> deletedItems) {
		this._deletedItems = deletedItems;
	}
	
	public List<DmContentItemTO> getLevelDescriptors() {
		return _levelDescriptors;
	}
	
	public void setLevelDescriptors(List<DmContentItemTO> levelDescriptors) {
		this._levelDescriptors = levelDescriptors;
	}
	
	public Double getOrder(String orderName) {
		if (orderName != null && _orders != null) {
			for (DmOrderTO order : _orders) {
				if (orderName.equalsIgnoreCase(order.getId())) {
					return order.getOrder();
				}
			}
		}
		return -1.0;
	}
	
	public String getBrowserUri() {
		if (isDocument()) {
			List<DmContentItemTO> contentItemTOList = getAssets();
			if (contentItemTOList != null && contentItemTOList.size() > 0) {
				return contentItemTOList.get(0).getBrowserUri();
			}
		}
		return _browserUri;
	}
	
	public void setBrowserUri(String browserUri) {
		this._browserUri = browserUri;
	}
	
	public String getCategoryRoot() {
		return _categoryRoot;
	}
	
	public void setCategoryRoot(String categoryRoot) {
		this._categoryRoot = categoryRoot;
	}
	
	public String getMandatoryParent() {
		return _mandatoryParent;
	}
	
	public void setMandatoryParent(String mandatoryParent) {
		this._mandatoryParent = mandatoryParent;
	}
	
	public boolean isNewFile() {
		return _isNewFile;
	}
	
	public void setNewFile(boolean isNewFile) {
		this._isNewFile = isNewFile;
		this._isNew = isNewFile;
	}
	
	public boolean isNow() {
		return _isNow;
	}
	
	public void setNow(boolean isNow) {
		this._isNow = isNow;
	}
	
	public boolean isAsset() {
		return _isAsset;
	}
	
	public void setAsset(boolean isAsset) {
		this._isAsset = isAsset;
	}
	
	public String getContentType() {
		return _contentType;
	}
	
	public void setContentType(String contentType) {
		this._contentType = contentType;
	}

	public String getMetaDescription() {
		return _metaDescription;
	}
	
	public void setMetaDescription(String metaDescription) {
		this._metaDescription = metaDescription;
	}
	
	public int getNumOfChildren() {
		return _numOfChildren;
	}
	
	public void setNumOfChildren(int numOfChildren) {
		_numOfChildren = numOfChildren;
	}
	
	public boolean isLevelDescriptor() {
		return _isLevelDescriptor;
	}
	
	public void setLevelDescriptor(boolean isLevelDescriptor) {
		this._isLevelDescriptor = isLevelDescriptor;
	}
	
	public String getFormPagePath() {
		return _formPagePath;
	}
	
	public void setFormPagePath(String formPagePath) {
		this._formPagePath = formPagePath;
	}
	
	public String getSubmittedByFirstName() {
		return _submittedByFirstName;
	}
	
	public void setSubmittedByFirstName(String submittedByFirstName) {
		this._submittedByFirstName = submittedByFirstName;
	}
	
	public String getSubmittedByLastName() {
		return _submittedByLastName;
	}
	
	public void setSubmittedByLastName(String submittedByLastName) {
		this._submittedByLastName = submittedByLastName;
	}

    public String getSubmissionComment() {
        return _submissionComment;
    }

    public void setSubmissionComment(String submissionComment) {
        this._submissionComment = submissionComment;
    }
	
	public String getNodeRef() {
		return _nodeRef;
	}
	
	public void setNodeRef(String nodeRef) {
		this._nodeRef = nodeRef;
	}
	
	public String getLockOwner() {
		return _lockOwner;
	}
    
    public void setLockOwner(String lockOwner) {
        this._lockOwner = lockOwner;
    }
	
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof DmContentItemTO)) {
			return false;
		}
		DmContentItemTO item = (DmContentItemTO) object;
		// it is the same item if the default webapp and the URI
		// are the same
		return item.getUri().equals(this._uri)
				&& item.getDefaultWebApp().equals(this._defaultWebApp);
	}
	
	public int hashCode() {
		int result = 17;
		return 31 * result + this.toString().hashCode();
	}
	
	public String toString() {
		return ":/" + DmConstants.WEB_PROJECT_ROOT + "/" + _defaultWebApp + _uri;
	}
	
	public String getForm() {
		return _form;
	}
	
	public void setForm(String form) {
		this._form = form;
	}
	
	public boolean isDeleted() {
		return _isDeleted;
	}
	
	public void setDeleted(boolean isDeleted) {
		this._isDeleted = isDeleted;
	}
	
	public boolean isSubmittedForDeletion() {
		return _submittedForDeletion;
	}
	
	public void setSubmittedForDeletion(boolean submittedForDeletion) {
		this._submittedForDeletion = submittedForDeletion;
	}
	
	public List<DmContentItemTO> getDocuments() {
		return _documents;
	}
	
	public void setDocuments(List<DmContentItemTO> documents) {
		this._documents = documents;
	}
	
	public boolean isPreviewable() {
		return _isPreviewable;
	}
	
	public void setPreviewable(boolean isPreviewable) {
		this._isPreviewable = isPreviewable;
	}
	
	public String getWorkflowId() {
		return _workflowId;
	}
	
	public void setWorkflowId(String workflowId) {
		this._workflowId = workflowId;
	}
	
	public String getTimezone() {
		return _timezone;
	}
	
	public void setTimezone(String timezone) {
		this._timezone = timezone;
	}
	
	public String getTitle() {
		return _title;
	}
	
	public void setTitle(String title) {
		this._title = title;
	}
	
	public boolean isDirectory() {
		return _isDirectory;
	}
	
	public void setDirectory(boolean directory) {
		_isDirectory = directory;
	}
	
	public Date getLastEditDate() {
		return _lastEditDate;
	}
	
	public void setLastEditDate(Date lastEditDate) {
		this._lastEditDate = lastEditDate;
	}
	
	public boolean isInFlight() {
		return _isInFlight;
	}
	
	public void setInFlight(boolean inFlight) {
		_isInFlight = inFlight;
	}
	
	public int getWidth() {
		return _width;
	}
	
	public void setWidth(int width) {
		this._width = width;
	}
	
	public int getHeight() {
		return _height;
	}
	
	public void setHeight(int height) {
		this._height = height;
	}
	
	/**
	 * This function filters out the content item which has hideInAuthoring as
	 * true.
	 * 
	 * @return
	 */
	public DmContentItemTO filterOutFiles() {
		if (_children != null) {
			List<DmContentItemTO> newchildren = new FastTable<DmContentItemTO>(_children.size());

			for (DmContentItemTO item : _children) {
				if (item.isContainer()) {
					item = item.filterOutFiles();
					newchildren.add(item);
				}
			}

			this._children = newchildren;
			_numOfChildren = newchildren.size();
		}

		return this;
	}
	
	/**
	 * This function filters out the content item which has hideInAuthoring as
	 * true.
	 * 
	 * @return
	 */
	public DmContentItemTO filterOutHidden() {
		if (this._hideInAuthoring) {
			if (_isComponent || _isAsset || _isDocument || _isLevelDescriptor) {
				// Make all the children null
				if (_children != null) {
					_children = null;
				}

				if (_documents != null) {
					_documents = null;
				}

				if (_assets != null) {
					_assets = null;
				}

				if (_pages != null) {
					_pages = null;
				}

				if (_deletedItems != null) {
					_deletedItems = null;
				}

				if (_levelDescriptors != null) {
					_levelDescriptors = null;
				}
				return null;
			} else {
				return null;
			}

			//
		} else {
			if (_children != null) {
				List<DmContentItemTO> newchildren = new FastTable<DmContentItemTO>(_children.size());
				for (DmContentItemTO item : _children) {
					item = item.filterOutHidden();
					if (item != null)
						newchildren.add(item);
				}
				this._children = newchildren;
				_numOfChildren = newchildren.size();
			}
			if (_documents != null) {
				List<DmContentItemTO> newdocuments = new FastTable<DmContentItemTO>(_documents.size());
				for (DmContentItemTO item : _documents) {
					item = item.filterOutHidden();
					if (item != null)
						newdocuments.add(item);
				}
				this._documents = newdocuments;
			}

			if (_assets != null) {
				List<DmContentItemTO> newassets = new FastTable<DmContentItemTO>(_assets.size());
				for (DmContentItemTO item : _assets) {
					item = item.filterOutHidden();
					if (item != null)
						newassets.add(item);
				}
				this._assets = newassets;
			}

			if (_pages != null) {
				List<DmContentItemTO> newpages = new FastTable<DmContentItemTO>(_pages.size());
				for (DmContentItemTO item : _pages) {
					item = item.filterOutHidden();
					if (item != null)
						newpages.add(item);
				}
				this._pages = newpages;
			}

			if (_deletedItems != null) {
				List<DmContentItemTO> newdeleteditems = new FastTable<DmContentItemTO>(_deletedItems.size());
				for (DmContentItemTO item : _deletedItems) {
					item = item.filterOutHidden();
					if (item != null)
						_deletedItems.add(item);
				}
				this._deletedItems = newdeleteditems;
			}
			if (_levelDescriptors != null) {
				List<DmContentItemTO> newleveldescriptors = new FastTable<DmContentItemTO>(_levelDescriptors.size());
				for (DmContentItemTO item : _levelDescriptors) {
					item = item.filterOutHidden();
					if (item != null)
						newleveldescriptors.add(item);
				}
				this._levelDescriptors = newleveldescriptors;
			}
		}
		return this;
	}
	
	public Map<String, String> getProperties() {
		return _properties;
	}
	
	public void setProperties(Map<String, String> properties) {
		this._properties = properties;
	}

    /**
     * @param orders
     *            the orders to set
     */
    public void setOrders(List<DmOrderTO> orders) {
        this._orders = orders;
    }

    /**
     * @return the orders
     */
    public List<DmOrderTO> getOrders() {
        return _orders;
    }
    
    public List<DmContentItemTO> getRenderingTemplates() {
        return this._renderingTemplates;
    }
    
    public void setRenderingTemplates(List<DmContentItemTO> templates) {
        this._renderingTemplates = templates;
    }

    public boolean isRenderingTemplate() {
        return this._renderingTemplate;
    }

    public void setRenderingTemplate(boolean isTemplate) {
        this._renderingTemplate = isTemplate;
    }

    protected String endpoint;
    public String getEndpoint() {
        return endpoint;
    }
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
