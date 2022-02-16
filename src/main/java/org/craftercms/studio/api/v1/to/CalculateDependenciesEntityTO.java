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

package org.craftercms.studio.api.v1.to;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CalculateDependenciesEntityTO implements Serializable {
    private static final long serialVersionUID = -2965831803399372182L;

    private String item;
    private List<Map<String, String>> dependencies;

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public List<Map<String, String>> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Map<String, String>> dependencies) {
        this.dependencies = dependencies;
    }
}
