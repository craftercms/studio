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

package org.craftercms.studio.api.v1.job;

public class CronJobContext {

    private static ThreadLocal<CronJobContext> threadLocal = new ThreadLocal<>();

    public static CronJobContext getCurrent() {
        return threadLocal.get();
    }

    public static void setCurrent(CronJobContext cronJobContext) {
        threadLocal.set(cronJobContext);
    }

    public static void clear() {
        threadLocal.remove();
    }

    public CronJobContext(String currentUser) {
        this.currentUser = currentUser;
    }

    public String getCurrentUser() { return currentUser; }
    public void setCurrentUser(String currentUser) { this.currentUser = currentUser; }

    private String currentUser;
}
