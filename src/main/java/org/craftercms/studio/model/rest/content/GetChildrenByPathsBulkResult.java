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

package org.craftercms.studio.model.rest.content;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Collection;

public class GetChildrenByPathsBulkResult {

    private Collection<ChildrenByPathResult> items;
    private Collection<String> missingItems;

    public GetChildrenByPathsBulkResult(Collection<ChildrenByPathResult> items, Collection<String> missingItems) {
        this.items = items;
        this.missingItems = missingItems;
    }

    public Collection<ChildrenByPathResult> getItems() {
        return items;
    }

    public void setItems(Collection<ChildrenByPathResult> items) {
        this.items = items;
    }

    public Collection<String> getMissingItems() {
        return missingItems;
    }

    public void setMissingItems(Collection<String> missingItems) {
        this.missingItems = missingItems;
    }

    public static class ChildrenByPathResult {

        private String path;
        private SandboxItem item;
        private GetChildrenResult result;

        @JsonUnwrapped
        public GetChildrenResult getResult() {
            return result;
        }

        public void setResult(GetChildrenResult result) {
            this.result = result;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public SandboxItem getItem() {
            return item;
        }

        public void setItem(SandboxItem item) {
            this.item = item;
        }
    }
}
