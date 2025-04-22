/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.io.FileUtils;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.rometools.utils.Strings.trim;
import static java.lang.String.format;
import static org.craftercms.studio.api.v1.constant.StudioConstants.TMP_FILE_SUFFIX;
import static org.craftercms.studio.api.v2.utils.StudioUtils.getStudioTemporaryFilesRoot;

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
    // Default max number of bytes to read from process output
    private static final int MAX_PROCESS_OUTPUT_BYTES = 1000;

    // Exception resolvers
    public final GitCliOutputExceptionResolver DEFAULT_EX_RESOLVER = RepositoryLockedExceptionResolver.INSTANCE;
    public final GitCliOutputExceptionResolver COMMIT_EX_RESOLVER = new CompositeGitCliExceptionResolver(
            RepositoryLockedExceptionResolver.INSTANCE, NoChangesToCommitExceptionResolver.INSTANCE);

    private final String gitProcName;
    private final int gitProcWaitForTimeoutSecs;
    private final int gitProcDestroyWaitForTimeoutSecs;
    private final int maxProcessOutputBytes;

    public GitCli() {
        this(DEFAULT_GIT_COMMAND_NAME, DEFAULT_GIT_PROC_WAIT_FOR_TIMEOUT, DEFAULT_GIT_PROC_DESTROY_WAIT_FOR_TIMEOUT, MAX_PROCESS_OUTPUT_BYTES);
    }

    public GitCli(String gitProcName, int gitProcWaitForTimeoutSecs,
                  int gitProcDestroyWaitForTimeoutSecs, int maxProcessOutputBytes) {
        this.gitProcName = gitProcName;
        this.gitProcWaitForTimeoutSecs = gitProcWaitForTimeoutSecs;
        this.gitProcDestroyWaitForTimeoutSecs = gitProcDestroyWaitForTimeoutSecs;
        this.maxProcessOutputBytes = maxProcessOutputBytes;
    }

    private void executeGitCommand(GitCommandLine commandLine) throws IOException, InterruptedException {
        doExecuteGitCommand(commandLine);
    }

    /**
     * Convenience method to execute a git command and return the output as a string when the
     * expected output is "short". e.g.: git rev-parse HEAD
     * It will read the first <code>maxProcessOutputBytes</code> bytes of the output file
     */
    private String executeShortOutputGitCommand(GitCommandLine commandLine) throws IOException, InterruptedException {
        File outputTempFile = Files.createTempFile(getStudioTemporaryFilesRoot(), UUID.randomUUID().toString(), TMP_FILE_SUFFIX).toFile();
        try {
            commandLine.setOutput(outputTempFile);
            doExecuteGitCommand(commandLine);
            return trim(readFileFirstBytes(outputTempFile));
        } finally {
            FileUtils.deleteQuietly(outputTempFile);
        }
    }

    /**
     * Reads the contents of a file into a string.
     * This method will read the first <code>maxProcessOutputBytes</code> bytes of the file.
     */
    private String readFileFirstBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[maxProcessOutputBytes];
            IOUtils.read(fis, buffer);
            return new String(buffer);
        }
    }

    protected void doExecuteGitCommand(GitCommandLine commandLine)
            throws IOException, InterruptedException {
        File directory = commandLine.getDirectory();
        checkGitDirectory(directory);
        ProcessBuilder pb = new ProcessBuilder(commandLine).directory(directory);
        if (commandLine.input != null) {
            pb.redirectInput(commandLine.input);
        }

        logger.debug("Executing git command: '{}'", commandLine);
        File errorTempFile = Files.createTempFile(getStudioTemporaryFilesRoot(), UUID.randomUUID().toString(), TMP_FILE_SUFFIX).toFile();
        errorTempFile.deleteOnExit();
        pb.redirectError(errorTempFile);
        File output = commandLine.getOutput();
        File outputTempFile = null;
        if (output == null) {
            outputTempFile = Files.createTempFile(getStudioTemporaryFilesRoot(), UUID.randomUUID().toString(), TMP_FILE_SUFFIX).toFile();
            outputTempFile.deleteOnExit();
            output = outputTempFile;
        }
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(output));
        // Start process
        Process p = pb.start();
        try {
			// Wait for the process to finish, up to gitProcWaitForTimeoutSecs
			boolean exited = p.waitFor(gitProcWaitForTimeoutSecs, TimeUnit.SECONDS);
			if (!exited) {
				handleProcessTimeout(p, directory.getAbsolutePath(), output, errorTempFile);
			}

			int exitValue = p.exitValue();
			if (exitValue != 0) {
				handleErrorExitValue(directory.getAbsolutePath(), commandLine.getExceptionResolver(), p, output, errorTempFile);
			}

            logger.debug("Git command '{}' successfully executed on '{}'", commandLine, directory);
		} finally {
			FileUtils.deleteQuietly(outputTempFile);
			FileUtils.deleteQuietly(errorTempFile);
			if (p.isAlive()) {
				// Destroy process
				destroyProcess(p);
			}
		}
    }

    private void handleErrorExitValue(String directory, GitCliOutputExceptionResolver exceptionResolver,
                                      Process p, File stdOutFile, File stdErrFile) throws IOException {
        int exitValue = p.exitValue();
        String errorOutput = readFileFirstBytes(stdErrFile);
        String stdOutput = readFileFirstBytes(stdOutFile);
        String errorMessage = format("Git command failed with exit value '%s' on '%s':\n\nSTDOUT: '%s'\nSTDERR: '%s'", exitValue, directory, stdOutput, errorOutput);
        logger.debug(errorMessage);

        throw Optional
				.ofNullable(exceptionResolver.resolveException(exitValue, errorOutput))
                .or(() -> Optional.ofNullable(exceptionResolver.resolveException(exitValue, stdOutput)))
                .orElse(new GitCliOutputException(exitValue, errorMessage));
    }

    private void handleProcessTimeout(Process p, String directory, File stdOutFile, File stdErrFile) throws IOException {
        String stdOutput = readFileFirstBytes(stdOutFile);
        String errorOutput = readFileFirstBytes(stdErrFile);
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
    private static void checkGitDirectory(final File directory) throws GitCliException {
        if (Files.notExists(directory.toPath())) {
            throw new GitCliException(format("Directory '%s' does not exist", directory));
        }
        if (Files.notExists(directory.toPath().resolve(".git"))) {
            throw new GitCliException(format("Directory '%s' is not a Git repository", directory));
        }
    }

    public void add(File directory, String... paths) throws GitCliException {
        try {
            executeGitCommand(new GitCommandLine(directory, "add", paths));
        } catch (Exception e) {
            throw new GitCliException("Git add failed on directory " + directory + " for paths " +
                    ArrayUtils.toString(paths), e);
        }
    }

    /**
     * Remove the given paths from the index and discard changes
     * @param directory the git repository directory
     * @param paths the paths to restore
     */
    public void restore(File directory, String... paths) throws GitCliException {
        GitCommandLine restoreCl = new GitCommandLine(directory, "restore");
        restoreCl.addParam("--source=HEAD");
        restoreCl.addParam("--staged");
        restoreCl.addParam("--worktree");
        restoreCl.addParams(paths);
        try {
            executeGitCommand(restoreCl);
        } catch (Exception e) {
            throw new GitCliException(format("Git restore failed on directory '%s' for paths %s", directory, ArrayUtils.toString(paths)), e);
        }
    }

    public String commit(File directory, String author, String message, String... paths) throws GitCliException {
        GitCommandLine commitCl = new GitCommandLine(directory, "commit");
        GitCommandLine revParseCl = new GitCommandLine(directory, "rev-parse", "HEAD");

        commitCl.addOption("--author", author);
        commitCl.addOption("--message", message);
        commitCl.addParams(paths);

        commitCl.setExceptionResolver(COMMIT_EX_RESOLVER);

        try {
            executeGitCommand(commitCl);
            return executeShortOutputGitCommand(revParseCl);
        } catch (Exception e) {
            throw new GitCliException("Git commit failed on directory " + directory + " for paths " +
                    ArrayUtils.toString(paths), e);
        }
    }

    /**
     * Check if the repository is clean, meaning there are no changes to commit
     *
     * @param directory the git repository directory
     * @return true if the repository is clean, false otherwise
     * @throws GitCliException if the git status command fails
     */
    public boolean isRepoClean(File directory) throws GitCliException {
        GitCommandLine statusCl = new GitCommandLine(directory, "status");
        // The --porcelain option is a short version specifically for scripts
        statusCl.addParam("--porcelain");

        try {
            String result = executeShortOutputGitCommand(statusCl);

            // No result means there's no changes, so the repo is clean
            return StringUtils.isEmpty(result);
        } catch (Exception e) {
            throw new GitCliException("Git GC failed on directory " + directory, e);
        }
    }

    /**
     * Git reset --hard
     *
     * @param repoDir the git repository directory
     * @throws GitCliException if the git reset command fails
     */
    public void resetHard(File repoDir) throws GitCliException {
        GitCommandLine resetCl = new GitCommandLine(repoDir, "reset", "--hard");
        try {
            executeGitCommand(resetCl);
        } catch (Exception e) {
            throw new GitCliException("Git reset --hard failed on directory " + repoDir.getAbsolutePath(), e);
        }
    }

    /**
     * Git clean (optionally -f)
     *
     * @param repoDir  the git repository directory
     * @param force     true to force clean
     * @param recursive true to clean directories recursively
     * @throws GitCliException if the git clean command fails
     */
    public void clean(File repoDir, boolean force, boolean recursive) throws GitCliException {
        GitCommandLine cleanCl = new GitCommandLine(repoDir, "clean");
        if (force) {
            cleanCl.addParam("-f");
        }
        if (recursive) {
            cleanCl.addParam("-d");
        }
        try {
            executeGitCommand(cleanCl);
        } catch (Exception e) {
            throw new GitCliException("Git clean failed on directory " + repoDir.getAbsolutePath(), e);
        }
    }

    protected class GitCommandLine extends ArrayList<String> {
        private final File directory;
        private File input;
        private File output;
        private GitCliOutputExceptionResolver exceptionResolver = DEFAULT_EX_RESOLVER;

        public GitCommandLine(final File directory, final String command) {
            this.directory = directory;
            add(gitProcName);
            add(command);
        }

        public GitCommandLine(final File directory, String command, String... params) {
            this(directory, command);
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

        public File getDirectory() {
            return directory;
        }

        public File getInput() {
            return input;
        }

        public void setInput(File input) {
            this.input = input;
        }

        public File getOutput() {
            return output;
        }

        public void setOutput(File output) {
            this.output = output;
        }

        public GitCliOutputExceptionResolver getExceptionResolver() {
            return exceptionResolver;
        }

        public void setExceptionResolver(GitCliOutputExceptionResolver exceptionResolver) {
            this.exceptionResolver = exceptionResolver;
        }
    }

}
