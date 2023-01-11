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

package org.craftercms.studio.model.rest.logging;

import org.craftercms.commons.validation.annotations.param.ValidateNoTagsParam;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * Holds a Logger name and its configured priority level.
 *
 * @author jmendeza
 * @since 4.0.2
 */
public class LoggerConfig {
    @NotEmpty
    // TODO: JM: Revisit this when validation annotations are java validation API
    // This will not be validated when the actual parameter is a sub-class
    @ValidateNoTagsParam
    private String name;
    @NotEmpty
    @Pattern(regexp = "off|error|warn|info|debug|trace|all", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String level;

    public LoggerConfig() {
    }

    public LoggerConfig(final String name, final String level) {
        this.name = name;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(final String level) {
        this.level = level;
    }
}
