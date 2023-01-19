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

package org.craftercms.studio.controller.rest.v2;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v2.service.ui.UiService;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.ui.MenuItem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.beans.ConstructorProperties;

import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ENVIRONMENT;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_MENU_ITEMS;

/**
 * Controller that provides the UI elements the current user has access to.
 *
 * @author avasquez
 */
@RestController
@RequestMapping("/api/2/ui")
public class UiController {

    private final UiService uiService;

    @ConstructorProperties("uiService")
    public UiController(final UiService uiService) {
        this.uiService = uiService;
    }

    @GetMapping("/views/global_menu")
    public ResultList<MenuItem> getGlobalMenu() throws AuthenticationException, ServiceLayerException {
        ResultList<MenuItem> result = new ResultList<>();
        result.setResponse(ApiResponse.OK);
        result.setEntities(RESULT_KEY_MENU_ITEMS, uiService.getGlobalMenu());
        return result;
    }

    @GetMapping("/system/active_environment")
    public ResultOne<String> getActiveEnvironment() throws AuthenticationException {
        ResultOne<String> result = new ResultOne<>();
        result.setResponse(ApiResponse.OK);
        result.setEntity(RESULT_KEY_ENVIRONMENT, uiService.getActiveEnvironment());
        return result;
    }
}
