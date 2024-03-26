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

package org.craftercms.studio.model.deployer;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.Map;

/**
 * Request body for Deployer API to duplicate a target.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DuplicateTargetRequest {
    private final String siteName;
    @JsonUnwrapped
    private final Map<String, Object> templateParams;

    public DuplicateTargetRequest(final String siteId, final Map<String, Object> templateParams) {
        this.siteName = siteId;
        this.templateParams = templateParams;
    }

    public String getSiteName() {
        return siteName;
    }

    @JsonUnwrapped
    @JsonAnyGetter
    public Map<String, Object> getTemplateParams() {
        return templateParams;
    }
}
