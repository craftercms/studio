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

package org.craftercms.studio.impl.v1.web.http;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MultiReadHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private ByteArrayOutputStream cachedBytes;
    private Map<String, String[]> parameterMap;

    public MultiReadHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public static void toMap(Iterable<NameValuePair> inputParams, Map<String, String[]> toMap) {
        for (NameValuePair e : inputParams) {
            String key = e.getName();
            String value = e.getValue();
            if (toMap.containsKey(key)) {
                String[] newValue = ArrayUtils.addAll(toMap.get(key), value);
                toMap.remove(key);
                toMap.put(key, newValue);
            } else {
                toMap.put(key, new String[]{value});
            }
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        String encoding = getRequest().getCharacterEncoding();
        if (StringUtils.isEmpty(encoding)) {
            encoding = Charset.defaultCharset().name();
        }
        if (cachedBytes == null) cacheInputStream(encoding);
        return new CachedServletInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    private void cacheInputStream(String encoding) throws IOException {
    /* Cache the inputStream in order to read it multiple times. For
     * convenience, I use apache.commons IOUtils
     */
        cachedBytes = new ByteArrayOutputStream();
        IOUtils.copy(new InputStreamReader(super.getInputStream()), cachedBytes, encoding);
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
            decode(getQueryString(), result);
            String encoding = getRequest().getCharacterEncoding();
            if (StringUtils.isEmpty(encoding)) {
                encoding = Charset.defaultCharset().name();
            }
            decode(getPostBodyAsString(encoding), result);
            parameterMap = Collections.unmodifiableMap(result);
        }
        return parameterMap;
    }

    private void decode(String queryString, Map<String, String[]> result) {
        if (queryString != null) toMap(decodeParams(queryString), result);
    }

    private Iterable<NameValuePair> decodeParams(String body) {
        String encoding = getRequest().getCharacterEncoding();
        if (StringUtils.isEmpty(encoding)) {
            encoding = Charset.defaultCharset().name();
        }
        List<NameValuePair> params = new ArrayList<>(
                URLEncodedUtils.parse(body, Charset.forName(encoding)));
        try {
            String cts = getContentType();
            if (cts != null) {
                ContentType ct = ContentType.parse(cts);
                if (ct.getMimeType().equals(ContentType.APPLICATION_FORM_URLENCODED.getMimeType())) {
                    List<NameValuePair> postParams = URLEncodedUtils.parse(
                            IOUtils.toString(getReader()), Charset.forName(encoding));
                    CollectionUtils.addAll(params, postParams);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return params;
    }

    public String getPostBodyAsString(String encoding) {
        try {
            if (cachedBytes == null) cacheInputStream(encoding);
            return cachedBytes.toString(encoding);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* An inputStream which reads the cached request body */
    public class CachedServletInputStream extends ServletInputStream {
        private ByteArrayInputStream input;

        public CachedServletInputStream() {
            /* create a new input stream from the cached request body */
            input = new ByteArrayInputStream(cachedBytes.toByteArray());
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new IllegalStateException();
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
        String encoding = getRequest().getCharacterEncoding();
        if (StringUtils.isEmpty(encoding)) {
            encoding = Charset.defaultCharset().name();
        }
        sb.append(getPostBodyAsString(encoding));
        sb.append("'");
        return sb.toString();
    }

}
