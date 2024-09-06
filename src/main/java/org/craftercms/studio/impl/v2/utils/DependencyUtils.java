/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.utils;

import org.apache.commons.collections4.MapUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.service.dependency.DependencyResolver.ResolvedDependency;
import org.craftercms.studio.api.v2.service.dependency.DependencyService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.*;

/**
 * Utility class for Dependency related helper methods
 */
public class DependencyUtils {

    /**
     * Add the script snippets to update the dependencies for the given path
     *
     * @param siteId  the site id
     * @param path    the content item path
     * @param oldPath the content item old path
     * @param file    the file
     * @throws IOException if an error occurs while updating the script
     */
    public static void addDependenciesScriptSnippets(String siteId, String path, String oldPath, Path file, DependencyService dependencyService)
            throws IOException, ServiceLayerException {
        Map<String, Set<ResolvedDependency>> dependencies = dependencyService.resolveDependencies(siteId, path);
        if (isEmpty(oldPath)) {
            Files.write(file, deleteDependencySourcePathRows(siteId, path).getBytes(UTF_8),
                    StandardOpenOption.APPEND);
            Files.write(file, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
        } else {
            Files.write(file, deleteDependencySourcePathRows(siteId, oldPath).getBytes(UTF_8),
                    StandardOpenOption.APPEND);
            // Invalidate existing dependencies pointing to the old item path
            Files.write(file, invalidateDependencies(siteId, oldPath).getBytes(UTF_8),
                    StandardOpenOption.APPEND);
        }

        // Validate existing broken dependencies pointing to the item path
        Files.write(file, validateDependencies(siteId, path).getBytes(UTF_8),
                StandardOpenOption.APPEND);

        if (MapUtils.isEmpty(dependencies)) {
            return;
        }
        for (Map.Entry<String, Set<ResolvedDependency>> entry : dependencies.entrySet()) {
            for (ResolvedDependency dependency : entry.getValue()) {
                Files.write(file, insertDependencyRow(siteId, path, dependency.path(), entry.getKey(), dependency.valid())
                        .getBytes(UTF_8), StandardOpenOption.APPEND);
                Files.write(file, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
            }
        }
    }
}
