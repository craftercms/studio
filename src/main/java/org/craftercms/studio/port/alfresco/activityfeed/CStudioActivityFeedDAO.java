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
package org.craftercms.cstudio.alfresco.activityfeed;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.util.ISO8601DateFormat;
import org.json.JSONException;
import org.json.JSONObject;

public class CStudioActivityFeedDAO implements Serializable {

	/**
	 * 
	 */
	protected static final long serialVersionUID = -3936087346732462454L;
	/**
	 * dependency properties
	 */
	protected long id;
	protected Date modifiedDate;
	protected Date creationDate;
	protected String summary;
	protected String summaryFormat;
	protected String contentId;
	protected String contentType;
	protected String type;
	protected String userId;
	protected String siteNetwork;
	
	/**
	 * default constructor
	 */
	public CStudioActivityFeedDAO() {
		modifiedDate= new Date();
	}

	public String getSummaryFormat() {
		return summaryFormat;
	}

	public void setSummaryFormat(String summaryFormat) {
		this.summaryFormat = summaryFormat;
	}
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSiteNetwork() {
		return siteNetwork;
	}

	public void setSiteNetwork(String siteNetwork) {
		this.siteNetwork = siteNetwork;
	}
	
	public String getJSONString() throws JSONException
    {
        JSONObject jo = new JSONObject();
        
        jo.put("id", id);
        jo.put("postUserId", userId);
        jo.put("postDate", ISO8601DateFormat.format(modifiedDate));
        if (userId != null) { jo.put("feedUserId", userId); } // eg. site feed
        jo.put("siteNetwork", siteNetwork);
        jo.put("activityType", type);
        jo.put("activitySummary", summary);
        jo.put("activitySummaryFormat", summaryFormat);
        jo.put("contentId", contentId);
        
        return jo.toString();
    }	
}
