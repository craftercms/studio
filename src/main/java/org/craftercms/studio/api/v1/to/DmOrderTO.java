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

/**
 * This class represents the sorting order of DM Content Item
 * 
 * @author hyanghee
 * @author Dejan Brkic
 * 
 */
public class DmOrderTO implements Comparable<DmOrderTO>, Serializable {

    private static final long serialVersionUID = -7831644335720471414L;
    protected String _id;
	protected double _order;
	protected String _name;
	protected String _disabled;
	protected String _placeInNav;

	/**
	 * default constructor
	 */
	public DmOrderTO() {
	}

	/**
	 * copy constructor
	 * 
	 * @param order order
	 */
	public DmOrderTO(DmOrderTO order) {
		this._id = order._id;
		this._order = order._order;
		this._name = order._name;
		this._disabled = order._disabled;
		this._placeInNav = order._placeInNav;
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		this._id = id;
	}

	public double getOrder() {
		return _order;
	}

	public void setOrder(double order) {
		this._order = order;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		this._name = name;
	}

	public String getDisabled() {
		return _disabled;
	}

	public void setDisabled(String disabled) {
		this._disabled = disabled;
	}

	public String getPlaceInNav() {
		return _placeInNav;
	}

	public void setPlaceInNav(String placeInNav) {
		this._placeInNav = placeInNav;
	}

	@Override
	public int compareTo(DmOrderTO o) {
		if (this.getOrder() > o.getOrder()) {
			return 1;
		} else if (this.getOrder() == o.getOrder()) {
			return 0;
		} else {
			return -1;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DmOrderTO other = (DmOrderTO) obj;
		if (!_id.equals(other._id))
			return false;
		return true;
	}

}
