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
package org.craftercms.studio.impl.v2.utils.git;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v2.exception.git.cli.GitCliException;
import org.craftercms.studio.api.v2.exception.git.cli.GitCliOutputException;
import org.craftercms.studio.api.v2.exception.git.cli.GitRepositoryLockedException;
import org.craftercms.studio.api.v2.exception.git.cli.NoChangesToCommitException;
import org.craftercms.studio.api.v2.utils.git.cli.GitCliOutputExceptionResolver;
import org.craftercms.studio.impl.v2.utils.git.cli.CompositeGitCliExceptionResolver;
import org.craftercms.studio.impl.v2.utils.git.cli.NoChangesToCommitExceptionResolver;
import org.craftercms.studio.impl.v2.utils.git.cli.RepositoryLockedExceptionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows doing Git operations throw the CLI.
 *
 * If you ever use this class, please lock/synchronize the calls (hopefully with the
 * {@link org.craftercms.studio.api.v1.service.GeneralLockService})
 *
 * @author Sumer Jabri
 * @author Alfonso Vasquez
 * @since  3.1.23
 */
public class GitCli {

    private static final Logger logger = LoggerFactory.getLogger(GitCli.class);

    private static final String DEFAULT_GIT_COMMAND_NAME = "git";
    private static final int DEFAULT_GIT_PROC_WAIT_FOR_TIMEOUT = 60 * 5; // 5 mins

    // Exception resolvers
    public final GitCliOutputExceptionResolver DEFAULT_EX_RESOLVER = RepositoryLockedExceptionResolver.INSTANCE;
    public final GitCliOutputExceptionResolver COMMIT_EX_RESOLVER = new CompositeGitCliExceptionResolver(
            RepositoryLockedExceptionResolver.INSTANCE, NoChangesToCommitExceptionResolver.INSTANCE);

    private String gitProcName;
    private int gitProcWaitForTimeoutSecs;

    public GitCli() {
        this.gitProcName = DEFAULT_GIT_COMMAND_NAME;
        this.gitProcWaitForTimeoutSecs = DEFAULT_GIT_PROC_WAIT_FOR_TIMEOUT;
    }

    public GitCli(String gitProcName, int gitProcWaitForTimeoutSecs) {
        this.gitProcName = gitProcName;
        this.gitProcWaitForTimeoutSecs = gitProcWaitForTimeoutSecs;
    }

    protected String executeGitCommand(String directory, GitCommandLine commandLine)
            throws IOException, InterruptedException {
        return executeGitCommand(directory, commandLine, DEFAULT_EX_RESOLVER);
    }

    protected String executeGitCommand(String directory, GitCommandLine commandLine,
                                       GitCliOutputExceptionResolver exceptionResolver)
            throws IOException, InterruptedException {
        if (Files.notExists(Paths.get(directory)) || Files.notExists(Paths.get(directory, ".git"))) {
            throw new IOException("Directory " + directory + " does not exist or is not a Git repository");
        }

        ProcessBuilder pb = new ProcessBuilder(commandLine);
        pb.directory(new File(directory));
        // Read stderr from stdout
        pb.redirectErrorStream(true);

        logger.debug("Executing git command: {}", commandLine);

        // Start process
        Process p = pb.start();

        // Read output before wait to avoid process from hanging
        String output = IOUtils.toString(p.getInputStream());

        // Wait till the process has finished
        boolean exited = p.waitFor(gitProcWaitForTimeoutSecs, TimeUnit.SECONDS);
        if (exited) {
            int exitValue = p.exitValue();
            if (exitValue == 0) {
                logger.debug("Git command successfully executed on {}:\n{}", directory, output);

                return output;
            } else {
                logger.debug("Git command failed with exit value {} on {}:\n{}", exitValue, directory, output);

                GitCliOutputException ex = exceptionResolver.resolveException(exitValue, output);
                if (ex == null) {
                    ex = new GitCliOutputException(exitValue, output);
                }

                throw ex;
            }
        } else {
            throw new IOException("Timeout while waiting for git command to exit");
        }
    }

    public void add(String directory, String... paths) throws GitCliException {
        try {
            executeGitCommand(directory, new GitCommandLine("add", paths));
        } catch (Exception e) {
            throw new GitCliException("Git add failed on directory " + directory + " for paths " +
                                      ArrayUtils.toString(paths), e);
        }
    }

    public String commit(String directory, String author, String message, String... paths) throws GitCliException {
        GitCommandLine commitCl = new GitCommandLine("commit");
        GitCommandLine revParseCl = new GitCommandLine("rev-parse", "HEAD");

        commitCl.addOption("--author", author);
        commitCl.addOption("--message", message);
        commitCl.addParams(paths);

        try {
            executeGitCommand(directory, commitCl, COMMIT_EX_RESOLVER);
            return StringUtils.trim(executeGitCommand(directory, revParseCl));
        } catch (Exception e) {
            throw new GitCliException("Git commit failed on directory " + directory + " for paths " +
                                      ArrayUtils.toString(paths), e);
        }
    }

    public boolean isRepoClean(String directory) throws GitCliException {
        GitCommandLine statusCl = new GitCommandLine("status");
        // The --porcelain option is a short version specifically for scripts
        statusCl.addParam("--porcelain");

        try {
            String result = executeGitCommand(directory, statusCl, DEFAULT_EX_RESOLVER);

            // No result means there's no changes, so the repo is clean
            return StringUtils.isEmpty(result);
        } catch (Exception e) {
            throw new GitCliException("Git gc failed on directory " + directory, e);
        }
    }

    protected class GitCommandLine extends ArrayList<String> {

        public GitCommandLine(String command) {
            add(gitProcName);
            add(command);
        }

        public GitCommandLine(String command, String... params) {
            add(gitProcName);
            add(command);
            addParams(params);
        }

        public void addParam(String param) {
            add(param);
        }

        public void addParams(String... params) {
            if (ArrayUtils.isNotEmpty(params)) {
                for (String arg : params) {
                    addParam(arg);
                }
            }
        }

        public void addOption(String optName, String optValue) {
            addParam(optName);
            addParam("\"" + optValue + "\"");
        }

    }

}
