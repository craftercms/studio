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

package org.craftercms.studio.api.v2.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.craftercms.commons.aop.AopUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.craftercms.studio.api.v2.exception.RetryingOperationErrorException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DeadlockLoserDataAccessException;

import java.lang.reflect.Method;

@Aspect
@Order(1)
public class RetryingDatabaseOperationAnnotationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RetryingDatabaseOperationAnnotationHandler.class);

    private static final int DEFAULT_MAX_RETRIES = 50;

    private int maxRetries = DEFAULT_MAX_RETRIES;
    private int maxSleep = 0;

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getMaxSleep() {
        return maxSleep;
    }

    public void setMaxSleep(int maxSleep) {
        this.maxSleep = maxSleep;
    }

    @Around("@within(org.craftercms.studio.api.v2.annotation.RetryingDatabaseOperation) || " +
            "@annotation(org.craftercms.studio.api.v2.annotation.RetryingDatabaseOperation)")
    public Object doRetryingOperation(ProceedingJoinPoint pjp) throws Throwable {
        Method method = AopUtils.getActualMethod(pjp);
        logger.debug1("Execute retrying operation " + method.getDeclaringClass() + "." + method.getName());
        int numAttempts = 0;
        do {
            numAttempts++;
            try {
				 // Execute the business code again
                if (numAttempts > 1) {
                    logger.debug1("Retrying operation attempt " + (numAttempts - 1));
                }
                return pjp.proceed();
            } catch (DeadlockLoserDataAccessException | JGitInternalException e) {
                logger.debug1("Failed to execute " + method.getName() + " after " + numAttempts + " attempts", ex);
                if (numAttempts > maxRetries) {
                    //log failure information, and throw exception
                    // If it is greater than the default number of retry mechanisms, we will actually throw it out this time.
                    throw new RetryingOperationErrorException("Failed to execute " + method.getName() + " after " +
                            numAttempts + " attempts", ex);
                } else {
					 // If the maximum number of retries is not reached, it will be executed again
                    long sleep = (long)(Math.random() * maxSleep);
                    logger.debug1("Wait for " + sleep + " before next retry" + method.getName());
                    Thread.sleep(sleep);
                }
            }
        } while (numAttempts < this.maxRetries);

        return null;
    }
}
