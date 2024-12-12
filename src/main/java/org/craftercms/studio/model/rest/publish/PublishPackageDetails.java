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

package org.craftercms.studio.model.rest.publish;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.craftercms.studio.api.v2.dal.publish.PublishItemWithMetadata;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.model.rest.PaginatedResultList;

import java.util.Collection;

import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ITEMS;

/**
 * Contains a {@link PublishPackage} general information and also its {@link PublishItemWithMetadata}s
 */
public class PublishPackageDetails extends PaginatedResultList<PublishItemWithMetadata> {

    private final PublishPackage publishPackage;

    public PublishPackageDetails(final PublishPackage publishPackage, final Collection<PublishItemWithMetadata> items) {
        this.publishPackage = publishPackage;
        setEntities(RESULT_KEY_ITEMS, items);
    }

    @JsonProperty("package")
    public PublishPackage getPublishPackage() {
        return publishPackage;
    }
}
