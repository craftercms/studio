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

package org.craftercms.studio.impl.v1.web.filter;


import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.craftercms.studio.impl.v1.web.http.MultiReadHttpServletRequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class MultiReadHttpServletRequestWrapperFilter implements Filter {
    public void init ( FilterConfig fc ) throws ServletException { }

    public void doFilter (ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException,	ServletException {
        if (!ServletFileUpload.isMultipartContent((HttpServletRequest)request)) {
            chain.doFilter(new MultiReadHttpServletRequestWrapper((HttpServletRequest) request), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    public void destroy () { }
}