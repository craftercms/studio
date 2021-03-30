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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.craftercms.studio.model.rest.SiteAwareRequest;

import java.util.List;

/**
 * Base class for any getChildren* request
 *
 * @author joseross
 * @since 4.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class GetChildrenRequest extends SiteAwareRequest {

    protected  String localeCode;

    protected  String keyword;

    protected List<String> excludes;

    protected String sortStrategy;

    protected String order = "ASC";

    protected int offset = 0;

    protected int limit = 10;

    public String getLocaleCode() {
        return localeCode;
    }

    public void setLocaleCode(String localeCode) {
        this.localeCode = localeCode;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public String getSortStrategy() {
        return sortStrategy;
    }

    public void setSortStrategy(String sortStrategy) {
        this.sortStrategy = sortStrategy;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

}