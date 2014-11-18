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


import java.util.HashMap;
import java.util.Map;

public class WorkflowProgress {
    public static Map<String, WorkflowLock> map = new HashMap<String, WorkflowLock>();

    public static void startWorkflow(String desc) {
        WorkflowLock lock = new WorkflowLock();
        map.put(desc, lock);
    }

    public static void endWorkFlow(String desc) {
        WorkflowLock lock = map.get(desc);
        if (null != lock) {
            synchronized (lock) {
                lock.setEnded(true);
                lock.notifyAll();
            }
        }
    }

    public static void waitTillWorkflowEnd(String desc) {
        WorkflowLock lock = map.get(desc);
        if (null != lock) {
            synchronized (lock) {
                while (!lock.isEnded()) {
                    try {
                        lock.wait();
                        Thread.sleep(15 * 1000);
                    } catch (InterruptedException e) {
                        //ignore
                    }
                }
            }
        }
    }

}
