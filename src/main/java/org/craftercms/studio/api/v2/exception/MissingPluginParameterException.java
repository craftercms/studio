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

package org.craftercms.studio.api.v2.exception;

import org.craftercms.commons.plugin.model.Parameter;
import org.craftercms.commons.plugin.model.PluginDescriptor;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;

/**
 * Exception thrown when a required parameter is not provided for a plugin
 *
 * @author joseross
 * @since 3.1.4
 */
public class MissingPluginParameterException extends ServiceLayerException {

    protected PluginDescriptor descriptor;
    protected Parameter parameter;

    public MissingPluginParameterException(final PluginDescriptor descriptor, final Parameter parameter) {
        super(String.format("Missing required parameter '%s' for plugin '%s'", parameter.getLabel(),
                descriptor.getPlugin().getId()));
        this.descriptor = descriptor;
        this.parameter = parameter;
    }

    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    public Parameter getParameter() {
        return parameter;
    }

}
