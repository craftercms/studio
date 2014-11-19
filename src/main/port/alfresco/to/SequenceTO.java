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
package org.craftercms.cstudio.alfresco.to;

import java.io.Serializable;

/**
 * class for storing sequence information
 * 
 * @author hyanghee
 *
 */
public class SequenceTO implements Serializable {

	/**
	 * 
	 */
	protected static final long serialVersionUID = -1753317263573340045L;

	/**
	 * the namespace of this sequence
	 */
	protected String namespace = null;

	/**
	 * the starting range of the current sequence
	 */
	protected long seqGenerator = 0;

	/**
	 * the next available ID
	 */
	protected long next = 0;
	
	/**
	 * the last available ID
	 */
	protected long last = 0;
	
	/**
	 * the ID space range
	 */
	protected int step = 100;
	
	public SequenceTO(String namespace, long seqGenerator, int step) {
		this.namespace = namespace;
		this.step = step;
		setSeqGenerator(seqGenerator);
	}
	
	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public long getLast() {
		return last;
	}

	public String getNamespace() {
		return namespace;
	}

	public long getSeqGenerator() {
		return seqGenerator;
	}

	public void setSeqGenerator(long seqGenerator) {
		this.seqGenerator = seqGenerator;
		next = seqGenerator * step + 1;
		last = seqGenerator * step + step;
	}

	public long getNext() {
		return next;
	}

	public void setNext(long next) {
		this.next = next;
	}

	public String toString() {
		return "namespace: " + namespace 
			+ ", seqGenerator: " + seqGenerator + ", next: " + next;
	}
}
