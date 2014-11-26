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



import java.util.List;



/**
 * Class for storing taxonomy type specific metadata
 * 
 * @author videepkumar1
 * 
 */
public class TaxonomyTypeTO {

	protected String name = null;
	protected List<TaxonomyTO> terms = null;
	

	/*
	 * Getters and Setters
	 */
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public List<TaxonomyTO> getTerms() {
		return terms;
	}
	public void setTerms(List<TaxonomyTO> terms) {
		this.terms = terms;
	}
	
}
