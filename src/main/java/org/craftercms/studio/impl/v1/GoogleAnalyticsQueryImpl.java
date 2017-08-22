/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2016 Crafter Software Corporation.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.studio.impl.v1;



import org.craftercms.studio.api.v1.service.analytics.AnalyticsQuery;

import java.util.*;

public class GoogleAnalyticsQueryImpl implements AnalyticsQuery {
  private static String listToString(List<String> strings) {
    StringBuilder sb = new StringBuilder();
    Iterator<String> iterator = strings.iterator();
    while (iterator.hasNext()) {
      sb.append(iterator.next());
      if (iterator.hasNext())
        sb.append(",");
    }
    return sb.toString();
  }

  private String username;
  private String password;
  private String tableId;
  private String startDate;
  private String endDate;
  private List<String> dimensions = new ArrayList<String>();
  private List<String> metrics = new ArrayList<String>();
  private String filters;
  private String sort;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public String getViewId() {
    return tableId;
  }

  public void setTableId(String tableId) {
    this.tableId = tableId;
  }

  @Override
  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  @Override
  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  @Override
  public List<String> getDimensions() {
    return dimensions;
  }

  public void addDimension(String dimension) {
    dimensions.add(dimension);
  }

  @Override
  public List<String> getMetrics() {
    return metrics;
  }

  public void addMetric(String metric) {
    metrics.add(metric);
  }

  @Override
  public String getFilters() {
    return filters;
  }

  public void setFilters(String filters) {
    this.filters = filters;
  }

  @Override
  public String getSort() {
    return sort;
  }

  public void setSort(String sort) {
    this.sort = sort;
  }

  // HELPER METHODS //

  public String getDimensionsAsString() {
    return listToString(dimensions);
  }

  public String getMetricsAsString() {
    return listToString(metrics);
  }
}
