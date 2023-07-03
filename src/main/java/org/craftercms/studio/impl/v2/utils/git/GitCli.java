/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v2.exception.git.cli.GitCliException;
import org.craftercms.studio.api.v2.exception.git.cli.GitCliOutputException;
import org.craftercms.studio.api.v2.utils.git.cli.GitCliOutputExceptionResolver;
import org.craftercms.studio.impl.v2.utils.git.cli.CompositeGitCliExceptionResolver;
import org.craftercms.studio.impl.v2.utils.git.cli.NoChangesToCommitExceptionResolver;
import org.craftercms.studio.impl.v2.utils.git.cli.RepositoryLockedExceptionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * Allows doing Git operations throw the CLI.
 * <br />
 * If you ever use this class, please lock/synchronize the calls (hopefully with the
 * {@link org.craftercms.studio.api.v1.service.GeneralLockService})
 *
 * @author Sumer Jabri
 * @author Alfonso Vasquez
 * @since 3.1.23
 */
public class GitCli {

    private static final Logger logger = LoggerFactory.getLogger(GitCli.class);

    private static final String DEFAULT_GIT_COMMAND_NAME = "git";
    private static final int DEFAULT_GIT_PROC_WAIT_FOR_TIMEOUT = 60 * 5; // 5 minutes
    private static final int DEFAULT_GIT_PROC_DESTROY_WAIT_FOR_TIMEOUT = 30;

    // Exception resolvers
    public final GitCliOutputExceptionResolver DEFAULT_EX_RESOLVER = RepositoryLockedExceptionResolver.INSTANCE;
    public final GitCliOutputExceptionResolver COMMIT_EX_RESOLVER = new CompositeGitCliExceptionResolver(
            RepositoryLockedExceptionResolver.INSTANCE, NoChangesToCommitExceptionResolver.INSTANCE);

    private final String gitProcName;
    private final int gitProcWaitForTimeoutSecs;
    private final int gitProcDestroyWaitForTimeoutSecs;

    public GitCli() {
        this.gitProcName = DEFAULT_GIT_COMMAND_NAME;
        this.gitProcWaitForTimeoutSecs = DEFAULT_GIT_PROC_WAIT_FOR_TIMEOUT;
        this.gitProcDestroyWaitForTimeoutSecs = DEFAULT_GIT_PROC_DESTROY_WAIT_FOR_TIMEOUT;
    }

    public GitCli(String gitProcName, int gitProcWaitForTimeoutSecs, int gitProcDestroyWaitForTimeoutSecs) {
        this.gitProcName = gitProcName;
        this.gitProcWaitForTimeoutSecs = gitProcWaitForTimeoutSecs;
        this.gitProcDestroyWaitForTimeoutSecs = gitProcDestroyWaitForTimeoutSecs;
    }

    protected String executeGitCommand(String directory, GitCommandLine commandLine)
            throws IOException, InterruptedException {
        return executeGitCommand(directory, commandLine, DEFAULT_EX_RESOLVER);
    }

    protected String executeGitCommand(String directory, GitCommandLine commandLine, GitCliOutputExceptionResolver exceptionResolver)
            throws IOException, InterruptedException {
        checkGitDirectory(directory);

        ProcessBuilder pb = new ProcessBuilder(commandLine).directory(new File(directory));
        logger.debug("Executing git command: '{}'", commandLine);

        // Start process
        Process p = pb.start();

        InputStream processInputStream = p.getInputStream();
        InputStream processErrorStream = p.getErrorStream();
        try {
            // Wait for the process to finish, up to gitProcWaitForTimeoutSecs
            boolean exited = p.waitFor(gitProcWaitForTimeoutSecs, TimeUnit.SECONDS);
            if (!exited) {
                handleProcessTimeout(p, directory, processInputStream, processErrorStream);
            }

            int exitValue = p.exitValue();
            if (exitValue != 0) {
                handleErrorExitValue(directory, exceptionResolver, p, processInputStream);
            }

            // Read std output if process has finished successfully
            String output = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());
            logger.debug("Git command successfully executed on '{}':\n'{}'", directory, output);
            return output;
        } finally {
            IOUtils.closeQuietly(processInputStream);
            IOUtils.closeQuietly(processErrorStream);
            if (p.isAlive()) {
                // Destroy process
                destroyProcess(p);
            }
        }
    }

    private void handleErrorExitValue(String directory, GitCliOutputExceptionResolver exceptionResolver,
                                      Process p, InputStream processInputStream) throws IOException {
        int exitValue = p.exitValue();
        String errorOutput = IOUtils.toString(p.getErrorStream(), Charset.defaultCharset());
        String stdOutput = IOUtils.toString(processInputStream, Charset.defaultCharset());

        String errorMessage = format("Git command failed with exit value '%s' on '%s':\n\nSTDOUT: '%s'\nSTDERR: '%s'", exitValue, directory, stdOutput, errorOutput);
        logger.debug(errorMessage);

        throw Optional
                .ofNullable(exceptionResolver.resolveException(exitValue, errorOutput))
                .or(() -> Optional.ofNullable(exceptionResolver.resolveException(exitValue, stdOutput)))
                .orElse(new GitCliOutputException(exitValue, errorMessage));
    }

    private void handleProcessTimeout(Process p, String directory, InputStream processInputStream, InputStream processErrorStream) throws IOException {
        // Read available bytes, avoiding blocking
        String stdOutput = new String(processInputStream.readNBytes(processInputStream.available()));
        String errorOutput = new String(processErrorStream.readNBytes(processErrorStream.available()));
        destroyProcess(p);
        String errorMessage = format("Timeout while waiting for git command to exit on '%s'\nSTDOUT: '%s'\nSTDERR: '%s'", directory, stdOutput, errorOutput);
        logger.debug(errorMessage);
        throw new GitCliException(errorMessage);
    }

    /**
     * Destroys the process. It will wait for {@link #gitProcDestroyWaitForTimeoutSecs} seconds for the process to
     * exit, and if it does not, it will destroy it forcibly.
     *
     * @param process the process
     */
    private void destroyProcess(Process process) {
        try {
            logger.debug("Destroying process with PID '{}'", process.pid());
            process.destroy();
            boolean destroyed = process.waitFor(gitProcDestroyWaitForTimeoutSecs, TimeUnit.SECONDS);
            if (!destroyed) {
                logger.warn("Git process with PID '{}' did not exit after '{}' seconds, destroying it", process.pid(), gitProcDestroyWaitForTimeoutSecs);
                process.destroyForcibly();
                process.waitFor();
                logger.debug("Process with PID '{}' destroyed", process.pid());
            }
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting for process with PID '{}' to exit", process.pid(), e);
        }
    }

    /**
     * Checks if the given directory exists and is a Git repository.
     *
     * @param directory the directory to check
     * @throws GitCliException if the directory does not exist or is not a Git repository
     */
    private static void checkGitDirectory(final String directory) throws GitCliException {
        if (Files.notExists(Paths.get(directory))) {
            throw new GitCliException(format("Directory '%s' does not exist", directory));
        }
        if (Files.notExists(Paths.get(directory, ".git"))) {
            throw new GitCliException(format("Directory '%s' is not a Git repository", directory));
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

    /**
     * Remove the given paths from the index and discard changes
     * @param directory the git repository directory
     * @param paths the paths to restore
     * @return the output of the git restore command
     */
    public String restore(String directory, String... paths) throws GitCliException {
        GitCommandLine restoreCl = new GitCommandLine("restore");
        restoreCl.addParam("--source=HEAD");
        restoreCl.addParam("--staged");
        restoreCl.addParam("--worktree");
        restoreCl.addParams(paths);
        try {
            return StringUtils.trim(executeGitCommand(directory, restoreCl));
        } catch (Exception e) {
            throw new GitCliException(format("Git restore failed on directory '%s' for paths %s", directory, ArrayUtils.toString(paths)), e);
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
            throw new GitCliException("Git GC failed on directory " + directory, e);
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
