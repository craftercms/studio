/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.cstudio.to;

import java.util.*;

public class AnalyticsReportTO {
  private List<Entry> entries;

  public AnalyticsReportTO() {
    entries = new ArrayList<Entry>();
  }

  public void addEntry(Entry entry) {
    entries.add(entry);
  }

  public List<Entry> getEntries() {
    return entries;
  }

  public static class Entry {
    private Map<String, String> data;

    public Entry() {
      data = new HashMap<String, String>();
    }

    public void put(String key, String value) {
      data.put(key, value);
    }

    public String get(String key) {
      return data.get(key);
    }

    public Map<String, String> getData() {
      return data;
    }
  }
}
