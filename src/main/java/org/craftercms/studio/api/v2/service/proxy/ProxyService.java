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
package org.craftercms.studio.api.v2.service.proxy;

import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;

/**
 * Provides functionality to proxy request to Crafter Engine
 *
 * @author jmendeza
 * @since 4.0.1
 */
public interface ProxyService {
    /**
     * Proxies a request to Crafter Engine.
     *
     * @param body    body of the request
     * @param siteId  ID of the crafter site
     * @param request the request
     * @return response from Crafter Engine
     * @throws URISyntaxException, if request URI is not valid
     */
    ResponseEntity<Object> proxyEngine(String body, String siteId, HttpServletRequest request) throws URISyntaxException, SiteNotFoundException;

    /**
     * Proxies a request to Crafter Engine log monitor service, after checking
     * current user has permission to access log data for siteId.
     *
     * @param body    body of the request.
     * @param siteId  ID of the crafter site
     * @param request the request
     * @return response from Crafter Engine
     * @throws URISyntaxException, if request URI is not valid
     */
    ResponseEntity<Object> getSiteLogEvents(String body, String siteId, HttpServletRequest request) throws URISyntaxException, SiteNotFoundException;
}
