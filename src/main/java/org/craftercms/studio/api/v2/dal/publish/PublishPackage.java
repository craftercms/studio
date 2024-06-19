/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.dal.publish;

import org.craftercms.studio.api.v2.dal.Site;

import java.time.Instant;

/**
 * Represents request to publish a content package
 */
public class PublishPackage {

    protected long id;
    protected long siteId;
    protected Site site;
    protected String target;
    protected Instant schedule;
    protected ApprovalState approvalState;
    protected long packageState;
    protected int liveError;
    protected int stagingError;
    protected long submitterId;
    protected String comment;
    protected Instant submittedOn;
    protected long reviewerId;
    protected String reviewerComment;
    protected Instant reviewedOn;
    protected Instant publishedOn;
    protected PackageType packageType;
    protected String commitId;
    protected String publishedStagingCommitId;
    protected String publishedLiveCommitId;

    public PublishPackage() {
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getSiteId() {
        return siteId;
    }

    public void setSiteId(final long siteId) {
        this.siteId = siteId;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(final String target) {
        this.target = target;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(final String commitId) {
        this.commitId = commitId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public long getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(final long submitterId) {
        this.submitterId = submitterId;
    }

    public Instant getSchedule() {
        return schedule;
    }

    public void setSchedule(Instant schedule) {
        this.schedule = schedule;
    }

    public ApprovalState getApprovalState() {
        return approvalState;
    }

    public void setApprovalState(ApprovalState approvalState) {
        this.approvalState = approvalState;
    }

    public long getPackageState() {
        return packageState;
    }

    public void setPackageState(long packageState) {
        this.packageState = packageState;
    }

    public PackageType getPackageType() {
        return packageType;
    }

    public void setPackageType(PackageType packageType) {
        this.packageType = packageType;
    }

    public String getPublishedStagingCommitId() {
        return publishedStagingCommitId;
    }

    public void setPublishedStagingCommitId(String publishedStagingCommitId) {
        this.publishedStagingCommitId = publishedStagingCommitId;
    }

    public String getPublishedLiveCommitId() {
        return publishedLiveCommitId;
    }

    public void setPublishedLiveCommitId(String publishedLiveCommitId) {
        this.publishedLiveCommitId = publishedLiveCommitId;
    }

    public int getLiveError() {
        return liveError;
    }

    public void setLiveError(int liveError) {
        this.liveError = liveError;
    }

    public int getStagingError() {
        return stagingError;
    }

    public void setStagingError(int stagingError) {
        this.stagingError = stagingError;
    }

    public Instant getSubmittedOn() {
        return submittedOn;
    }

    public void setSubmittedOn(Instant submittedOn) {
        this.submittedOn = submittedOn;
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

    public Instant getReviewedOn() {
        return reviewedOn;
    }

    public void setReviewedOn(Instant reviewedOn) {
        this.reviewedOn = reviewedOn;
    }

    public Instant getPublishedOn() {
        return publishedOn;
    }

    public void setPublishedOn(Instant publishedOn) {
        this.publishedOn = publishedOn;
    }

    /**
     * Possible values for the package approval state
     */
    public enum ApprovalState {
        SUBMITTED,
        APPROVED,
        REJECTED
    }

    /**
     * Possible values for the package processing state
     */
    public enum PackageState {
        READY(0),
        PROCESSING(1),
        LIVE_SUCCESS(2),
        LIVE_COMPLETED_WITH_ERRORS(3),
        LIVE_FAILED(4),
        STAGING_SUCCESS(5),
        STAGING_COMPLETED_WITH_ERRORS(6),
        STAGING_FAILED(7),
        CANCELLED(8);

        public final long value;

        PackageState(final long exponent) {
            this.value = Math.round(Math.pow(2, exponent));
        }
    }

    /**
     * Possible values for the package type
     */
    public enum PackageType {
        INITIAL_PUBLISH,
        PUBLISH_ALL,
        ITEM_LIST
    }
}
