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

package org.craftercms.studio.api.v1.service.dependency;

import java.util.Map;
import java.util.Set;

/**
 * Resolves dependencies
 */
public interface DependencyResolver {

    String XML_CONFIGURATION_ROOT_ELEMENT = "item-types";
    String XML_CONFIGURATION_ITEM_TYPE = "item-type";
    String XML_CONFIGURATION_NAME = "name";
    String XML_CONFIGURATION_INCLUDES = "includes";
    String XML_CONFIGURATION_EXCLUDES = "excludes";
    String XML_CONFIGURATION_PATH_PATTERN = "path-pattern";
    String XML_CONFIGURATION_DEPENDENCY_TYPES = "dependency-types";
    String XML_CONFIGURATION_DEPENDENCY_TYPE = "dependency-type";
    String XML_CONFIGURATION_PATTERN = "pattern";
    String XML_CONFIGURATION_FIND_REGEX = "find-regex";
    String XML_CONFIGURATION_TRANSFORMS = "transforms";
    String XML_CONFIGURATION_TRANSFORM = "transform";
    String XML_CONFIGURATION_MATCH = "match";
    String XML_CONFIGURATION_REPLACE = "replace";
    String XML_CONFIGURATION_SPLIT = "split";

    /**
     * Resolves dependent files for given content of given path
     *
     * @param site site identifier
     * @param path path of the item
     * @return set of paths of files that content is dependant on
     */
    Map<String, Set<String>> resolve(String site, String path);

}
