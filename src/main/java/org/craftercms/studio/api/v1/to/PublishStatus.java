/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v1.to;

public class PublishStatus {

    private String status;
    private String message;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public PublishStatus() { }

    private PublishStatus(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public static PublishStatus IDLE = new PublishStatus("idle", "Last successful publish was for item: {item_path} on {date}");

    public static PublishStatus BUSY = new PublishStatus("busy", "Currently publishing item: {item_path} on {date}");

    public static PublishStatus STOPPED = new PublishStatus("stopped", "Stopped while trying to publish item: {item_path} on {date}");

    public static PublishStatus MANUALLY_STOPPED = new PublishStatus("stopped", "User {username} stopped the site on {date}");
}
