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
package org.craftercms.cstudio.alfresco.dm.util.api;

import org.alfresco.service.cmr.repository.NodeRef;
import org.dom4j.Node;

public interface DmImportService {

	/**
	 * import from a file system location
	 * 
	 * @param site
	 * 			site to import to
	 * @param publishChannelGroup 
	 * 			publishing channel group 
	 * @param node
	 * 			a XML node that contains target folders configuration
	 * @param fileRoot
	 * 			the file system location root to import data from
	 * @param targetRoot
	 * 			the target location root path
	 * @param targetRef
	 * 			noderef of the target location
	 * @param publish
	 * 			publish content imported?
	 * @param chunkSize
	 * 			# of items to submit in batch. a size less than 1 will submit all items together
	 * @param delayInterval 
	 * 			how often to add delay during import in sec
	 * @param delayLength 
	 * 			how long to pause importing
	 * @throws Exception If something went wrong (Exception is logged)
	 */
	public void importFromConfigNode(String site, String publishChannelGroup, 
			Node node, String fileRoot, String targetRoot, NodeRef targetRef, boolean publish, int chunkSize, int delayInterval, int delayLength) throws Exception;
	
	/**
	 * publish imported contents
	 * 
	 * @param site
	 * @param publishChannelGroup
	 * @param targetRoot
	 * 			the imported contents root location
	 * @param targetRef
	 * 			the imported contents root location noderef
	 * @param startPath
	 * 			the path to start publishing from
	 * @param chunkSize
	 * 			# of items to submit in batch. a size less than 1 will submit all items together
	 */
	public void publishImprotedContents(String site, String publishChannelGroup, String targetRoot, 
			NodeRef targetRef, String startPath, int chunkSize);
	
}
