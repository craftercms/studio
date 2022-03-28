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
import java.util.List;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.DATE_FROM;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.DATE_TO;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ORDER;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PATHS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.STATUS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.WORKFLOW_PACKAGE_ID;

public interface WorkflowPackageDAO {

    /**
     * Get total number of workflow packages for site
     * @param siteId site identifier
     * @param status package status to filter by
     * @param dateFrom lower boundary to filter by date-time range
     * @param dateTo upper boundary to filter by date-time range
     * @return number of workflow packages
     */
    int getWorkflowPackagesTotal(@Param(SITE_ID) String siteId, @Param(STATUS) String status,
                                 @Param(DATE_FROM) ZonedDateTime dateFrom, @Param(DATE_TO) ZonedDateTime dateTo);

    /**
     * Get  workflow packages for site ordered by schedule
     * @param siteId site identifier
     * @param status package status to filter by
     * @param dateFrom lower boundary to filter by date-time range
     * @param dateTo upper boundary to filter by date-time range
     * @param offset offset of the first result item
     * @param limit number of results to return
     * @param order ascending or descending
     * @return paginated list of workflow packages
     */
    List<WorkflowPackage> getWorkflowPackages(@Param(SITE_ID) String siteId, @Param(STATUS) String status,
                                              @Param(DATE_FROM) ZonedDateTime dateFrom,
                                              @Param(DATE_TO) ZonedDateTime dateTo, @Param(OFFSET) int offset,
                                              @Param(LIMIT) int limit, @Param(ORDER) String order);

    /**
     * Create workflow package
     *
     * @param workflowPackage workflow package object
     */
    void createWorkflowPackage(WorkflowPackage workflowPackage);

    /**
     * Add items to workflow package
     * @param workflowPackageId workflow package identifier
     * @param siteId site id
     * @param paths list of paths of the items
     */
    void addWorkflowPackageItems(@Param(WORKFLOW_PACKAGE_ID) String workflowPackageId,
                                 @Param(SITE_ID) long siteId, @Param(PATHS) List<String> paths);
}
