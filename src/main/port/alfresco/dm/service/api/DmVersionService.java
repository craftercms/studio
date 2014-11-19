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

package org.craftercms.cstudio.alfresco.dm.service.api;

// Java imports
import java.util.List;
import java.util.Map;

// internal imports
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.craftercms.cstudio.alfresco.dm.to.DmVersionDetailTO;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;

/**
 * Holds Version Service specific methods for content in DM 
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
	public List<DmVersionDetailTO> getVersionHistory(String site,String noderef, int maxHistory) throws ServiceException;
	
	public void restore(String site, String noderef,String version,Boolean deep) throws ServiceException;
	
	/**
	 * Creates a new version of <code>nodeRef</code> instance using alfresco version service  and returns a new <code>DmVersionDetailTO</code> instance.
	 * 
	 * @param nodeRef used to create a new version
	 * 
	 * @param versionProperties map to create the version
	 * 
	 * @return a new <code>DmVersionDetailTO</code> instance
	 */
	public DmVersionDetailTO createVersion(String nodeRef, Map versionProperties) throws ServiceException;
	
	/**
	 * Creates a new version of <code>nodeRef</code> instance using alfresco version service  and returns a new <code>DmVersionDetailTO</code> instance.
	 * 
	 * @param nodeRef used to create a new version
	 * 
	 * @param versionProperties map to create the version
	 * 
	 * @return a new <code>DmVersionDetailTO</code> instance
	 */
	public DmVersionDetailTO createVersion(NodeRef nodeRef, Map versionProperties) throws ServiceException;
	
	/**
	 * Gets the current version of a node.
	 * 
	 * @param nodeRef queried
	 * 
	 * 
	 */
	public DmVersionDetailTO getCurrentVersion(String noderef) throws ServiceException;
	
	/**
	 * Gets the current version of a node.
	 * 
	 * @param nodeRef queried
	 * 
	 * 
	 */
	public DmVersionDetailTO getCurrentVersion(NodeRef noderef) throws ServiceException;
	
	/**
	 * Deletes version history of the node passed as argument
	 * 
	 * @param nodeRef used to delete version history
	 * 
	 */
	public void deleteVersionHistory(String nodeRef) throws ServiceException;
	
	/**
	 * Deletes version history of the node passed as argument
	 * 
	 * @param nodeRef used to delete version history
	 * 
	 */
	public void deleteVersionHistory(NodeRef nodeRef) throws ServiceException;
	
	/**
	 * Deletes version of the node passed as argument
	 * 
	 * @param nodeRef used to delete a version
	 * 
	 * @param version to be deleted
	 * 
	 */
	void deleteVersion(NodeRef nodeRef, Version version) throws ServiceException;
	
	/**
	 * Deletes version of the node passed as argument
	 * 
	 * @param nodeRef used to delete a version
	 * 
	 * @param version to be deleted
	 * 
	 */
	void deleteVersion(String nodeRef, Version version) throws ServiceException;
	
    public void createNextMajorVersion(String site, String path);

    public void createNextMajorVersion(String site, List<String> pathList);
}
