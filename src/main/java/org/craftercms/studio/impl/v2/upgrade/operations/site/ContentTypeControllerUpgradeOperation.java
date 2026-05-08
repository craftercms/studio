/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.upgrade.operations.site;

import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.apache.logging.log4j.util.Strings.EMPTY;

/**
 * Upgrade operation that review content type controller files:
 * <ul>
 *   <li>Check if they are equal to the default ones, if so, delete them</li>
 *   <li>If they are different, comment out the whole file and add a log
 *   error message for admins to review and manually upgrade them</li>
 * </ul>
 */
public class ContentTypeControllerUpgradeOperation extends AbstractContentUpgradeOperation implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(ContentTypeControllerUpgradeOperation.class);

	private static final String MULTILINE_COMMENT_PATTERN = "(?s)/\\*.*?\\*/";
	private static final String EMPTY_LINE_PATTERN = "(?m)^\\s*\\n";
	private static final String DEFAULT_CONTROLLER_PATH = "crafter/studio/upgrade/5.0.x/content-type/controller.groovy";
	private static final String TRAILING_SEMICOLON_PATTERN = "(?m)^(.*);\\s*$";
	private static final String TRAILING_SEMICOLON_REPLACEMENT = "$1";

	protected String defaultScript;

	public ContentTypeControllerUpgradeOperation(StudioConfiguration studioConfiguration) {
		super(studioConfiguration);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		ClassPathResource defaultScriptResource = new ClassPathResource(DEFAULT_CONTROLLER_PATH);
		defaultScript = Files.readString(Path.of(defaultScriptResource.getURI()));
	}

	@Override
	protected void updateFile(StudioUpgradeContext context, Path path) throws UpgradeException {
		try {
			String fileContent = Files.readString(path);
			if (isDefaultControllerScript(fileContent)) {
				Files.delete(path);
				return;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("""
					/*
					  WARNING: This controller script was not updated automatically.
					  Please review and update it manually to ensure compatibility with the new version of Crafter Studio.
					  Original content commented out below
					*/
					""");
			commentOutScript(sb, fileContent);
			sb.append("logger.error('This controller is disabled. Please review and upgrade to use the 5.0 content lifecycle API')\n");
			Files.writeString(path, sb.toString());
			logger.warn("Custom controller script found at {}. Please review and update it manually to ensure compatibility with the new version of Crafter Studio.", path);
		} catch (IOException e) {
			throw new UpgradeException("Error reading file " + path, e);
		}
	}

	/**
	 * Try to determine if the content is equal to the default controller script
	 * It removes comments and empty lines before comparing the text
	 *
	 * @param content the content of the controller script
	 * @return true if the content is equal to the default controller script, false otherwise
	 */
	private boolean isDefaultControllerScript(String content) {
		String noCommentsScript = content
				.replaceAll("\r\n", "\n")
				.replaceAll(MULTILINE_COMMENT_PATTERN, EMPTY)
				.replaceAll(EMPTY_LINE_PATTERN, EMPTY)
				.replaceAll(TRAILING_SEMICOLON_PATTERN, TRAILING_SEMICOLON_REPLACEMENT);


		return noCommentsScript.trim().equals(defaultScript.trim());
	}

	/**
	 * Comments out the whole content by adding '//' at the beginning of each line
	 *
	 * @param sb      the StringBuilder to append the commented content to
	 * @param content the content to be commented out
	 */
	private void commentOutScript(StringBuilder sb, String content) {
		String[] lines = content.split("\n");
		for (String line : lines) {
			sb.append("//").append(line).append("\n");
		}
	}
}
