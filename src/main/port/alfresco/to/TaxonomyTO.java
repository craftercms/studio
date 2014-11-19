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
 * Class for storing taxonomy specific metadata
 * 
 * @author videepkumar1
 * 
 */
public class TaxonomyTO {

	// instance properties 
	protected String _name = null;
	protected String _type = null; // prefixed qname 
	protected String _label = null;
	protected long _id;
	protected String _nodeRef = null; // instance noderef string
	protected boolean _created = false;
	protected boolean _updated = false;
	protected boolean _deleted = false;
	protected boolean _isCurrent = false;
	protected long _order;
	protected String _parent = null; // parent noderef string
	protected String _iconPath = null;
	protected List<TaxonomyTO> _children = null;

	/**
	 * @return the name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this._name = name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return _type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this._type = type;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return _label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this._label = label;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return _id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this._id = id;
	}

	/**
	 * @return the nodeRef
	 */
	public String getNodeRef() {
		return _nodeRef;
	}

	/**
	 * @param nodeRef
	 *            the nodeRef to set
	 */
	public void setNodeRef(String nodeRef) {
		this._nodeRef = nodeRef;
	}

	/**
	 * @return the created
	 */
	public boolean isCreated() {
		return _created;
	}

	/**
	 * @param created
	 *            the created to set
	 */
	public void setCreated(boolean created) {
		this._created = created;
	}

	/**
	 * @return the updated
	 */
	public boolean isUpdated() {
		return _updated;
	}

	/**
	 * @param updated
	 *            the updated to set
	 */
	public void setUpdated(boolean updated) {
		this._updated = updated;
	}

	/**
	 * @return the deleted
	 */
	public boolean isDeleted() {
		return _deleted;
	}

	/**
	 * @param deleted
	 *            the deleted to set
	 */
	public void setDeleted(boolean deleted) {
		this._deleted = deleted;
	}

	/**
	 * @return the isCurrent
	 */
	public boolean isCurrent() {
		return _isCurrent;
	}

	/**
	 * @param isCurrent
	 *            the isCurrent to set
	 */
	public void setCurrent(boolean isCurrent) {
		this._isCurrent = isCurrent;
	}

	/**
	 * @return the order
	 */
	public long getOrder() {
		return _order;
	}

	/**
	 * @param order
	 *            the order to set
	 */
	public void setOrder(long order) {
		this._order = order;
	}

	/**
	 * @return the parent
	 */
	public String getParent() {
		return _parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(String parent) {
		this._parent = parent;
	}

	/**
	 * @return the children
	 */
	public List<TaxonomyTO> getChildren() {
		return _children;
	}

	/**
	 * @param children
	 *            the children to set
	 */
	public void setChildren(List<TaxonomyTO> children) {
		this._children = children;
	}

	/**
	 * @param iconPath the iconPath to set
	 */
	public void setIconPath(String iconPath) {
		this._iconPath = iconPath;
	}

	/**
	 * @return the iconPath
	 */
	public String getIconPath() {
		return _iconPath;
	}

}
