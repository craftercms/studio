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

package org.craftercms.studio.controller.rest.v2;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.dal.BlueprintDescriptor;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.ResultList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_BLUEPRINTS;

@RestController
@RequestMapping("/api/2/sites")
public class SitesController {

    private final static Logger logger = LoggerFactory.getLogger(SitesController.class);

    private SitesService sitesService;

    @GetMapping("/available_blueprints")
    public ResponseBody getAvailableBlueprints() throws ServiceLayerException {
        List<BlueprintDescriptor> blueprintDescriptors = null;
        try {
            blueprintDescriptors = sitesService.getAvailableBlueprints();
        } catch (Exception e) {
            throw new ServiceLayerException(e);
        }
        ResponseBody responseBody = new ResponseBody();
        ResultList<BlueprintDescriptor> result = new ResultList<BlueprintDescriptor>();
        result.setEntities(RESULT_KEY_BLUEPRINTS, blueprintDescriptors);
        result.setResponse(ApiResponse.OK);
        responseBody.setResult(result);
        return responseBody;
    }

    public SitesService getSitesService() {
        return sitesService;
    }

    public void setSitesService(SitesService sitesService) {
        this.sitesService = sitesService;
    }
}
