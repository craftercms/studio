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

public class SequenceDAO {

	private long _id;
	private String _namespace; // id namespace
	private long _sql_generator; // the current id space
	private int _step; // id space increase amount

	/**
	 * constructor
	 * 
	 */
	public SequenceDAO() {}
	
	/**
	 * constructor
	 * 
	 * @param namespace
	 * @param i
	 * @param step
	 */
	public SequenceDAO(String namespace, int sql_generator, int step) {
		this._namespace = namespace;
		this._sql_generator = sql_generator;
		this._step = step;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return _id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this._id = id;
	}

	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return _namespace;
	}

	/**
	 * @param namespace
	 *            the namespace to set
	 */
	public void setNamespace(String namespace) {
		this._namespace = namespace;
	}

	/**
	 * @return the sql_generator
	 */
	public long getSql_generator() {
		return _sql_generator;
	}

	/**
	 * @param sqlGenerator
	 *            the sql_generator to set
	 */
	public void setSql_generator(long sqlGenerator) {
		_sql_generator = sqlGenerator;
	}

	/**
	 * @return the step
	 */
	public int getStep() {
		return _step;
	}

	/**
	 * @param step
	 *            the step to set
	 */
	public void setStep(int step) {
		this._step = step;
	}

}
