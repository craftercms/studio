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

package org.craftercms.studio.api.v2.dal;

import org.apache.ibatis.annotations.Param;

import java.time.ZonedDateTime;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.INITIATOR_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OPERATION;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PUBLISHING_COMMENT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PUBLISHING_TARGET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SCHEDULE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.STATUS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.WORKFLOW_PACKAGE_ID;

public interface PublishingQueueDAO {

    /**
     * Insert package into publishing queue
     * @param workflowPackageId workflow package id
     * @param siteId site id
     * @param status publishing queue status
     * @param operation publishing operation
     * @param publishingTarget publishing target
     * @param schedule schedule for publishing
     * @param initiatorId user id that initiated publishing
     * @param publishingComment publishing comment
     */
    void createPublishingQueuePackage(@Param(WORKFLOW_PACKAGE_ID) String workflowPackageId,
                                      @Param(SITE_ID) long siteId, @Param(STATUS) String status,
                                      @Param(OPERATION) String operation,
                                      @Param(PUBLISHING_TARGET) String publishingTarget,
                                      @Param(SCHEDULE) ZonedDateTime schedule, @Param(INITIATOR_ID) long initiatorId,
                                      @Param(PUBLISHING_COMMENT) String publishingComment);
}
