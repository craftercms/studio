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
package org.craftercms.studio.impl.v2.utils;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.slf4j.Logger;

import static java.lang.String.format;

/**
 * Common operations related to git
 *
 * @author joseross
 * @since 4.0
 */
public abstract class GitUtils extends org.craftercms.commons.git.utils.GitUtils {

	public static void translateException(TransportException e, Logger logger, String remoteName, String remoteUrl) throws RemoteRepositoryNotFoundException,
		InvalidRemoteRepositoryCredentialsException {
		if (StringUtils.endsWithIgnoreCase(e.getMessage(), "not authorized")) {
			logger.error("Bad credentials or read-only repository '{}' URL '{}'",
				remoteName, remoteUrl, e);
			throw new InvalidRemoteRepositoryCredentialsException(
				format("Bad credentials or read-only repository '%s' URL '%s'", remoteName, remoteUrl), e);
		} else if (StringUtils.endsWithIgnoreCase(e.getMessage(), "key did not validate")) {
			logger.error("Invalid private key for repository '{}' URL '{}'", remoteName, remoteUrl, e);
			throw new InvalidRemoteRepositoryCredentialsException(
				format("Invalid private key for repository '%s' URL '%s'", remoteName, remoteUrl), e);
		} else {
			logger.error("Remote repository '{}' URL '{}' was not found", remoteName, remoteUrl, e);
			throw new RemoteRepositoryNotFoundException(
				format("Remote repository '%s' URL '%s' was not found", remoteName, remoteUrl), e);
		}
	}

}
