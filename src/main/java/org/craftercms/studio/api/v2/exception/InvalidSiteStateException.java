/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

/**
 * Exception to be thrown when an operation is attempted on a site with an unsupported state
 */
public class InvalidSiteStateException extends RuntimeException {
    private final String siteId;

    public InvalidSiteStateException(String siteId) {
        this.siteId = siteId;
    }

    public InvalidSiteStateException(String siteId, String message) {
        super(message);
        this.siteId = siteId;
    }

    public String getSiteId() {
        return siteId;
    }
}
