/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.api.v2.dal;

import org.craftercms.studio.model.rest.content.SandboxItem;

import java.time.ZonedDateTime;
import java.util.List;

public class WorkflowQueuePackage {

    private long id;
    private String workflowPackageId;
    private long siteId;
    private String site;
    private String status;
    private String operation;
    private String publishingTarget;
    private ZonedDateTime schedule;
    private long initiatorId;
    private String initiator;
    private String publishingComment;
    private ZonedDateTime completedOn;
    private int numberOfRetries;
    private List<SandboxItem> items;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getWorkflowPackageId() {
        return workflowPackageId;
    }

    public void setWorkflowPackageId(String workflowPackageId) {
        this.workflowPackageId = workflowPackageId;
    }

    public long getSiteId() {
        return siteId;
    }

    public void setSiteId(long siteId) {
        this.siteId = siteId;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getPublishingTarget() {
        return publishingTarget;
    }

    public void setPublishingTarget(String publishingTarget) {
        this.publishingTarget = publishingTarget;
    }

    public ZonedDateTime getSchedule() {
        return schedule;
    }

    public void setSchedule(ZonedDateTime schedule) {
        this.schedule = schedule;
    }

    public long getInitiatorId() {
        return initiatorId;
    }

    public void setInitiatorId(long initiatorId) {
        this.initiatorId = initiatorId;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public String getPublishingComment() {
        return publishingComment;
    }

    public void setPublishingComment(String publishingComment) {
        this.publishingComment = publishingComment;
    }

    public ZonedDateTime getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(ZonedDateTime completedOn) {
        this.completedOn = completedOn;
    }

    public int getNumberOfRetries() {
        return numberOfRetries;
    }

    public void setNumberOfRetries(int numberOfRetries) {
        this.numberOfRetries = numberOfRetries;
    }

    public List<SandboxItem> getItems() {
        return items;
    }

    public void setItems(List<SandboxItem> items) {
        this.items = items;
    }
}
