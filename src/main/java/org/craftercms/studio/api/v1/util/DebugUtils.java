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

package org.craftercms.studio.api.v1.util;

import org.craftercms.commons.http.RequestContext;
import org.craftercms.studio.api.v1.job.CronJobContext;
import org.craftercms.studio.api.v1.log.Logger;

import javax.servlet.http.HttpServletRequest;

public class DebugUtils {

    public static void addDebugStack(Logger logger) {
        if (logger.getLevel().equals(Logger.LEVEL_DEBUG)) {
            Thread thread = Thread.currentThread();
            String threadName = thread.getName();
            logger.debug("Thread: " + threadName);
            StackTraceElement[] stackTraceElements = thread.getStackTrace();
            StringBuilder sbStack = new StringBuilder();
            int stackSize = (20 < stackTraceElements.length-2) ? 20 : stackTraceElements.length;
            for (int i = 2; i < stackSize+2; i++){
                sbStack.append("\n\t").append(stackTraceElements[i].toString());
            }
            RequestContext context = RequestContext.getCurrent();
            CronJobContext cronJobContext = CronJobContext.getCurrent();
            if (context != null) {
                HttpServletRequest request = context.getRequest();
                String url = request.getRequestURI() + "?" + request.getQueryString();
                logger.debug("Http request: " + url);
            } else if (cronJobContext != null) {
                logger.debug("Cron Job");

            }
            logger.debug("Stack trace (depth 20): " + sbStack.toString());
        }
    }
}
