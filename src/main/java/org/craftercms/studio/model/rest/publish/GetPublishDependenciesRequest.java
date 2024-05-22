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

package org.craftercms.studio.model.rest.publish;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.craftercms.commons.validation.annotations.param.ValidSiteId;
import org.craftercms.studio.api.v2.service.publish.PublishService.PublishRequestPath;

import java.util.List;

import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.ALPHANUMERIC;

/**
 * Request for publishing dependencies
 */
public class GetPublishDependenciesRequest {

    @NotEmpty
    @ValidSiteId
    private String siteId;
    @NotEmpty
    @Size(max = 20)
    @EsapiValidatedParam(type = ALPHANUMERIC)
    private String publishingTarget;
    private List<@Valid PublishRequestPath> paths;
    private List<@NotEmpty String> commitIds;

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

    public List<PublishRequestPath> getPaths() {
        return paths;
    }

    public void setPaths(List<PublishRequestPath> paths) {
        this.paths = paths;
    }

    public List<String> getCommitIds() {
        return commitIds;
    }

    public void setCommitIds(List<String> commitIds) {
        this.commitIds = commitIds;
    }
}
