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
package org.craftercms.cstudio.alfresco.service.api;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import org.craftercms.cstudio.alfresco.service.exception.ServiceException;

/**
 * Provides form-related data and configuration.
 * 
 * @author videepkumar1
 *
 */
public interface FormService {
	
	/**
	 * Load form definition given form id
	 * 
	 * @param formId
	 * @return {@link Document}
	 */
	public Document loadForm(String formId) throws ServiceException;

	/**
	 * generic method for getting a given form asset as a string
	 * @param formId
	 * @param componentName
	 * @return component as string
	 */
	public String loadComponentAsString(String formId, String componentName) throws ServiceException;
	
	/**
	 * Load form definition given form id
	 * 
	 * @param formId
	 * @return {@link String}
	 */
	public String loadFormAsString(String formId) throws ServiceException;
}
