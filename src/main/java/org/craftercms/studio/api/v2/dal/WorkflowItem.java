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

import java.time.ZonedDateTime;

public class WorkflowItem {

    private long id;
    private Item item;
    private String targetEnvironment;
    private String state;
    private long submitterId;
    private String submitterComment;
    private long reviewerId;
    private String reviewerComment;
    private int notifySubmitter;
    private ZonedDateTime schedule;
    private String publishingPackageId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getTargetEnvironment() {
        return targetEnvironment;
    }

    public void setTargetEnvironment(String targetEnvironment) {
        this.targetEnvironment = targetEnvironment;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(long submitterId) {
        this.submitterId = submitterId;
    }

    public String getSubmitterComment() {
        return submitterComment;
    }

    public void setSubmitterComment(String submitterComment) {
        this.submitterComment = submitterComment;
    }

    public long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerComment() {
        return reviewerComment;
    }

    public void setReviewerComment(String reviewerComment) {
        this.reviewerComment = reviewerComment;
    }

    public int getNotifySubmitter() {
        return notifySubmitter;
    }

    public void setNotifySubmitter(int notifySubmitter) {
        this.notifySubmitter = notifySubmitter;
    }

    public ZonedDateTime getSchedule() {
        return schedule;
    }

    public void setSchedule(ZonedDateTime schedule) {
        this.schedule = schedule;
    }

    public String getPublishingPackageId() {
        return publishingPackageId;
    }

    public void setPublishingPackageId(String publishingPackageId) {
        this.publishingPackageId = publishingPackageId;
    }
}
