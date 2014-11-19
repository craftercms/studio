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
package org.craftercms.cstudio.alfresco.dm.util;

import java.util.Map;


public class ThreadLocalContainer {

    protected static ThreadLocal<Map<String,String>> threadLocal = new ThreadLocal<Map<String,String>>();

    public static void set(Map<String,String> map) {
        if(threadLocal.get() == null)
            threadLocal.set(map);
    }

    public static Map<String,String> get() {
        return threadLocal.get();
    }

    public static void remove() {
        if(threadLocal.get() != null) {
            threadLocal.set(null);
        }
    }

    public static String getProperty(String name) {
        Map<String,String> map = get();
        if(map != null) {
            return map.get(name);
        }
        return null;
    }

}
