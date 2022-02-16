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
package org.craftercms.studio.api.v2.service.scripting;

import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.craftercms.studio.api.v2.exception.configuration.ConfigurationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Defines all operations related to scripts
 *
 * @author joseross
 * @since 4.0
 */
public interface ScriptingService {

    /**
     * Executes a REST script from the site repository
     *
     * @param siteId the id of the site
     * @param path the path of the script
     * @param request the current request
     * @param response the current response
     * @return the value returned by the script
     * @throws ResourceException if there is any error loading the script
     * @throws ScriptException if there is any error executing the script
     */
    Object executeRestScript(String siteId, String path, HttpServletRequest request, HttpServletResponse response)
            throws ResourceException, ScriptException, ConfigurationException;

    /**
     * Reloads the classpath for the given site
     *
     * @param siteId the id of the site
     */
    void reload(String siteId);

}
