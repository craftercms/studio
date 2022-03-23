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
import org.craftercms.commons.aop.AopUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.RetryingOperationErrorException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.springframework.dao.DeadlockLoserDataAccessException;

import java.lang.reflect.Method;

@Aspect
public class RetryingOperationAspect {

    private static final Logger logger = LoggerFactory.getLogger(RetryingOperationAspect.class);

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

    @Around("@within(org.craftercms.studio.api.v2.annotation.RetryingOperation) ||" +
            " @annotation(org.craftercms.studio.api.v2.annotation.RetryingOperation)")
    // TODO: AV - This has the same problem as the old RetryingRepositoryOperationAspect. We can just copy the fixed
    // code or consolidate the code
    public Object doRetryingOperation(ProceedingJoinPoint pjp) throws Throwable {
        Method method = AopUtils.getActualMethod(pjp);

        int numAttempts = 0;
        do {
            numAttempts++;
            try {
				 // Execute the business code again
                if (numAttempts > 1) {
                    logger.debug("Retrying operation attempt " + (numAttempts - 1));
                }
                return pjp.proceed();
            } catch (DeadlockLoserDataAccessException | JGitInternalException ex) {
                logger.debug("Failed to execute " + method.getName() + " after " + numAttempts + " attempts", ex);
                if (numAttempts > maxRetries) {
                    //log failure information, and throw exception
                    // If it is greater than the default number of retry mechanisms, we will actually throw it out this time.
                    throw new RetryingOperationErrorException("Failed to execute " + method.getName() + " after " +
                            numAttempts + " attempts", ex);
                } else {
					 // If the maximum number of retries is not reached, it will be executed again
                    long sleep = (long)(Math.random() * maxSleep);
                    logger.debug("Wait for " + sleep + " before next retry" + method.getName());
                    Thread.sleep(sleep);
                }
            }
        } while (numAttempts < this.maxRetries);

        return null;
    }
}
