/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.alfresco.to;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jitesh Mehta
 * @since Sep 24, 2010
 */
public class NavigationRootMenuTo {

    protected List<ModuleHookTo> modulehooks = new ArrayList<ModuleHookTo>();

    public List<ModuleHookTo> getModulehooks() {
        return modulehooks;
    }

    public void setModulehooks(List<ModuleHookTo> modulehooks) {
        this.modulehooks = modulehooks;
    }

    public static class ModuleHookTo {
        protected String name = "";

        protected Param params;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Param getParams() {
            return params;
        }

        public void setParams(Param params) {
            this.params = params;
        }


    }

    public static class Param {
        protected String label;
        protected String path;
        protected String showRootItem;
        protected String onClick;
        protected List<CannedSearch> cannedSearches = new ArrayList<CannedSearch>();

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getShowRootItem() {
            return showRootItem;
        }

        public void setShowRootItem(String showRootItem) {
            this.showRootItem = showRootItem;
        }

        public String getOnClick() {
            return onClick;
        }

        public void setOnClick(String onClick) {
            this.onClick = onClick;
        }

        public List<CannedSearch> getCannedSearches() {
            return cannedSearches;
        }

        public void setCannedSearches(List<CannedSearch> cannedSearches) {
            this.cannedSearches = cannedSearches;
        }

        public List<CannedSearch> addParam(CannedSearch cannedSearch) {
            cannedSearches.add(cannedSearch);
            return cannedSearches;
        }

    }

    public static class QueryParam {
        protected String name;
        protected String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class CannedSearch {
        protected String path;
        protected String insertAs;
        protected String label;
        protected String newPath;
        protected List<QueryParam> queryParams = new ArrayList<QueryParam>();

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getInsertAs() {
            return insertAs;
        }

        public void setInsertAs(String insertAs) {
            this.insertAs = insertAs;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getNewPath() {
            return newPath;
        }

        public void setNewPath(String newPath) {
            this.newPath = newPath;
        }

        public List<QueryParam> getQueryParams() {
            return queryParams;
        }

        public void setQueryParams(List<QueryParam> queryParams) {
            this.queryParams = queryParams;
        }
    }
}
