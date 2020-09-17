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

package org.craftercms.studio.api.v2.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.craftercms.commons.aop.AopUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.RetryingOperationErrorException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

@Aspect
public class RetyingOperationAspect {

    private static final Logger logger = LoggerFactory.getLogger(RetyingOperationAspect.class);

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

    @Around("@within(org.craftercms.studio.api.v2.annotation.RetryingOperation) || " +
            "@annotation(org.craftercms.studio.api.v2.annotation.RetryingOperation)")
    public Object doRetryingOperation(ProceedingJoinPoint pjp) throws Throwable {
        logger.error("DO RETRYING OPERATION");
        RetryingOperation retryingOperation = getRetryingOperationAnnotation(pjp);
        maxRetries = retryingOperation.numReties();
        maxSleep = retryingOperation.maxSleep();
        int numAttempts = 0;
        do {
            numAttempts++;
            try {
				 // Execute the business code again
                logger.error("Retrying operation attempt " + numAttempts);
                return pjp.proceed();
            } catch (DeadlockLoserDataAccessException ex) {
                if (numAttempts > maxRetries) {
                    //log failure information, and throw exception
                    // If it is greater than the default number of retry mechanisms, we will actually throw it out this time.
                    logger.error("&&&&&& Error", ex);
                    Method method = AopUtils.getActualMethod(pjp);
                    throw new RetryingOperationErrorException("Failed to execute " + method.getName() + " after " + numAttempts + " attempts");
                } else {
					 // If the maximum number of retries is not reached, it will be executed again
                    logger.error("&&&&&& Error", ex);
                    long sleep = (long)(Math.random() * maxSleep);
                    logger.error("Wait for " + sleep + " before next retry");
                    Thread.sleep(sleep);
                }
            }
        } while (numAttempts < this.maxRetries);

        return null;
    }

    protected RetryingOperation getRetryingOperationAnnotation(ProceedingJoinPoint pjp) {
        Method method = AopUtils.getActualMethod(pjp);
        logger.error("Method " + method.getName());
        RetryingOperation retryingOperation = method.getAnnotation(RetryingOperation.class);

        if (retryingOperation == null) {
            Class<?> targetClass = pjp.getTarget().getClass();
            retryingOperation = targetClass.getAnnotation(RetryingOperation.class);
        }

        return retryingOperation;
    }
}
