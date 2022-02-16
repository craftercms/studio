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
package org.craftercms.studio.api.v2.service.marketplace.registry;

import org.craftercms.commons.plugin.model.Installation;

/**
 * Holds the data about a configuration update during a plugin installation
 *
 * @author joseross
 * @since 4.0.0
 */
public class ConfigRecord {

    public static ConfigRecord from(Installation installation) {
        ConfigRecord record = new ConfigRecord();
        record.setType(installation.getType());
        record.setElementXpath(installation.getElementXpath());
        return record;
    }

    /**
     * The type of configuration
     */
    private String type;

    /**
     * The XPath selector of the injected element
     */
    private String elementXpath;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getElementXpath() {
        return elementXpath;
    }

    public void setElementXpath(String elementXpath) {
        this.elementXpath = elementXpath;
    }

}
