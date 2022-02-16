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

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.exception.marketplace.MarketplaceException;
import org.craftercms.studio.api.v2.service.marketplace.Constants;
import org.craftercms.studio.api.v2.service.marketplace.MarketplaceService;
import org.craftercms.studio.api.v2.service.marketplace.registry.PluginRecord;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.PaginatedResultList;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.marketplace.InstallPluginRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ITEMS;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_PLUGINS;

/**
 * REST controller that provides access to Marketplace operations
 *
 * @author joseross
 * @since 3.1.2
 */
@RestController
@RequestMapping("/api/2/marketplace")
public class MarketplaceController {

    protected final MarketplaceService marketplaceService;

    @ConstructorProperties({"marketplaceService"})
    public MarketplaceController(final MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/search")
    public ResponseBody searchPlugins(@RequestParam(required = false) String type,
                                      @RequestParam(required = false) String keywords,
                                      @RequestParam(required = false, defaultValue = "false") boolean showIncompatible,
                                      @RequestParam(required = false, defaultValue = "0") long offset,
                                      @RequestParam(required = false, defaultValue = "10") long limit)
        throws MarketplaceException {
        Map<String, Object> page = marketplaceService.searchPlugins(type, keywords, showIncompatible, offset, limit);

        ResponseBody response = new ResponseBody();
        PaginatedResultList<Map<String, Object>> result = new PaginatedResultList<>();

        result.setResponse(ApiResponse.OK);
        result.setEntities(RESULT_KEY_PLUGINS, (List<Map<String, Object>>) page.get(Constants.RESULT_ITEMS));
        result.setTotal((int) page.get(Constants.RESULT_TOTAL));
        result.setOffset((int) offset);
        result.setLimit((int) limit);
        response.setResult(result);

        return response;
    }

    @GetMapping("/installed")
    public ResponseBody getInstalledPlugins(@RequestParam String siteId) throws MarketplaceException {
        ResultList<PluginRecord> result = new ResultList<>();
        result.setResponse(ApiResponse.OK);
        result.setEntities(RESULT_KEY_PLUGINS, marketplaceService.getInstalledPlugins(siteId));

        ResponseBody response = new ResponseBody();
        response.setResult(result);

        return response;
    }

    @PostMapping("/install")
    public ResponseBody installPlugin(@Valid @RequestBody InstallPluginRequest request) throws MarketplaceException {
        marketplaceService.installPlugin(request.getSiteId(), request.getPluginId(), request.getPluginVersion(),
                                         request.getParameters());

        Result result = new Result();
        result.setResponse(ApiResponse.OK);

        ResponseBody response = new ResponseBody();
        response.setResult(result);

        return response;
    }

    @GetMapping("/usage")
    public ResponseBody getDependantItems(@RequestParam String siteId, @RequestParam String pluginId)
            throws ServiceLayerException {
        ResultList<String> result = new ResultList<>();
        result.setResponse(ApiResponse.OK);
        result.setEntities(RESULT_KEY_ITEMS, marketplaceService.getPluginUsage(siteId, pluginId));

        ResponseBody response = new ResponseBody();
        response.setResult(result);

        return response;
    }

    @PostMapping("/remove")
    public ResponseBody removePlugin(@Valid @RequestBody RemovePluginRequest request) throws ServiceLayerException {
        marketplaceService.removePlugin(request.getSiteId(), request.getPluginId(), request.isForce());

        Result result = new Result();
        result.setResponse(ApiResponse.OK);

        ResponseBody response = new ResponseBody();
        response.setResult(result);

        return response;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    protected static class RemovePluginRequest {

        @NotEmpty
        protected String siteId;

        @NotEmpty
        protected String pluginId;

        protected boolean force;

        public String getSiteId() {
            return siteId;
        }

        public void setSiteId(String siteId) {
            this.siteId = siteId;
        }

        public String getPluginId() {
            return pluginId;
        }

        public void setPluginId(String pluginId) {
            this.pluginId = pluginId;
        }

        public boolean isForce() {
            return force;
        }

        public void setForce(boolean force) {
            this.force = force;
        }

    }

    @PostMapping("copy")
    public ResponseBody copyPlugin(@Valid @RequestBody CopyPluginRequest request) throws MarketplaceException {
        marketplaceService.copyPlugin(request.getSiteId(), request.getPath(), request.getParameters());

        Result result = new Result();
        result.setResponse(ApiResponse.OK);

        ResponseBody response = new ResponseBody();
        response.setResult(result);

        return response;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    protected static class CopyPluginRequest {

        @NotEmpty
        protected String siteId;

        @NotEmpty
        protected String path;

        protected Map<String, String> parameters = new HashMap<>();

        public String getSiteId() {
            return siteId;
        }

        public void setSiteId(String siteId) {
            this.siteId = siteId;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
        }

    }

}
