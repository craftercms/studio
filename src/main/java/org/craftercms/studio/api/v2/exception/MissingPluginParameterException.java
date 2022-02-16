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

import org.craftercms.commons.plugin.model.Parameter;
import org.craftercms.commons.plugin.model.Plugin;
import org.craftercms.studio.api.v2.exception.marketplace.PluginInstallationException;

/**
 * Exception thrown when a required parameter is not provided for a plugin
 *
 * @author joseross
 * @since 3.1.4
 */
public class MissingPluginParameterException extends PluginInstallationException {

    protected Plugin plugin;
    protected Parameter parameter;

    public MissingPluginParameterException(final Plugin plugin, final Parameter parameter) {
        super(String.format("Missing required parameter '%s' for plugin '%s'", parameter.getLabel(),
                plugin.getId()));
        this.plugin = plugin;
        this.parameter = parameter;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Parameter getParameter() {
        return parameter;
    }

}
