/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.api.v1.to;

import java.util.List;
import java.util.Map;

public class DependencyResolverConfigTO {

    private Map<String, ItemType> itemTypes;

    public Map<String, ItemType> getItemTypes() {
        return itemTypes;
    }

    public void setItemTypes(Map<String, ItemType> itemTypes) {
        this.itemTypes = itemTypes;
    }

    public static class ItemType {

        private String name;
        private List<String> includes;
        private Map<String, DependencyType> dependencyTypes;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public List<String> getIncludes() { return includes; }
        public void setIncludes(List<String> includes) { this.includes = includes; }

        public Map<String, DependencyType> getDependencyTypes() { return dependencyTypes; }
        public void setDependencyTypes(Map<String, DependencyType> dependencyTypes) { this.dependencyTypes = dependencyTypes; }
    }

    public static class DependencyType {

        private String name;
        private List<DependencyExtractionPattern> includes;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public List<DependencyExtractionPattern> getIncludes() { return includes; }
        public void setIncludes(List<DependencyExtractionPattern> includes) { this.includes = includes; }
    }

    public static class DependencyExtractionPattern {

        private String findRegex;
        private List<DependencyExtractionTransform> transforms;

        public String getFindRegex() { return findRegex; }
        public void setFindRegex(String findRegex) { this.findRegex = findRegex; }

        public List<DependencyExtractionTransform> getTransforms() { return transforms; }
        public void setTransforms(List<DependencyExtractionTransform> transforms) { this.transforms = transforms; }
    }

    public static class DependencyExtractionTransform {

        private String match;
        private String replace;

        public String getMatch() { return match; }
        public void setMatch(String match) { this.match = match; }

        public String getReplace() { return replace; }
        public void setReplace(String replace) { this.replace = replace; }
    }
}
