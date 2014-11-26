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
 * contains taxonomy model data instance
 * 
 * @author hyanghee
 * 
 */
public class ModelDataTO {

	/** model instance prefixed QName **/
	protected String _type;
	/** model instance label **/
	protected String _label;
	/** model instance value **/
	protected Serializable _value;
	/** model instance id **/
	protected Long _id;
	/** child model instances **/
	protected List<ModelDataTO> _children;
	/** model description **/
	protected String _description;

	/** item logically deleted */
	protected boolean _deleted;
	/** order */
	protected long _order;

	/** basic constructor **/
	public ModelDataTO() {

	}

	/**
	 * Constructor to use Static values turn into Model
	 * 
	 * @param type
	 * @param label
	 * @param value
	 * @param id
	 * @param description
	 */
	public ModelDataTO(String type, String label, Long id, Serializable value, String description, long order, boolean deleted) {
		this._type = type;
		this._label = label;
		this._value = value;
		this._id = id;
		this._description = description;
		this._children = null;
		this._order = order;
		this._deleted = deleted;
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
		_order = order;
	}

	/**
	 * @return the value
	 */
	public Serializable getValue() {
		return _value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Serializable value) {
		this._value = value;
	}

	/**
	 * @return the children
	 */
	public List<ModelDataTO> getChildren() {
		return _children;
	}

	/**
	 * @param children
	 *            the children to set
	 */
	public void setChildren(List<ModelDataTO> children) {
		this._children = children;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Type:" + this._type + 
		       ", Label:" + this._label +
		       ", Order:"+this._order+
		       ", Deleted:"+this._deleted+
		       ", Value:" + this._value;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this._type = type;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return _type;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this._id = id;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return _id;
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
		_deleted = deleted;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this._description = description;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return _description;
	}
}
