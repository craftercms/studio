/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2017 Crafter Software Corporation.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyResolverConfigTO {

    private Map<String, DependencyType> dependencyTypes = new HashMap<String, DependencyType>();

    public Map<String, DependencyType> getDependencyTypes() {
        return dependencyTypes;
    }

    public void setDependencyTypes(Map<String, DependencyType> dependencyTypes) {
        this.dependencyTypes = dependencyTypes;
    }

    public static class DependencyType {

        private String name;
        private Map<String, MimeType> mimetypes;
        private List<String> includePaths;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Map<String, MimeType> getMimetypes() { return mimetypes; }
        public void setMimetypes(Map<String, MimeType> mimetypes) { this.mimetypes = mimetypes; }

        public List<String> getIncludePaths() {
            return includePaths;
        }

        public void setIncludePaths(List<String> includePaths) {
            this.includePaths = includePaths;
        }
    }

    public static class MimeType {

        private String name;
        private List<String> patterns;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public List<String> getPatterns() { return patterns; }
        public void setPatterns(List<String> patterns) { this.patterns = patterns; }
    }
}
