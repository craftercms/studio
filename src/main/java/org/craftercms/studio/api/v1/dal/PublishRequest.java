/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * @author Dejan Brkic
 */
public class PublishRequest implements Serializable {

    private static final long serialVersionUID = 1287567542272432148L;

    private long id;
    private String site;
    private String environment;
    private String path;
    private String oldPath;
    private String user;
    private ZonedDateTime scheduledDate;
    private String state;
    private String action;
    private String contentTypeClass;
    private String submissionComment;
    private String commitId;
    private String packageId;

    public class State {
        public final static String READY_FOR_LIVE = "READY_FOR_LIVE";
        public final static String PROCESSING = "PROCESSING";
        public final static String COMPLETED = "COMPLETED";
        public final static String CANCELLED = "CANCELLED";
        public final static String BLOCKED = "BLOCKED";
    }

    public class Action {
        public final static String NEW = "NEW";
        public final static String UPDATE = "UPDATE";
        public final static String DELETE = "DELETE";
        public final static String MOVE = "MOVE";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public ZonedDateTime getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(ZonedDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getContentTypeClass() {
        return contentTypeClass;
    }

    public void setContentTypeClass(String contentTypeClass) {
        this.contentTypeClass = contentTypeClass;
    }

    public String getSubmissionComment() {
        return submissionComment;
    }

    public void setSubmissionComment(String submissionComment) {
        this.submissionComment = submissionComment;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }
}
