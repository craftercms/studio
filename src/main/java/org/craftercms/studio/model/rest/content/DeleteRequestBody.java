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

import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;

import javax.validation.constraints.NotEmpty;
import java.util.List;

import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.HTTPURI;
import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.SITE_ID;

public class DeleteRequestBody {

    @EsapiValidatedParam(type= SITE_ID)
    private String siteId;
    @NotEmpty
    private List<@NotEmpty @EsapiValidatedParam(type = HTTPURI) @ValidateSecurePathParam String> items;
    private List<@NotEmpty @EsapiValidatedParam(type=HTTPURI) @ValidateSecurePathParam String> optionalDependencies;
    private String comment;

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public List<String> getOptionalDependencies() {
        return optionalDependencies;
    }

    public void setOptionalDependencies(List<String> optionalDependencies) {
        this.optionalDependencies = optionalDependencies;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
