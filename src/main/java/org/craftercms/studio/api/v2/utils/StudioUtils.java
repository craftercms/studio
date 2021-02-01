/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.utils;

import com.amazonaws.services.s3.internal.Mimetypes;
import org.apache.commons.io.FilenameUtils;
import org.craftercms.commons.http.RequestContext;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_SITEID;

public abstract class StudioUtils {

    public static String getMimeType(String filename) {
        Mimetypes mimetypes = Mimetypes.getInstance();
        return mimetypes.getMimetype(FilenameUtils.getName(filename));
    }

    /**
     * Obtains the siteId from the current request, always fails if called out of a request context
     * @return the siteId
     */
    public static String getSiteId() {
        var context = RequestContext.getCurrent();
        if (context == null) {
            throw new IllegalStateException("There is no request to get the siteId");
        }

        var request = context.getRequest();
        var siteId = request.getParameter(REQUEST_PARAM_SITEID);
        if (isEmpty(siteId)) {
            throw new IllegalStateException("There is no parameter to get the siteId");
        }

        return siteId;
    }

}
