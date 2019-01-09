/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.api.v1.dal;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class AuditFeed implements Serializable {

    private static final long serialVersionUID = 4251603625791912910L;
    protected long id;
    protected ZonedDateTime modifiedDate;
    protected ZonedDateTime creationDate;
    protected String summary;
    protected String summaryFormat;
    protected String contentId;
    protected String contentType;
    protected String type;
    protected String userId;
    protected String siteNetwork;
    protected String source;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public ZonedDateTime getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(ZonedDateTime modifiedDate) { this.modifiedDate = modifiedDate; }

    public ZonedDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(ZonedDateTime creationDate) { this.creationDate = creationDate; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getSummaryFormat() { return summaryFormat; }
    public void setSummaryFormat(String summaryFormat) { this.summaryFormat = summaryFormat; }

    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSiteNetwork() { return siteNetwork; }
    public void setSiteNetwork(String siteNetwork) { this.siteNetwork = siteNetwork; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getJSONString() throws JSONException {
        JSONObject jo = new JSONObject();

        jo.put("id", id);
        jo.put("postUserId", userId);
        if (modifiedDate != null) {
            jo.put("postDate", modifiedDate.toString());
        }
        if (userId != null) {
            jo.put("feedUserId", userId);
        } // eg. site feed
        jo.put("siteNetwork", siteNetwork);
        jo.put("activityType", type);
        jo.put("activitySummary", summary);
        jo.put("activitySummaryFormat", summaryFormat);
        jo.put("contentId", contentId);
        jo.put("source", source);

        return jo.toString();
    }
}
