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
import java.util.Map;

/**
 * This class represents a pool of reviewers that matches a certain content
 * criteria
 * 
 * @author hyanghee
 * 
 */
public class PoolTO {

	/**
	 * the name of reviwer pool
	 */
	protected String name;
	
	/**
	 * a set of criteria of targeting content
	 */
	protected Map<String, List<String>> criteria;
	
	/**
	 * a set of reviewers who can review content matching criteria
	 */
	protected List<String> reviewers;

	/**
	 * workflow related to this pool of reviewers 
	 */
	protected String workflow;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getReviewers() {
		return reviewers;
	}

	public void setReviewers(List<String> reviewers) {
		this.reviewers = reviewers;
	}

	public Map<String, List<String>> getCriteria() {
		return criteria;
	}

	public void setCriteria(Map<String, List<String>> criteria) {
		this.criteria = criteria;
	}

	public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(String workflow) {
		this.workflow = workflow;
	}
}
