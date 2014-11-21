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
package org.craftercms.cstudio.alfresco.sequence;

import java.sql.SQLException;

import org.craftercms.cstudio.alfresco.service.exception.SequenceException;
import org.craftercms.cstudio.alfresco.to.SequenceTO;


public interface SequenceDaoService {

    public void initIndexes();
	
	/**
	 * get the next step for the given namespace
	 * 
	 * @param namespace
	 * @return sequence
	 * @throws SQLException
	 */
	public SequenceTO getIdSpace(String namespace, boolean create) throws SQLException;

	/**
	 * get sequence by the namespace given
	 * 
	 * @param namespace
	 *            the spoke of the sequence. (case insensitive)
	 * @return sequence. null if the given namspace does not exists
	 */
	public SequenceTO getSequence(String namespace);
	
	/**
	 * create a sequence for the given namespace. This method will return true
	 * if a sequence already exists for the given namespace
	 * 
	 * @param namespace
	 *            the spoke of the sequence. (case insensitive)
	 * @return true if sequence created
	 */
	public SequenceTO createSequence(String namespace);
	
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
