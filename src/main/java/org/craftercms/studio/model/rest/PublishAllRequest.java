/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.model.rest;


import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.craftercms.commons.validation.annotations.param.ValidSiteId;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.ALPHANUMERIC;

/**
 * Request to publish all content in a site
 */
public class PublishAllRequest {
    @NotEmpty
    @ValidSiteId
    protected String siteId;
    @NotEmpty
    @Size(max = 20)
    @EsapiValidatedParam(type = ALPHANUMERIC)
    protected String publishingTarget;

    protected String submissionComment;

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getPublishingTarget() {
        return publishingTarget;
    }

    public void setPublishingTarget(String publishingTarget) {
        this.publishingTarget = publishingTarget;
    }

    public String getSubmissionComment() {
        return submissionComment;
    }

    public void setSubmissionComment(String submissionComment) {
        this.submissionComment = submissionComment;
    }
}
