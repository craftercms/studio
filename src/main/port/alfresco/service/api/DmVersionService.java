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

// Java imports
import java.util.List;

// internal imports
import org.craftercms.cstudio.alfresco.dm.to.DmVersionDetailTO;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;

/**
 * Holds Version Service specific methods for content in WCM 
 * 
 * @author SubhashA
 *
 */
public interface DmVersionService {

	
	/**
	 * Get version History
	 * 
	 * @param site
	 * @throws ServiceException
	 */
    public List<DmVersionDetailTO> getVersionHistory(String site, String path, int maxHistory, boolean showMinor) throws ServiceException;

    public void restore(String site, String path, String version) throws ServiceException;

    public void createNextMajorVersion(String site, String path);

    public void createNextMajorVersion(String site, String path, String comment);

    public void createNextMajorVersion(String site, List<String> pathList);

    public void createNextMajorVersion(String site, List<String> pathList, String comment);

    public void createNextMinorVersion(String site, String path);

    public void createNextMinorVersion(String site, String path, String comment);

    public void createNextMinorVersion(String site, List<String> pathList);

    public void createNextMinorVersion(String site, List<String> pathList, String comment);

    public void disableVersionable();

    public void enableVersionable();
}
