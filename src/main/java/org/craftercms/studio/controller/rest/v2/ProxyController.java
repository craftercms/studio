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

import org.craftercms.studio.api.v2.service.proxy.ProxyService;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.net.URISyntaxException;

import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.*;

/**
 * Controller to proxy request to preview & add the management token if needed
 */
@RestController
@RequestMapping(PROXY_ENGINE)
public class ProxyController {

    protected ProxyService proxyService;

    @ConstructorProperties({"proxyService"})
    public ProxyController(final ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    /**
     * This handler exists to allow securing the preview log monitor URL
     */
    @GetMapping(LOG_MONITOR_ENGINE_PROXY)
    public ResponseEntity<Object> getSiteLogEvents(@RequestBody(required = false) String body,
                                                   @RequestParam("crafterSite") String siteId,
                                                   HttpServletRequest request) throws URISyntaxException {
        return proxyService.getSiteLogEvents(body, siteId, request);
    }


    @RequestMapping(ALL_SUB_URLS)
    public ResponseEntity<Object> proxyEngine(@RequestBody(required = false) String body,
                                              @RequestParam("crafterSite") String siteId,
                                              HttpMethod method, HttpServletRequest request)
            throws URISyntaxException {
        return proxyService.proxyEngine(body, siteId, request);
    }

}
