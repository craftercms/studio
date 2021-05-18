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

package org.craftercms.studio.controller.rest.v2;

import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.craftercms.commons.exceptions.InvalidManagementTokenException;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.service.scripting.ScriptingService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultOne;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.beans.ConstructorProperties;

import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_RESULT;

/**
 * Controller that executes Rest scripts from plugins
 * @author joseross
 * @since 3.1.1
 */
@RestController
@RequestMapping("/api/2/plugin")
public class PluginController extends ManagementTokenAware {

    protected final ScriptingService scriptingService;

    @ConstructorProperties({"studioConfiguration", "securityService", "scriptingService"})
    public PluginController(StudioConfiguration studioConfiguration, SecurityService securityService,
                            ScriptingService scriptingService) {
        super(studioConfiguration, securityService);
        this.scriptingService = scriptingService;
    }

    /**
     * Reloads the groovy classes for the given site
     */
    @GetMapping("/script/reload")
    public ResponseBody reload(@RequestParam String siteId, @RequestParam String token)
            throws InvalidParametersException, InvalidManagementTokenException {
        validateToken(token);

        scriptingService.reload(siteId);

        var result = new Result();
        result.setResponse(ApiResponse.OK);

        var response = new ResponseBody();
        response.setResult(result);
        return response;
    }

    /**
     *  Executes a rest script for the given site
     */
    @RequestMapping("/script/**")
    public ResponseBody runScript(@RequestParam String siteId, HttpServletRequest request, HttpServletResponse response)
            throws ResourceException, ScriptException {

        // No better way to do this for now, later can be replaced by "/script/{*scriptUrl}"
        var scriptUrl = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        scriptUrl = removeStart(removeExtension(scriptUrl), "/api/2/plugin/script");

        // Add the binding with the right values
        // Execute the script
        var object = scriptingService.executeRestScript(siteId, scriptUrl, request, response);

        // Check if the script already committed the response
        if (response.isCommitted()) {
            // Stop the execution
            return null;
        }

        // Wrap the response in a proper result
        var result = new ResultOne<>();
        result.setEntity(RESULT_KEY_RESULT, object);
        result.setResponse(ApiResponse.OK);

        var body = new ResponseBody();
        body.setResult(result);

        return body;
    }

}
