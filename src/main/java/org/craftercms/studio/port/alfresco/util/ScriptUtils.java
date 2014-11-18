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
package org.craftercms.cstudio.alfresco.util;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.webscripts.Status;

import org.craftercms.cstudio.alfresco.to.ResultTO;

/**
 * script util methods
 * 
 * @author hyanghee
 *
 */
public class ScriptUtils {
	

	/**
	 * create a success result
	 * 
	 * @param item
	 * @return success result
	 */
	public static ResultTO createSuccessResult(Serializable item) {
		return createResult(true, Status.STATUS_OK, item); 
	}

	/**
	 * create a failure result
	 * 
	 * @param status
	 * @param message
	 * @return failure result
	 */
	public static ResultTO createFailureResult(int status, String message) {
		if (!StringUtils.isEmpty(message)) {
			String target = "Exception: ";
			int index = message.lastIndexOf(target);
			if (index >= 0) {
				index += target.length();
				message = message.substring(index);
			}
		}
		return createResult(false, status, message); 
	}
	
	/**
	 * create a result object
	 * 
	 * @param success
	 * @param status
	 * @param item
	 * 			response item. an error message must be passed in the error case
	 * @return result
	 */
	protected static ResultTO createResult(boolean success, int status, Serializable item) {
		ResultTO result = new ResultTO();
		result.setStatus(status);
		result.setSuccess(success);
		if (success) {
			result.setItem(item);
			result.setMessage("");
		} else {
			result.setItem(null);
			result.setMessage((String) item);
		}
		return result;
	}
	
}
