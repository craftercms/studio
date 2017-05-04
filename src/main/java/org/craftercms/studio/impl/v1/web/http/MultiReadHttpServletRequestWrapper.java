/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2017 Crafter Software Corporation.
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

package org.craftercms.studio.impl.v1.web.http;

import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class MultiReadHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private final byte[] body;

    public MultiReadHttpServletRequestWrapper ( HttpServletRequest request ) throws IOException {
        super(request);
        if (request.getContentType() != null && request.getContentType().contains(MediaType.APPLICATION_JSON.toString())) {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                body = IOUtils.toByteArray(inputStream);
                return;
            }
        }
        body = null;
    }

    @Override
    public ServletInputStream getInputStream () throws IOException {
        if (body == null) return super.getInputStream();
        final ByteArrayInputStream stream = new ByteArrayInputStream(body);
        ServletInputStream inputStream = new ServletInputStream() {
            @Override
            public int read () throws IOException {
                return stream.read();
            }
        };
        return inputStream;
    }

    @Override
    public BufferedReader getReader () throws IOException {
        if (body == null) return super.getReader();
        return new BufferedReader(new InputStreamReader( new ByteArrayInputStream(body), StandardCharsets.UTF_8));
    }

}
