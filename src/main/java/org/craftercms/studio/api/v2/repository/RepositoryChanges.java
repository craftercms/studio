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


package org.craftercms.studio.api.v2.repository;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Holds the set of changed files during a publishing operation
 *
 * @since 3.1.24
 */
public class RepositoryChanges {

    /**
     * Set of created or updated paths
     */
    protected final Set<String> updatedPaths;

    /**
     * Set of deleted paths
     */
    protected final Set<String> deletedPaths;

    public RepositoryChanges(Set<String> updatedPaths, Set<String> deletedPaths) {
        this.updatedPaths = updatedPaths;
        this.deletedPaths = deletedPaths;
    }

    public RepositoryChanges() {
        this(emptySet(), emptySet());
    }

    public Set<String> getUpdatedPaths() {
        return updatedPaths;
    }

    public Set<String> getDeletedPaths() {
        return deletedPaths;
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(updatedPaths) && CollectionUtils.isEmpty(deletedPaths);
    }

}
