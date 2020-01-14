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

package org.craftercms.studio.impl.v1.web.http;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MultiReadHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private byte[] cachedBytes;
    private Map<String, String[]> parameterMap;

    public MultiReadHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (cachedBytes == null) cacheInputStream();
        return new CachedServletInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharset()));
    }

    private void cacheInputStream() throws IOException {
        cachedBytes = IOUtils.toByteArray(super.getInputStream());
    }

    @Override
    public String getParameter(String key) {
        Map<String, String[]> parameterMap = getParameterMap();
        String[] values = parameterMap.get(key);
        return values != null && values.length > 0 ? values[0] : null;
    }

    @Override
    public String[] getParameterValues(String key) {
        Map<String, String[]> parameterMap = getParameterMap();
        return parameterMap.get(key);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (parameterMap == null) {
            Map<String, String[]> result = new LinkedHashMap<String, String[]>();
            parseQueryString(result);
            parseUrlEncodedBody(result);
            parameterMap = Collections.unmodifiableMap(result);
        }
        return parameterMap;
    }

    private void parseQueryString(Map<String, String[]> params) {
        String queryString = getQueryString();
        if (StringUtils.isNotEmpty(queryString)) {
            toMap(URLEncodedUtils.parse(queryString, getCharset()), params);
        }
    }

    private void parseUrlEncodedBody(Map<String, String[]> params) {
        String contentTypeStr = getContentType();
        if (contentTypeStr != null) {
            ContentType contentType = ContentType.parse(contentTypeStr);
            if (contentType.getMimeType().equals(ContentType.APPLICATION_FORM_URLENCODED.getMimeType())) {
                toMap(URLEncodedUtils.parse(getBodyAsString(), getCharset()), params);
            }
        }
    }

    private String getBodyAsString() {
        try {
            if (cachedBytes == null) cacheInputStream();
            return new String(cachedBytes, getCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Charset getCharset() {
        String encoding = getRequest().getCharacterEncoding();
        if (StringUtils.isEmpty(encoding)) {
            return Charset.defaultCharset();
        } else {
            return Charset.forName(encoding);
        }
    }

    private void toMap(Iterable<NameValuePair> valuePairs, Map<String, String[]> map) {
        for (NameValuePair e : valuePairs) {
            String key = e.getName();
            String value = e.getValue();
            if (map.containsKey(key)) {
                String[] newValue = ArrayUtils.addAll(map.get(key), value);
                map.put(key, newValue);
            } else {
                map.put(key, new String[]{value});
            }
        }
    }

    /* An inputStream which reads the cached request body */
    public class CachedServletInputStream extends ServletInputStream {
        private ByteArrayInputStream input;

        public CachedServletInputStream() {
            /* create a new input stream from the cached request body */
            input = new ByteArrayInputStream(cachedBytes);
        }

        @Override
        public int read() throws IOException {
            return input.read();
        }
    }

    @Override
    public String toString() {
        String query = StringUtils.isEmpty(getQueryString()) ? StringUtils.EMPTY : getQueryString();
        StringBuilder sb = new StringBuilder();
        sb.append("URL='").append(getRequestURI()).append(query.isEmpty() ? "" : "?" + query).append("', body='");
        sb.append(getBodyAsString());
        sb.append("'");
        return sb.toString();
    }

}
