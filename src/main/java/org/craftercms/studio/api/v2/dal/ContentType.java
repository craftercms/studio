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

package org.craftercms.studio.api.v2.dal;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

public class ContentType {

    protected String name = null;
    protected String label = null;
    protected String form = null;
    protected String formPath = null;
    protected String type = null;
    protected boolean contentAsFolder = false;
    protected boolean useRoundedFolder = false;
    protected Set<String> allowedRoles = null;
    protected ZonedDateTime lastUpdated;
    protected List<DeleteDependency> deleteDependencies = null;
    protected List<CopyDependency> copyDepedencyPattern=null;
    protected boolean isPreviewable = false;
    protected String imageThumbnail;
    protected boolean noThumbnail;
    protected List<String> pathIncludes;
    protected List<String> pathExcludes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getFormPath() {
        return formPath;
    }

    public void setFormPath(String formPath) {
        this.formPath = formPath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isContentAsFolder() {
        return contentAsFolder;
    }

    public void setContentAsFolder(boolean contentAsFolder) {
        this.contentAsFolder = contentAsFolder;
    }

    public boolean isUseRoundedFolder() {
        return useRoundedFolder;
    }

    public void setUseRoundedFolder(boolean useRoundedFolder) {
        this.useRoundedFolder = useRoundedFolder;
    }

    public Set<String> getAllowedRoles() {
        return allowedRoles;
    }

    public void setAllowedRoles(Set<String> allowedRoles) {
        this.allowedRoles = allowedRoles;
    }

    public ZonedDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(ZonedDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<DeleteDependency> getDeleteDependencies() {
        return deleteDependencies;
    }

    public void setDeleteDependencies(List<DeleteDependency> deleteDependencies) {
        this.deleteDependencies = deleteDependencies;
    }

    public List<CopyDependency> getCopyDepedencyPattern() {
        return copyDepedencyPattern;
    }

    public void setCopyDepedencyPattern(List<CopyDependency> copyDepedencyPattern) {
        this.copyDepedencyPattern = copyDepedencyPattern;
    }

    public boolean isPreviewable() {
        return isPreviewable;
    }

    public void setPreviewable(boolean previewable) {
        isPreviewable = previewable;
    }

    public String getImageThumbnail() {
        return imageThumbnail;
    }

    public void setImageThumbnail(String imageThumbnail) {
        this.imageThumbnail = imageThumbnail;
    }

    public boolean isNoThumbnail() {
        return noThumbnail;
    }

    public void setNoThumbnail(boolean noThumbnail) {
        this.noThumbnail = noThumbnail;
    }

    public List<String> getPathIncludes() {
        return pathIncludes;
    }

    public void setPathIncludes(List<String> pathIncludes) {
        this.pathIncludes = pathIncludes;
    }

    public List<String> getPathExcludes() {
        return pathExcludes;
    }

    public void setPathExcludes(List<String> pathExcludes) {
        this.pathExcludes = pathExcludes;
    }

    public class DeleteDependency {

        protected String pattern;
        protected boolean removeEmptyFolder;

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public boolean isRemoveEmptyFolder() {
            return removeEmptyFolder;
        }

        public void setRemoveEmptyFolder(boolean removeEmptyFolder) {
            this.removeEmptyFolder = removeEmptyFolder;
        }
    }

    public class CopyDependency {

        protected String pattern;
        protected String target;

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }
    }
}
