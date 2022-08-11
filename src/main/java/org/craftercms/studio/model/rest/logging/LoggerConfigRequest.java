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

package org.craftercms.studio.model.rest.logging;

/**
 * Simple extension to {@link LoggerConfig} to allow extra parameters. e.g.: createIfAbsent.
 *
 * @author jmendeza
 * @since 4.0.2
 */
public class LoggerConfigRequest extends LoggerConfig {

    private boolean createIfAbsent;

    public boolean isCreateIfAbsent() {
        return createIfAbsent;
    }

    public void setCreateIfAbsent(boolean createIfAbsent) {
        this.createIfAbsent = createIfAbsent;
    }
}
