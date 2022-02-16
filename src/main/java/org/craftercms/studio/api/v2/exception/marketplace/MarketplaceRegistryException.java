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
package org.craftercms.studio.api.v2.exception.marketplace;

/**
 * Exception thrown when there is an issue reading or updating the plugin registry for a site
 *
 * @author joseross
 * @since 4.0.0
*/
public class MarketplaceRegistryException extends MarketplaceException {

    public MarketplaceRegistryException(String message) {
        super(message);
    }

    public MarketplaceRegistryException(String message, Exception e) {
        super(message, e);
    }

}
