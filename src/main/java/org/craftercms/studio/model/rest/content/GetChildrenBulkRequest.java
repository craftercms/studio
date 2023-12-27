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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;
import org.craftercms.commons.validation.annotations.param.ValidExistingContentPath;
import org.craftercms.commons.validation.annotations.param.ValidateNoTagsParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;

import javax.validation.Valid;
import java.util.List;

import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.ALPHANUMERIC;
import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.SITE_ID;

/**
 * Holds the parameters  for the getChildrenByPaths request
 *
 * @since 4.0.8
 */
@JsonIgnoreProperties
public class GetChildrenBulkRequest {

    @EsapiValidatedParam(type = SITE_ID)
    private String siteId;
    private List<@Valid PathParams> paths;

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public List<PathParams> getPaths() {
        return paths;
    }

    public void setPaths(List<PathParams> paths) {
        this.paths = paths;
    }

    public static class PathParams {
        @ValidExistingContentPath
        private String path;
        @ValidateNoTagsParam
        private String localeCode;
        @ValidateNoTagsParam
        private String keyword;
        private List<@EsapiValidatedParam(type = ALPHANUMERIC) String> systemTypes;
        private List<@ValidExistingContentPath String> excludes;
        @ValidateStringParam(whitelistedPatterns = "alphabetical|foldersFirst")
        private String sortStrategy;
        @ValidateStringParam(whitelistedPatterns = "(?i)(ASC|DESC)")
        private String order = "ASC";
        private int offset = 0;
        private int limit = 10;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

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

        public List<String> getSystemTypes() {
            return systemTypes;
        }

        public void setSystemTypes(List<String> systemTypes) {
            this.systemTypes = systemTypes;
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
}
