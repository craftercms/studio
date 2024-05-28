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
    protected PackageState packageState;
    protected String error;
    protected long submitterId;
    protected String comment;
    protected Instant submittedOn;
    protected long reviewerId;
    protected String reviewerComment;
    protected Instant reviewedOn;
    protected Instant publishedOn;
    protected boolean publishAll;
    protected String commitId;
    protected String publishedStagingCommitId;
    protected String publishedLiveCommitId;

    protected PublishPackage() {
    }

    private PublishPackage(final PublishAllBuilder builder) {
        setCommonProperties(builder);
        this.publishAll = true;
    }

    private PublishPackage(PublishBuilder builder) {
        setCommonProperties(builder);
        this.publishAll = false;
        this.schedule = builder.schedule;
    }

    private void setCommonProperties(final AbstractPublishPackageBuilder<?> builder) {
        this.siteId = builder.siteId;
        this.target = builder.target;
        this.commitId = builder.commitId;
        this.comment = builder.comment;
        this.submitterId = builder.submitterId;
        approvalState = builder.requestApproval ? ApprovalState.SUBMITTED : ApprovalState.APPROVED;
    }

    /**
     * Create a builder for a publish all request
     */
    public static PublishAllBuilder publishAll() {
        return new PublishAllBuilder();
    }

    /**
     * Create a builder for a publish request
     */
    public static PublishBuilder builder() {
        return new PublishBuilder();
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

    public PackageState getPackageState() {
        return packageState;
    }

    public void setPackageState(PackageState packageState) {
        this.packageState = packageState;
    }

    public boolean isPublishAll() {
        return publishAll;
    }

    public void setPublishAll(boolean publishAll) {
        this.publishAll = publishAll;
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
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

    @SuppressWarnings("unchecked")
    abstract static class AbstractPublishPackageBuilder<T extends AbstractPublishPackageBuilder<T>> {
        protected long siteId;
        protected String target;
        protected String commitId;
        protected String comment;
        protected long submitterId;
        protected boolean requestApproval;

        public T withSiteId(final long siteId) {
            this.siteId = siteId;
            return (T) this;
        }

        public T withTarget(final String target) {
            this.target = target;
            return (T) this;
        }

        public T withCommitId(final String commitId) {
            this.commitId = commitId;
            return (T) this;
        }

        public T withComment(final String comment) {
            this.comment = comment;
            return (T) this;
        }

        public T withSubmitterId(final long submitterId) {
            this.submitterId = submitterId;
            return (T) this;
        }

        public T withRequestApproval(final boolean requestApproval) {
            this.requestApproval = requestApproval;
            return (T) this;
        }
    }

    /**
     * AbstractPublishPackageBuilder implementation for a publish-all request
     */
    public static class PublishAllBuilder extends AbstractPublishPackageBuilder<PublishAllBuilder> {
        public PublishPackage build() {
            return new PublishPackage(this);
        }
    }

    /**
     * AbstractPublishPackageBuilder implementation for a regular publish request
     */
    public static class PublishBuilder extends AbstractPublishPackageBuilder<PublishBuilder> {
        private Instant schedule;

        public PublishBuilder withSchedule(final Instant schedule) {
            this.schedule = schedule;
            return this;
        }

        public PublishPackage build() {
            return new PublishPackage(this);
        }
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
        READY,
        PROCESSING,
        COMPLETED,
        COMPLETED_WITH_ERRORS,
        FAILED,
        CANCELLED
    }
}
