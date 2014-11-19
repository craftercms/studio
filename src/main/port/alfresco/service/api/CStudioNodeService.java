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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

public interface CStudioNodeService {

	public NodeRef getCompanyHomeNodeRef();

	/**
	 * @deprecated Use {@link PersistenceManagerService#getNodeRef(String)}
	 */
	@Deprecated
	public NodeRef getNodeRef(String fullPath);

	/**
	 * @deprecated Use {@link PersistenceManagerService#getNodeRef(NodeRef, String)}
	 */
	@Deprecated
	public NodeRef getNodeRef(NodeRef rootNode, String relativePath);

	/**
	 * @deprecated Use {@link PersistenceManagerService#getNodeRef(String, String)}
	 */
	@Deprecated
	public NodeRef getNodeRef(String rootPath, String relativePath);

	public String getNodePath(NodeRef nodeRef);

	public String getRelativeNodePath(NodeRef rootNodeRef, NodeRef nodeRef);

	public NodeRef createNewFolder(String fullPath);

	public NodeRef createNewFolder(String fullPath,
			Map<QName, Serializable> nodeProperties);

	public NodeRef createNewFolder(String parentPath, String folderName);

	public NodeRef createNewFolder(String parentPath, String folderName,
			Map<QName, Serializable> nodeProperties);

	public NodeRef createNewFolder(NodeRef parentNodeRef, String folderName);

	public NodeRef createNewFolder(NodeRef parentNodeRef, String folderName,
			Map<QName, Serializable> nodeProperties);

	public NodeRef createNewFile(String fullPath, InputStream content);

	public NodeRef createNewFile(String fullPath, InputStream content,
			Map<QName, Serializable> nodeProperties);

	public NodeRef createNewFile(String parentPath, String fileName,
			InputStream content);

	public NodeRef createNewFile(String parentPath, String fileName,
			InputStream content, Map<QName, Serializable> nodeProperties);

	public NodeRef createNewFile(NodeRef parentNodeRef, String fileName,
			InputStream content);

	public NodeRef createNewFile(NodeRef parentNodeRef, String fileName,
			InputStream content, Map<QName, Serializable> nodeProperties);

	public void deleteNode(NodeRef nodeRef);
}
