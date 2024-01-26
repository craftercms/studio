/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.util.Arrays;

/**
 * Handles the {@link LogExecutionTime} annotation.
 * Log execution time of a method if the method's class logger is enabled trace
 */
@Aspect
@Order()
public class LogExecutionTimeAnnotationHandler {

    private static final Logger logger = LoggerFactory.getLogger(LogExecutionTimeAnnotationHandler.class);

    // This method matches:
    // - methods declared on classes annotated with LogExecutionTime
    // - methods declared on classes meta-annotated with LogExecutionTime (only one level deep). e.g.: @LogExecutionTime, which is annotated with @LogExecutionTime
    // - methods annotated with LogExecutionTime
    // - methods meta-annotated with LogExecutionTime (only one level deep)
    @Around("@within(LogExecutionTime) || " +
            "within(@LogExecutionTime *) || " +
            "within(@(@LogExecutionTime *) *) || " +
            "@annotation(LogExecutionTime) || " +
            "execution(@(@LogExecutionTime *) * *(..))")
    public Object logExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        String methodName = pjp.getSignature().toShortString();
        String args = Arrays.toString(pjp.getArgs());
        Logger methodLogger = LoggerFactory.getLogger(pjp.getSignature().getDeclaringType());
        if (methodLogger == null) {
            logger.debug("Method '{}' is annotated with @LogExecutionTime but does not have a valid logger. " +
                    "This annotation will be ignored.", methodName);
            return pjp.proceed();
        }

        long startTime = 0;
        if (methodLogger.isTraceEnabled()) {
            startTime = System.currentTimeMillis();
        }
        Object process = pjp.proceed();
        if (methodLogger.isTraceEnabled()) {
            methodLogger.trace("Method '{}' with parameters '{}' executed in '{}' milliseconds", methodName, args, System.currentTimeMillis() - startTime);
        }

        return process;
    }
}
