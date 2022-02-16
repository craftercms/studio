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

import org.apache.http.HttpStatus;

/**
 * Service call result object to be used in javascript API
 * 
 * @author hyanghee
 * 
 */
public class ResultTO implements Serializable {

	/**
	 * 
	 */
	protected static final long serialVersionUID = 235517104847909394L;
	
	/** is the call success? **/
	protected boolean _success = false;
	protected int status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
	/** success or failure message **/
	protected String _message = "";
	/** response item **/
	protected Serializable _item;

    protected boolean _invalidateCache = false;

    protected String commitId = null;

	/**
	 * @param success
	 *            the success to set
	 */
	public void setSuccess(final boolean success) {
		this._success = success;
	}

	/**
	 * @return the success
	 */
	public boolean isSuccess() {
		return _success;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(final String message) {
		this._message = message;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return _message;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param item the item to set
	 */
	public void setItem(Serializable item) {
		this._item = item;
	}

	/**
	 * @return the item
	 */
	public Serializable getItem() {
		return _item;
	}

    public boolean isInvalidateCache() {
        return _invalidateCache;
    }

    public void setInvalidateCache(boolean _invalidateCache) {
        this._invalidateCache = _invalidateCache;
    }

    public String getCommitId() { return commitId; }
    public void setCommitId(String commitId) { this.commitId = commitId; }
}
