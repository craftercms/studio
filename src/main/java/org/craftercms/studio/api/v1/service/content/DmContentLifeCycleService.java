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
package org.craftercms.studio.api.v1.service.content;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public interface DmContentLifeCycleService {

    /**
     * content operations
     *
     * @author hyanghee
     *
     */
    enum ContentLifeCycleOperation {

        // cut is rename and duplicate is copy
        COPY, DELETE, DUPLICATE, NEW, RENAME, REVERT, UPDATE;

        /**
         * get the enum type of the operation given
         *
         * @param operation
         * @return enum type of the operation
         */
        public static ContentLifeCycleOperation getOperation(String operation) {
            if (!StringUtils.isEmpty(operation)) {
                return Enum.valueOf(ContentLifeCycleOperation.class, operation);
            } else {
                return null;
            }
        }

    }

    /**
     * process contnet lifecycle
     *
     * @param site
     * @param user
     * @param path
     * @param contentType
     * @param operation
     * @param params
     */
    void process(String site, String user, String path, String contentType, ContentLifeCycleOperation operation,
                        Map<String, String> params);
}
