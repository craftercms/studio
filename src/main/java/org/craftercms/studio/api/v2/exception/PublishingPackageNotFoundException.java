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

package org.craftercms.studio.api.v2.exception;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;

/**
 * @author joseross
 * @since 4.0.0
 */
public class PublishingPackageNotFoundException extends ServiceLayerException {

    protected String siteId;

    protected String packageId;

    public PublishingPackageNotFoundException(String siteId, String packageId) {
        this.siteId = siteId;
        this.packageId = packageId;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    @Override
    public String toString() {
        return "PublishingPackageNotFoundException{" +
                "siteId='" + siteId + '\'' +
                ", packageId='" + packageId + '\'' +
                '}';
    }

}
