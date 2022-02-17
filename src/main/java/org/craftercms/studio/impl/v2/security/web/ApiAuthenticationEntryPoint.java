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
package org.craftercms.studio.impl.v2.security.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.model.rest.ApiResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Implementation of {@link AuthenticationEntryPoint} that returns a JSON object matching Studio's API specs
 *
 * @author joseross
 * @since 4.0
 */
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public static final Map<String, String> API_1_RESPONSE = Map.of("message", "Unauthorized");

    public static final Map<String, Object> API_2_RESPONSE = Map.of("response", ApiResponse.UNAUTHENTICATED);

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());

        PrintWriter out = response.getWriter();

        if (StringUtils.startsWithIgnoreCase(request.getRequestURI(), "/api/1")) {
            objectMapper.writeValue(out, API_1_RESPONSE);
        } else {
            objectMapper.writeValue(out, API_2_RESPONSE);
        }

        out.flush();
    }

}
