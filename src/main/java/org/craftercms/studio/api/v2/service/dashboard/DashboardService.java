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

package org.craftercms.studio.api.v2.service.dashboard;

import org.craftercms.studio.model.rest.dashboard.ContentDashboardItem;

import java.time.ZonedDateTime;
import java.util.List;

public interface DashboardService {

    int getContentDashboardTotal(String siteId, String path, String modifier, String contentType,
                                                   long state, ZonedDateTime dateFrom, ZonedDateTime dateTo);

    List<ContentDashboardItem> getContentDashboard(String siteId, String path, String modifier, String contentType,
                                                   long state, ZonedDateTime dateFrom, ZonedDateTime dateTo,
                                                   String sortBy, String order, String groupBy, int offset, int limit);
}
