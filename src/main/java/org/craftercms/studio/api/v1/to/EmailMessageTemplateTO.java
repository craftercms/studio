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

public class EmailMessageTemplateTO implements Serializable {

    private static final long serialVersionUID = 5222897831966329668L;
    /** message title **/
	protected String _subject;
	/** message body **/
	protected String _message;


	public EmailMessageTemplateTO(final String _subject, final String _message) {
		this._subject = _subject;
		this._message = _message;
	}

	public EmailMessageTemplateTO() {
	}

	/**
	 * @return the title
	 */
	public String getSubject() {
		return _subject;
	}

	/**
	 * @param subject
	 *            the title to set
	 */
	public void setSubject(final String subject) {
		this._subject = subject;
	}

	/**
	 * @return the body
	 */
	public String getMessage() {
		return _message;
	}

	/**
	 * @param message
	 *            the body to set
	 */
	public void setMessage(final String message) {
		this._message = message;
	}
	
}
