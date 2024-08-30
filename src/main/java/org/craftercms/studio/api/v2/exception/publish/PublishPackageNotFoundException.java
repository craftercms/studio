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

package org.craftercms.studio.api.v2.exception.publish;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;

/**
 * Exception thrown when one or more publishing packages cannot be found.
 */
public class PublishPackageNotFoundException extends ServiceLayerException {

    private final String siteId;

    private final Long packageId;

    public PublishPackageNotFoundException(final String siteId, final Long packageId) {
        this.packageId = packageId;
        this.siteId = siteId;
    }

    public Long getPackageId() {
        return packageId;
    }

    public String getSiteId() {
        return siteId;
    }

    @Override
    public String toString() {
        return "PublishPackageNotFoundException{" +
                "packageId=" + packageId +
                ", siteId='" + siteId + '\'' +
                '}';
    }
}
