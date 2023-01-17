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

package org.craftercms.studio.model.rest;

import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.HTTPURI;
import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.SITE_ID;

public class ResolveConflictRequest {

    @NotEmpty
    @Size(max = 50)
    @EsapiValidatedParam(type = SITE_ID)
    private String siteId;
    @NotEmpty
    @ValidateSecurePathParam
    @EsapiValidatedParam(type = HTTPURI)
    private String path;
    @ValidateStringParam(whitelistedPatterns = "ours|theirs")
    private String resolution;

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
}
