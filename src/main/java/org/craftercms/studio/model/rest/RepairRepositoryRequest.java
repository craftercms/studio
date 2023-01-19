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
import org.craftercms.studio.api.v1.constant.GitRepositories;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.SITE_ID;

/**
 * Repository Repair Request
 */
public class RepairRepositoryRequest {
    @NotEmpty
    @Size(max = 50)
    @EsapiValidatedParam(type = SITE_ID)
    protected String siteId;

    @NotNull
    protected GitRepositories repositoryType;

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(final String siteId) {
        this.siteId = siteId;
    }

    public GitRepositories getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(final GitRepositories repositoryType) {
        this.repositoryType = repositoryType;
    }
}
