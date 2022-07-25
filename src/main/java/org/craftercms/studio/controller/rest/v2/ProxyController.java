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

import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.service.proxy.ProxyService;
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

    protected final ProxyService proxyService;

    @ConstructorProperties({"proxyService"})
    public ProxyController(final ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    /**
     * This handler exists to allow securing the preview log monitor URL
     */
    @GetMapping(LOG_MONITOR_ENGINE_PROXY)
    public ResponseEntity<Object> getSiteLogEvents(@RequestBody(required = false) final String body,
                                                   @RequestParam("crafterSite") final String siteId,
                                                   final HttpServletRequest request) throws URISyntaxException, SiteNotFoundException {
        return proxyService.getSiteLogEvents(body, siteId, request);
    }


    @RequestMapping(ALL_SUB_URLS)
    public ResponseEntity<Object> proxyEngine(@RequestBody(required = false) final String body,
                                              @RequestParam("crafterSite") final String siteId,
                                              final HttpServletRequest request)
            throws URISyntaxException, SiteNotFoundException {
        return proxyService.proxyEngine(body, siteId, request);
    }

}
