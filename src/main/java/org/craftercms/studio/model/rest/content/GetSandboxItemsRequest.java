/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.studio.model.rest.SiteAwareRequest;

/**
 * Base class for any getSandboxItems* request
 *
 * @author joseross
 * @since 4.0
 */
public abstract class GetSandboxItemsRequest extends SiteAwareRequest {

    protected boolean preferContent;

    public boolean isPreferContent() {
        return preferContent;
    }

    public void setPreferContent(boolean preferContent) {
        this.preferContent = preferContent;
    }

}
