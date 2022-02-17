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

public class MessageTO implements Serializable {

    private static final long serialVersionUID = 2139304695120112701L;
    /** message title **/
	protected String _title;
	/** message body **/
	protected String _body;

	protected String key;

	public MessageTO(final String _title, final String _body,final String key) {
		this._title = _title;
		this._body = _body;
		this.key = key;
	}

	public MessageTO() {
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return _title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(final String title) {
		this._title = title;
	}

	/**
	 * @return the body
	 */
	public String getBody() {
		return _body;
	}

	/**
	 * @param body
	 *            the body to set
	 */
	public void setBody(final String body) {
		this._body = body;
	}


	public String getKey() {
		return key;
	}

	public void setKey(final String key) {
		this.key = key;
	}
}
