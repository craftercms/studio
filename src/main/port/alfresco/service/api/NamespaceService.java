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

import java.util.Collection;

import org.alfresco.service.namespace.QName;

/**
 * Provides services for creating QNames and getting namespace-related information.
 * 
 * @author hyanghee
 * 
 */
public interface NamespaceService {

	/**
	 * create QName from the given prefixedQName (e.g. cm:content)
	 * 
	 * @param prefixedQName
	 * @return QName if the given QName is in a valid format. Otherwise it
	 *         returns null
	 */
	public QName createQName(String prefixedQName);

	/**
	 * create a valid QName for the given content name
	 * 
	 * @param contentName
	 * @return content QName if contentName is not empty. Otherwise it returns
	 *         null
	 */
	public QName createContentName(String contentName);

	/**
	 * get prefixes given the namespace URI
	 * 
	 * @param namespaceURI
	 * @return a collection of prefixes registered in Alfresco
	 */
	public Collection<String> getPrefixes(String namespaceURI);

	/**
	 * get the prefixed type name of the given type
	 * 
	 * @param type
	 * @return prefixed type name (e.g cm:name)
	 */
	public String getPrefixedTypeName(QName type);
	
	/**
	 * get the prefixed property name of the given type
	 * 
	 * @param property
	 * @return prefixed property name
	 */
	public String getPrefixedPropertyName(QName property);

	
}
