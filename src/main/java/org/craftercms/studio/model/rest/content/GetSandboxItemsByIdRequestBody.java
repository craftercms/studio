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
package org.craftercms.studio.model.rest.content;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Holds data for the getSandboxItemsById request
 *
 * @author joseross
 * @since 4.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetSandboxItemsByIdRequestBody {

    @NotEmpty
    private String siteId;
    @NotEmpty
    private List<Long> ids;
    private boolean preferContent;

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public boolean isPreferContent() {
        return preferContent;
    }

    public void setPreferContent(boolean preferContent) {
        this.preferContent = preferContent;
    }
}
