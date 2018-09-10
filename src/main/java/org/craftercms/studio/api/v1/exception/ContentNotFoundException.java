/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2016 Crafter Software Corporation.
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
package org.craftercms.studio.api.v1.exception;

public class ContentNotFoundException extends ServiceLayerException {

	/**
	 * 
	 */
	protected static final long serialVersionUID = 8988159536378832232L;

    protected String path;

    protected String site;

    public ContentNotFoundException() {
	}

    public ContentNotFoundException(String path,String site,String message) {
		super(message);
        this.path = path;
        this.site = site;
    }

	public ContentNotFoundException(Exception e) {
		super(e);
	}

	public ContentNotFoundException(String message) {
		super(message);
	}

	public ContentNotFoundException(String message, Exception e) {
		super(message, e);
	}

    public String getPath() {
        return path;
    }
    public String getSite() {
        return site;
    }
}
