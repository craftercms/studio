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

package org.craftercms.studio.api.v2.dal.publish;

import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * Provide access to DB publish related tables
 */
public interface PublishDAO {
    String PACKAGE_ID_PARAM = "packageId";
    String ITEMS_PARAM = "items";

    /**
     * Convenience transactional method to create a package and its items
     *
     * @param publishPackage the package
     * @param publishItems   the items
     */
    @Transactional
    default void insertPackageAndItems(PublishPackage publishPackage, Collection<PublishItem> publishItems) {
        insertPackage(publishPackage);
        insertItems(publishPackage.getId(), publishItems);
    }

    /**
     * Insert a new publish package
     *
     * @param publishPackage the package to insert
     */
    void insertPackage(PublishPackage publishPackage);

    /**
     * Insert items into a publish package
     *
     * @param packageId    the package id
     * @param publishItems the items to insert
     */
    void insertItems(@Param(PACKAGE_ID_PARAM) long packageId, @Param(ITEMS_PARAM) Collection<PublishItem> publishItems);
}
