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
package org.craftercms.studio.model.rest.clipboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.craftercms.studio.model.clipboard.Operation;
import org.craftercms.studio.model.clipboard.PasteItem;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Holds all data needed for a clipboard operation
 *
 * @author joseross
 * @since 3.2
 */
public class PasteRequest {

    /**
     * The id of the site
     */
    @NotEmpty
    protected String siteId;

    /**
     * The operation to perform
     */
    @NotNull
    protected Operation operation;

    /**
     * The target path
     */
    @NotEmpty
    protected String targetPath;

    /**
     * The item to be copied/moved
     */
    @Valid
    @NotNull
    protected PasteItem item;

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public PasteItem getItem() {
        return item;
    }

    public void setItem(PasteItem item) {
        this.item = item;
    }

}
