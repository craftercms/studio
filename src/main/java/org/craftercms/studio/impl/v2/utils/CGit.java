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
package org.craftercms.studio.impl.v2.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;

import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_SANDBOX_REPOSITORY_GIT_LOCK;

/**
 * C-git operations
 *
 * @author Sumer Jabri
 * @ 3.1
 *
 */
public abstract class CGit {
	private static final Logger logger = LoggerFactory.getLogger(GitRepositoryHelper.class);

	private static String executeGitCommand(String directory, String command, String... params) throws Exception {
		String result = null;
		List<String> finalCommand = new ArrayList<>();
		finalCommand.add("git");
		finalCommand.add(command);
		for (int i = 0; i < params.length; i++) {
			finalCommand.add(params[i]);
		}

		ProcessBuilder pb = new ProcessBuilder(finalCommand);
		pb.directory(new File(directory));
		try {
			Process p = pb.start();
			p.waitFor();
			BufferedReader bf = new BufferedReader(new InputStreamReader(p.getInputStream()));
			result = new String(bf.lines().collect(Collectors.joining("\n")));
			logger.error("Result from invoking the CLI is: " + result);
		} catch (IOException | InterruptedException e) {
			throw new Exception(e);
		}

		return result;
	}
	public static String add(GeneralLockService generalLockService, String site, String directory,
							 String... paths) throws Exception {
		String commit_id = null;
		String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
		generalLockService.lock(gitLockKey);

		try {
			logger.error("CGIT add: with site: " + site + " directory: " + directory + " params: " + paths);
			executeGitCommand(directory, "add", paths);
		} finally {
			generalLockService.unlock(gitLockKey);
		}

		return commit_id;
	}

	public static String commit(GeneralLockService generalLockService, String site, String directory, String author,
								String message, String... paths) throws Exception {
		String commit_id = null;
		String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
		generalLockService.lock(gitLockKey);

		List<String> finalParams = new ArrayList<>();
		finalParams.add("--author=\"" + author + "\"");
		finalParams.add("--message=\"" + message + "\"");
		for (int i = 0; i < paths.length; i++) {
			finalParams.add(paths[i]);
		}

		try {
			executeGitCommand(directory, "commit", finalParams.toArray(new String[finalParams.size()]));
			commit_id = executeGitCommand(directory, "rev-parse", "HEAD");
		} finally {
			generalLockService.unlock(gitLockKey);
		}

		return commit_id;
	}

}
