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

import org.craftercms.cstudio.alfresco.service.exception.SequenceException;

/**
 * Service provides an abstract mechanism for retrieving unique identities for a
 * given name space
 * 
 * @author hyanghee
 * 
 */
public interface SequenceService {

	/**
	 * get the next identity from the default name space. since this is a
	 * default namespace no assumptions should be made about what ID is returned
	 * beyond the fact that it will be unique over the lifespan of the sequence.
	 * 
	 * @return next available id
	 * @throws SequenceException
	 */
	public long next() throws SequenceException;

	/**
	 * get the next identity for the given namespace. this method will error if
	 * the given name space does not exist.
	 * 
	 * @param namespace
	 *            the spoke of the sequence. (case insensitive)
	 * @return next available id
	 * @throws SequenceException
	 */
	public long next(String namespace) throws SequenceException;

	/**
	 * get the next identity for the given namespace. this method will create
	 * the if it does not exist and create is true
	 * 
	 * @param namespace
	 *            the spoke of the sequence. (case insensitive)
	 * @param create
	 * @return next available id
	 * @throws SequenceException
	 */
	public long next(String namespace, boolean create) throws SequenceException;

	/**
	 * create a sequence for the given namespace. This method will return true
	 * if a sequence already exists for the given namespace
	 * 
	 * @param namespace
	 *            the spoke of the sequence. (case insensitive)
	 * @return true if sequence created
	 */
	public boolean createSequence(String namespace);

	/** 
	 * remove a sequence for the given namespace
	 * 
	 * @param namespace
	 */
	public void deleteSequence(String namespace) throws SequenceException;
	
	/**
	 * does a sequence exist for the given namespace?
	 * 
	 * @param namespace
	 *            the spoke of the sequence. (case insensitive)
	 * @return true if sequence exists
	 */
	public boolean sequenceExists(String namespace);
	
}
