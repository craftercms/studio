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

package org.craftercms.studio.model.rest.marketplace;

import java.util.HashMap;
import java.util.Map;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.craftercms.commons.plugin.model.Version;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Holds the information needed to create a site from a Marketplace Blueprint
 *
 * @author joseross
 * @since 3.1.4
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateSiteRequest {

    @NotBlank
    private String blueprintId;

    @NotNull
    private Version blueprintVersion;

    @NotBlank
    private String siteId;

    private String description;

    @NotBlank
    private String name;

    private Map<String, String> siteParams = new HashMap<>();

    private String sandboxBranch;

    private String remoteName;

    public String getBlueprintId() {
        return blueprintId;
    }

    public void setBlueprintId(final String blueprintId) {
        this.blueprintId = blueprintId;
    }

    public Version getBlueprintVersion() {
        return blueprintVersion;
    }

    public void setBlueprintVersion(final Version blueprintVersion) {
        this.blueprintVersion = blueprintVersion;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(final String siteId) {
        this.siteId = siteId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getSiteParams() {
        return siteParams;
    }

    public void setSiteParams(final Map<String, String> siteParams) {
        this.siteParams = siteParams;
    }

    public String getSandboxBranch() {
        return sandboxBranch;
    }

    public void setSandboxBranch(final String sandboxBranch) {
        this.sandboxBranch = sandboxBranch;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public void setRemoteName(final String remoteName) {
        this.remoteName = remoteName;
    }

}
