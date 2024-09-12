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

import jakarta.validation.constraints.NotEmpty;
import org.craftercms.commons.validation.annotations.param.ValidSiteId;

/**
 * Request to enable/disable the publisher task for a site
 */
public class EnablePublisherRequest {
    @NotEmpty
    @ValidSiteId
    private String siteId;
    private boolean enable;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public @NotEmpty @ValidSiteId String getSiteId() {
        return siteId;
    }

    public void setSiteId(@NotEmpty @ValidSiteId String siteId) {
        this.siteId = siteId;
    }
}