/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.api.v1.webdav;

/**
 * Holds the necessary information to connect to a WebDAV server.
 * @author joseross
 */
public class WebDavProfile {

    /**
     * The base url of the webdav server.
     */
    protected String baseUrl;

    /**
     * The base url to generate asset urls.
     */
    protected String deliveryBaseUrl;

    /**
     * The username used to connect to the server.
     */
    protected String username;

    /**
     * The password used to connect to the server.
     */
    protected String password;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDeliveryBaseUrl() {
        return deliveryBaseUrl;
    }

    public void setDeliveryBaseUrl(final String deliveryBaseUrl) {
        this.deliveryBaseUrl = deliveryBaseUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

}
