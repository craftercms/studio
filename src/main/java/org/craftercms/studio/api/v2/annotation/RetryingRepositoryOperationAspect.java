/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.craftercms.commons.aop.AopUtils;
import org.craftercms.commons.git.utils.GitUtils;

import org.craftercms.studio.api.v2.exception.RetryingOperationErrorException;
import org.craftercms.studio.api.v2.exception.git.cli.GitCliException;
import org.craftercms.studio.api.v2.exception.git.cli.GitCliOutputException;
import org.craftercms.studio.api.v2.exception.git.cli.GitRepositoryLockedException;

import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.errors.LockFailedException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.lib.Repository;

import java.io.EOFException;
import java.io.IOException;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class RetryingRepositoryOperationAspect {

    private static final Logger logger = LoggerFactory.getLogger(RetryingRepositoryOperationAspect.class);

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

    @Around("@within(org.craftercms.studio.api.v2.annotation.RetryingRepositoryOperation) || " +
            "@annotation(org.craftercms.studio.api.v2.annotation.RetryingRepositoryOperation)")
    public Object doRetryingOperation(ProceedingJoinPoint pjp) throws Throwable {
        Method method = AopUtils.getActualMethod(pjp);
        Exception lastException;

        int numAttempts = 0;
        do {
            numAttempts++;
            try {
                // Execute the business code again
                if (numAttempts > 1) {
                    logger.debug("Retrying repository operation attempt '{}'", numAttempts - 1);
                }
                return pjp.proceed();
            } catch (JGitInternalException | GitCliException ex) {
                lastException = ex;
                if (isRepositoryLocked(ex)) {
                    logger.debug("Failed to execute '{}' after '{}' attempts", method.getName(), numAttempts, ex);
                } else if (isRepositoryCorrupted(ex)) {
                    String repoPath = extractRepoPathFromPjp(pjp);
                    logger.warn("The local repository '{}' is corrupt, trying to fix it", repoPath);
                    try {
                        GitUtils.deleteGitIndex(repoPath);
                        logger.info(".git/index is deleted from local repository '{}'", repoPath);
                    } catch (IOException ioe) {
                        throw new RetryingOperationErrorException("Error deleting index.lock for local repo " + repoPath, ioe);
                    }
                } else {
                    throw new RetryingOperationErrorException("Failed to execute " + method.getName() + " due to " +
                                                              "a Git error that does not cause retry attempts", ex);
                }

                if (numAttempts < maxRetries) {
                    // If the maximum number of retries is not reached, sleep and execute it again
                    long sleep = (long) (Math.random() * maxSleep);
                    logger.debug("Git operation failed due to the repository being locked. Will wait for '{}' before next retry '{}'", sleep, method.getName());
                    Thread.sleep(sleep);
                }
            }
        } while (numAttempts < maxRetries);

        // If it gets here, numAttempts >= maxRetries, so we should fail entirely
        throw new RetryingOperationErrorException("Failed to execute " + method.getName() +
                                                  " due to the Git repository being locked after " +
                                                  numAttempts + " attempts", lastException);
    }

    protected boolean isRepositoryLocked(Throwable ex) {
        // Check for JGit exception first, then for CLI exception (not need to check for null with instanceof)
        return ex.getCause() instanceof LockFailedException ||
               ExceptionUtils.getRootCause(ex) instanceof GitRepositoryLockedException;
    }

    protected boolean isRepositoryCorrupted(Throwable ex) {
        Throwable cause = ex.getCause();
        return cause instanceof CorruptObjectException || cause instanceof EOFException;
    }

    protected String extractRepoPathFromPjp(ProceedingJoinPoint pjp) {
        Object[] methodParams = pjp.getArgs();

        if (methodParams.length > 0 && methodParams[0] instanceof GitCommand) {
            GitCommand gitCommand = (GitCommand) methodParams[0];
            Repository repository = gitCommand.getRepository();
            File localRepoFolder = repository.getWorkTree();
            return localRepoFolder.getAbsolutePath();
        } else {
            return null;
        }
    }

}
